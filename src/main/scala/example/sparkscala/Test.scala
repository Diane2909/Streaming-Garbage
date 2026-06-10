import org.apache.spark.sql.SparkSession

object Test {

  def main_fake(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
        .appName("TestApp")
        .master("local[*]")
        .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val df = spark.createDataFrame(Seq(
        (1, "Alice"),
        (2, "Bob")
    ))

    df.show()

    spark.stop()
  }

}