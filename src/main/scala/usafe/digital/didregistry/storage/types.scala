package usafe.digital.didregistry.storage

import usafe.digital.didregistry.types.MethodSpecificId

import scala.util.control.NoStackTrace

object types {

  sealed trait DocumentStorageFault extends NoStackTrace

  final case class DocumentExists(id: MethodSpecificId) extends DocumentStorageFault {
    override val getMessage: String = s"A document ${id.value} already exists."
  }

  final case class DocumentNotFound(id: MethodSpecificId) extends DocumentStorageFault {
    override val getMessage: String = s"A document ${id.value} not found."
  }

  final case class StorageInitializationError(msg: String) extends DocumentStorageFault {
    override val getMessage: String = msg
  }

  final case class IntegrityViolationFailure(message: String) extends Error(message)

}
