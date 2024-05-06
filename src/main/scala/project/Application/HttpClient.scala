package project.Application

import java.net.{HttpURLConnection, URI, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.io.Source

class HttpClient {

  def makeGETRequest(urlStr:String): String = {
    var response: String = "[something went wrong]"

    val url = URI.create(encodeQueryParameters(urlStr)).toURL
//    println("TOTO:::: Making GET request to :" + url)

    try {
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]

      // Set request method
      connection.setRequestMethod("GET")

      // Get the response code
      val responseCode = connection.getResponseCode

      // Read the response
      if (responseCode == HttpURLConnection.HTTP_OK) {
        response = Source.fromInputStream(connection.getInputStream).mkString
      }

      // Close the connection
      connection.disconnect()
    }
    catch case e => {}

    response

  }

  private def encodeQueryParameters(url: String): String = {
    try {
      val splitUrl = url.split("\\?", 2)
      val baseUrl = splitUrl.headOption.getOrElse("")
      val queryPart = splitUrl.lift(1).getOrElse("")

      val encodedQueryPart = queryPart.split("&")
        .map { param =>
          val keyValue = param.split("=")
          if (keyValue.length == 2) {
            URLEncoder.encode(keyValue(0), StandardCharsets.UTF_8.toString) +
              "=" +
              URLEncoder.encode(keyValue(1), StandardCharsets.UTF_8.toString)
          } else {
            param // Return as is if no "=" sign found (invalid query parameter)
          }
        }
        .mkString("&")

      if (encodedQueryPart.isEmpty) {
        baseUrl
      } else {
        s"$baseUrl?$encodedQueryPart"
      }
    } catch {
      case e: IllegalArgumentException =>
        println(s"Error encoding URL: ${e.getMessage}")
        url // Return original URL if encoding fails
    }
  }

/*
def makeRequest(endpoint: String, attributes: Map[String, String]): String = {
  var urlStr = endpoint

  // Append attributes as query parameters
  if (attributes.nonEmpty) {
    val queryString = attributes.map { case (key, value) => s"${encodeUrl(key)}=${encodeUrl(value)}" }.mkString("&")
    urlStr += s"?$queryString"
  }

  val url = URI.create(urlStr).toURL
  val connection = url.openConnection().asInstanceOf[HttpURLConnection]

  // Set request method
  connection.setRequestMethod("GET")

  // Get the response code
  val responseCode = connection.getResponseCode

  // Read the response
  var response:String = ""
  if (responseCode == HttpURLConnection.HTTP_OK) {
    response = Source.fromInputStream(connection.getInputStream).mkString
  }

  // Close the connection
  connection.disconnect()
  response
}

// Helper function to URL-encode query parameters
private def encodeUrl(str: String): String = {
  try {
    java.net.URLEncoder.encode(str, "UTF-8")
  } catch {
    case e: UnsupportedEncodingException =>
      throw new RuntimeException("UTF-8 encoding not supported", e)
  }
}
*/
  
}
