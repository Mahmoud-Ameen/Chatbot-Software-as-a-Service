package project.user_interface

import project.Application.Chatbot
import project.Application.session_management.UserSessionManager

import scala.io.StdIn.readLine

@main
def main(): Unit = {
  val chatbot = Chatbot("university", UserSessionManager.generateSessionId());

  while (true){
    val question = readLine("Question: ")
    println(chatbot.handleUserInput(question))
  }
}
