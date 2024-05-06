package project.user_interface

import project.Application.Chatbot
import scala.io.StdIn.readLine

@main
def main(): Unit = {
  val chatbot = Chatbot("university");

  while (true){
    val question = readLine("Question: ")
    println(chatbot.generateResponse(question))
  }
}
