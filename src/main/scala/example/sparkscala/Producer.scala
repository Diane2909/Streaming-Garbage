import org.apache.spark.sql.SparkSession
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, FileUtil}

object Producer {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
        .appName("TestApp")
        .master("local[*]")
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    val frequency = 5
    val batch = 2
    val loops = 10
    var loopCount = 0

    while (loopCount < loops) {
      copyFiles(fs ,frequency, batch, spark.sparkContext.hadoopConfiguration)
      loopCount += 1
    }
    
    spark.stop()
  }

  def copyFiles(fs: FileSystem, frequency: Int, batch: Int, conf: Configuration) {
    fs.listStatus(new Path("data/source")).grouped(batch)
      .foreach { batch => 
        batch.foreach { status =>

          val file = status.getPath

          val destination =
            new Path("data/input", System.currentTimeMillis() + "_" + file.getName)

          println(s"Copying File: ${file.getName}")

          FileUtil.copy(
            fs, file,          // source file
            fs, destination,   // destination file
            false,             // don't delete source
            conf
          )
        }
        println("----------- END OF BATCH ------------")
        Thread.sleep(frequency * 1000)
      }
  }

}