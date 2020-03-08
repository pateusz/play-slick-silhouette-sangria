package forms

case class SignUp(
  identifier: String,
  password: String,
  email: String,
  firstName: String,
  lastName: String,
  isAdmin: Boolean)