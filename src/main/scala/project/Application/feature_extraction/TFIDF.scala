package project.Application.feature_extraction

import scala.collection.mutable.Map
import math.log
import scala.collection.mutable

/*
  Given the dataset in the form of a map
  key -> category id
  value -> vector of strings : all tokenized questions corresponding to this response
* */
class TFIDF(tokenizedDocs: mutable.Map[String, Vector[Vector[String]]]) {

  // Calculate TF for each response category
  val tf: mutable.Map[String, Vector[mutable.Map[String, Double]]] = {
    // Initialize a mutable map to store TF scores for each response category
    val tfScores = mutable.Map[String, Vector[mutable.Map[String, Double]]]()

    // Iterate over each response category
    tokenizedDocs.keys.foreach ( categoryId =>{
      // Initialize a mutable map to store TF scores for terms in the current response category
      var categoryTF = Vector[mutable.Map[String, Double]]()
      tokenizedDocs(categoryId).foreach(question =>{
        var questionTF = mutable.Map[String, Double]().withDefaultValue(0.0)
        // Count the frequency of each term in the current question
        question.foreach ( word => {questionTF(word) += 1})

        // Compute TF score for each term and update the questionTF map
        val totalWords = question.length.toDouble
        questionTF = questionTF.map((term, freq) => (term,freq / totalWords))

        categoryTF = categoryTF:+questionTF
      })
      // Store the TF scores for the current response category in the tfScores map
      tfScores(categoryId) = categoryTF}
      )

      tfScores // Return the computed TF scores
    }


    val idf: mutable.Map[String, Double] = {
      val documentFrequency = mutable.Map[String, Int]().withDefaultValue(0)
      val totalDocuments = tokenizedDocs.size.toDouble

      // Count document frequency for each term
      tokenizedDocs.values.foreach { document =>
        document.foreach { termList =>
          termList.toSet.foreach { term =>
            documentFrequency(term) += 1
          }
        }
      }

      // Calculate IDF
      documentFrequency.map { (term, df) =>
        (term, log(totalDocuments / (1 + df) + 1))
      }
    }

    val tfidf: mutable.Map[String, mutable.Map[String, Double]] = {
      // Initialize a mutable map to store TF-IDF scores for each response category
      val tfidfScores = mutable.Map[String, mutable.Map[String, Double]]()

      // Iterate over each response category
      tf.keys.foreach { categoryId =>
        if (!tfidfScores.contains(categoryId)) {
          tfidfScores(categoryId) = mutable.Map[String, Double]()
        }
        tf(categoryId).foreach(question =>
          question.foreach((word, freq) => {
            if(!tfidfScores(categoryId).contains(word))
              tfidfScores(categoryId)(word) = 0.0

            tfidfScores(categoryId)(word) += freq * idf(word)
          })
        )
      }
      tfidfScores // Return the computed TF-IDF scores
    }


  // Function to calculate TF-IDF vector for a single question
  def calculateTFIDFVector(question: Vector[String]): mutable.Map[String, Double] = {
    // Initialize a mutable map to store TF-IDF scores for the question
    val tfidfVector = mutable.Map[String, Double]().withDefaultValue(0.0)

    // Calculate TF for the question
    val questionTF = mutable.Map[String, Double]().withDefaultValue(0.0)
    question.foreach { term => questionTF(term) += 1 }
    val totalWords = question.length.toDouble
    val tf = questionTF.mapValues(_ / totalWords)

    // Calculate TF-IDF scores using IDF values
    tf.foreach { case (term, tfScore) =>
      val tfidfScore = tfScore * idf.getOrElse(term, 0.0)
      tfidfVector(term) = tfidfScore
    }

    // Convert the mutable map to an immutable map and return
    tfidfVector
  }
}
