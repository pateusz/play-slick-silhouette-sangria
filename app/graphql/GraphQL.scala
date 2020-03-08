package graphql

import com.google.inject.Inject
import graphql.schemas.ProductSchema
import sangria.execution.deferred.DeferredResolver
import sangria.schema.{ObjectType, Schema, fields}


class GraphQL @Inject()(productSchema: ProductSchema) {

  val resolver: DeferredResolver[SecureGraphQLContext] = productSchema.resolver

  private val query: ObjectType[SecureGraphQLContext, Unit] =
    ObjectType("Query",
      fields(
        productSchema.Queries: _*
      )
    )

  private val mutation: ObjectType[SecureGraphQLContext, Unit] =
    ObjectType("Mutation",
      fields(
        productSchema.Mutations: _*
      )
    )

  val Schema: Schema[SecureGraphQLContext, Unit] =
    sangria.schema.Schema(
      query = query,
      mutation = Some(mutation)
    )
}