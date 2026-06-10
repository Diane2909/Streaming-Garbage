import org.apache.spark.sql.SparkSession
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, FileUtil}
import java.util.Properties

object Producer {

  def main(args: Array[String]): Unit = {

    val props = new Properties()

    props.load(
      getClass.getClassLoader.getResourceAsStream("app.properties")
    )

    val spark = SparkSession.builder()
        .appName(props.getProperty("app.name"))
        .master(props.getProperty("app.master"))
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    val inputPath = props.getProperty("producer.input.path")
    val outputPath = props.getProperty("producer.output.path")
    val frequency = props.getProperty("producer.frequency").toInt
    val batch = props.getProperty("producer.batch").toInt
    val loop = props.getProperty("producer.loop", "false").toBoolean
    val debug = props.getProperty("debug").toBoolean

    copyFiles(fs, inputPath, outputPath, frequency, batch, spark.sparkContext.hadoopConfiguration, loop, debug)
    
    spark.stop()
  }

  def copyFiles(fs: FileSystem, inputPath: String, outputPath: String, frequency: Int, batch: Int, conf: Configuration, loop: Boolean = false, debug: Boolean = false) {
    fs.listStatus(new Path("data/source")).grouped(batch)
      .foreach { batch => 
        batch.foreach { status =>

          val file = status.getPath

          val destination =
            new Path("data/input", System.currentTimeMillis() + "_" + file.getName)

          if (debug) { println(s"Copying File: ${file.getName}") }

          FileUtil.copy(
            fs, file,          // source file
            fs, destination,   // destination file
            false,             // don't delete source
            conf
          )
        }
        if (debug) { println("----------- END OF BATCH ------------") }
        Thread.sleep(frequency * 1000)
      }
    if (loop) {
      copyFiles(fs, inputPath, outputPath, frequency, batch, conf, loop)
    }
  }

}