package com.sugavi.xcel

import com.sugavi.xcel.model.XcelValue

object Conversions extends Conversions

trait Conversions:

  given [A](using bij: Bijection[A, XcelValue]): Conversion[A, XcelValue] = bij.aToB
  given [A](using bij: Bijection[A, XcelValue]): Conversion[XcelValue, A] = bij.bToA

  extension (b: Boolean)
    def toOption: Option[Boolean] = if b then Some(true) else None
    def not: Boolean              = !b
