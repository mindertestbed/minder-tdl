package mtdl

/**
 * Created by yerlibilgin on 05/12/14.
 */
case class SlotImpl(override val wrapperId: String, override val signature: String) extends SignalSlot(wrapperId, signature) {
  /**
   * Create a rivet from the param pipe list that we receive from the tdl definition.
   * @param lists
   * @return
   */
  override def shall(lists: List[ParameterPipe]*)(implicit tdl: MinderTdl): Rivet = {
    val rivet = new Rivet(this, lists.map(list => {
      list.map(f => {
        assignSlotToPipe(f);
      })
    }).toList)

    tdl.RivetDefs.add(rivet);
    rivet
  }

  /**
   * Sets the slot value and fills the output pin of the parameter pipe.
   * @param prm
   * @return
   */
  private def assignSlotToPipe(prm: ParameterPipe): ParameterPipe = {
    //BUGfix for BUG-1
    if (prm.in == -1) {
      prm.outRef = Param(-1, this)
    } else {
      if (!(this hasParam prm.out)) {
        throw new IllegalArgumentException("Slot " + wrapperId + "." + signature + " does not have a param " + prm.out)
      }
      prm.outRef = this.params(prm.out);
    }
    prm
  }


  override def asIs(implicit tdl: MinderTdl): Rivet = {
    val rivet = new Rivet(this, List())
    tdl.RivetDefs.add(rivet);
    rivet
  }

  override def equals(o: Any): Boolean = {
    if (!o.isInstanceOf[SignalImpl]) false
    else super.equals(o)
  }

  override def handleTimeout(rte: RuntimeException): Unit = throw rte
}


class NullSlot() extends SlotImpl("NULLWRAPPER", "NULLSLOT") {
  override def hasParam(param: Int): Boolean = true;

  override def initialize() {
    params = Array.ofDim(1000);

    for (i <- 1 until 1000) {
      params(i - 1) = Param(i, this)
    }
  }
}