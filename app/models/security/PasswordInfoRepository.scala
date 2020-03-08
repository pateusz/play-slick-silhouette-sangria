package models.security

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


@Singleton
class PasswordInfoRepository @Inject()
  (val dbConfigProvider: DatabaseConfigProvider)
  (implicit ec: ExecutionContext, val classTag: ClassTag[PasswordInfo])
  extends DelegableAuthInfoDAO[PasswordInfo] with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  case class DBPasswordInfo(
    hasher: String,
    password: String,
    salt: Option[String],
    loginInfoId: String
  )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag,"password_info") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[String]("login_info_id")
    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  val slickPasswordInfos = TableQuery[PasswordInfos]

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val query = slickPasswordInfos.filter(_.loginInfoId === loginInfo.providerKey)
    db.run(query.result.headOption).map(x => x.map(y => PasswordInfo(y.hasher, y.password, y.salt)))
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    // H2 doesnt support returning whole row thus poor workaround
    db.run(slickPasswordInfos +=
      DBPasswordInfo(authInfo.hasher, authInfo.password, authInfo.salt, loginInfo.providerKey))
      .map(_ => authInfo)
  }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {

    remove(loginInfo)
    add(loginInfo, authInfo)
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = add(loginInfo, authInfo)

  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(slickPasswordInfos.filter(_.loginInfoId === loginInfo.providerKey).delete).map(_ => ())
}