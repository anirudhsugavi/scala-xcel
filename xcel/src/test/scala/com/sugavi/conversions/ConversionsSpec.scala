package com.sugavi.conversions

import com.sugavi.xcel.mappers.Mappers
import com.sugavi.xcel.model.*
import com.sugavi.xcel.syntax.given
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}
import scala.language.implicitConversions

class ConversionsSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {

  "2 way conversions" should "test scala <-> xcel data conversions" in {
    val sheet = Mappers.deriveSheet(Seq(User("ani", Some(32)), User("ana", None)))
    // val sheet = Mappers.deriveSheet(Seq(User("ani", 32), User("ana", 28)))
    def getUsers: Seq[User] = Seq.empty
    println(Mappers.deriveSheet(getUsers))
    println(sheet)
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

case class User(name: String, age: Option[Int])
