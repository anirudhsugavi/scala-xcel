package com.sugavi.xcel.writer

import com.sugavi.xcel.model.*
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFWorkbook}

import XcelOptions.*

object XSSFXcelHandler:

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

  private def setPoiCellValue(
    poiCell: XSSFCell,
    cell: Cell,
    numericStyle: CellStyle,
    dateStyle: CellStyle,
    dateTimeStyle: CellStyle
  ): Unit =
    cell.value match
      case StringXcel(s) =>
        poiCell.setCellValue(s)
      case IntXcel(i) =>
        poiCell.setCellValue(i.toString)
        poiCell.setCellStyle(numericStyle)
      case LongXcel(l) =>
        poiCell.setCellValue(l.toString)
        poiCell.setCellStyle(numericStyle)
      case DoubleXcel(d) =>
        poiCell.setCellValue(d)
      case BooleanXcel(b) =>
        poiCell.setCellValue(b)
      case DateXcel(d) =>
        poiCell.setCellValue(d)
        poiCell.setCellStyle(dateStyle)
      case DateTimeXcel(dt) =>
        poiCell.setCellValue(dt)
        poiCell.setCellStyle(dateTimeStyle)
      case EmptyXcel =>
        poiCell.setBlank()

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
