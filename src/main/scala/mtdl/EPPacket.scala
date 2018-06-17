package mtdl

import scala.tools.nsc.interpreter.InputStream

/**
  * A packet that is received from an endpoint
  *
  * @author yerlibilgin
  */
class EPPacket(val httpURL: String, val headers: java.util.Map[String, String], val httpInputStream: InputStream) {

}
