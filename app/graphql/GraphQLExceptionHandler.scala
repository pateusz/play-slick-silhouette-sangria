package graphql

import sangria.execution.{ExceptionHandler, HandledException}

object GraphQLExceptionHandler {
  val handler: ExceptionHandler = ExceptionHandler {
    case (_, AuthenticationException(message)) => HandledException(message)
    case (_, AuthorisationException(message)) => HandledException(message)
    case (_, ForeignKeyException(message)) => HandledException(message)
    case (_, UniqueKeyException(message)) => HandledException(message)
  }
}
