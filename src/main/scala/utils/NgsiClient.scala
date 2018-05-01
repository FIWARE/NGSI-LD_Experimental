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
    val getRequest = new HttpGet(s"${apiBase}/entities")

    if (tenant.length > 0) {
      getRequest.setHeader("Fiware-Service", tenant)
    }

    // send the GET request
    val httpClient = HttpClientBuilder.create().build()
    val result = httpClient.execute(getRequest)
    println (s"HTTStatus: ${result.getStatusLine.getStatusCode}")
    println (s"${result.getHeaders("Content-Type")(0).getValue}")

    EntityUtils.toString(result.getEntity, "UTF-8")
  }

  def entityById(id:String,tenant:String="") = {
    val getRequest = new HttpGet(s"${apiBase}/entities/${id}")

    if (tenant.length > 0) {
      getRequest.setHeader("Fiware-Service", tenant)
    }

    // send the GET request
    val httpClient = HttpClientBuilder.create().build()
    val result = httpClient.execute(getRequest)

    val entityData = EntityUtils.toString(result.getEntity, "UTF-8")
    ParserUtil.parse(entityData).asInstanceOf[Map[String,Any]]
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
