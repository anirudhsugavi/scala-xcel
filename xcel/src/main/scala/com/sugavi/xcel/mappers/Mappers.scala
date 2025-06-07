package com.sugavi.xcel.mappers

import com.sugavi.xcel.model.*

import java.time.{LocalDate, LocalDateTime}
import scala.annotation.tailrec
import scala.quoted.*

object Mappers {

  inline def deriveSheet[A](records: Seq[A]): Sheet = ${ deriveSheetImpl('records) }

  private def deriveSheetImpl[A: Type](recordsExpr: Expr[Seq[A]])(using Quotes): Expr[Sheet] =
    import quotes.reflect.*

    val tpe           = TypeRepr.of[A]
    val sym           = tpe.typeSymbol
    val fields        = sym.caseFields
    val classNameExpr = Expr(sym.name)
    '{
      if $recordsExpr.isEmpty then Sheet(name = $classNameExpr, header = None, rows = Seq.empty)
      else
        val header = deriveHeader($recordsExpr.head)
        val rows   = $recordsExpr.map(deriveRow)
        Sheet(name = $classNameExpr, header = Some(header), rows = rows)
    }

  private inline def deriveHeader[A](record: A): Row = ${ deriveHeaderImpl('record) }

  private def deriveHeaderImpl[A: Type](recordExpr: Expr[A])(using Quotes): Expr[Row] = {
    import quotes.reflect.*
    import com.sugavi.xcel.syntax.given

    val tpe           = TypeRepr.of[A]
    val sym           = tpe.typeSymbol
    val fields        = sym.caseFields
    val classNameExpr = Expr(sym.name)

    @tailrec
    def isSupportedType(t: TypeRepr): Boolean =
      t match
        case _ if t =:= TypeRepr.of[String]        => true
        case _ if t =:= TypeRepr.of[Double]        => true
        case _ if t =:= TypeRepr.of[Int]           => true
        case _ if t =:= TypeRepr.of[Long]          => true
        case _ if t =:= TypeRepr.of[Boolean]       => true
        case _ if t =:= TypeRepr.of[LocalDate]     => true
        case _ if t =:= TypeRepr.of[LocalDateTime] => true
        case _ if t <:< TypeRepr.of[Option[_]]     => isSupportedType(t.typeArgs.head)
        case _                                     => false

    def requireOnlySupportedFields(): Expr[Unit] =
      val unsupportedTypes = fields.filterNot(f => isSupportedType(tpe.memberType(f)))
      if unsupportedTypes.isEmpty then '{}
      else
        val unsupported = unsupportedTypes
          .map(u => u.name -> tpe.memberType(u))
          .map(ut => s"${ut._1}: ${ut._2.show}")
          .mkString("[", ", ", "]")
        report.errorAndAbort(s"Unsupported field types: $unsupported")

    requireOnlySupportedFields()

    val headerCells: Seq[Expr[Cell]] = fields.map { f =>
      val name = Expr(f.name)
      '{
        val converted = summon[Conversion[String, XcelValue]].apply($name)
        Cell(converted)
      }
    }

    '{
      Row(${ Expr.ofSeq(headerCells) })
    }
  }

  private inline def deriveRow[A](record: A): Row = ${ deriveRowImpl('record) }

  private def deriveRowImpl[A: Type](recordExpr: Expr[A])(using Quotes): Expr[Row] = {
    import quotes.reflect.*
    import com.sugavi.xcel.syntax.given

    val sym    = TypeRepr.of[A].typeSymbol
    val fields = sym.caseFields

    def cellMapper(field: quotes.reflect.Symbol): Expr[Cell] =
      val fieldTerm = Select.unique(recordExpr.asTerm, field.name)
      val fieldVal  = fieldTerm.asExpr

      fieldTerm.tpe.asType match
        case '[Option[t]] =>
          '{
            val convert = summon[Conversion[Option[t], XcelValue]].apply($fieldVal.asInstanceOf[Option[t]])
            Cell(convert)
          }
        case '[t] =>
          '{
            val convert = summon[Conversion[t, XcelValue]].apply($fieldVal)
            Cell(convert)
          }

    '{
      val cells = ${ Expr.ofSeq(fields.map(cellMapper)) }
      Row(cells)
    }
  }
}
