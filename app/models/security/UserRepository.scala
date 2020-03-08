package models.security
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class User(id: String,
  firstName: String,
  lastName: String,
  email: String,
  isAdmin: Boolean) extends Identity


class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends IdentityService[User]  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  class Users(tag: Tag) extends Table[User](tag,"users") {

    def id = column[String]("id", O.PrimaryKey)
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def email = column[String]("email")
    def isAdmin = column[Boolean]("is_admin")
    def * = (id, firstName, lastName, email, isAdmin) <> ((User.apply _).tupled, User.unapply)
  }

  val slickUsers = TableQuery[Users]

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    db.run(slickUsers.filter(_.id === loginInfo.providerKey).take(1).result.headOption)
  }

  def save(user: User): Future[User] = db.run {
    // H2 doesnt support returning whole row thus poor workaround
    (slickUsers += user).map(_ => user)
  }

}
