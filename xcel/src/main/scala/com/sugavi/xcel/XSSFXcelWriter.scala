package com.sugavi.xcel

import com.sugavi.xcel.model.*
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFWorkbook}

object XSSFXcelWriter:

  def toXSSFWorkbook(sheets: Seq[Sheet], options: XcelOptions = XcelOptions.Default): XSSFWorkbook =
    val workbook = new XSSFWorkbook()

    val numericStyle  = getNumericCellStyle(workbook, options.numberFormat)
    val dateStyle     = getDateCellStyle(workbook, options.dateFormat)
    val dateTimeStyle = getDateTimeCellStyle(workbook, options.dateTimeFormat)

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

  private def getNumericCellStyle(workbook: XSSFWorkbook, numberFormat: String): CellStyle =
    val style  = workbook.createCellStyle()
    val format = workbook.createDataFormat()
    style.setDataFormat(format.getFormat(numberFormat))

    style

  private def getDateCellStyle(workbook: XSSFWorkbook, dateFormat: String): CellStyle =
    val style  = workbook.createCellStyle()
    val format = workbook.createDataFormat()
    style.setDataFormat(format.getFormat(dateFormat))

    style

  private def getDateTimeCellStyle(workbook: XSSFWorkbook, dateTimeFormat: String): CellStyle =
    val style  = workbook.createCellStyle()
    val format = workbook.createDataFormat()
    style.setDataFormat(format.getFormat(dateTimeFormat))

    style
