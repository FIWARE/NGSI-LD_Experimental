package utils

import org.apache.http.HttpEntity

case class NgsiResult(code:Int,data:Any,httpEntity: HttpEntity)
