package com.sugavi.xcel.mappers

import com.sugavi.xcel.model.*

import java.time.{LocalDate, LocalDateTime}
import scala.annotation.tailrec
import scala.quoted.*

object Mappers:

  inline def deriveSheet[A](records: Seq[A]): Sheet = ${ deriveSheetImpl('records) }

  private def deriveSheetImpl[A: Type](recordsExpr: Expr[Seq[A]])(using Quotes): Expr[Sheet] =
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

    def deriveHeader(fields: Seq[quotes.reflect.Symbol], tpe: TypeRepr): Expr[Row] =
      val unsupportedTypes = fields.filterNot(f => isSupportedType(tpe.memberType(f)))
      if unsupportedTypes.nonEmpty then
        val unsupported = unsupportedTypes
          .map(u => u.name -> tpe.memberType(u))
          .map(ut => s"${ut._1}: ${ut._2.show}")
          .mkString("[", ", ", "]")
        report.errorAndAbort(s"Unsupported field types: $unsupported")
      else
        val headerCells: Expr[Seq[Cell]] = Expr.ofSeq(
          fields.map { f =>
            '{
              val name      = ${ Expr(f.name) }
              val converted = name // this summons the implicit Conversion[String, XcelValue]
              Cell(converted)
            }
          }
        )

        '{ Row($headerCells) }

    def deriveRow(record: Expr[A], fields: Seq[quotes.reflect.Symbol]): Expr[Row] =

      def cellMapper(field: quotes.reflect.Symbol): Expr[Cell] =
        val fieldTerm = Select.unique(record.asTerm, field.name)
        val fieldVal  = fieldTerm.asExpr

        fieldTerm.tpe.asType match
          case '[Option[t]] =>
            '{
              val convert =
                $fieldVal.asInstanceOf[Option[t]] // this summons the implicit Conversion[Option[A], XcelValue]
              Cell(convert)
            }
          case '[t] =>
            '{
              val convert = $fieldVal // this summons the implicit Conversion[A, XcelValue]
              Cell(convert)
            }

      val rowCells = fields.map(cellMapper)
      '{ Row(${ Expr.ofSeq(rowCells) }) }

    '{
      if $recordsExpr.isEmpty then Sheet(name = $classNameExpr, header = None, rows = Seq.empty)
      else
        val header = ${ deriveHeader(fields, tpe) }
        val rows   = $recordsExpr.map(r => ${ deriveRow('r, fields) })
        Sheet(name = $classNameExpr, header = Some(header), rows = rows)
    }
