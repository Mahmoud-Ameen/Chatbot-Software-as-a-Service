package project.Application

import project.Application.classification.NaiveBayesClassifier
import project.Application.feature_extraction.TFIDF
import project.Application.preprocessing.InputParsing
import project.data_storage.DataManager
import project.Application.session_management.{Message, UserSession, UserSessionManager}

import scala.util.Random

class Chatbot (clientId: String, sessionId:String){
  private val dataManager = DataManager()

  private val dataset = dataManager.getData(clientId)
  private val tokenizedDataSet = InputParsing.tokenizeDataset(
    dataset.map((category,patternResponse) => (category, patternResponse.patterns)))

  private val featureExtractor = TFIDF(tokenizedDataSet)
  private val classifier = NaiveBayesClassifier()
  classifier.train(featureExtractor.tfidf)

  private val random = new Random()

  private val apiPlaceholderPattern = "\\[api_request:(.*?)\\]".r
  private val placeholderPattern = "\\{(.*?)\\}".r

  private def getSession: UserSession = UserSessionManager.getSession(sessionId, clientId)

  // Update session state with provided attribute values
  private def updateSessionStateAttribute(attributeName: String, attributeValue: String): Unit =
    UserSessionManager.addOrUpdateSession(
      getSession.copy(attributes = getSession.attributes + (attributeName -> attributeValue))
    )

  // Retrieve attribute values from session state
  private def getAttributeValue(attributeName: String): Option[String] = {
    getSession.attributes.get(attributeName)
  }

  private def handleAttributePlaceholders(response: String): String = {

    // Replace attribute placeholders with their values
    val placeholders = placeholderPattern.findAllIn(response)

    placeholders.foreach { placeholder =>
      val attributeName = placeholder.stripPrefix("{").stripSuffix("}")
      if (!getSession.attributes.contains(attributeName)) {
        // Prompt user for missing attribute
        UserSessionManager.addOrUpdateSession(
            getSession.copy(
              incompleteResponse = Some(response),
              promptedAttribute = Some(attributeName)))
      }
    }

    getSession.promptedAttribute match {
      case Some(attributeName) =>  s"What is your $attributeName ?"
      case None =>
        // Response generation
        // Replace placeholders with attribute values
        var result = response
        getSession.attributes.foreach { case (attributeName, attributeValue) =>
          result = result.replace(s"{$attributeName}", attributeValue)
        }
        result
    }
  }

  private def handleAPIRequests(response: String) : String = {

    var result = response
    // Placeholder processing for API requests
    val apiPlaceholders = apiPlaceholderPattern.findAllMatchIn(response).toList
    apiPlaceholders.foreach { matching =>
      val url = matching.matched.stripPrefix("[api_request:").stripSuffix("]")
      val reqRes = HttpClient().makeGETRequest(url)
      result = result.replace(matching.matched, reqRes)
    }

    result
  }
  private def processResponse(response:String) : String = {
    handleAPIRequests(handleAttributePlaceholders(response))
  }

  def handleUserInput(input:String) : String = {
    getSession.promptedAttribute match {
      case Some(promptedAttribute) => {
        updateSessionStateAttribute(promptedAttribute, input)
        UserSessionManager.addOrUpdateSession(getSession.copy(promptedAttribute = None))
        processResponse(getSession.incompleteResponse.get)
      }
      case None => generateResponse(input)
    }
  }

  private def addMessage(text:String, isUser:Boolean):Unit = {
    UserSessionManager.addOrUpdateSession(getSession.copy(messages = getSession.messages :+ Message(text,isUser)))
  }

  // Generate response based on user question and
  private def generateResponse(question: String): String = {

    val preprocessedQuestion = InputParsing.parseQuestion(question)
    println("preprocessedQuestion " + preprocessedQuestion )
    val tfidfVector = featureExtractor.calculateTFIDFVector(preprocessedQuestion)
    val category = classifier.predict(tfidfVector)
    val response = dataset(category).responses(random.nextInt(dataset(category).responses.length))

    // Replace attribute placeholders with their values
    // and perform necessary API calls and place update response
    processResponse(response)
  }

}
