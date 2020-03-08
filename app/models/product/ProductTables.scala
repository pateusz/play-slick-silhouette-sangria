package models.product

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

abstract class ProductTables extends HasDatabaseConfigProvider[JdbcProfile] {
  import dbConfig.profile.api._

  class ProductTable(tag: Tag)
    extends Table[Product](tag, "products") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)
    def * = (id, name) <>
      ((Product.apply _).tupled, Product.unapply)
  }


  class ProductOpinions(tag: Tag)
    extends Table[ProductOpinion](tag, "product_opinions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def productId = column[Long]("product_id")
    def productIdFK = foreignKey("product_id", productId, products)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def text = column[String]("text")
    def * = (id, productId, text) <>
      ((ProductOpinion.apply _).tupled, ProductOpinion.unapply)
  }


  val products = TableQuery[ProductTable]
  val productOpinions = TableQuery[ProductOpinions]

}


