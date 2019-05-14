# NGSI-LD Experimental

[![](https://nexus.lab.fiware.org/repository/raw/public/badges/chapters/core.svg)](https://www.fiware.org/developers/catalogue/)
[![MIT license][license-image]][license-url]
[![Docker badge](https://img.shields.io/docker/pulls/fiware/ngsi-ld_wrapper.svg)](https://hub.docker.com/r/fiware/ngsi-ld_wrapper/)
[![NGSI-LD badge](https://img.shields.io/badge/NGSI-LD-red.svg)](https://www.etsi.org/deliver/etsi_gs/CIM/001_099/009/01.01.01_60/gs_CIM009v010101p.pdf)
<br/>
[![Build badge](https://img.shields.io/travis/FIWARE/NGSI-LD_Experimental.svg?branch=master "Travis build status")](https://travis-ci.org/FIWARE/NGSI-LD_Experimental/?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/Fiware/NGSI-LD_Experimental/badge.svg?branch=master)](https://coveralls.io/github/Fiware/NGSI-LD_Experimental?branch=master)

このプロジェクトの目的は、
[NGSI-LD](https://www.etsi.org/deliver/etsi_gs/CIM/001_099/009/01.01.01_60/gs_CIM009v010101p.pdf)
のさまざまな実装オプションを研究することです。

最初の実装は [FIWARE Context Broker](https://github.com/fiware/context.Orion)
の上のプロキシによって具体化されたラッパーに基づいています。
[FIWARE NGSI](http://fiware.github.io/specifications/ngsiv2/latest/) を利用して、
NGSI-LD は [ETSI ISG CIM](https://portal.etsi.org/tb.aspx?tbid=854&SubTB=854)
によって開発されたグループ仕様であり、複数のシナリオでコンテキスト情報を提供、
消費、サブスクライブするための API を定義することを目的としています。
IoT だけでなく、さまざまな情報源からの情報にリアルタイムに近いアクセスが可能です。

FIWARE NGSI の根幹となる、[OMA NGSI-9/10 情報モデル](https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/NGSI-9/NGSI-10_information_model) は、[JSON-LD](https://json-ld.org/primer/latest/) の機能を活用した、リンクト・データ (エンティティのリレーションシップ)、[プロパティ・グラフ](https://neo4j.com/lp/book-graph-databases/)、セマンティクスをよりよくサポートするために ETSI CIM によって強化されています。結果の仕様は、**NGSI-LD** と命名されました。注目すべきは、[NGSI-LD 情報モデル](doc.ja/NGSI-LD_Information_Model.md) が OMA NGSI-9/10 情報モデル の一般化であることです。結果として、両方の情報モデルの間に良好なレベルの互換性と明確な移行パスが期待されます。

ラッパー実装は [FIWARE Context Broker](https://github.com/fiware/context.Orion)
の上で動作し、基本的に NGSIv2 (JSON) 表現と **NGSI-LD** (JSON-LD)
表現の間で適応します。

NGSI-LD の Orion ベースのネイティブ実装をお探しの場合は、
[Orion-LD](https://github.com/fiware/context.Orion-LD) をご覧ください。

## ビルド方法

### 前提条件

* Java 8
* Scala runtime
* SBT build tool

```console
$ sbt compile
$ export NGSI_Endpoint=http://<Your_NGSI_Endpoint i.e. Orion's host:port>
$ sbt jetty:start
```

## テスト方法

```console
$ sbt test
```

## Docker を使った実行方法

```console
$ docker run -e NGSI_Endpoint="http://<Your_NGSI_Endpoint i.e. Orion's host:port>" fiware/ngsi-ld_wrapper

$ curl http://localhost:1030/version
```

## Docker Compose を使った実行方法

```console
$ wget https://raw.githubusercontent.com/Fiware/NGSI-LD_Wrapper/master/docker-compose.yml
$ docker-compose up

$ curl http://localhost:1030/version
```

## 設定を確認する方法 (NGSI endpoint)

```console
$ curl http://localhost:1030/configuration
```

## API オペレーションを呼び出す方法

```console
$ curl http://localhost:1030/ngsi-ld/v1/entities/
```

## 参照 :

https://github.com/fiware/dataModels

https://github.com/fiware/context.Orion

https://github.com/fiware/NGSI-LD_Tests

[license-image]: https://opensource.org/licenses/MIT
[license-url]: LICENSE
