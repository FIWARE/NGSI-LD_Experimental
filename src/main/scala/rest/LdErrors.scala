package rest

object LdErrors {
  private val AlreadyExistsName = "AlreadyExists"
  private val NotFoundName = "NotFound"
  private val BadRequestDataName = "BadRequestData"

  private val UriPrefix = "http://uri.etsi.org/ngsi-ld/errors"
  private val errors = Map(
    AlreadyExistsName -> "NGSI-LD Element already exists",
    NotFoundName -> "Element not found",
    BadRequestDataName -> "Bad Request Data. Please check your data"
  )

  def error(t:String,msg:Option[String]=None) = {
    Map("type" -> s"${UriPrefix}/${t}", "description" -> msg.getOrElse(errors.getOrElse(t,"Unknown")))
  }

  def AlreadyExists(msg:Option[String]=None): Map[String,String] = {
    error(AlreadyExistsName,msg)
  }

  def NotFound(msg:Option[String]=None): Map[String,String] = {
    error(NotFoundName,msg)
  }

  def BadRequestData(msg:Option[String]=None) = {
    error(BadRequestDataName,msg)
  }
}
