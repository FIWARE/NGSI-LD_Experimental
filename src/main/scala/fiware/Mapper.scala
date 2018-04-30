package fiware

trait Mapper {
  // TODO: Refine this to a proper regular expression as per attribute name rules
  val AnyProp = raw"(.+)".r
  // Reference type to recognize relationships
  val ReferenceAttr = raw"ref(.+)".r

  val UrnPattern = raw"urn:ngsi-ld:(.+):(.+)".r
}
