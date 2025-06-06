package com.sugavi.conversions

import com.sugavi.xcel.model.*
import com.sugavi.xcel.syntax.given
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}
import scala.language.implicitConversions

class ConversionsSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {

  "2 way conversions" should "test scala <-> xcel data conversions" in {
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
        val strX: XcelValue      = str
        val dobX: XcelValue      = dob
        val intX: XcelValue      = int
        val longX: XcelValue     = long
        val boolX: XcelValue     = bool
        val dateX: XcelValue     = date
        val dateTimeX: XcelValue = dateTime
        val optStrX: XcelValue   = optStr
        val optDobX: XcelValue   = optDob

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

case class User(name: String, age: Option[Int])
