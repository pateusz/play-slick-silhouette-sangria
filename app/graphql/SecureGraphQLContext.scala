package graphql

import models.product.ProductRepository
import models.security.User

case class AuthenticationException(message: String) extends Exception(message)
case class AuthorisationException(message: String) extends Exception(message)
case class ForeignKeyException(message: String) extends Exception(message)
case class UniqueKeyException(message: String) extends Exception(message)

case class SecureGraphQLContext(identity: User, productRepo: ProductRepository) {
  def authorisedAsAdmin[T](fn: => T): T =
    if (identity.isAdmin) fn
    else throw AuthorisationException("You do not have permission to do this operation")

}
