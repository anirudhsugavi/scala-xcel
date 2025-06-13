package com.sugavi.xcel

import com.sugavi.xcel.mappers.Mappers
import com.sugavi.xcel.model.Sheet
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try, Using}

object ScalaXcel extends ScalaXcel:

  override inline def toExcelWorkbook[A](
    records: Seq[A],
    options: XcelOptions = XcelOptions.AllDefaults
  ): XSSFWorkbook =
    Mappers
      .deriveSheet(records)
      .pipe(applySheetOptions(_, options))
      .pipe(sh => XSSFXcelHandler.toXSSFWorkbook(Seq(sh)))

  override inline def toExcelWorkbookFuture[A](records: Seq[A], options: XcelOptions = XcelOptions.AllDefaults)(
    using ec: ExecutionContext
  ): Future[XSSFWorkbook] =
    Future(blocking(toExcelWorkbook(records, options)))

  override inline def toExcelBytes[A](
    records: Seq[A],
    options: XcelOptions = XcelOptions.AllDefaults
  ): Try[Array[Byte]] =
    toExcelWorkbook(records).pipe(toBytes)

  override inline def toExcelBytesFuture[A](records: Seq[A], options: XcelOptions = XcelOptions.AllDefaults)(
    using ec: ExecutionContext
  ): Future[Array[Byte]] =
    toExcelWorkbookFuture(records)
      .flatMap(workbook => Future(blocking(toBytes(workbook))))
      .flatMap {
        case Success(bytes) => Future.successful(bytes)
        case Failure(ex)    => Future.failed(ex)
      }

  private def toBytes(workbook: XSSFWorkbook): Try[Array[Byte]] =
    Using(new ByteArrayOutputStream()) { bos =>
      workbook.write(bos)
      bos.toByteArray
    }

  private def applySheetOptions(sheet: Sheet, opts: XcelOptions): Sheet =
    opts
      .sheetName
      .map(n => sheet.copy(name = n))
      .getOrElse(sheet)
      .pipe(sh => if opts.includeHeader then sh else sh.copy(header = None))

trait ScalaXcel:
  /**
   * Converts a sequence of case classes to an Excel workbook.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @param options
   *   configuration options for Excel generation, including header inclusion, sheet name, and formatting
   * @return
   *   an Excel workbook containing the case classes
   */
  def toExcelWorkbook[A](records: Seq[A], options: XcelOptions): XSSFWorkbook

  /**
   * Asynchronously converts a sequence of case classes to an Excel workbook.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @param options
   *   configuration options for Excel generation, including header inclusion, sheet name, and formatting
   * @param ec
   *   the execution context to run the future in
   * @return
   *   a future containing the Excel workbook
   */
  def toExcelWorkbookFuture[A](records: Seq[A], options: XcelOptions)(
    using ec: ExecutionContext
  ): Future[XSSFWorkbook]

  /**
   * Converts a sequence of case classes to an Excel file as bytes.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @param options
   *   configuration options for Excel generation, including header inclusion, sheet name, and formatting
   * @return
   *   the Excel file as a byte array wrapped in a Try
   */
  def toExcelBytes[A](records: Seq[A], options: XcelOptions): Try[Array[Byte]]

  /**
   * Asynchronously converts a sequence of case classes to an Excel file as bytes.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @param options
   *   configuration options for Excel generation, including header inclusion, sheet name, and formatting
   * @param ec
   *   the execution context to run the future in
   * @return
   *   a future containing the Excel file as a byte array
   */
  def toExcelBytesFuture[A](records: Seq[A], options: XcelOptions)(
    using ec: ExecutionContext
  ): Future[Array[Byte]]
