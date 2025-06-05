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
          val strX: StringXcel        = str
          val dobX: NumberXcel        = dob
          val intX: NumberXcel        = int
          val longX: NumberXcel       = long
          val boolX: BooleanXcel      = bool
          val dateX: DateXcel         = date
          val dateTimeX: DateTimeXcel = dateTime
          val optStrX: XcelValue      = optStr
          val optDobX: XcelValue      = optDob

          val str1: String             = strX
          val dob1: Double             = dobX
          val int1: Int                = intX
          val long1: Long              = longX
          val bool1: Boolean           = boolX
          val date1: LocalDate         = dateX
          val dateTime1: LocalDateTime = dateTimeX
          val optStr1: Option[String]  = optStrX
          val optDob1: Option[Double]  = optDobX
        }
    }
  }
}
