import org.apache.spark.sql.SparkSession
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, FileUtil}
import java.util.Properties

object Producer {

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
  
  val frequency = props.getProperty("producer.frequency").toInt
  val batch = props.getProperty("producer.batch").toInt
  val loop = props.getProperty("producer.loop", "false").toBoolean
  val recursive = props.getProperty("producer.recursive", "false").toBoolean
  val debug = props.getProperty("debug").toBoolean

  val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

  def main(args: Array[String]): Unit = {

    val inputPath = props.getProperty("producer.input.path")
    val outputPath = props.getProperty("producer.output.path")

    copyFiles(inputPath, outputPath)
    
    spark.stop()
  }

  def copyFiles(inputPath: String,
                outputPath: String,
                topLevel: Boolean = true)
  {
    val files = fs.listStatus(new Path(inputPath)).filter(_.isFile)
    files.grouped(batch)
      .foreach { batch => 
        batch.foreach { status =>

          val file = status.getPath

          val destination =
            new Path(outputPath, java.time.LocalDateTime.now().format(formatter) + "_" + file.getName)

          if (debug) { println(s"Copying File: ${file.getName}") }

          FileUtil.copy(
            fs, file,          // source file
            fs, destination,   // destination file
            false,             // don't delete source
            spark.sparkContext.hadoopConfiguration
          )
        }
        if (debug) { println("----------- END OF BATCH ------------") }
        Thread.sleep(frequency * 1000)
      }

    if (recursive) {
      val directories = fs.listStatus(new Path(inputPath)).filter(_.isDirectory)
      directories.foreach { directory =>
        copyFiles(inputPath + "/" + directory, outputPath + "/" + directory)
      }
    }
    
    if (loop & topLevel) {
      copyFiles(inputPath, outputPath)
    }
  }

}