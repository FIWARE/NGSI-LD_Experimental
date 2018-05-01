package json

import scala.util.parsing.combinator.JavaTokenParsers


/**
  *
  *  JSON Parser
  *
  *  (Adapted from example in the book "Programming in Scala" by Odersky and others)
  *
  *
  */
class JSONParser extends JavaTokenParsers {

  def obj: Parser[Map[String, Any]] =
    "{"~> repsep(member, ",") <~"}" ^^ (Map() ++ _)

  def arr: Parser[List[Any]] =
    "["~> repsep(value, ",") <~"]"

  def member: Parser[(String, Any)] =
    stringLiteral~":"~value ^^ {
      case name~":"~value => {
        (name.substring(1,name.length -1), value)
      }
    }

  def value: Parser[Any] = (
    obj
      | arr
      | stringLiteral ^^ (x => {
        // Remove quotes
        x.substring(1, x.length - 1)
      })
      | decimalNumber ^^ (_.toDouble)
      | floatingPointNumber ^^ (_.toDouble)
      | wholeNumber ^^ (_.toLong)
      | "null"  ^^ (x => null)
      | "true"  ^^ (x => true)
      | "false" ^^ (x => false)
    )
}