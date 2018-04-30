package fiware

class Ld2NgsiTestSuite extends FunSuite {

  def urn(id:String,t:String) = {
    s"urn:ngsi-ld:${t}:${id}"
  }

  def assert_urn(urnToTest:Any,id:String,t:String) = {
    assert(urn(id,t) == urnToTest)
  }

  val testNgsiLdData = Map("id"->urn("myId","Car"), "type" -> "Car",
  "speed" -> Map("type" -> "Property", "value" -> 45, "observedAt" -> "2018-04-27T12:00:00",
    "accuracy" -> Map("type" -> "Property","value" -> 0.89),
    "providedBy" -> Map("type" -> "Relationship", "object" -> urn("A99","Agent"))),
  "parkedIn" -> Map("type" -> "Relationship", "object" -> urn("P99","Parking"),
    "providedBy" -> Map("type" -> "Relationship","object" -> urn("A99","Agent")),
    "validUntil" -> Map("type" -> "TemporalProperty","value" -> "2018-04-27T19:00:00")),
  "location" -> Map("type" -> "GeoProperty", "value" -> Map("type" -> "Point", "coordinates" -> List(-4.0,41.0)))
  )

  val result = Ngsi2LdModelMapper.toNgsi(testNgsiLdData)

  // Console.println(JsonSerializer.serialize(result))

  def node(node:Any) = {
    node.asInstanceOf[Map[String,Any]]
  }

  def metadata(n:Map[String,Any],attrName:String,metadataName:String) = {
    val theNode = node(node(node(n(attrName))("metadata"))(metadataName))
    (theNode("value"),theNode.getOrElse("type",null))
  }

  test("Id should be kept") {
     assert_urn(result("id"),"myId","Car")
  }

  test("Type should be kept") {
    assert(result("type") == "Car")
  }

  test("Nodes of type property should not have any type") {
    assert(node(result("speed")).getOrElse("type",null) == null)
  }

  test("Nodes of type property should have a value") {
    assert(node(result("speed"))("value") == 45)
  }

  test("Nodes of type relationship should be mapped to type Relationship") {
    assert(node(result("parkedIn"))("type") == "Relationship")
  }

  test("Nodes of type relationship should have a value which is a URI") {
    assert_urn(node(result("parkedIn"))("value"),"P99","Parking")
    assert(metadata(result,"parkedIn","entityType")._1 == "Parking")
  }

  test("GeoProperty should be mapped to geo:json") {
    assert(node(result("location"))("type") == "geo:json")
  }

  test("GeoProperty value should be kept") {
    assert(node(node(result("location"))("value"))("type") == "Point")
    assert(node(node(result("location"))("value"))("coordinates").asInstanceOf[List[Double]](0) == -4.0)
  }

  test("observedAt should be mapped to a timestamp metadata") {
    assert(metadata(result,"speed","timestamp")._1 == "2018-04-27T12:00:00")
  }

  test("Property of property should be mapped to metadata") {
    assert(metadata(result,"speed","accuracy")._1 == 0.89)
  }

  test("Property of property should not have metadata") {
    assert(node(node(node(result("speed"))("metadata"))("accuracy")).getOrElse("metadata",null) == null)
  }

  test("Relationship of property should not have metadata") {
    assert(node(node(node(result("speed"))("metadata"))("providedBy")).getOrElse("metadata",null) == null)
  }

  test("Property of relationship should be mapped to metadata") {
    assert(metadata(result,"parkedIn","validUntil")._1 == "2018-04-27T19:00:00")
  }

  test("Temporal property should be mapped to DateTime") {
    assert(metadata(result,"parkedIn","validUntil")._2 == "DateTime")
  }

  test("Relationship of property should be mapped to metadata") {
    val meta = metadata(result,"speed","providedBy")
    assert_urn(meta._1, "A99","Agent")
    assert(meta._2 == "Relationship")
  }

  test("Relationship of relationship should be mapped to metadata") {
    val meta = metadata(result,"parkedIn","providedBy")
    assert_urn(meta._1, "A99","Agent")
    assert(meta._2 == "Relationship")
  }

  test("Relationship of relationship should not have metadata") {
    assert(node(node(node(result("parkedIn"))("metadata"))("providedBy")).getOrElse("metadata",null) == null)
  }
}