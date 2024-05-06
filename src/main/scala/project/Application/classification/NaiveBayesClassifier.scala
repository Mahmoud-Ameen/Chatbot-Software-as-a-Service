package project.Application.classification

import scala.collection.mutable

class NaiveBayesClassifier {
  // Vocabulary of terms
  private var vocabulary: Set[String] = Set()

  // Prior probabilities of classes
  private var classPriors: mutable.Map[String, Double] = mutable.Map()

  // Conditional probabilities of terms given classes
  private var conditionalProbs: mutable.Map[String, mutable.Map[String, Double]] = mutable.Map()

  // Method to train the classifier
  def train(tfidf: mutable.Map[String, mutable.Map[String, Double]]): Unit = {
    // Initialize class counts
    val classCounts = mutable.Map[String, Int]().withDefaultValue(0)

    // Count occurrences of each term in each class and calculate class counts
    val termCounts = mutable.Map[String, mutable.Map[String, Int]]().withDefaultValue(mutable.Map())
    tfidf.keys.foreach( (key) => {
      val label = key
      val terms = tfidf(key).keys.toSet
      vocabulary ++= terms
      classCounts(label) += 1
      terms.foreach ( (term) => {
        termCounts(label) = termCounts(label).updated(term, termCounts(label).getOrElse(term, 0) + 1)
      })
    })

    // Calculate class priors
    val numDocuments = tfidf.keys.size
    classPriors = classCounts.map((key,value) => (key,value.toDouble / numDocuments))

    // Calculate conditional probabilities
    conditionalProbs = termCounts.map { (id,termCount) =>
      (id, termCount.map { (term, count) =>
        (term,(count.toDouble / termCount.values.sum))
      })
    }
  }

  // Method to predict the class of a new document
  def predict(tfidfVector: mutable.Map[String, Double]): String = {
    val classScores = mutable.Map[String, Double]().withDefaultValue(0.0)
    // Calculate scores for each class
    classPriors.keys.foreach ( label => {
      var score = math.log(classPriors(label))
      tfidfVector.keys.foreach( term =>
        if (conditionalProbs(label).contains(term)) {
          // adding 2 is called laplace smoothing
          score += math.log(conditionalProbs(label)(term) + 2)
        }
      )
      classScores(label) = score
    })

    // Return the class with the highest score
    classScores.maxBy(_._2)._1
  }
}
