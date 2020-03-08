package controllers

import com.google.inject.Singleton
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers._
import common.DefaultEnv
import forms.SignUp
import javax.inject.Inject
import models.security.UserRepository
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignInController @Inject()(components: ControllerComponents,
                                          userService: UserRepository,
                                          silhouette: Silhouette[DefaultEnv],
                                          credentialsProvider: CredentialsProvider,
                                          messagesApi: MessagesApi)
                                         (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val credentialFormat: OFormat[Credentials] = Json.format[Credentials]

  implicit val signUpFormat: OFormat[SignUp] = Json.format[SignUp]

  def authenticate: Action[Credentials] = Action.async(parse.json[Credentials]) { implicit request =>
    val credentials =
      Credentials(request.body.identifier, request.body.password)
    credentialsProvider
      .authenticate(credentials)
      .flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            silhouette.env.authenticatorService
              .create(loginInfo)
              .flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService
                  .init(authenticator)
                  .flatMap { token =>
                    silhouette.env.authenticatorService
                      .embed(
                        token,
                        Ok(Json.obj("token" ->  token))
                      )
                  }
              }
          case None =>
            Future.failed(new IdentityNotFoundException("No such user"))
        }
      }
      .recover {
        case _: InvalidPasswordException => Forbidden(Json.obj("error" ->  "Wrong password"))
        case _: IdentityNotFoundException => Forbidden(Json.obj("error" ->  "No such user"))
        case x: ProviderException => Forbidden(Json.obj("error" ->  x.getMessage))
      }
  }
}

