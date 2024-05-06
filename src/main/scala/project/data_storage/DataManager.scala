package project.data_storage

import scala.annotation.tailrec
import scala.io.StdIn.readLine
import scala.io.Source
import java.io.{File, PrintWriter}
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization.write
import scala.collection.mutable

implicit val formats: DefaultFormats.type = DefaultFormats

case class PatternResponse(patterns: Vector[String], responses: Vector[String])

class DataManager(company:String) {

	// TODO: fix file path
	private val filePath = "Temporarily please replace this string with the files path"+company+".json"

	def getData(): mutable.Map[String, PatternResponse] = {
		val json = readAllJson(filePath)
		json.extract[mutable.Map[String, PatternResponse]]
	}

	private def readAllJson(filename: String): JValue = {
		val source = Source.fromFile(filename)
		val json = try source.mkString finally source.close()
		parse(json) match { // parse a JSON string as input and returns a JValue object
			case JNothing => JNothing
			case data => data
		}
	}
}
