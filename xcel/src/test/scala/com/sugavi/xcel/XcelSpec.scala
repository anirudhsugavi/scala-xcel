package com.sugavi.xcel

import com.sugavi.xcel.XcelSpec.arbRestaurant
import org.scalacheck.Gen
import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalDateTime}
import scala.jdk.CollectionConverters.*

object XcelSpec:

  val genLocalDate: Gen[LocalDate]         = Gen.choose(0, 100).map(y => LocalDate.ofEpochDay(y))
  val genLocalDateTime: Gen[LocalDateTime] = genLocalDate.map(_.atStartOfDay)

  given arbRestaurant: Gen[Restaurant] =
    for
      name         <- Gen.alphaStr.suchThat(_.nonEmpty)
      rating       <- Gen.choose(1.0, 5.0)
      isInBusiness <- Gen.oneOf(true, false)
      established  <- genLocalDate
      lastVisited  <- genLocalDateTime
      latestPromo  <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
    yield Restaurant(name, rating, isInBusiness, established, lastVisited, latestPromo)

class XcelAsyncSpec extends AsyncFlatSpec with Matchers with ScalaCheckPropertyChecks:

  forAll(arbRestaurant) { restaurant =>
    it should s"convert $restaurant to XSSF Workbook" in {
        ScalaXcel.toExcelWorkbookFuture(Seq(restaurant)).map { workbook =>
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
    ScalaXcel.toExcelWorkbookFuture(Seq.empty[Restaurant]).map { workbook =>
      val sheet = workbook.getSheetAt(0)
      sheet.getSheetName shouldEqual "Restaurant"
      sheet.rowIterator().asScala shouldBe empty
    }
  }

  it should "convert a Seq with more than 1 item to a sheet with multiple rows" in {
    val restaurants = Gen.listOf(arbRestaurant).retryUntil(_.size > 1).sample.get

    ScalaXcel.toExcelWorkbookFuture(restaurants).map { workbook =>
      val sheet = workbook.getSheetAt(0)
      sheet.getSheetName shouldEqual "Restaurant"
      sheet.getRow(0).getCell(0).getStringCellValue shouldEqual "name"
      sheet.getRow(1).getCell(0).getStringCellValue shouldEqual restaurants.head.name
      sheet.getRow(2).getCell(0).getStringCellValue shouldEqual restaurants(1).name
    }
  }

  it should "honor provided options" in {
    val restaurant      = arbRestaurant.sample.get
    val customSheetName = "CustomSheet"
    ScalaXcel
      .toExcelWorkbookFuture(Seq(restaurant), XcelOptions(includeHeader = false, sheetName = Some(customSheetName)))
      .map { workbook =>
        val sheet = workbook.getSheetAt(0)
        sheet.getSheetName shouldEqual customSheetName
        sheet.rowIterator().asScala.size shouldEqual 1
        sheet.getRow(0).getCell(0).getStringCellValue shouldEqual restaurant.name
      }
  }

  it should "apply custom number format to numeric cells" in {
    val record       = FormatTestRecord("test", 42, 100L, LocalDate.of(2024, 1, 1), LocalDateTime.of(2024, 1, 1, 12, 0))
    val customFormat = "#,##0.00"
    ScalaXcel
      .toExcelWorkbookFuture(Seq(record), XcelOptions(numberFormat = customFormat))
      .map { workbook =>
        val sheet = workbook.getSheetAt(0)
        // Int cell (index 1)
        sheet.getRow(1).getCell(1).getCellStyle.getDataFormatString shouldEqual customFormat
        // Long cell (index 2)
        sheet.getRow(1).getCell(2).getCellStyle.getDataFormatString shouldEqual customFormat
      }
  }

  it should "apply custom date format to date cells" in {
    val record       = FormatTestRecord("test", 42, 100L, LocalDate.of(2024, 1, 1), LocalDateTime.of(2024, 1, 1, 12, 0))
    val customFormat = "dd/mm/yyyy"
    ScalaXcel
      .toExcelWorkbookFuture(Seq(record), XcelOptions(dateFormat = customFormat))
      .map { workbook =>
        val sheet = workbook.getSheetAt(0)
        // LocalDate cell (index 3)
        sheet.getRow(1).getCell(3).getCellStyle.getDataFormatString shouldEqual customFormat
      }
  }

  it should "apply custom datetime format to datetime cells" in {
    val record       = FormatTestRecord("test", 42, 100L, LocalDate.of(2024, 1, 1), LocalDateTime.of(2024, 1, 1, 12, 0))
    val customFormat = "dd/mm/yyyy HH:mm"
    ScalaXcel
      .toExcelWorkbookFuture(Seq(record), XcelOptions(dateTimeFormat = customFormat))
      .map { workbook =>
        val sheet = workbook.getSheetAt(0)
        // LocalDateTime cell (index 4)
        sheet.getRow(1).getCell(4).getCellStyle.getDataFormatString shouldEqual customFormat
      }
  }

  it should "apply default formats when using default options" in {
    val record = FormatTestRecord("test", 42, 100L, LocalDate.of(2024, 1, 1), LocalDateTime.of(2024, 1, 1, 12, 0))
    ScalaXcel
      .toExcelWorkbookFuture(Seq(record))
      .map { workbook =>
        val sheet = workbook.getSheetAt(0)
        sheet.getRow(1).getCell(1).getCellStyle.getDataFormatString shouldEqual XcelOptions.DefaultNumberFormat
        sheet.getRow(1).getCell(3).getCellStyle.getDataFormatString shouldEqual XcelOptions.DefaultDateFormat
        sheet.getRow(1).getCell(4).getCellStyle.getDataFormatString shouldEqual XcelOptions.DefaultDateTimeFormat
      }
  }

class XcelSyncSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks:

  forAll(arbRestaurant) { restaurant =>
    it should s"convert $restaurant to XSSF Workbook synchronously" in {
        val workbook = ScalaXcel.toExcelWorkbook(Seq(restaurant))
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
    import scala.compiletime.testing.*

    val errors = typeCheckErrors(
      """
        |ScalaXcel.toExcelWorkbook(Seq(ClassWithUnsupportedFieldType(1, 2)))
        |ScalaXcel.toExcelWorkbook(Seq(ClassWithUnsupportedOptionFieldType("hi", None)))
        |""".stripMargin
    )

    val expectedErrors = Seq(
      "Unsupported field types: [opt: scala.Option[scala.Short]]",
      "Unsupported field types: [short: scala.Short]"
    )

    errors.map(_.message) should contain theSameElementsAs expectedErrors
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

case class FormatTestRecord(
  name: String,
  count: Int,
  total: Long,
  createdDate: LocalDate,
  updatedAt: LocalDateTime
)
