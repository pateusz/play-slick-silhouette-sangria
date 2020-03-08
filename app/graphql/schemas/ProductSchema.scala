package graphql.schemas

import graphql.SecureGraphQLContext
import models.product._
import sangria.execution.deferred._
import sangria.schema._


class ProductSchema {

  import sangria.macros.derive._

  private val IdentifiableType = InterfaceType(
    "Identifiable",
    fields[SecureGraphQLContext, Identifiable](
      Field("id", LongType, resolve = _.value.id)
    )
  )

  private lazy val ProductOpinionType: ObjectType[SecureGraphQLContext, ProductOpinion] =
    deriveObjectType[SecureGraphQLContext, ProductOpinion](
      Interfaces(IdentifiableType)
    )

  private lazy val ProductType: ObjectType[SecureGraphQLContext, Product] =
    deriveObjectType[SecureGraphQLContext, Product](
      Interfaces(IdentifiableType),
      AddFields(Field("opinions", ListType(ProductOpinionType),
        resolve = c => opinionsFetcher.deferRelSeq(opinionByProductId, c.value.id)
      )))

  private val opinionByProductId =
    Relation[ProductOpinion, Long]("byParentId", l => Seq(l.productId))

  private val opinionsFetcher = Fetcher.rel(
    (c: SecureGraphQLContext, ids: Seq[Long]) =>
      c.productRepo.findOpinions(ids),
    (c: SecureGraphQLContext, ids: RelationIds[ProductOpinion]) =>
      c.productRepo.findOpinionsByProductIds(ids(opinionByProductId))
  )

  val resolver: DeferredResolver[SecureGraphQLContext] =
    DeferredResolver.fetchers(opinionsFetcher)

  private val Id = Argument("id", LongType)

  val Queries: List[Field[SecureGraphQLContext, Unit]] = List(
    Field(
      name = "product",
      fieldType = OptionType(ProductType),
      arguments = Id :: Nil,
      resolve = c => c.ctx.productRepo.findProduct(c.arg(Id))
    ),
    Field(
      name = "products",
      fieldType = ListType(ProductType),
      resolve = c => c.ctx.productRepo.findAllProducts
    )
  )

  private val productName = Argument("name", StringType)
  private val productOpinionText = Argument("text", StringType)
  private val productId = Argument("product_id", LongType)

  val Mutations: List[Field[SecureGraphQLContext, Unit]] = List(
    Field(
      name = "insertProduct",
      fieldType = ProductType,
      arguments = productName :: Nil,
      resolve = c => c.ctx.authorisedAsAdmin(
        c.ctx.productRepo.addProduct(c.arg(productName))
      )
    ),
    Field(
      name = "insertProductOpinion",
      fieldType = ProductOpinionType,
      arguments = productId :: productOpinionText :: Nil,
      resolve = c => c.ctx.authorisedAsAdmin(
        c.ctx.productRepo.addOpinion(c.arg(productId), c.arg(productOpinionText))
        )
    )
  )

}