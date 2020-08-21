package fft

import fft.Entry.Entry
import io.circe._
import io.circe.generic.semiauto._

object Document {

  case class Document(entries: Seq[Entry])

  implicit val documentEncoder: Encoder[Document] = deriveEncoder
  implicit val documentDecoder: Decoder[Document] = deriveDecoder

}
