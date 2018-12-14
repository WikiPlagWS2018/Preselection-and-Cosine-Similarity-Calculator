
import org.apache.spark.{SparkContext, SparkConf}
import com.datastax.spark.connector._

object MainClass {
    def main(args: Array[String]): Unit = {

      //Create a SparkContext to initialize Spark
      val conf = new SparkConf()
      //conf.setMaster("local")
      conf.setAppName("IDF")


      conf.set("spark.cassandra.connection.host", "hadoop05.f4.htw-berlin.de")
        .set("spark.cassandra.auth.username", "s0556238")
        .set("spark.cassandra.auth.password", "9p6a_U-W")
        //.setMaster("local[*]").setAppName(getClass.getName)


      val sc = new SparkContext(conf)

      val rdd = sc.cassandraTable("wiki2018", "tokens")
      println("Spark init")


      //IDF Berechnung
      val totalDocumentsSize = rdd.count()
      val regex = "^[a-zÀ-ÿ]+$"
      println("Start spark calculation")

      val idfDict = rdd.map(x => ( x.get[Int]("docid"),x.get[List[String]]("tokens")  ) )
        .flatMap(x => x._2.distinct)
        .filter(x=> (x matches regex) && (x.length>2) )
        .groupBy(x => x)
        .map(x => (x._1, totalDocumentsSize / x._2.size))

      println("Start save to Cassandra:")
      idfDict.saveToCassandra("wiki2018", "idf", SomeColumns("word", "value"))
      sc.stop()
      println("IDF saved")

    }
  }

//PROBLEM: mit 383 dokumente, idf table = 113524
//mit Filter werden dann: 108548