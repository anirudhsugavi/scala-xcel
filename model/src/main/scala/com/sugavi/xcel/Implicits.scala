package com.sugavi.xcel

import com.sugavi.xcel.model.*

import java.time.{LocalDate, LocalDateTime}

trait Bijection[A, B] {
  def aToB(a: A): B
  def bToA(b: B): A
}

object Bijection {
  def apply[A, B](ab: A => B, ba: B => A): Bijection[A, B] = new Bijection[A, B] {
    override def aToB(a: A): B = ab(a)
    override def bToA(b: B): A = ba(b)
  }
}

final class IdentityBijection[A] extends Bijection[A, A] {
  override def aToB(a: A): A = a
  override def bToA(b: A): A = b
}

trait Implicits

trait BijectionImplicits extends Implicits {
  given Bijection[String, StringXcel]          = Bijection(StringXcel.apply, _.value)
  given Bijection[Double, NumberXcel]          = Bijection(NumberXcel.apply, _.value)
  given Bijection[Int, NumberXcel]             = Bijection(NumberXcel.apply, _.value.toInt)
  given Bijection[Long, NumberXcel]            = Bijection(NumberXcel.apply, _.value.toLong)
  given Bijection[Boolean, BooleanXcel]        = Bijection(BooleanXcel.apply, _.value)
  given Bijection[LocalDate, DateXcel]         = Bijection(DateXcel.apply, _.value)
  given Bijection[LocalDateTime, DateTimeXcel] = Bijection(DateTimeXcel.apply, _.value)

  given liftToXcel[A, B <: XcelValue](using bij: Bijection[A, B]): Bijection[A, XcelValue] = Bijection(
    a => bij.aToB(a),
    b => bij.bToA(b.asInstanceOf[B])
  )
  given optionToXcel[A, B <: XcelValue](using bij: Bijection[A, B]): Bijection[Option[A], XcelValue] = Bijection(
    ab = _.map(bij.aToB).getOrElse(EmptyXcel),
    ba = {
      case EmptyXcel => None
      case xv        => Some(bij.bToA(xv.asInstanceOf[B]))
    }
  )

  given [A]: Bijection[A, XcelVal[A]] = Bijection(XcelVal.apply, _.value)

  given [A](using bij: Bijection[A, XcelVal[A]]): Bijection[Option[A], XcelDataType[A]] = Bijection(
    ab = _.map(bij.aToB).getOrElse(EmptyX),
    ba = {
      case EmptyX      => None
      case XcelVal(xv) => Some(bij.bToA(xv))
    }
  )

  given bijectionToConversion[A, B](using bij: Bijection[A, B]): Conversion[A, B]   = bij.aToB
  given bijectionFromConversion[A, B](using bij: Bijection[A, B]): Conversion[B, A] = bij.bToA
}

object BijectionImplicits extends BijectionImplicits
