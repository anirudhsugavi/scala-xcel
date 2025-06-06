package com.sugavi.xcel

import com.sugavi.xcel.model.*
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFWorkbook}

import java.time.{LocalDate, LocalDateTime}
import scala.annotation.tailrec

object XSSFXcelHandler {

  val DefaultNumberFormat   = "0"
  val DefaultDateFormat     = "yyyy-mm-dd"
  val DefaultDateTimeFormat = "yyyy-mm-dd HH:mm:ss"

  def toXSSFWorkbook(sheets: Seq[Sheet]): XSSFWorkbook =
    val workbook = new XSSFWorkbook()

    val numericStyle  = getNumericCellStyle(workbook)
    val dateStyle     = getDateCellStyle(workbook)
    val dateTimeStyle = getDateTimeCellStyle(workbook)

    sheets.foreach { sheet =>
      val xssfSheet = workbook.createSheet(sheet.name)
      val allRows   = sheet.header.toSeq ++ sheet.rows

      allRows.zipWithIndex.foreach {
        case (row, i) =>
          val xssfRow = xssfSheet.createRow(i)
          row.cells.zipWithIndex.foreach {
            case (cell, j) =>
              val xssfCell = xssfRow.createCell(j)
              setPoiCellValue(xssfCell, cell, numericStyle, dateStyle, dateTimeStyle)
          }
      }

      // set auto-sizing for columns
      allRows.headOption.toSeq.flatMap(_.cells).indices.foreach(xssfSheet.autoSizeColumn)
    }

    workbook

  @tailrec
  private def setPoiCellValue(
    poiCell: XSSFCell,
    cell: Cell[_],
    numericStyle: CellStyle,
    dateStyle: CellStyle,
    dateTimeStyle: CellStyle
  ): Unit =
    cell.value match
      case s: String =>
        poiCell.setCellValue(s)
      case n: Int =>
        poiCell.setCellValue(n.toString)
        poiCell.setCellStyle(numericStyle)
      case l: Long =>
        poiCell.setCellValue(l.toString)
        poiCell.setCellStyle(numericStyle)
      case d: Double =>
        poiCell.setCellValue(d)
      case b: Boolean =>
        poiCell.setCellValue(b)
      case d: LocalDate =>
        poiCell.setCellValue(d)
        poiCell.setCellStyle(dateStyle)
      case dt: LocalDateTime =>
        poiCell.setCellValue(dt)
        poiCell.setCellStyle(dateTimeStyle)
      case Some(op) =>
        setPoiCellValue(poiCell, Cell(op), numericStyle, dateStyle, dateTimeStyle)
      case None =>
        poiCell.setBlank()
      case _ =>
        throw new IllegalArgumentException(s"Unsupported cell value type: ${cell.value.getClass.getName}")

  private def getNumericCellStyle(workbook: XSSFWorkbook): CellStyle =
    val style  = workbook.createCellStyle()
    val format = workbook.createDataFormat()
    style.setDataFormat(format.getFormat(DefaultNumberFormat))

    style

  private def getDateCellStyle(workbook: XSSFWorkbook): CellStyle =
    val style  = workbook.createCellStyle()
    val format = workbook.createDataFormat()
    style.setDataFormat(format.getFormat(DefaultDateFormat))

    style

  private def getDateTimeCellStyle(workbook: XSSFWorkbook): CellStyle =
    val style  = workbook.createCellStyle()
    val format = workbook.createDataFormat()
    style.setDataFormat(format.getFormat(DefaultDateTimeFormat))

    style
}
