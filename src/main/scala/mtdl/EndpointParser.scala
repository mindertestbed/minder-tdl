package mtdl

import java.util
import java.util.Collections
import java.util.regex.Pattern

import com.google.common.base.Strings
import org.slf4j.LoggerFactory

/**
  * #author: yerlibilgin
  * #date:   05/09/15.
  *
  * This class performs special operations on the MTDL before it gets compiled.
  * The aim is to make the language more felxible and
  * save the user from the boundaries of the scala language.
  */
object EndpointParser {
  private val LOGGER = LoggerFactory.getLogger(EndpointParser.getClass)

  /**
    * Parse the tdl source and match the waitForEP functions,
    * validate that the first parameter is an identifier name and add it to the list
    *
    * @param source
    * @return
    */
  def detectEndPointIdentifiers(source: String): (util.Set[String], String) = {
    LOGGER.debug("Try to identify end point parameters")
    if (Strings.isNullOrEmpty(source)) {
      LOGGER.warn("Empty tdl provided")
      (Collections.EMPTY_SET.asInstanceOf[util.Set[String]], "")
    } else {
      //handle the comments and strings in the future.
      //assume that the line starts with waitForEp
      val methodPatternStr = "((\\[\\s*GET\\s*(\\,\\s*\\d+)?\\])|(\\[\\s*POST\\s*(\\,\\s*\\d+)?\\])|(\\[\\s*PUT\\s*(\\,\\s*\\d+)?\\])|(\\[\\s*DELETE\\s*(\\,\\s*\\d+)?\\]))"

      val validPattern = Pattern.compile("(" + methodPatternStr + "\\s*)?waitForEP\\s*\\(\\s*\\w(_|\\d|\\w)*\\,", Pattern.MULTILINE);
      val allPatterns = Pattern.compile ("(" + methodPatternStr + "\\s*)?waitForEP\\s*\\(\\s*", Pattern.MULTILINE);

      val methodAndTimeoutPattern = Pattern.compile(methodPatternStr);

      val validMatcher = validPattern.matcher(source);
      val allMatcher = allPatterns.matcher(source);

      //create a linked hash set, just ensure order.
      val identifierSet = new util.LinkedHashSet[String]()

      var newSource = "";

      var previousIndex = 0;
      while (allMatcher.find()) {
        val found = validMatcher.find()

        if (!found || allMatcher.start() != validMatcher.start()) {
          //there is a problem with this expression.
          //report it
          throw new IllegalArgumentException(allMatcher.start() + " first argument of waitForEP must be a constant parameter name.")
        }

        newSource += source.substring(previousIndex, allMatcher.start())

        //enlist the name of the parameter
        val paramName = source.substring(allMatcher.end(), validMatcher.end() - 1).trim;

        val subStr = source.substring(allMatcher.start(), allMatcher.end())
        val methodMatcher = methodAndTimeoutPattern.matcher(subStr);

        var method = "GET"
        var timeout = 0
        if(methodMatcher.find()) {
          val methodAndTimeout = subStr.substring(methodMatcher.start(), methodMatcher.end()).replaceAll("\\[|\\]", "").trim.split("\\,");

          val (method_, timeout_) = if (methodAndTimeout.length == 1) {
            (methodAndTimeout(0), 0)
          } else {
            (methodAndTimeout(0), methodAndTimeout(1).toInt)
          }

          method = method_
          timeout = timeout_
        }

        LOGGER.debug(s"Identifier $paramName")
        LOGGER.debug(s"Method: $method")
        LOGGER.debug(s"Timeout: $timeout")

        newSource += "waitForEP(\"" + paramName + "\", \"" + method + "\", " + timeout + ", "

        identifierSet.add(s"$method:$paramName");

        previousIndex = validMatcher.end();
      }

      //apend the remaining part if exists
      if(previousIndex < source.length)
        newSource += source.substring(previousIndex)

      (identifierSet, newSource)
    }
  }
}

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