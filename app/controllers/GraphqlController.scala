package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import com.mohiva.play.silhouette.api.exceptions.{AuthenticatorException, NotAuthenticatedException, NotAuthorizedException}
import common.DefaultEnv
import graphql.{GraphQL, GraphQLExceptionHandler, SecureGraphQLContext}
import javax.inject._
import models.product.ProductRepository
import models.security.User
import play.api.libs.json.{Json, _}
import play.api.mvc._
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.playJson._
import sangria.parser.QueryParser

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GraphqlController @Inject()(graphQL: GraphQL,
                                  productRepo: ProductRepository,
                                  cc: MessagesControllerComponents,
                                  silhouette: Silhouette[DefaultEnv],
                                 )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {


  def graphql: Action[JsValue] = silhouette.SecuredAction.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQLQuery(queryAst, operation, variables.getOrElse(Json.obj()), request.identity)
      case Failure(error: Throwable) =>
        Future.successful(BadRequest(Json.obj("error" -> error.getMessage)))
    }
  }

  def executeGraphQLQuery(query: Document, op: Option[String], vars: JsObject, identity: User): Future[Result] = {
    val securedContext = SecureGraphQLContext(identity, productRepo)
    Executor.execute(
      graphQL.Schema,
      query,
      securedContext,
      operationName = op,
      variables = vars,
      deferredResolver = graphQL.resolver,
      exceptionHandler = GraphQLExceptionHandler.handler)
      .map(Ok(_))
      .recover {
        case error: QueryAnalysisError => BadRequest(error.resolveError)
        case error: ErrorWithResolver => InternalServerError(error.resolveError)
      }
  }

  def parseVariables(variables: String): JsObject = {
    if (variables.trim == "" || variables.trim == "null")
      Json.obj()
    else
      Json.parse(variables).as[JsObject]
  }


}
