package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import common.DefaultEnv
import forms.SignUp
import javax.inject.{Inject, Singleton}
import models.security.{User, UserRepository}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignUpController @Inject()(components: ControllerComponents,
                                 userService: UserRepository,
                                 configuration: Configuration,
                                 silhouette: Silhouette[DefaultEnv],
                                 clock: Clock,
                                 credentialsProvider: CredentialsProvider,
                                 authInfoRepository: AuthInfoRepository,
                                 passwordHasherRegistry: PasswordHasherRegistry,
                                 messagesApi: MessagesApi)
                                (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val signUpFormat: OFormat[SignUp] = Json.format[SignUp]

  def signUp: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUp].map { signUp =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.identifier)
      userService.retrieve(loginInfo).flatMap {
        case None =>
          val user = User(signUp.identifier, signUp.firstName, signUp.lastName, signUp.email, signUp.isAdmin)
          val authInfo = passwordHasherRegistry.current.hash(signUp.password)
          for {
            _ <- userService.save(user)
            _ <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(token,
              Ok(Json.obj(
                "id" -> user.id,
                "token" -> token,
                "firstName" -> user.firstName,
                "lastName" -> user.lastName,
                "is_admin" -> user.isAdmin,
                "email" -> user.email
              ))
            )
          } yield {
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        case Some(_) => /* user already exists! */
          Future(Conflict(Json.obj("error" -> "user already exists")))
      }
    }.recoverTotal {
      error =>
        Future.successful(BadRequest(Json.obj("error" -> error.toString)))
    }
  }
}
