package com.sugavi.xcel.writer

object XcelOptions:
  val DefaultNumberFormat   = "0"
  val DefaultDateFormat     = "yyyy-mm-dd"
  val DefaultDateTimeFormat = "yyyy-mm-dd HH:mm:ss"

  val AllDefaults: XcelOptions = XcelOptions()

case class XcelOptions(
  includeHeader: Boolean = true,
  sheetName: Option[String] = None,
  numberFormat: String = XcelOptions.DefaultNumberFormat,
  dateFormat: String = XcelOptions.DefaultDateFormat,
  dateTimeFormat: String = XcelOptions.DefaultDateTimeFormat
)
