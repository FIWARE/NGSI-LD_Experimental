package utils

import org.apache.http.HttpEntity

/**
  *
  * Case class for NGSIv2 API call results
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
case class NgsiResult(code:Int,data:Any,httpEntity: HttpEntity)
