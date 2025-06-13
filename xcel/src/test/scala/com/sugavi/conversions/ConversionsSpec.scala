package com.sugavi.conversions

import com.sugavi.xcel.model.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}
import scala.language.implicitConversions

class ConversionsSpec extends AnyFlatSpec, Matchers, ScalaCheckPropertyChecks:

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
        import scala.compiletime.testing.*

        val errors = typeCheckErrors(
          """
            |import com.sugavi.xcel.syntax.given
            |
            |val strX: XcelValue      = str
            |val dobX: XcelValue      = dob
            |val intX: XcelValue      = int
            |val longX: XcelValue     = long
            |val boolX: XcelValue     = bool
            |val dateX: XcelValue     = date
            |val dateTimeX: XcelValue = dateTime
            |val optStrX: XcelValue   = optStr
            |val optDobX: XcelValue   = optDob
            |
            |XcelVals(str, dob, int, long, bool, date, dateTime, optStr, optDob)
            |ScalaVals(strX, dobX, intX, longX, boolX, dateX, dateTimeX, optStrX, optDobX)
            |""".stripMargin
        )

        errors shouldBe empty
    }
  }

case class ScalaVals(
  str: String,
  dob: Double,
  int: Int,
  long: Long,
  bool: Boolean,
  date: LocalDate,
  dateTime: LocalDateTime,
  optStr: Option[String],
  optDob: Option[Double]
)

case class XcelVals(
  str: XcelValue,
  dob: XcelValue,
  int: XcelValue,
  long: XcelValue,
  bool: XcelValue,
  date: XcelValue,
  dateTime: XcelValue,
  optStr: XcelValue,
  optDob: XcelValue
)
