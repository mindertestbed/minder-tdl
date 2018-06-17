package mtdl

/**
  * A class that represents an HTTP Endpoint that
  * has a name and probably parameters
  *
  * @author ${user}
  */
@Deprecated
class HTTPEndpoint[+T](val fullPath: String) {

  /**
    * Create an endpoint with the parameters.
    *
    * Expected parameters types:
    * (String -> String) or String
    *
    * if the parameters are String tuples they are first concatenated in key=value format.
    *
    * @param path prefix path.
    * @param parameters
    */
  def this(path: String, parameters: List[T]) {
    this(s"$path?${
      //build pamareter list after concatenating the
      //keys to values using = sign
      HTTPEndpoint.concatenate(parameters.map(param => {
        param match {
          case p: (String, String) => {
            if (p._1 != null && p._2 != null)
              s"${p._1}=${p._2}"
            else ""
          }
          case p: String => p
        }
      }))
    }")
  }
}

@Deprecated
object HTTPEndpoint {
  /**
    * Concatenate the given list of parameters with the & character
    *
    * @param parameters
    * @return
    */
  def concatenate(parameters: List[String]): String = {
    var res = parameters.foldLeft[String]("") { (all, p) => s"$all$p&" }
    if (res.length > 0)
      res = res.substring(0, res.length - 1)

    res
  }

}
