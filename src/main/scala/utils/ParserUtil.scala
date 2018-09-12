package utils

import json.JSONParser

/**
  *
  * JSON Parser utilities
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
object ParserUtil {
  def parse(data:String) ={
    val parser = new JSONParser
    parser.parse(parser.value, data) match {
      case parser.Success(matched,_) => matched
      case parser.Failure(msg,_) => throw new Exception(s"Unexpected failure: ${msg}")
      case parser.Error(msg,_) => throw new Exception(s"Unexpected error: ${msg}")
    }
  }
}
