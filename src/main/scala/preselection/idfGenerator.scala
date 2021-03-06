package preselection

import org.apache.spark.{SparkContext, SparkConf}
import com.datastax.spark.connector._
import scala.reflect.api.materializeTypeTag

object Generator {
    def generate_idf(): Unit = {

      // Create a SparkContext to initialize Spark
      val conf = new SparkConf()
      // conf.setMaster("local")
      conf.setAppName("[WIKIPLAG] create IDF")

      conf.set("spark.cassandra.connection.host", "hadoop05.f4.htw-berlin.de")
        .set("spark.cassandra.auth.username", "********") // user name for spark cluster
        .set("spark.cassandra.auth.password", "*********") // password for spark cluster
        //.setMaster("local[*]").setAppName(getClass.getName)
      println("spark config OK")
      
      // getting RDD
      val sc = new SparkContext(conf)
      val rdd = sc.cassandraTable("wiki2018", "tokens")

      //IDF Berechnung
      println("start calculation")
      val totalDocumentsSize = rdd.count()
      val regex = "^[a-zÀ-ÿ]+$"
      println("Start spark calculation")

      val idfDict = rdd.map(x => ( x.get[Int]("docid"),x.get[List[String]]("tokens")  ) )
        .flatMap(x => x._2.distinct)
        .filter(x=> (x matches regex) && (x.length>2) )
        .groupBy(x => x)
        .map(x => (x._1, totalDocumentsSize / x._2.size))

      println("start saving to db")
      idfDict.saveToCassandra("wiki2018", "idf2", SomeColumns("word", "value"))
      sc.stop()
      println("success")

    }
  }

//PROBLEM: mit 383 dokumente, idf table = 113524
//mit Filter werden dann: 108548
