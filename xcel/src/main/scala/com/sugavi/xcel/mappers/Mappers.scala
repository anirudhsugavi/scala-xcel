package com.sugavi.xcel.mappers

import com.sugavi.xcel.model.*

import java.time.{LocalDate, LocalDateTime}
import scala.quoted.*

object Mappers {
  inline def deriveSheet[A](a: A): Sheet = ${ deriveSheetImpl('a) }

  private def deriveSheetImpl[A: Type](exp: Expr[A])(using Quotes): Expr[Sheet] = {
    import quotes.reflect.*
    import com.sugavi.xcel.syntax.given

    val tpe           = TypeRepr.of[A]
    val fields        = tpe.typeSymbol.caseFields
    val classNameExpr = Expr(tpe.typeSymbol.name)

    val cells: Seq[Expr[Cell]] = fields.map { f =>
      val name    = Expr(f.name)
      val nameExp = '{ summon[Conversion[String, XcelValue]].apply($name) }
      '{ Cell($nameExp) }
    }
    val headerExpr = '{ Row(${ Expr.ofSeq(cells) }) }

    val cellVals = fields.map { f =>
      val fieldTpe  = tpe.memberType(f)
      val fieldName = f.name
      val fieldVal  = Select.unique(exp.asTerm, fieldName).asExpr

      fieldTpe.asType match {
        case '[String] =>
          '{ Cell(summon[Conversion[String, XcelValue]].apply($fieldVal.asInstanceOf[String])) }
        case '[Double] =>
          '{ Cell(summon[Conversion[Double, XcelValue]].apply($fieldVal.asInstanceOf[Double])) }
        case '[Int] =>
          '{ Cell(summon[Conversion[Int, XcelValue]].apply($fieldVal.asInstanceOf[Int])) }
        case '[Long] =>
          '{ Cell(summon[Conversion[Long, XcelValue]].apply($fieldVal.asInstanceOf[Long])) }
        case '[Boolean] =>
          '{ Cell(summon[Conversion[Boolean, XcelValue]].apply($fieldVal.asInstanceOf[Boolean])) }
        case '[LocalDate] =>
          '{ Cell(summon[Conversion[LocalDate, XcelValue]].apply($fieldVal.asInstanceOf[LocalDate])) }
        case '[LocalDateTime] =>
          '{ Cell(summon[Conversion[LocalDateTime, XcelValue]].apply($fieldVal.asInstanceOf[LocalDateTime])) }
        case _ =>
          report.errorAndAbort(s"Unsupported type: ${fieldTpe.show} for field: $fieldName.")
      }
    }
    val rowExpr = '{ Row(${ Expr.ofSeq(cellVals) }) }

    '{
      Sheet(
        name = $classNameExpr,
        header = Some($headerExpr),
        rows = Seq($rowExpr)
      )
    }
  }
}
