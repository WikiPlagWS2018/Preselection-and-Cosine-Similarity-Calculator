
import org.apache.spark.{SparkContext, SparkConf}
import com.datastax.spark.connector._

object MainClass {
    def main(args: Array[String]): Unit = {

      //Configuration:
      val conf = new SparkConf()
      conf.setMaster("local")
      conf.setAppName("Word Count")


      conf.set("spark.cassandra.connection.host", "hadoop05.f4.htw-berlin.de")
        .set("spark.cassandra.auth.username", "s0556238")
        .set("spark.cassandra.auth.password", "9p6a_U-W")
        //.setMaster("local[*]").setAppName(getClass.getName)

      val sc = new SparkContext(conf)
      val rdd = sc.cassandraTable("wikitest", "tokenized")




      //IDF Berechnung
      val totalDocumentsSize = rdd.count()
      val wordsInDocs = rdd.map(x => ( x.get[Int]("docid"),x.get[List[String]]("tokens")  ) )
             .flatMap(x => x._2.distinct).groupBy(x => x)
      val idfDict = wordsInDocs.map(x => (x._1, totalDocumentsSize / x._2.size))




      //Cassandra Speicherung
      idfDict.saveToCassandra("wikitest", "idf2", SomeColumns("word", "value"))
      println("IDF saved")
    }
  }

//PROBLEM: mit 383 dokumente, idf table = 113524