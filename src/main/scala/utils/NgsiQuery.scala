package utils

/**
  *
  * Case class for holding the data behind a query
  *
  * Coypright (c) 2018 FIWARE Foundation e.V.
  *
  * Author: José M. Cantera
  *
  * LICENSE: MIT
  *
  *
  */
case class NgsiQuery(t:String,id:String=null,idPattern:String=null,georel:String=null)
