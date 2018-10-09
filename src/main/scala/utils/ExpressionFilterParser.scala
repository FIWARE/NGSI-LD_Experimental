package utils

import scala.util.parsing.combinator.JavaTokenParsers

class ExpressionFilterParser(ldContext:Map[String,String]) extends JavaTokenParsers {

  def value: Parser[String] =
    decimalNumber ^^ (x => { x.toString }) |
      stringLiteral ^^ (x => { x.toString }) |
      floatingPointNumber ^^ (x => { x.toString }) |
      wholeNumber ^^ (x => { x.toString })

  def attrName: Parser[String] =
    """[[A-Z][a-z][0-9]]+""".r ^^ (x => {
      ldContext.getOrElse(x,x)
    })

  def operator: Parser[String] =
    ("<=" | ">=" | ">" | "<" | "==" | "!=") ^^ {
      x=> x
    }

  def expr: Parser[String] =
    attrName ~ operator ~ value ^^ {
      case attrName ~ operator ~ value => attrName + operator + value
    }

  def fullExpr: Parser[String] =
    repsep(expr, ";") ^^ (x => {
      x.mkString(";")
    })
}
