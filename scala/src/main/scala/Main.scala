import java.nio.{ByteBuffer, ByteOrder}
import java.nio.file.{Files, Paths}

import fft.Document.Document
import io.circe.Json
import io.circe.parser._

import scala.io.Source

object Main extends App {

  val jsonFile = Source.fromFile("C:/Users/Izmar/git/file-format-tool/wave.json")
  val jsonFileData = jsonFile.getLines.mkString

  jsonFile.close()

  val json: Json = parse(jsonFileData) match {

    case Left(_) =>
      println("error!")
      Json.fromString("")

    case Right(json) =>
      json
  }

  val document = json.as[Document] match {
    case Left(_) => Document(Seq.empty)
    case Right(document) => document
  }

  val wavFile = Files.readAllBytes(Paths.get("C:/Users/Izmar/git/file-format-tool/break.wav"))

  var bytesRead = 0

  var intDict = scala.collection.mutable.Map.empty[String, Int]

  document.entries.foreach { entry =>

    val bytesToRead = (entry.size, entry.values, entry.derivedSize) match {
      case (Some(entrySize), None, None) => entrySize
      case (None, Some(allowedValues), None) => allowedValues.headOption.getOrElse("").length
      case (None, None, Some(derivedSizeKey)) => intDict.getOrElse(derivedSizeKey, 0)
      case _ => 0
    }

    if (bytesToRead == 0)
      System.exit(1)

    val bytes = wavFile.slice(bytesRead, bytesRead + bytesToRead)

    val valueString = entry.`type` match {
      case "int" =>
        getIntOrShort(entry.name, bytes, entry.size, entry.endianness)
      case "ascii" =>
        new String(bytes)

      case "int[]" =>
        val elementSize = entry.elementSize.getOrElse(0)
        val elements = bytes.grouped(elementSize)
        elements.take(10).map { element =>
          getIntOrShort(entry.name, element, entry.elementSize, entry.endianness)
        }
        .mkString(", ")
    }

    println(s"${entry.name}: $valueString")

    bytesRead += bytesToRead
  }

  def getIntOrShort(name: String, bytes: Array[Byte], elementSize: Option[Int], endianness: Option[String]) = {
    val order = endianness.getOrElse("") match {
      case "big" => ByteOrder.BIG_ENDIAN
      case "little" => ByteOrder.LITTLE_ENDIAN
      case _ => ByteOrder.BIG_ENDIAN
    }

    val bb = ByteBuffer.wrap(bytes).order(order)
    elementSize.getOrElse(0) match {
      case 2 => bb.getShort.toString
      case 4 =>
        val intValue = bb.getInt
        intDict.addOne(name, intValue)
        intValue.toString
      case _ => ""
    }
  }

}
