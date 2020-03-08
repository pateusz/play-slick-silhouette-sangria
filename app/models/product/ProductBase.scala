package models.product

import play.api.libs.json._
import sangria.execution.deferred.HasId

case class Product(
  id: Long,
  name: String) extends Identifiable


object Product {
  implicit val questionsFormat: OFormat[Product] = Json.format[Product]
}


case class ProductOpinion(
  id: Long,
  productId: Long,
  text: String) extends Identifiable

object ProductOpinion {
  implicit val answersFormat: OFormat[ProductOpinion] = Json.format[ProductOpinion]
}

trait Identifiable {
  val id: Long
}

object Identifiable {
  implicit def hasId[T <: Identifiable]: HasId[T, Long] = HasId(_.id)
}