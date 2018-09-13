package utils

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import scala.collection.mutable

/**
  *
  * It is in charge of resolving a JSON-LD @context
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
object LdContextResolver {
  private val ldContexts = mutable.Map[String, Map[String, Any]]()

  def resolveContext(ldContextLoc: String, ldContextAcc: mutable.Map[String, String]): Unit = synchronized {
    var ldContext:Map[String, Any] = Map[String,Any]()

    if (ldContexts.contains(ldContextLoc)) {
      Console.println(s"loading @context from cache: ${ldContextLoc}")
      ldContext = ldContexts(ldContextLoc)
    }
    else {
      Console.println(s"Resolving JSON-LD @context: ${ldContextLoc}")

      val getRequest = new HttpGet(ldContextLoc)

      // send the GET request
      val httpClient = HttpClientBuilder.create().build()
      val result = httpClient.execute(getRequest)

      if (result.getStatusLine.getStatusCode == 200) {
        val ldContextStr = EntityUtils.toString(result.getEntity, "UTF-8")
        ldContext = ParserUtil.parse(ldContextStr).asInstanceOf[Map[String, Any]]

        ldContexts += (ldContextLoc -> ldContext)
      }
    }

    val firstLevel = ldContext.getOrElse("@context", None)

    if (firstLevel == None) {
      Console.println(s"It seems ${ldContextLoc} does not contain a valid JSON-LD @context")
      return
    }

    if (firstLevel.isInstanceOf[Map[String, String]]) {
      ldContextAcc ++= firstLevel.asInstanceOf[Map[String, String]]
    }
    else if (firstLevel.isInstanceOf[List[Any]]) {
      val list = firstLevel.asInstanceOf[List[Any]]

      list.foreach(l => {
        if (l.isInstanceOf[String]) {
          resolveContext(l.asInstanceOf[String], ldContextAcc)
        }
        else if (l.isInstanceOf[Map[String, String]]) {
          ldContextAcc ++= l.asInstanceOf[Map[String, String]]
        }
      })
    }
    else {
      Console.println(s"Cannot resolve @context: ${ldContextLoc}")
      return
    }
  }

}
