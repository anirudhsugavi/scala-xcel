package com.sugavi.xcel
import com.sugavi.xcel.model.XcelValue

trait Conversions {

  extension [A](a: A) {
    def as[B](using bij: Bijection[A, B]): B = bij.aToB(a)
  }

  extension [B](b: B) {
    def to[A](using bij: Bijection[A, B]): A = bij.bToA(b)
  }
}

object Conversions extends Conversions
