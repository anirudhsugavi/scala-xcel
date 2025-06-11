package com.sugavi.xcel

import com.sugavi.xcel.mappers.Mappers
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.util.{Failure, Success, Using}

object ScalaXcel extends ScalaXcel:

  override inline def toExcelWorkbook[A](records: Seq[A]): XSSFWorkbook =
    val sheet = Mappers.deriveSheet(records)
    XSSFXcelHandler.toXSSFWorkbook(Seq(sheet))

  override inline def toExcelWorkbookFuture[A](records: Seq[A])(using ec: ExecutionContext): Future[XSSFWorkbook] =
    Future {
      blocking {
        val sheet = Mappers.deriveSheet(records)
        XSSFXcelHandler.toXSSFWorkbook(Seq(sheet))
      }
    }

  override inline def toExcelBytes[A](records: Seq[A]): Array[Byte] =
    val workbook = toExcelWorkbook(records)
    toBytes(workbook)

  override inline def toExcelBytesFuture[A](records: Seq[A])(using ec: ExecutionContext): Future[Array[Byte]] =
    toExcelWorkbookFuture(records).flatMap(workbook => Future(blocking(toBytes(workbook))))

  private def toBytes(workbook: XSSFWorkbook): Array[Byte] =
    Using(new ByteArrayOutputStream()) { bos =>
      workbook.write(bos)
      bos.toByteArray
    } match
      case Success(bytes) => bytes
      case Failure(ex)    => throw ex

trait ScalaXcel:
  /**
   * Converts a sequence of case classes to an Excel workbook.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @return
   *   an Excel workbook containing the case classes
   */
  def toExcelWorkbook[A](records: Seq[A]): XSSFWorkbook

  /**
   * Asynchronously converts a sequence of case classes to an Excel workbook.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @param ec
   *   the execution context to run the future in
   * @return
   *   a future containing the Excel workbook
   */
  def toExcelWorkbookFuture[A](records: Seq[A])(using ec: ExecutionContext): Future[XSSFWorkbook]

  /**
   * Converts a sequence of case classes to an Excel file as bytes.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @return
   *   the Excel file as a byte array
   */
  def toExcelBytes[A](records: Seq[A]): Array[Byte]

  /**
   * Asynchronously converts a sequence of case classes to an Excel file as bytes.
   *
   * @tparam A
   *   the type of case classes
   * @param records
   *   the sequence of case classes to convert
   * @param ec
   *   the execution context to run the future in
   * @return
   *   a future containing the Excel file as a byte array
   */
  def toExcelBytesFuture[A](records: Seq[A])(using ec: ExecutionContext): Future[Array[Byte]]
