package com.sugavi.xcel.mappers

import com.sugavi.xcel.model.*

import scala.quoted.*

object Mappers {
  inline def deriveSheet[A](records: Seq[A]): Sheet = ${ deriveSheetImpl('records) }

  private def deriveSheetImpl[A: Type](recordsExpr: Expr[Seq[A]])(using Quotes): Expr[Sheet] = {
    import quotes.reflect.*

    val tpe           = TypeRepr.of[A]
    val fields        = tpe.typeSymbol.caseFields
    val classNameExpr = Expr(tpe.typeSymbol.name)

    val headerCells: Seq[Expr[Cell[String]]] = fields.map { f =>
      val name = Expr(f.name)
      '{ Cell(XcelVal($name)) }
    }
    val headerExpr = '{ Row(${ Expr.ofSeq(headerCells) }) }

    val rowMapper: Expr[A => Row] = '{ (record: A) =>
      val cells: Seq[Cell[_]] = ${
        Expr.ofSeq(
          fields.map { f =>
            val fieldVal = Select.unique('record.asTerm, f.name).asExpr
            '{ Cell(XcelVal($fieldVal)) }
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
