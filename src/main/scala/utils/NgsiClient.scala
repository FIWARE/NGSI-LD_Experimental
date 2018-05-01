package utils

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

object NgsiClient {
  private var currentEndpoint = "http://localhost:1026"
  private val ngsiBase = "/v2"
  private var apiBase = s"${currentEndpoint}${ngsiBase}"

  def endpoint(endpoint:String) = {
    currentEndpoint = endpoint
    apiBase = s"${currentEndpoint}${ngsiBase}"
  }

  def queryEntities(query:String,tenant:String="") = {
    val getRequest = new HttpGet(s"${apiBase}/entities?${query}")

    if (tenant.length > 0) {
      getRequest.setHeader("Fiware-Service", tenant)
    }

    // send the GET request
    val httpClient = HttpClientBuilder.create().build()
    val result = httpClient.execute(getRequest)

    if (result.getStatusLine.getStatusCode == 200) {
      val entityData = EntityUtils.toString(result.getEntity, "UTF-8")
      NgsiResult(200, ParserUtil.parse(entityData))
    }
    else NgsiResult(result.getStatusLine.getStatusCode,null)
  }

  def entityById(id:String,queryString:String,tenant:String="") = {
    val getRequest = new HttpGet(s"${apiBase}/entities/${id}?${queryString}")

    if (tenant.length > 0) {
      getRequest.setHeader("Fiware-Service", tenant)
    }

    // send the GET request
    val httpClient = HttpClientBuilder.create().build()
    val result = httpClient.execute(getRequest)

    if (result.getStatusLine.getStatusCode == 200) {
      val entityData = EntityUtils.toString(result.getEntity, "UTF-8")
      NgsiResult(200, ParserUtil.parse(entityData))
    }
    else NgsiResult(result.getStatusLine.getStatusCode,null)

  }

  def createEntity(entityData:Map[String,Any],tenant:String=null) = {

  }

  def appendAttributes() = {

  }

  def updateEntity() = {

  }

  def deleteEntity(id:String,tenant:String=null) = {

  }
}
