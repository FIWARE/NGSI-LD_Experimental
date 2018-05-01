package fiware

import org.scalatest.FunSuite

/**
  *
  *  Test the NGSI to NGSI-LD data mapper
  *
  *  Coypright (c) 2018 FIWARE Foundation e.V.
  *
  *  Author: José M. Cantera
  *
  *  LICENSE: MIT
  *
  */
class Ngsi2LdTestSuite extends FunSuite {

  // TODO: Divide in different simple and smaller cases
  val testData = Map("id"->"urn:ngsi-ld:Car:myId", "type" -> "MyType",
    "refOther" -> Map("type"->"Relationship","value" -> "anId",
      "metadata" -> Map("entityType" -> Map("value" -> "Parking"))),
    "dateCreated" -> Map("value" -> "2018-04-23T12:00:00", "type" -> "DateTime"),
    "speed" -> Map("value"->100,
      "metadata" -> Map("accuracy" -> Map("value" -> 0.89),
        "timestamp" -> Map("value" -> "2018-04-23T12:00:00",
          "type" -> "DateTime"),
        "providedBy" -> Map("type" -> "Reference", "value" -> "4567"))),
    "location" -> Map("type" -> "geo:json","value" -> Map("type" -> "Point", "coordinates" -> List(-4,55))),
    "boughtAt" -> Map("type" -> "DateTime","value" -> "2018-02-02T12:00:00"),
    "typelessAttr" -> Map("value" -> "hello")
  )

  val result = Ngsi2LdModelMapper.fromNgsi(testData)

  def node(node:Any) = {
    node.asInstanceOf[Map[String,Any]]
  }

  test("Id should be kept when it is already a URN") {
     assert(result("id") == "urn:ngsi-ld:Car:myId")
  }

  test("Type should be kept") {
    assert(result("type") == "MyType")
  }

  test("dateCreated should be mapped to createdAt") {
    assert(result("createdAt") == "2018-04-23T12:00:00")
  }

  test("Regular Attributes should be mapped to nodes of type Property") {
    assert(node(result("speed"))("type") == "Property")
  }

  test("Regular Attribute values should be kept") {
    assert(node(result("speed"))("value") == 100)
  }

  test("Timestamp should be mapped to observedAt (non-reified)") {
    assert(node(result("speed"))("observedAt") == "2018-04-23T12:00:00")
  }

  test("Metadata should be mapped to property of property") {
    assert(node(node(result("speed"))("accuracy"))("value") == 0.89)
  }

  test("Ref attributes should be mapped to node type Relationship") {
    assert(node(result("refOther"))("type") == "Relationship")
  }

  test("Relationship nodes should have object property") {
    assert(node(result("refOther"))("object") == "urn:ngsi-ld:Parking:anId")
  }

  test("Metadata of type reference or relationship should be mapped to relationship of property") {
    assert(node(node(result("speed"))("providedBy"))("type") == "Relationship")
    assert(node(node(result("speed"))("providedBy"))("object") == "urn:ngsi-ld:Thing:4567")
  }

  test("Properties of type geo:json should be converted to GeoProperty") {
    assert(node(result("location"))("type") == "GeoProperty")
  }

  test("Properties of type geo:json. GeoJSON content should be left as it is") {
    assert(node(node(result("location"))("value"))("type") == "Point")
    assert(node(node(result("location"))("value"))("coordinates") == List(-4,55))
  }

  test("Properties of type DateTime should be converted to TemporalProperty") {
    assert(node(result("boughtAt"))("type") == "TemporalProperty")
  }

  test("Typeless attributes should be converted to Property") {
    assert(node(result("typelessAttr"))("type") == "Property")
  }
}