package models.product

import graphql.{ForeignKeyException, UniqueKeyException}
import javax.inject.{Inject, Singleton}
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import play.api.cache.AsyncCacheApi
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ProductRepository @Inject()
(cache: AsyncCacheApi, protected val dbConfigProvider: DatabaseConfigProvider)
(implicit ec: ExecutionContext) extends ProductTables {

  import profile.api._

  import scala.concurrent.duration._


  def findAllProducts: Future[Seq[Product]] = db.run {
    products
      .result
  }

  def findProducts(ids: Seq[Long]): Future[Seq[Product]] = db.run {
    products
      .filter(_.id.inSet(ids))
      .result
  }


  def findProduct(id: Long): Future[Option[Product]] = db.run {
    products
      .filter(_.id === id)
      .take(1)
      .result
      .headOption
  }

  def addProduct(name: String): Future[Product] = db.run {
    (products returning products.map(_.id) += Product(0, name))
      .map(x => Product(x, name))
  }.recoverWith {
    case _: JdbcSQLIntegrityConstraintViolationException =>
      throw UniqueKeyException("product with given name already exists")
  }


  def findOpinions(ids: Seq[Long]): Future[Seq[ProductOpinion]] = db.run {
    productOpinions
      .filter(_.id.inSet(ids))
      .result
  }

  def findOpinionsByProductIds(ids: Seq[Long]): Future[Seq[ProductOpinion]] = db.run {
    productOpinions
      .filter(_.productId.inSet(ids))
      .result
  }

  def addOpinion(productId: Long, text: String): Future[ProductOpinion] = db.run {
    (productOpinions returning productOpinions.map(_.id) += ProductOpinion(0, productId, text))
      .map(x => ProductOpinion(x, productId, text))
  }.recoverWith {
    case _: JdbcSQLIntegrityConstraintViolationException =>
      throw ForeignKeyException("referenced product does not exist")
  }


}