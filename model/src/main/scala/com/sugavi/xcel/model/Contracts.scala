package com.sugavi.xcel.model

import java.time.{LocalDate, LocalDateTime}

class Contracts

sealed trait Workbook extends Product with Serializable

case class Sheet(
  name: String,
  header: Option[Row],
  rows: Seq[Row]
) extends Workbook

case class Row(cells: Seq[Cell])

case class Cell(value: XcelValue)

sealed trait XcelValue extends Product with Serializable

case class StringXcel(value: String)          extends XcelValue
case class NumberXcel(value: Double)          extends XcelValue
case class BooleanXcel(value: Boolean)        extends XcelValue
case class DateXcel(value: LocalDate)         extends XcelValue
case class DateTimeXcel(value: LocalDateTime) extends XcelValue
case object EmptyXcel                         extends XcelValue
