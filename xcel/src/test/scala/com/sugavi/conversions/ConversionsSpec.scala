package com.sugavi.conversions

import com.sugavi.xcel.model.*
import com.sugavi.xcel.syntax.{*, given}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}

class ConversionsSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {

  "Scala to Xcel" should "scala primitives to Xcel" in {
    forAll {
      (
        str: String,
        dob: Double,
        int: Int,
        long: Long,
        bool: Boolean,
        date: LocalDate,
        dateTime: LocalDateTime,
        optStr: Option[String],
        optDob: Option[Double]
      ) =>
        whenever(long <= Int.MaxValue && long >= Int.MinValue) {
          val strX      = str.as[StringXcel]
          val dobX      = dob.as[NumberXcel]
          val intX      = int.as[NumberXcel]
          val longX     = long.as[NumberXcel]
          val boolX     = bool.as[BooleanXcel]
          val dateX     = date.as[DateXcel]
          val dateTimeX = dateTime.as[DateTimeXcel]
          val optStrX   = optStr.as[XcelValue]
          val optDobX   = optDob.as[XcelValue]

          strX.to[String] shouldEqual str
          dobX.to[Double] shouldEqual dob
          intX.to[Int] shouldEqual int
          longX.to[Long] shouldEqual long
          boolX.to[Boolean] shouldEqual bool
          dateX.to[LocalDate] shouldEqual date
          dateTimeX.to[LocalDateTime] shouldEqual dateTime
          optStrX.to[Option[String]] shouldEqual optStr
          optDobX.to[Option[Double]] shouldEqual optDob
        }
    }
  }
}

case class TestStringToXcel(xcelString: StringXcel)
