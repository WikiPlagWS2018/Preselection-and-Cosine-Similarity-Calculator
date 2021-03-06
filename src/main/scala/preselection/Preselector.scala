package preselection

import org.apache.spark.{ SparkContext, SparkConf }
import com.datastax.spark.connector._

class Preselector(userinput: String) {

  //Create a SparkContext to initialize Spark
  val conf = new SparkConf()
  conf.setMaster("local[*]")
  conf.setAppName("[WIKIPLAG] create IDF")

  conf.set("spark.cassandra.connection.host", "hadoop05.f4.htw-berlin.de")
    .set("spark.cassandra.auth.username", "********") // user name for spark cluster
    .set("spark.cassandra.auth.password", "*******") //password for spark cluster
    .setMaster("local[*]").setAppName(getClass.getName)
  println("spark config OK")

  val sc = new SparkContext(conf)
  val idfRdd = sc.cassandraTable("wiki2018", "idf")
  val tokenRdd = sc.cassandraTable("wiki2018", "tokens") //wiki2018 tokens

  /**
   * (word, idf value) map from the cassandra database
   */
  val wikiIdf = idfRdd.map(x => (x.get[String]("word"), x.get[Double]("value"))).collect().toMap

  /**
   * The whole wikipedia corpus as map (Int, List[String]) - (Document ID, List of tokens)
   */
  val corpus = tokenRdd.map(x => (x.get[Int]("docid"), x.get[List[String]]("tokens")))

  /**
   * The userinput is transformed into a map with (word,idf value)
   */
  var idfInput: Map[String, Double] = _

  /**
   * Calculates the idf of the user input
   */
  def calculateIDF = {
    val input = this.userinput
    this.idfInput = this.tokenizeString(input).map(X => (X, this.wikiIdf.getOrElse(X, 0.0))).filter(X => X._2 != 0.0).toMap
  }

  /**
   * returns the idf map of the input
   */
  def getIdfInput: Map[String, Double] = this.idfInput

  /**
   * get the top n words - n words with the highest idf
   */
  def getTopNWords(n: Int): List[String] = this.idfInput.toList.sortWith((A, B) => A._2 > B._2).map(_._1).take(n)

  def getTopN(n: Int): Map[String, List[String]] = {
    val importantWords = getTopNWords(n)
    print(importantWords)
    val topN = corpus.filter(X => importantWords.diff(X._2).length < importantWords.length).collect().toMap
    topN.map(X => (X._1.toString(), X._2)) + ("userinput" -> this.tokenizeString(this.userinput))
  }

  def tokenizeString(s: String): List[String] = {
    //val split_regex = "\\W+"
    val words = s.toLowerCase.split(" ").toList
    words.filter(_ != "")
  }
}
