package preselection

object PreselectorTestWorksheet {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(221); 
  def tokenizeString(s: String): List[String] = {
    val split_regex = "\\W+"

    val words = s.toLowerCase.split(split_regex).toList
    words.filter(_ != "")
  };System.out.println("""tokenizeString: (s: String)List[String]""");$skip(78); 

  val testInput = "Hallo wer bin ich, ich bin Steven, ich habe einen Laptop";System.out.println("""testInput  : String = """ + $show(testInput ));$skip(82); 

  val wikiIdf = Map("ich" -> 2.0, "laptop" -> 3.0, "hallo" -> 1.0, "bin" -> 5.0);System.out.println("""wikiIdf  : scala.collection.immutable.Map[String,Double] = """ + $show(wikiIdf ));$skip(236); 

  val corpusUntokenized = Map(
    1 -> "Was meinst du mit Ich-Satz",
    2 -> "Es gibt Erzählungen, die in der ersten Person geschrieben sind, da kommt natürlich zu Hauf vor",
    3 -> "ich meine Beides", 4 -> "Laptop laptop laptop");System.out.println("""corpusUntokenized  : scala.collection.immutable.Map[Int,String] = """ + $show(corpusUntokenized ));$skip(73); 

  val corpus = corpusUntokenized.map(X => (X._1, tokenizeString(X._2)));System.out.println("""corpus  : scala.collection.immutable.Map[Int,List[String]] = """ + $show(corpus ));$skip(115); 
  val idfInput = tokenizeString(testInput).map(X => (X, wikiIdf.getOrElse(X, 0.0))).filter(X => X._2 != 0.0).toMap;System.out.println("""idfInput  : scala.collection.immutable.Map[String,Double] = """ + $show(idfInput ));$skip(84); 
  val top2Words = idfInput.toList.sortWith((A, B) => A._2 < B._2).map(_._1).take(2);System.out.println("""top2Words  : List[String] = """ + $show(top2Words ));$skip(76); val res$0 = 

  corpus.filter(X => top2Words.diff(X._2).length < top2Words.length).toMap;System.out.println("""res0: scala.collection.immutable.Map[Int,List[String]] = """ + $show(res$0))}
}
