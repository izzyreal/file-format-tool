package fft

import io.circe._, io.circe.generic.semiauto._

object Entry {

  case class Entry(name: String,
                   `type`: String,
                   values: Option[Seq[String]],
                   signedness: Option[Boolean],
                   endianness: Option[String],
                   size: Option[Int],
                   derivedSize: Option[String],
                   elementSize: Option[Int]
                  )

  implicit def optionDec[A: Decoder]: Decoder[Option[A]] = Decoder.decodeOption[A].handleErrorWith { _ =>
    Decoder.const(None)
  }

  implicit val entryDecoder: Decoder[Entry] = deriveDecoder

  implicit val entryEncoder: Encoder[Entry] = deriveEncoder
}