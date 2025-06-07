package com.sugavi.xcel.mappers

import com.sugavi.xcel.model.*

import java.time.{LocalDate, LocalDateTime}
import scala.quoted.*

object Mappers {
  inline def deriveSheet[A](records: Seq[A]): Sheet = ${ deriveSheetImpl('records) }

  private def deriveSheetImpl[A: Type](recordsExpr: Expr[Seq[A]])(using Quotes): Expr[Sheet] = {
    import quotes.reflect.*
    import com.sugavi.xcel.syntax.given

    val tpe           = TypeRepr.of[A]
    val sym           = tpe.typeSymbol
    val fields        = sym.caseFields
    val classNameExpr = Expr(sym.name)

    val headerCells: Seq[Expr[Cell]] = fields.map { f =>
      val name      = Expr(f.name)
      val converted = '{ summon[Conversion[String, XcelValue]].apply($name) }
      '{ Cell($converted) }
    }
    val headerExpr = '{ Row(${ Expr.ofSeq(headerCells) }) }

    val rowMapper: Expr[A => Row] = '{ (record: A) =>
      val cells: Seq[Cell] = ${
        Expr.ofSeq(
          fields.map { f =>
            val fieldVal = Select.unique('record.asTerm, f.name).asExpr
            tpe.memberType(f).asType match
              case '[String] =>
                val converted = '{ summon[Conversion[String, XcelValue]].apply($fieldVal.asInstanceOf[String]) }
                '{ Cell($converted) }
              case '[Double] =>
                val converted = '{ summon[Conversion[Double, XcelValue]].apply($fieldVal.asInstanceOf[Double]) }
                '{ Cell($converted) }
              case '[Int] =>
                val converted = '{ summon[Conversion[Int, XcelValue]].apply($fieldVal.asInstanceOf[Int]) }
                '{ Cell($converted) }
              case '[Long] =>
                val converted = '{ summon[Conversion[Long, XcelValue]].apply($fieldVal.asInstanceOf[Long]) }
                '{ Cell($converted) }
              case '[Boolean] =>
                val converted = '{ summon[Conversion[Boolean, XcelValue]].apply($fieldVal.asInstanceOf[Boolean]) }
                '{ Cell($converted) }
              case '[LocalDate] =>
                val converted = '{ summon[Conversion[LocalDate, XcelValue]].apply($fieldVal.asInstanceOf[LocalDate]) }
                '{ Cell($converted) }
              case '[LocalDateTime] =>
                val converted = '{
                  summon[Conversion[LocalDateTime, XcelValue]].apply($fieldVal.asInstanceOf[LocalDateTime])
                }
                '{ Cell($converted) }
              case '[Option[String]] =>
                '{
                  Cell(summon[Conversion[Option[String], XcelValue]].apply($fieldVal.asInstanceOf[Option[String]]))
                }
              case '[Option[Double]] =>
                '{
                  Cell(summon[Conversion[Option[Double], XcelValue]].apply($fieldVal.asInstanceOf[Option[Double]]))
                }
              case '[Option[Int]] =>
                '{
                  Cell(summon[Conversion[Option[Int], XcelValue]].apply($fieldVal.asInstanceOf[Option[Int]]))
                }
              case '[Option[Long]] =>
                '{
                  Cell(summon[Conversion[Option[Long], XcelValue]].apply($fieldVal.asInstanceOf[Option[Long]]))
                }
              case '[Option[Boolean]] =>
                '{
                  Cell(summon[Conversion[Option[Boolean], XcelValue]].apply($fieldVal.asInstanceOf[Option[Boolean]]))
                }
              case '[Option[LocalDate]] =>
                '{
                  Cell(
                    summon[Conversion[Option[LocalDate], XcelValue]].apply($fieldVal.asInstanceOf[Option[LocalDate]])
                  )
                }
              case '[Option[LocalDateTime]] =>
                '{
                  Cell(
                    summon[Conversion[Option[LocalDateTime], XcelValue]]
                      .apply($fieldVal.asInstanceOf[Option[LocalDateTime]])
                  )
                }
              case _ =>
                report.error(
                  s"""Unsupported type "${f.name}".""",
                  f.pos.getOrElse(Position.ofMacroExpansion)
                )
                '{ Cell(EmptyXcel) }
          }
        )
      }
      Row(cells)
    }

    val rowsExpr: Expr[Seq[Row]] = '{ $recordsExpr.map($rowMapper) }

    '{
      Sheet(
        name = $classNameExpr,
        header = Some($headerExpr),
        rows = $rowsExpr
      )
    }
  }
}
