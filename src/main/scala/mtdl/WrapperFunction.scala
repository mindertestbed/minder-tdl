package mtdl

import mtdl.ReflectionUtils._

/**
  * Created by yerlibilgin on 05/12/14.
  */
case class WrapperFunction(val wrapperId: String, val signature: String) {
  //parse the parameters from id

  var params: Array[Param] = null

  initialize();

  private var _timeout: Long = 0L

  def timeout = _timeout

  def timeout_=(timeout: Long) {
    _timeout = timeout
  }

  def initialize() {
    val splt = signature.split(Array('(', ',', ')'))
    if (splt == null || splt.length == 0)
      throw new Exception(signature + " is invalid")

    params = Array.ofDim(splt.length - 1);

    for (i <- 1 until splt.length) {
      var prm = splt(i).trim();
      params(i - 1) = normalize(splt(i), i - 1)
    }
  }

  /**
    * Simple check. Do we have enough number of params so that we cover the given index
    */
  def hasParam(param: Int): Boolean = {
    params.length >= param
  }

  /**
    * Check whether the parameter is a valid Java Type
    */
  def normalize(prm: String, index: Int) = {
    //remove all spaces
    val prm2 = prm.replaceAll("\\s", "");

    if (!isValidJavaType(prm2)) {
      throw new IllegalArgumentException(prm + " is not a valid java type")
    }

    Param(index, this)
  }

  override def toString(): String = {
    wrapperId + "::" + signature;
  }


  override def equals(o: Any) = {
    if (!o.isInstanceOf[WrapperFunction]) {
      false
    } else if (o == null) {
      false
    } else {
      this.toString() == o.toString
    }
  }

  override def hashCode = this.wrapperId.hashCode * 31 + this.signature.hashCode


  /**
    * default signal timeout handler throws the exception
    */
  var timeoutHandler: (RuntimeException => Unit) = (rte: RuntimeException) => {
    throw rte
  }

  def handleTimeout(rte: RuntimeException): Unit = {
    timeoutHandler(rte);
  }

  /**
    * Create a rivet from the param pipe list that we receive from the tdl definition.
    * @param lists
    * @return
    */
  def shall(lists: List[ParameterPipe]*)(implicit tdl: MinderTdl): Rivet = {
    val rivet = new Rivet(this, lists.map(list => {
      list.map(f => {
        assignToPipe(f);
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
  private def assignToPipe(prm: ParameterPipe): ParameterPipe = {
    //BUGfix for BUG-1
    if (prm.in == -1) {
      prm.outRef = Param(-1, this)
    } else {
      if (!(this hasParam prm.out)) {
        throw new IllegalArgumentException("WrapperFunction " + wrapperId + "." + signature + " does not have a param " + prm.out)
      }
      prm.outRef = this.params(prm.out);
    }
    prm
  }


  def asIs(implicit tdl: MinderTdl): Rivet = {
    val rivet = new Rivet(this, List())
    tdl.RivetDefs.add(rivet);
    rivet
  }


}


class NullSlot() extends WrapperFunction(MinderTdl.NULL_WRAPPER_NAME, MinderTdl.NULL_SLOT_NAME) {
  override def hasParam(param: Int): Boolean = true;

  override def initialize() {
    params = Array.ofDim(1000);

    for (i <- 1 until 1000) {
      params(i - 1) = Param(i, this)
    }
  }
}