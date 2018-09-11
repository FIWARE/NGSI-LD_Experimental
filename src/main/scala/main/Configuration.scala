package main

trait Configuration {
  val Port = "NGSI-LD_Port"
  val NgsiLdApiPath = "NGSI_LD_Api_Path"
  val NgsiEndpoint = "NGSI_Endpoint"
  val DefaultNgsiEndpoint = "http://localhost:1026"
  val DefaultPort = "1030"
  val DefaultNgsiLdApiPath = "/ngsi-ld/v1"
}
