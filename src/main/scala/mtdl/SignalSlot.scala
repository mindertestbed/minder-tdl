package mtdl

import minderengine.ReflectionUtils
import minderengine.ReflectionUtils._

/**
 * Created by yerlibilgin on 05/12/14.
 */
abstract class SignalSlot(val wrapperId: String, val signature: String) {
  //parse the parameters from id
  var splt = signature.split(Array('(', ',', ')'));
  if (splt == null || splt.length == 0)
    throw new Exception(signature + " is invalid")

  var params: Array[Param] = Array.ofDim(splt.length - 1);

  for (i <- 1 until splt.length) {
    var prm = splt(i).trim();
    params(i - 1) = normalize(splt(i), i-1)
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

    if (!isValidJavaType(prm2)){
      throw new IllegalArgumentException(prm + " is not a valid java type")
    }

    Param(index, cannonical2Class(prm2), this)
  }

  /**
   * A rivet definition function that requires the existence of signals or free values
   * @param lst
   * @param tdl
   * @return
   */
  def shall(lst: List[ParameterPipe]*)(implicit tdl: MinderTdl): Rivet;

  /**
   * A rivet definition for slots that do not take anything
   * @return
   */
  def asIs(implicit tdl: MinderTdl): Rivet
}
