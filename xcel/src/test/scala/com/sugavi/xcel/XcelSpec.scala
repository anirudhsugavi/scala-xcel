package com.sugavi.xcel

import com.sugavi.xcel.XcelSpec.arbRestaurant
import org.scalacheck.Gen
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}

object XcelSpec {

  val genLocalDate: Gen[LocalDate]         = Gen.choose(0, 100).map(y => LocalDate.ofEpochDay(y))
  val genLocalDateTime: Gen[LocalDateTime] = genLocalDate.map(_.atStartOfDay)

  given arbRestaurant: Gen[Restaurant] =
    for {
      name         <- Gen.alphaStr.suchThat(_.nonEmpty)
      rating       <- Gen.choose(1.0, 5.0)
      isInBusiness <- Gen.oneOf(true, false)
      established  <- genLocalDate
      lastVisited  <- genLocalDateTime
      latestPromo  <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
    } yield Restaurant(name, rating, isInBusiness, established, lastVisited, latestPromo)
}

class XcelSpec extends AsyncFlatSpec with Matchers with ScalaCheckPropertyChecks {

  forAll(arbRestaurant) { restaurant =>
    "ScalaXcel" should s"convert $restaurant to XSSF Workbook" in {
      ScalaXcel.toExcelWorkbook(Seq(restaurant)).map { workbook =>
        val sheet = workbook.getSheetAt(0)
        sheet.getSheetName shouldEqual "Restaurant"
      }
    }
  }
}

case class Restaurant(
  name: String,
  rating: Double,
  isInBusiness: Boolean,
  established: LocalDate,
  lastVisited: LocalDateTime,
  latestPromo: Option[String]
)
