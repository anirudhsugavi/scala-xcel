package com.sugavi.xcel

import com.sugavi.xcel.mappers.Mappers
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.util.{Failure, Success, Using}

object ScalaXcel:

  inline def toExcelWorkbookSync[A](records: Seq[A]): XSSFWorkbook =
    val sheet = Mappers.deriveSheet(records)
    XSSFXcelHandler.toXSSFWorkbook(Seq(sheet))

  inline def toExcelWorkbook[A](records: Seq[A])(using ec: ExecutionContext): Future[XSSFWorkbook] = Future {
    blocking {
      val sheet = Mappers.deriveSheet(records)
      XSSFXcelHandler.toXSSFWorkbook(Seq(sheet))
    }
  }

  inline def toExcelBytesSync[A](records: Seq[A]): Array[Byte] =
    val workbook = toExcelWorkbookSync(records)
    toBytes(workbook)

  inline def toExcelBytes[A](records: Seq[A])(using ec: ExecutionContext): Future[Array[Byte]] =
    toExcelWorkbook(records).flatMap(workbook => Future(blocking(toBytes(workbook))))

  private def toBytes(workbook: XSSFWorkbook): Array[Byte] =
    Using(new ByteArrayOutputStream()) { bos =>
      workbook.write(bos)
      bos.toByteArray
    } match
      case Success(bytes) => bytes
      case Failure(ex)    => throw ex
