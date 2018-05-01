package json

import org.scalatest.FunSuite

/**
  *
  *  Test the JSON parser
  *
  *  Coypright (c) 2018 FIWARE Foundation e.V.
  *
  *  Author: José M. Cantera
  *
  *  LICENSE: MIT
  *
  */
class JSONParserTestSuite extends FunSuite {

  def parse(data:String) ={
    val parser = new JSONParser
    parser.parse(parser.value, data) match {
      case parser.Success(matched,_) => matched
      case parser.Failure(msg,_) => fail(s"Unexpected failure: ${msg}")
      case parser.Error(msg,_) => fail(s"Unexpected error: ${msg}")
    }
  }

  test("Should parse single string") {
    val data = """"This is a string""""
    assert(parse(data) == "This is a string")
  }

  test("Should parse a single number") {
    val data = "45"
    assert(parse(data) == 45)
  }

  test("Should parse a single floating point number") {
    val data = "1.45"
    assert(parse(data) == 1.45)
  }

  test("Should parse list") {
    val data = "[34,67,89]"
    assert(parse(data) == List(34,67,89))
  }

  test("Should parse empty list") {
    val data = "[]"
    assert(parse(data) == List())
  }

  test("Should parse map") {
    val data = """{"c1":"v1","c2":45,"c3":[34,56],"c4":{"c41":"v"}}"""
    val expected = Map("c1" -> "v1","c2"->45,"c3" -> List(34,56), "c4" -> Map("c41" -> "v"))
    assert(parse(data) == expected)
  }

  test("Should parse empty map") {
    val data = "{}"
    assert(parse(data) == Map())
  }
}
