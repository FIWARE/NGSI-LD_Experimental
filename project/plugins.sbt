addSbtPlugin("org.scalatra.sbt" % "sbt-scalatra" % "1.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.4")
resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/eed3si9n/sbt-plugins/"))(Resolver.ivyStylePatterns)