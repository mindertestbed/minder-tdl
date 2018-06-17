package mtdl

/**
  * This class represents an endpoint,
  * and endpoint is where the test case sleeps and
  * waits for the endpoint event to occurr.
  *
  * Then the target function is executed
  */
case class EndpointRivet(val endPointIdentifier: String, val targetFunc: EPPacket => Unit, val method: String = "GET", val timeout: Long = 0)(implicit tdl: MinderTdl) extends Rivet(AdapterFunction(EndpointRivet.ADAPTER_NAME, endPointIdentifier), null) {
}

object EndpointRivet {
  val ADAPTER_NAME = "ENDPOINT_ADAPTER";
}
