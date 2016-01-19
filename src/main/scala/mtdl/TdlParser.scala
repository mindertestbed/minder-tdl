package mtdl

import java.io.FileReader
import java.util.regex.Pattern

import scala.io.Source

/**
 * #author: yerlibilgin
 * #date:   05/09/15.
 *
 * This class performs special operations on the MTDL before it gets compiled.
 * The aim is to make the language more felxible and
 * save the user from the boundaries of the scala language.
 */
object TdlParser {

  def main2(args: Array[String]) {
    val tdl = Source.fromFile("sample.tdl").mkString
    println(tdl);
    //def ident: scala.util.parsing.combinator.Parsers  Parser[String] = """[a-zA-Z_]\w*""".r
  }

  def main(args: Array[String]): Unit = {
    println(System.getProperty("user.dir"))
    val tdl = Source.fromFile("parse.txt").mkString

    val pattern = Pattern.compile("""this\.anyRef2MinderAnyRef\(.*\)\.onto""", Pattern.MULTILINE)

    val matcher = pattern.matcher(tdl);

    while (matcher.find()) {
      println(matcher.group())//tdl.substring(matcher.start(), matcher.end));
    }

    println("What happened here")

    //val reader = new FileReader("sample.dsl.txt")

    //println(FileHandler.parseAll(FileHandler.commandList, reader))
  }
}



import scala.util.parsing.combinator.JavaTokenParsers
/*
object FileHandler extends JavaTokenParsers {
  def string : Parser[Any] = super.stringLiteral
  def email : Parser[Any] = "EMAIL"~string
  def move : Parser[Any] = "MOVE"~string
  def operator : Parser[Any] = "contains" | "endsWith" | "startsWith"
  def expr : Parser[Any] = "$FILENAME"~operator~string
  def block : Parser[Any] = "{"~rep(email | move)~"}"
  def ifClause :Parser[Any] = "if"~"("~expr~")"~block
  def elseIfClause : Parser[Any] = "else"~ifClause
  def elseClause : Parser[Any] = "else"~block
  def ifElse : Parser[Any] = ifClause~opt(rep(elseIfClause))~opt(elseClause)
  def commandList = rep(email | move | ifElse)
}*/