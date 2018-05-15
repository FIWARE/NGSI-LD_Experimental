package main

trait Configuration {
  val Port = "NGSI-LD_Port"
  val NgsiEndpoint = "NGSI_Endpoint"
  val DefaultNgsiEndpoint = "http://localhost:1026"
  val DefaultPort = "1030"
  val DefaultContextLink = """<http://uri.etsi.org/ngsi-ld/context.jsonld>;
                             rel="http://www.w3.org/ns/json-ld#context";
                             type="application/ld+json";"""
}
