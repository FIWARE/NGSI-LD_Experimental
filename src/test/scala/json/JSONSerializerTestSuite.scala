package json

import org.scalatest.FunSuite

/**
  *
  *  Test the JSON serializer
  *
  *  Coypright (c) 2018 FIWARE Foundation e.V.
  *
  *  Author: José M. Cantera
  *
  *  LICENSE: MIT
  *
  */
class JSONSerializerTestSuite extends FunSuite {

  test("Should serialize single string") {
    val obj = "This is a string"

    assert(JSONSerializer.serialize(obj) == """"This is a string"""")
  }

  test("Should serialize single number") {
    val obj = 45

    assert(JSONSerializer.serialize(obj) == "45")
  }

  test("Should serialize list") {
    val list = List(34,67,89)

    assert(JSONSerializer.serialize(list) == "[34,67,89]")
  }

  test("Should serialize empty list") {
    val list = List()

    assert(JSONSerializer.serialize(list) == "[]")
  }

  test("Should serialize map") {
    val data = Map("c1" -> "v1","c2"->45,"c3" -> List(34,56), "c4" -> Map("c41" -> "v"))

    assert(JSONSerializer.serialize(data) == """{"c1":"v1","c2":45,"c3":[34,56],"c4":{"c41":"v"}}""")
  }

  test("Should serialize nested map") {
    val data = Map("c1" -> "v1","c2"->45,"c3" -> List(34,56), "c4" -> Map("c41" -> "v", "c42" -> Map("r" -> "s"),
    "c43" -> List(3,4)))

    System.out.println(JSONSerializer.serialize(data))

    assert(JSONSerializer.serialize(data) == """{"c1":"v1","c2":45,"c3":[34,56],"c4":{"c41":"v","c42":{"r":"s"},"c43":[3,4]}}""")
  }

  test("Should serialize empty map") {
    val data = Map()

    assert(JSONSerializer.serialize(data) == "{}")
  }
}
