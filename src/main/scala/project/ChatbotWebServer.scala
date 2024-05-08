package project
import project.Application.session_management.UserSessionManager
import project.Application.Chatbot

import java.io.*
import java.net.*
import java.util.HashMap as JHashMap

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import java.util.HashMap as JHashMap

object ChatbotWebServer {

  def main(args: Array[String]): Unit = {
    val serverSocket = new ServerSocket(8080)
    println("Server is listening on port 8080...")

    while (true) {
      val clientSocket = serverSocket.accept()
      println(s"Received request from ${clientSocket.getInetAddress}:${clientSocket.getPort}")

      val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
      val out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream))

      val request = in.readLine()
      println(s"Received request: $request")

      // Parse the request to get the endpoint and query parameters
      val endpointAndParams = request.split(" ")(1).split("\\?") // Assuming the first word after the method is the endpoint
      val endpoint = endpointAndParams(0)
      val queryParams = if (endpointAndParams.length > 1) parseQueryParams(endpointAndParams(1)) else new JHashMap[String, String]()

      // Delegate response generation to functions based on the endpoint
      val response = endpoint match {
        case "/start" => startSession(queryParams)
        case "/chat" => chat(queryParams)
        case _ => generateNotFoundResponse()
      }

      // Add CORS headers to the response
      val responseWithCorsHeaders = response

      // Send response with Content-Type header
      out.write(s"HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\n\r\n$responseWithCorsHeaders")
      out.flush()
      println("Response sent. : " + response)

      clientSocket.close()
    }
  }

  private def parseQueryParams(queryString: String): JHashMap[String, String] = {
    val params = new JHashMap[String, String]()
    queryString.split("&").foreach { param =>
      val keyValue = param.split("=")
      if (keyValue.length == 2) {
        params.put(keyValue(0), keyValue(1))
      }
    }
    params
  }

  private def startSession(queryParams: JHashMap[String, String]): String = {
    val responseJson = ("status" -> "200 OK") ~ ("sessionId" -> UserSessionManager.generateSessionId())

    compact(render(responseJson))
  }

  private def chat(queryParams: JHashMap[String, String]): String = {
    if (!queryParams.containsKey("clientId") || !queryParams.containsKey("sessionId") || !queryParams.containsKey("input")) {
      val responseJson = ("status" -> "400 Bad Request") ~ ("message" -> "Bad Request")

      compact(render(responseJson))
    } else {
      val chatbotResponse = Chatbot(queryParams.get("clientId"), queryParams.get("sessionId")).handleUserInput(URLDecoder.decode(queryParams.get("input"), "UTF-8"))

      val responseJson = ("status" -> "200 OK") ~ ("response" -> chatbotResponse)

      compact(render(responseJson))
    }
  }

  private def generateNotFoundResponse(): String = {
    val responseJson = ("status" -> "404 Not Found") ~ ("message" -> "Endpoint not found")

    compact(render(responseJson))
  }
}
