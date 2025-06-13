package com.sugavi.xcel

import com.sugavi.xcel.model.*

import java.time.{LocalDate, LocalDateTime}

trait Bijection[A, B]:
  def aToB(a: A): B
  def bToA(b: B): A

object Bijection:

  def apply[A, B](ab: A => B, ba: B => A): Bijection[A, B] = new Bijection[A, B]:
    override def aToB(a: A): B = ab(a)
    override def bToA(b: B): A = ba(b)

final class IdentityBijection[A] extends Bijection[A, A]:
  override def aToB(a: A): A = a
  override def bToA(b: A): A = b

trait Implicits

trait BijectionImplicits extends Implicits:

  given [A]: Bijection[A, XcelValue] = Bijection(
    ab =
      case s: String          => StringXcel(s)
      case d: Double          => DoubleXcel(d)
      case i: Int             => IntXcel(i)
      case l: Long            => LongXcel(l)
      case b: Boolean         => BooleanXcel(b)
      case ld: LocalDate      => DateXcel(ld)
      case ldt: LocalDateTime => DateTimeXcel(ldt)
    ,
    ba = xcelVal =>
      (xcelVal: @unchecked) match
        case StringXcel(v)   => v.asInstanceOf[A]
        case DoubleXcel(v)   => v.asInstanceOf[A]
        case IntXcel(v)      => v.asInstanceOf[A]
        case LongXcel(v)     => v.asInstanceOf[A]
        case BooleanXcel(v)  => v.asInstanceOf[A]
        case DateXcel(v)     => v.asInstanceOf[A]
        case DateTimeXcel(v) => v.asInstanceOf[A]
        // case EmptyXcel will be converted to Option[A] using Bijection[Option[A], XcelValue]
  )

  given [A](using bij: Bijection[A, XcelValue]): Bijection[Option[A], XcelValue] = Bijection(
    ab = _.map(bij.aToB).getOrElse(EmptyXcel),
    ba = {
      case EmptyXcel => None
      case xv        => Some(bij.bToA(xv))
    }
  )

object BijectionImplicits extends BijectionImplicits
