package util

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

object NgsiClient {
  def getData(endpoint:String,tenant:String="") = {
    val getRequest = new HttpGet(endpoint + "/v2/entities")

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
}
