package utils

import json.JSONSerializer
import org.apache.http.HttpEntity
import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
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
      NgsiResult(200, ParserUtil.parse(entityData),result.getEntity)
    }
    else NgsiResult(result.getStatusLine.getStatusCode,None,result.getEntity)
  }

  def queryStr(queryString:String) = {
    if (queryString == null) "" else queryString
  }

  def entityById(id:String,queryString:String,tenant:String="") = {
    val getRequest = new HttpGet(s"${apiBase}/entities/${id}?${queryStr(queryString)}")

    if (tenant.length > 0) {
      getRequest.setHeader("Fiware-Service", tenant)
    }

    // send the GET request
    val httpClient = HttpClientBuilder.create().build()
    val result = httpClient.execute(getRequest)

    if (result.getStatusLine.getStatusCode == 200) {
      val entityData = EntityUtils.toString(result.getEntity, "UTF-8")
      NgsiResult(200, ParserUtil.parse(entityData),result.getEntity)
    }
    else
      NgsiResult(result.getStatusLine.getStatusCode,None,result.getEntity)
  }

  def createEntity(entityData:Map[String,Any],tenant:String=null) = {
    val data = JSONSerializer.serialize(entityData)
    val postRequest = new HttpPost(s"${apiBase}/entities/")

    postRequest.setHeader("Content-Type","application/json")
    postRequest.setEntity(new StringEntity(data))

    // send the POST request
    val httpClient = HttpClientBuilder.create().build()
    httpClient.execute(postRequest)
  }

  def appendAttributes() = {

  }

  def updateEntity() = {

  }

  def deleteEntity(id:String,tenant:String=null) = {
    val delRequest = new HttpDelete(s"${apiBase}/entities/${id}")

    // send the GET request
    val httpClient = HttpClientBuilder.create().build()
    httpClient.execute(delRequest)
  }

  def createSubscription() = {

  }

  def querySubscriptions() = {

  }

  def updateSubscription() = {

  }

  def subscriptionById() = {

  }

  def removeSubscription() = {

  }

  def createRegistration() = {

  }

  def queryRegistrations() = {

  }

  def updateRegistrations() = {

  }

  def registrationById() = {

  }

  def removeRegistration() = {

  }
}
