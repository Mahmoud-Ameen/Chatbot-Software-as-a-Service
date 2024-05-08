package project.Application.session_management
import scala.io.Source
import java.io.{File, PrintWriter}
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization

import java.util.UUID

case class Message(text:String, isUser:Boolean)
case class UserSession(sessionId: String,
                       clientId: String,
                       attributes: Map[String, String],
                       incompleteResponse: Option[String],
                       promptedAttribute: Option[String],
                       messages:Vector[Message] = Vector())

object UserSessionManager {
  // Directory to store session files
  private val sessionDirectory = "sessions/"

  // Generate a unique session ID
  def generateSessionId(): String = {
    val timestamp = System.currentTimeMillis()
    val randomComponent = UUID.randomUUID().toString.replace("-", "")
    s"$timestamp-$randomComponent"
  }

  // Read sessions from the JSON file
  def readSessions(clientId: String): List[UserSession] = {
    val jsonFilePath = s"$sessionDirectory$clientId.sessions.json"
    println("reading from : " + jsonFilePath)
    if (new File(jsonFilePath).exists()) {
      val source = Source.fromFile(jsonFilePath)
      try {
        val jsonString = source.getLines.mkString
        implicit val formats: DefaultFormats.type = DefaultFormats
        parse(jsonString).extract[List[UserSession]]
      } finally {
        source.close()
      }
    } else {
      List.empty[UserSession]
    }
  }

  // Write sessions to the JSON file
  private def writeSessions(clientId: String, sessions: List[UserSession]): Unit = {
    val jsonFilePath = s"$sessionDirectory$clientId.sessions.json"
    println("writing to : " + jsonFilePath)
    val sessionDirectoryFile = new File(sessionDirectory)
    if (!sessionDirectoryFile.exists()) {
      sessionDirectoryFile.mkdirs()
    }
    implicit val formats: DefaultFormats.type = DefaultFormats
    val json = Serialization.writePretty(sessions)
    val writer = new PrintWriter(new File(jsonFilePath))
    try {
      writer.write(json)
    } finally {
      writer.close()
    }
  }

  // Add or update a user session
  def addOrUpdateSession(session: UserSession): Unit = {
    val sessions = readSessions(session.clientId)
    val updatedSessions = sessions.filterNot(_.sessionId == session.sessionId) :+ session
    writeSessions(session.clientId, updatedSessions)
  }

  // Retrieve a user session by session ID and client ID
  // If session doesn't exist, one with these ids is created and returned
  def getSession(sessionId: String, clientId: String): UserSession = {
    val sessions = readSessions(clientId)
    val session = sessions.find(_.sessionId == sessionId)
    session match{
      case Some(value) => value
      case None =>
        val created = UserSession(sessionId,clientId,Map[String,String](),None,None)
        addOrUpdateSession(created)
        created
    }
  }

  // Delete a user session by session ID and client ID
  def deleteSession(sessionId: String, clientId: String): Unit = {
    val sessions = readSessions(clientId)
    val updatedSessions = sessions.filterNot(_.sessionId == sessionId)
    writeSessions(clientId, updatedSessions)
  }
}
