# NGSI-LD_Wrapper

[![MIT license][license-image]][license-url]
[![Build badge](https://img.shields.io/travis/Fiware/NGSI-LD_Wrapper.svg "Travis build status")](https://travis-ci.org/fiware/NGSI-LD_Wrapper/)
[![Docker badge](https://img.shields.io/docker/pulls/fiware/ngsi-ld_wrapper.svg)](https://hub.docker.com/r/fiware/ngsi-ld_wrapper/)


The purpose of this project is to create an implementation of [NGSI-LD](https://docbox.etsi.org/ISG/CIM/Open/ISG_CIM_NGSI-LD_API_Draft_for_public_review.pdf), based on a wrapper (incarnated by a proxy) on top of the [FIWARE Context Broker](https://github.com/fiware/context.Orion). Leveraging on [FIWARE NGSI](http://fiware.github.io/specifications/ngsiv2/latest/), NGSI-LD is an international standard developed by [ETSI ISG CIM](https://portal.etsi.org/tb.aspx?tbid=854&SubTB=854), intended to provide, consume and subscribe to context information in multiple scenarios and involving multiple stakeholders. It enables close to real-time access to information coming from many different sources (not only IoT). 

The [OMA NGSI-9/10 information model](https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/NGSI-9/NGSI-10_information_model), the root basis of FIWARE NGSI, is currently being evolved by ETSI CIM to better support linked data (entity's relationships), [property graphs](https://neo4j.com/lp/book-graph-databases/) and semantics (exploiting the capabilities offered by [JSON-LD](https://json-ld.org/primer/latest/)).  The resulting specification has been named **NGSI-LD**. It is noteworthy that the [NGSI-LD information model](doc/NGSI-LD_Information_Model.md) is a generalization of the OMA NGSI-9/10 information model. As a result, it is expected a good level of compatibility and a clear migration path between both information models.  

This wrapper works on top of the [FIWARE Context Broker](https://github.com/fiware/context.Orion) and basically adapts between NGSIv2 (JSON) representations and the **NGSI-LD** (JSON-LD) representations.

An example illustrating the usage of NGSI-LD can be found [here](doc/example.md). 

## How to build / test

### Prerequisites

* Java 8
* Scala runtime
* SBT build tool

```console
sbt compile
export NGSI_Endpoint=http://<Your_NGSI_Endpoint i.e. Orion's host:port>
sbt jetty:start
```

```console
sbt test
```

## How to run using Docker

```console
docker run -e NGSI_Endpoint="http://<Your_NGSI_Endpoint i.e. Orion's host:port>" fiware/ngsi-ld_wrapper
```

## How to run using Docker Compose

```console
wget https://raw.githubusercontent.com/Fiware/NGSI-LD_Wrapper/master/docker-compose.yml
docker-compose up
```

## See also:

https://github.com/fiware/dataModels

https://github.com/fiware/context.Orion

[license-image]: https://img.shields.io/badge/license-MIT-blue.svg
[license-url]: LICENSE

