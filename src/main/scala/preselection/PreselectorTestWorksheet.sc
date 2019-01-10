package preselection

object PreselectorTestWorksheet {
  def tokenizeString(s: String): List[String] = {
    val split_regex = "\\W+"

    val words = s.toLowerCase.split(split_regex).toList
    words.filter(_ != "")
  }

  val testInput = "Hallo wer bin ich, ich bin Steven, ich habe einen Laptop"

  val wikiIdf = Map("ich" -> 2.0, "laptop" -> 3.0, "hallo" -> 1.0, "bin" -> 5.0)

  val corpusUntokenized = Map(
    1 -> "Was meinst du mit Ich-Satz",
    2 -> "Es gibt Erzählungen, die in der ersten Person geschrieben sind, da kommt natürlich zu Hauf vor",
    3 -> "ich meine Beides", 4 -> "Laptop laptop laptop")

  val corpus = corpusUntokenized.map(X => (X._1, tokenizeString(X._2)))
  val idfInput = tokenizeString(testInput).map(X => (X, wikiIdf.getOrElse(X, 0.0))).filter(X => X._2 != 0.0).toMap
  val top2Words = idfInput.toList.sortWith((A, B) => A._2 < B._2).map(_._1).take(2)

  corpus.filter(X => top2Words.diff(X._2).length < top2Words.length).toMap
}