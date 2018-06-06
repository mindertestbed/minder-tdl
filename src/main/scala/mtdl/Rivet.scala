package mtdl

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

/**
 * Created by yerlibilgin on 05/12/14.
 */
class Rivet(val adapterFunction: AdapterFunction, pipeListList: List[List[ParameterPipe]])(implicit tdl: MinderTdl) {
  /**
    * Fields added for GITB compliance
    * tplStepType
    */

  var tplStepType:TPLStepType  = TPLStepType.TEST_STEP;
  var tplStepDescription:String = "";

  //TODO: How to reset?
  var tplStepId: Long = tdl.getNextRivetId()

  /**
   * Hold a flat list of all parameter pipes.
   */
  val pipes = if(pipeListList != null && !pipeListList.isEmpty) pipeListList.flatten else List()

  private var _result:Object = null
  def result = _result

  def result_=(result: Object) {
    _result = result
  }
  /**
   * This hashmap contains signal names as strings and parameter pipe lists as values.
   *
   * We have to keep this data structure in order to consume the signal queue values once.
   */
  val signalPipeMap = new mutable.LinkedHashMap[(String,String), List[ParameterPipe]]()
  val freeVariablePipes = new mutable.MutableList[ParameterPipe]


  println("Pipes size " + pipes.size);
  println("Slot name " + adapterFunction.adapterId + "." + adapterFunction.signature)
  println("Slot params size " + (adapterFunction.params == null));

  //if the pipe list is empty, then the adapterFunction should also be zero-param
  if (pipes.isEmpty && !adapterFunction.params.isEmpty && adapterFunction.params.size != 1000)
    throw new IllegalArgumentException("The adapterFunction requires arguments but none is supplied")


  //the length of the parameters has to be the same as the number of adapterFunctions if the signals and adapterFunctions are not zero param.
  if (adapterFunction.params.size != 1000 && pipes.size != adapterFunction.params.size && pipes.size != 1 && (pipes(0).in != -1 || pipes(0).out != -1))
    throw new IllegalArgumentException("The number of adapterFunction arguments has to match the number of parameter pipes")

  pipeListList.foreach(f = pipeList => {
    //ensure the list is not empty and take the first key
    if (!(pipeList isEmpty)) {
      val firstPipe = pipeList(0)

      //grab the key to this list

        if (firstPipe.inRef != null) {
          if (firstPipe.inRef.source == null) throw new IllegalArgumentException("A pipe having a param ref without a signal")
          val key =(firstPipe.inRef.source.adapterId,firstPipe.inRef.source.signature)
          signalPipeMap += (key -> pipeList)
        }else{
          freeVariablePipes ++= pipeList
        }

    }
  })

  override def toString() = {
    "Rivet for " + adapterFunction.adapterId + "." + adapterFunction.signature
  }

  def describe() = {
    var desc = mutable.StringBuilder.newBuilder

    desc ++= toString()

    signalPipeMap.keysIterator.foreach(key => {
      desc ++= "\n  Using\n    [" + key + "]\n      with\n      "

      (signalPipeMap get key).foreach(list => {
        list.foreach(p => {
          desc.++=("(").append(p.in + 1).append("->").append(p.out + 1).append("), ")
        })
      })

      desc.deleteCharAt(desc.size-1)
      desc.deleteCharAt(desc.size-1)
      desc += '\n'
    })
    desc.toString()
  }


  override def equals(other: Any): Boolean = {
    if (other.isInstanceOf[Rivet]) {
      val rvt = other.asInstanceOf[Rivet];
      if (adapterFunction == null) {
        rvt.adapterFunction == null
      } else {
        if (adapterFunction != rvt.adapterFunction)
          false
        else {
          adapterFunction.signature == rvt.adapterFunction.signature && this.describe() == rvt.describe()
        }
      }
    } else
      false
  }

  override def hashCode(): Int = adapterFunction.signature.hashCode

  def setGITBMetadata(tplStepType: TPLStepType, tplStepDescription: String): Unit ={
    this.tplStepType = tplStepType;
    this.tplStepDescription = tplStepDescription;
  }

}

class Suspend(implicit tdl: MinderTdl)  extends Rivet(new NullSlot,List()){
}
