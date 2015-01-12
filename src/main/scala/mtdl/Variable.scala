package mtdl

/**
 * Created by yerlibilgin on 05/12/14.
 */
case class Variable(override val wrapperId: String, override val signature: String) extends SignalSlot(wrapperId, signature) {
  override def shall(lst: List[ParameterPipe]*)(implicit tdl: MinderTdl): Rivet = {
    throw new IllegalStateException("the method shall is forbidden for a signal")
  }

  override def asIs(implicit tdl: MinderTdl): Rivet = {
    throw new IllegalStateException("the method asIs is forbidden for a signal")
  }
}

