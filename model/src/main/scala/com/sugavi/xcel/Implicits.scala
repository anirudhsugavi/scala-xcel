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
  given Bijection[String, XcelValue]        = Bijection(StringXcel.apply, _.asInstanceOf[StringXcel].value)
  given Bijection[Double, XcelValue]        = Bijection(DoubleXcel.apply, _.asInstanceOf[DoubleXcel].value)
  given Bijection[Int, XcelValue]           = Bijection(IntXcel.apply, _.asInstanceOf[IntXcel].value)
  given Bijection[Long, XcelValue]          = Bijection(LongXcel.apply, _.asInstanceOf[LongXcel].value)
  given Bijection[Boolean, XcelValue]       = Bijection(BooleanXcel.apply, _.asInstanceOf[BooleanXcel].value)
  given Bijection[LocalDate, XcelValue]     = Bijection(DateXcel.apply, _.asInstanceOf[DateXcel].value)
  given Bijection[LocalDateTime, XcelValue] = Bijection(DateTimeXcel.apply, _.asInstanceOf[DateTimeXcel].value)

  given [A](using bij: Bijection[A, XcelValue]): Bijection[Option[A], XcelValue] = Bijection(
    ab = _.map(bij.aToB).getOrElse(EmptyXcel),
    ba = {
      case EmptyXcel => None
      case xv        => Some(bij.bToA(xv))
    }
  )

  given [A](using bij: Bijection[A, XcelValue]): Conversion[A, XcelValue]                 = bij.aToB
  given [A](using bij: Bijection[A, XcelValue]): Conversion[XcelValue, A]                 = bij.bToA
  given [A](using bij: Bijection[Option[A], XcelValue]): Conversion[Option[A], XcelValue] = bij.aToB
  given [A](using bij: Bijection[Option[A], XcelValue]): Conversion[XcelValue, Option[A]] = bij.bToA
}

object BijectionImplicits extends BijectionImplicits
