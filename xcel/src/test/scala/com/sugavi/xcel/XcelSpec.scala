package com.sugavi.xcel

import com.sugavi.xcel.XcelSpec.arbRestaurant
import org.scalacheck.Gen
import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}
import scala.jdk.CollectionConverters.*

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

class XcelAsyncSpec extends AsyncFlatSpec with Matchers with ScalaCheckPropertyChecks {

  forAll(arbRestaurant) { restaurant =>
    it should s"convert $restaurant to XSSF Workbook" in {
      ScalaXcel.toExcelWorkbook(Seq(restaurant)).map { workbook =>
        val sheet = workbook.getSheetAt(0)
        sheet.getSheetName shouldEqual "Restaurant"
        sheet.getRow(0).getCell(0).getStringCellValue shouldEqual "name"
        sheet.getRow(1).getCell(0).getStringCellValue shouldEqual restaurant.name

        sheet.getRow(0).getCell(1).getStringCellValue shouldEqual "rating"
        sheet.getRow(1).getCell(1).getNumericCellValue shouldEqual restaurant.rating

        sheet.getRow(0).getCell(4).getStringCellValue shouldEqual "lastVisited"
        sheet.getRow(1).getCell(4).getLocalDateTimeCellValue shouldEqual restaurant.lastVisited
      }
    }
  }

  it should "convert empty Seq to an empty sheet" in {
    ScalaXcel.toExcelWorkbook(Seq.empty[Restaurant]).map { workbook =>
      val sheet = workbook.getSheetAt(0)
      sheet.getSheetName shouldEqual "Restaurant"
      sheet.rowIterator().asScala shouldBe empty
    }
  }

  it should "convert a Seq with more than 1 item to a sheet with multiple rows" in {
    val restaurants = Gen.listOf(arbRestaurant).suchThat(_.size > 1).sample.get

    ScalaXcel.toExcelWorkbook(restaurants).map { workbook =>
      val sheet = workbook.getSheetAt(0)
      sheet.getSheetName shouldEqual "Restaurant"
      sheet.getRow(0).getCell(0).getStringCellValue shouldEqual "name"
      sheet.getRow(1).getCell(0).getStringCellValue shouldEqual restaurants.head.name
      sheet.getRow(2).getCell(0).getStringCellValue shouldEqual restaurants(1).name
    }
  }
}

class XcelSyncSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {

  forAll(arbRestaurant) { restaurant =>
    it should s"convert $restaurant to XSSF Workbook synchronously" in {
      val workbook = ScalaXcel.toExcelWorkbookSync(Seq(restaurant))
      val sheet    = workbook.getSheetAt(0)
      sheet.getSheetName shouldEqual "Restaurant"
      sheet.getRow(0).getCell(0).getStringCellValue shouldEqual "name"
      sheet.getRow(1).getCell(0).getStringCellValue shouldEqual restaurant.name

      sheet.getRow(0).getCell(1).getStringCellValue shouldEqual "rating"
      sheet.getRow(1).getCell(1).getNumericCellValue shouldEqual restaurant.rating

      sheet.getRow(0).getCell(4).getStringCellValue shouldEqual "lastVisited"
      sheet.getRow(1).getCell(4).getLocalDateTimeCellValue shouldEqual restaurant.lastVisited
    }
  }

  it should "throw compiler error for unsupported field types" in {
    import scala.compiletime.testing.typeChecks

    typeChecks("ScalaXcel.toExcelWorkbookSync(Seq(ClassWithUnsupportedFieldType(1, 2)))") shouldBe false
    typeChecks("""ScalaXcel.toExcelWorkbookSync(Seq(ClassWithUnsupportedOptionFieldType("hi", None)))""") shouldBe false
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

case class ClassWithUnsupportedFieldType(int: Int, short: Short)
case class ClassWithUnsupportedOptionFieldType(s: String, opt: Option[Short])
