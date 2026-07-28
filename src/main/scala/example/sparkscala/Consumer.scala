package example.sparkscala

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import java.util.Properties
import java.nio.file.{Files, Paths}

object Consumer {

  val props = new Properties()
  props.load(getClass.getClassLoader.getResourceAsStream("app.properties"))

  val spark = SparkSession
    .builder()
    .appName(props.getProperty("app.name"))
    .master(props.getProperty("app.master"))
    .config("spark.sql.sources.parallelPartitionDiscovery.threshold", "1000000")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")
  import spark.implicits._

  val inputPath = props.getProperty("consumer.input.path")
  val outputPath = props.getProperty("consumer.output.path")
  val checkpointPath = props.getProperty("consumer.checkpoint.path")
  val modelPath = props.getProperty("consumer.model.path")
  val imgWidth = props.getProperty("consumer.model.input.width").toInt
  val imgHeight = props.getProperty("consumer.model.input.height").toInt
  val imgChannels =
    props.getProperty("consumer.model.input.channels", "1").toInt
  val classLabels =
    props.getProperty("consumer.model.classes").split(",").map(_.trim)
  val triggerInterval =
    props.getProperty("consumer.trigger.interval", "5").toInt
  val fileGlob = props.getProperty("consumer.file.glob", "*.{jpg,jpeg,png}")
  val debug = props.getProperty("debug", "false").toBoolean

  val enablePrediction =
    props.getProperty("consumer.enable.prediction", "true").toBoolean

  val extractLabelFromPath =
    props.getProperty("consumer.extract.label.from.path", "true").toBoolean

  val modelBroadcast =
    if (enablePrediction && Files.exists(Paths.get(modelPath))) {
      val bytes = Files.readAllBytes(Paths.get(modelPath))
      Some(spark.sparkContext.broadcast(bytes))
    } else {
      if (enablePrediction) {
        println(
          s"[Consumer] ATTENTION: modèle introuvable à '$modelPath'. " +
            s"La prédiction sera désactivée pour ce run (mets consumer.enable.prediction=false pour enlever cet avertissement)."
        )
      }
      None
    }

  def main(args: Array[String]): Unit = {

    val binaryFileSchema = StructType(
      Seq(
        StructField("path", StringType, nullable = true),
        StructField("modificationTime", TimestampType, nullable = true),
        StructField("length", LongType, nullable = true),
        StructField("content", BinaryType, nullable = true)
      )
    )

    val rawStream: DataFrame = spark.readStream
      .format("binaryFile")
      .schema(binaryFileSchema)
      .option("pathGlobFilter", fileGlob)
      .option("recursiveFileLookup", "true")
      .load(inputPath)

    val withPathInfo = if (extractLabelFromPath) {
      val pathParts = split($"path", "/")
      val partsLen = size(pathParts)

      rawStream
        .withColumn(
          "true_label",
          when(partsLen >= 2, element_at(pathParts, -2)).otherwise(lit("unknown"))
        )
        .withColumn(
          "split",
          when(partsLen >= 3, element_at(pathParts, -3)).otherwise(lit("unknown"))
        )
    } else {
      rawStream
        .withColumn("true_label", lit(null: String))
        .withColumn("split", lit(null: String))
    }

    val decodeUdf = udf { bytes: Array[Byte] =>
      ImageUtils.decodeAndResize(bytes, imgWidth, imgHeight, imgChannels)
    }

    val withFeatures = withPathInfo
      .withColumn("features", decodeUdf($"content"))
      .filter($"features".isNotNull)

    val statsUdf = udf { features: Seq[Float] =>
      ImageUtils.descriptiveStats(features)
    }

    val withStats = withFeatures
      .withColumn("stats", statsUdf($"features"))
      .withColumn("mean_pixel", $"stats.mean")
      .withColumn("std_pixel", $"stats.std")
      .withColumn("min_pixel", $"stats.min")
      .withColumn("max_pixel", $"stats.max")
      .drop("stats")

    val result = modelBroadcast match {
      case Some(bcast) =>
        val predictUdf = udf { features: Seq[Float] =>
          OnnxPredictor.initSession(bcast.value)
          OnnxPredictor.predict(
            features.toArray,
            imgHeight,
            imgWidth,
            imgChannels,
            classLabels
          )
        }

        val withPrediction = withStats
          .withColumn("prediction", predictUdf($"features"))
          .withColumn("predicted_class", $"prediction.label")
          .withColumn("confidence", $"prediction.confidence")
          .withColumn(
            "is_correct",
            when($"true_label".isNotNull, $"predicted_class" === $"true_label")
              .otherwise(lit(null).cast("boolean"))
          )
          .withColumn("processTime", current_timestamp())
          .drop("prediction", "features", "content")

        withPrediction.select(
          $"path",
          $"modificationTime",
          $"length",
          $"split",
          $"true_label",
          $"mean_pixel",
          $"std_pixel",
          $"min_pixel",
          $"max_pixel",
          $"predicted_class",
          $"confidence",
          $"is_correct",
          $"processTime"
        )

      case None =>
        withStats.select(
          $"path",
          $"modificationTime",
          $"length",
          $"split",
          $"true_label",
          $"mean_pixel",
          $"std_pixel",
          $"min_pixel",
          $"max_pixel"
        )
    }

    if (debug) {
      result.writeStream
        .format("console")
        .option("truncate", "false")
        .outputMode("append")
        .trigger(Trigger.ProcessingTime(s"$triggerInterval seconds"))
        .start()
    }

    val query = result.writeStream
      .format("json")
      .option("path", outputPath)
      .option("checkpointLocation", checkpointPath)
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(s"$triggerInterval seconds"))
      .start()

    query.awaitTermination()
  }
}
