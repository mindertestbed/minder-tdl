package mtdl

import scala.collection.mutable

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
    var rivet = new Rivet(this, lists.map(list => {
      list.map(f => {
        assignSlotToPipe(f);
      })
    }).toList)

    tdl.SlotDefs.add(rivet);
    rivet
  }

  /**
   * Sets the slot value and fills the output pin of the parameter pipe.
   * @param prm
   * @return
   */
  private def assignSlotToPipe(prm: ParameterPipe): ParameterPipe = {

    if (!(this hasParam prm.out)) {
      throw new IllegalArgumentException("Slot " + wrapperId + "." + signature + " does not have a param " + prm.out)
    }
    prm.outRef = this.params(prm.out);
    prm
  }


  override def asIs(implicit tdl: MinderTdl): Rivet = {
    val rivet = new Rivet(this, List())
    tdl.SlotDefs.add(rivet);
    rivet
  }
}
