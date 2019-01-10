package CosineJacard
import java.io.{ File, PrintWriter }

class CosineJaccardCalc(inputMap: Map[String, List[String]]) {

  val totalNumberOfDocs: Double = inputMap.size.toDouble

  val output_filename: String = "output.txt"

  /**
    * Count the occurrence of each word in a string
    * @param text - string
    * @return - List of tuples (word, occurrence)
    */
  def countWords(text: List[String]): List[(String, Int)] = {
    text.groupBy(identity).mapValues(_.size).toList
  }

  /**
    * Calculates the term frequency
    * @param list
    * @return
    */
  def termFreq(list: List[(String, Int)]): List[(String, Double)] = {
    val maxFreq = list.maxBy(_._2)._2

    list.map(x => (x._1, x._2.toDouble / maxFreq.toDouble))
  }

  /**
    * creates an inverse word index
    * @param map
    * @return
    */
  def createInverseWordIndex(map: Map[String, List[String]]): Map[String, Iterable[(String, String)]] = {
    def helper(key: String, list: List[String]): List[(String, String)] = {
      list.map(x => (x, key)).distinct
    }

    map.map(x => helper(x._1, x._2)).flatten.groupBy(_._1)
  }

  /**
    * calculates the inverse document frequency
    * @param wordList
    * @return
    */
  def calcIDF(wordList: List[(String, Double)]): List[(String, Double)] = {
    def helper(word: String): Double = {
      val docsWithTerm = inverseWordIndex.get(word).toList.flatten.size

      math.log(totalNumberOfDocs / docsWithTerm.toDouble)
    }

    wordList.map(f => (f._1, helper(f._1) * f._2))
  }

  /**
    * Creates a list of ngrams of a string
    * @param text - input string
    * @param n - size of each ngram
    * @return
    */
  def createNgrams(text: List[String], n: Int): List[List[String]] = {
    text.sliding(n).toList
  }

  /**
    * Calculates the cosine similarity of two ngrams
    * @param n1
    * @param n2
    * @param tfidfMap
    * @return
    */
  def calcCosineSimilarity(n1: List[String], n2: List[String], tfidfMap: Map[String, Double]): Double = {
    val baseVector = n1 ++ n2

    def getTfidf(s: String, list: List[String]): Double = {
      if (list.contains(s)) tfidfMap(s) else 0.0
    }

    val n1vec = baseVector.map(f => f -> getTfidf(f, n1)).distinct.toMap
    val n2vec = baseVector.map(f => f -> getTfidf(f, n2)).distinct.toMap

    val dotProduct = n1vec.foldLeft(0.0)((x, y) => x + (y._2 * n2vec(y._1)))
    val n1vecMagnitude = math.sqrt(n1vec.values.map(x => x * x).sum)
    val n2vecMagnitude = math.sqrt(n2vec.values.map(x => x * x).sum)

    dotProduct / (n1vecMagnitude * n2vecMagnitude)
  }

  /**
    * Calculates the Jaccard Similarity of two ngramms
    * @param n1 - ngramm1
    * @param n2 - ngramm2
    * @return jaccardValue
    */
  def calcJaccardSimilarity(n1: List[String], n2: List[String]): Double = {
    // amount of the intersection of n1 and n2
    val intersection = n1.intersect(n2).size

    // amount of the union of n1 and n2
    val allElements = n1.union(n2).distinct.size

    intersection.toDouble / allElements.toDouble
  }

  def calculate(userNgramms: List[List[String]], potPlagNgramms: List[List[String]], tfidfMap: Map[String, Double]): Unit = {
    val pw = new PrintWriter(new File(output_filename))

    for(userngramm <- userNgramms; potngramm <- potPlagNgramms) {
      val cosineScore = calcCosineSimilarity(userngramm, potngramm, tfidfMap)
      val jaccardScore = calcJaccardSimilarity(userngramm, potngramm)

      pw.write(userngramm + " " + potngramm + " " + cosineScore + " " + jaccardScore + "\n")
    }

    pw.close()
  }

  // Count all words
  val countWordsMap: Map[String, List[(String, Int)]] = inputMap.map(f => f._1 -> countWords(f._2))

  // create inverseWordIndex
  val inverseWordIndex: Map[String, Iterable[(String, String)]] = createInverseWordIndex(inputMap)

  // create tfidf map
  val tfidfMap: Map[String, Double] = countWordsMap.map(f => f._1 -> calcIDF(termFreq(f._2))).values.flatten.toMap

  // create ngramm (length 13) map
  val ngrammMap: Map[String, List[List[String]]] = inputMap.map(f => f._1 -> createNgrams(f._2, 13))

  // contains all the userinput ngrams
  val userinput_ngrams: List[List[String]] = ngrammMap("userinput")

  // contains all the potential plagiarism ngrams
  val pot_plag_ngrams: List[List[String]] = (ngrammMap - "userinput").values.flatten.toList

  // calculates cosineSimilarity and jaccard for every ngramm pair
  // writes the output to -> output.txt
  this.calculate(userinput_ngrams, pot_plag_ngrams, tfidfMap)
}