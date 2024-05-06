package project.Application.preprocessing

import java.io.{FileInputStream, InputStream}
import scala.collection.mutable

object InputParsing {

  def tokenizeDataset(data: mutable.Map[String,Vector[String]]): mutable.Map[String,Vector[Vector[String]]] = {
    data.map((category,questions) => (category, questions.map(question => parseQuestion(question))))
  }

  private def preprocessing(input: String): String = {
    var str = input.toLowerCase()

    val nonUsefulWords = Set(
      "to", "I", "the", "you", "and", "or", "in", "of", "for", "on", "at", "with",
      "is", "are", "am", "was", "were", "be", "been", "being", "this", "that",
      "these", "those", "it", "he", "she", "they", "we", "us", "them", "my", "your",
      "his", "her", "its", "our", "their", "a", "an", "some", "any", "all", "every",
      "each", "many", "much", "more", "most", "few", "fewer", "less", "no", "not",
      "never", "none", "nothing", "nobody", "nowhere", "someone", "something",
      "somewhere", "everybody", "everything", "everywhere", "anybody", "anything",
      "anywhere", "somebody", "sometime", "sometimes", "somewhat", "sometimes",
      "nowhere", "anywhere", "however", "therefore", "nevertheless", "furthermore",
      "meanwhile", "consequently", "although", "because", "since", "unless", "until",
      "while", "whereas", "whether", "while", "also", "even", "just", "only", "already",
      "even", "yet", "still", "so", "then", "thus", "hence", "accordingly", "besides",
      "moreover", "therefore", "otherwise", "instead", "further", "lest", "thus", "so",
      "thereby", "notwithstanding", "but", "however", "although", "while", "whereas",
      "yet", "though", "even", "if", "unless", "except", "even", "only", "merely",
      "just", "simply", "hardly", "scarcely", "barely", "approximately", "about",
      "around", "nearly", "roughly", "almost", "practically", "virtually", "literally",
      "exactly", "precisely", "definitely", "certainly", "surely", "undoubtedly",
      "absolutely", "altogether", "completely", "entirely", "totally", "wholly",
      "fully", "quite", "rather", "somewhat", "fairly", "reasonably", "relatively",
      "moderately", "approximately", "not quite", "not wholly", "not entirely",
      "not completely", "not absolutely", "not entirely", "not totally", "not wholly",
      "not fully", "not quite", "not necessarily", "not necessarily", "not relatively",
      "not reasonably", "not fairly", "not moderately", "not approximately",
      "not hardly", "not scarcely", "not barely", "not almost", "not practically",
      "not virtually", "not literally", "not exactly", "not precisely", "not definitely",
      "not certainly", "not surely", "not undoubtedly", "not absolutely", "not altogether",
      "not completely", "not entirely", "not totally", "not wholly", "not quite",
      "not rather", "not somewhat", "not fairly", "not reasonably", "not relatively",
      "not moderately", "not approximately", "not quite", "not a", "not an", "not some",
      "not any", "not all", "not every", "not each", "not many", "not much", "not more",
      "not most", "not few", "not fewer", "not less", "not no", "not never", "not none",
      "not nothing", "not nobody", "not nowhere", "not someone", "not something",
      "not somewhere", "not everybody", "not everything", "not everywhere", "not anybody",
      "not anything", "not some", "not somebody", "not sometimes", "not somewhere",
      "not somewhat", "not sometimes", "not nowhere", "not anywhere", "not however",
      "not therefore", "not nevertheless", "not furthermore", "not meanwhile",
      "not consequently", "not although", "not because", "not since", "not unless",
      "not until", "not while", "not whereas", "not whether", "not while", "not also",
      "not even", "not just", "not only", "not already", "not even", "not yet", "not still",
      "not so", "not then", "not thus", "not hence", "not accordingly")
    nonUsefulWords.foreach(word => str.replace(str,""))

    str = str.replace("won't", "will not")
    str = str.replace("can't", "can not")
    str = str.replace("'ain't", " am not")
    str = str.replace("n't", " not")
    str = str.replace("'ll", " will")
    str = str.replace("'ve", " have")
    str = str.replace("'re", " are")
    str = str.replace("'m", " am")
    str = str.replace("'s", " is")
    str = str.replace("'em", " them")
    str = str.replace("'all", " all")
    str = str.replace("'bout", " about")
    str = str.replace("'cause", " because")
    str = str.replace("'til", " until")
    str = str.replace("'d", " would")
    str = str.replace("[^a-zA-Z0-9]", " ")
    str = str.replace("\\W+", " ") // W+ means one or more non-word characters, so it will split the string by one or more non-word characters.
    // Handling contractions and special cases appropriately (e.g., "don't" -> "do not").
    str
  }

  // function to make tokens from the input, it take the string returned by preprocessing function
  private def tokenization(input: String): Vector[Vector[Any]] = {
    val tokens = input.split("\\W+").zipWithIndex.toVector
    tokens.grouped(3).map { group =>
      group.map { case (token, index) => Vector(token, index, 0) } // THE REAL ART.
    }.toVector
  }

  // a function to return the tokens vector but with the first element in the inner vector only and without their POS tags
  def parseQuestion(input: String): Vector[String] = {
    val preprocessedInput = preprocessing(input)
    val tokens = tokenization(preprocessedInput)
    //val posTaggedTokens = posTagOpenNLP(tokens)

    // return the tokens vector but with the first element in the inner vector only and without their POS tags
    tokens.flatten.map {
      case Vector(token: String, _, _) => token
    }
    //posTaggedTokens
  }

}