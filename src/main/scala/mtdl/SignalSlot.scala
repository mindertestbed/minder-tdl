package mtdl

import mtdl.ReflectionUtils._

/**
 * Created by yerlibilgin on 05/12/14.
 */
abstract class SignalSlot(val wrapperId: String, val signature: String) {

  private var loader : ClassLoader = getClass.getClassLoader

  def setTdlClassLoader(ldr: ClassLoader) = {loader = ldr}

  //parse the parameters from id

  var params: Array[Param] = null

  initialize();

  private var _timeout:Long = 0L

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

    Param(index, cannonical2Class(prm2, loader), this)
  }

  override def toString(): String = {
    wrapperId + "::" + signature;
  }


  override def equals(o: Any) = {
    if (!o.isInstanceOf[SignalSlot]) {
      false
    } else if (o == null) {
      false
    } else {
      this.toString() == o.toString
    }
  }

  override def hashCode = this.wrapperId.hashCode * 31 + this.signature.hashCode

  /**
   * A rivet definition function that requires the existence of signals or free values
   * @param lst
   * @param tdl
   * @return
   */
  def shall(lst: List[ParameterPipe]*)(implicit tdl: MinderTdl): Rivet

  /**
   * A rivet definition for slots that do not take anything
   * @return
   */
  def asIs(implicit tdl: MinderTdl): Rivet

}
