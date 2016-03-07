package mtdl

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

/**
 * Created by yerlibilgin on 05/12/14.
 */
class Rivet(val wrapperFunction: WrapperFunction, pipeListList: List[List[ParameterPipe]])(implicit tdl: MinderTdl) {
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
  val pipes = if(pipeListList != null) pipeListList.flatten else List()

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
  println("Slot name " + wrapperFunction.wrapperId + "." + wrapperFunction.signature)
  println("Slot params size " + (wrapperFunction.params == null));

  //if the pipe list is empty, then the wrapperFunction should also be zero-param
  if (pipes.isEmpty && !wrapperFunction.params.isEmpty)
    throw new IllegalArgumentException("The wrapperFunction requires arguments but none is supplied")


  //the length of the parameters has to be the same as the number of wrapperFunctions if the signals and wrapperFunctions are not zero param.
  if (wrapperFunction.params.size != 1000 && pipes.size != wrapperFunction.params.size && pipes.size != 1 && (pipes(0).in != -1 || pipes(0).out != -1))
    throw new IllegalArgumentException("The number of wrapperFunction arguments has to match the number of parameter pipes")

  pipeListList.foreach(f = pipeList => {
    //ensure the list is not empty and take the first key
    if (!(pipeList isEmpty)) {
      val firstPipe = pipeList(0)

      //grab the key to this list

        if (firstPipe.inRef != null) {
          if (firstPipe.inRef.source == null) throw new IllegalArgumentException("A pipe having a param ref without a signal")
          val key =(firstPipe.inRef.source.wrapperId,firstPipe.inRef.source.signature)
          signalPipeMap += (key -> pipeList)
        }else{
          freeVariablePipes ++= pipeList
        }

    }
  })

  override def toString() = {
    "Rivet for " + wrapperFunction.wrapperId + "." + wrapperFunction.signature
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
      if (wrapperFunction == null) {
        rvt.wrapperFunction == null
      } else {
        if (wrapperFunction != rvt.wrapperFunction)
          false
        else {
          wrapperFunction.signature == rvt.wrapperFunction.signature && this.describe() == rvt.describe()
        }
      }
    } else
      false
  }

  override def hashCode(): Int = wrapperFunction.signature.hashCode

  def setGITBMetadata(tplStepType: TPLStepType, tplStepDescription: String): Unit ={
    this.tplStepType = tplStepType;
    this.tplStepDescription = tplStepDescription;
  }

}

class Suspend(implicit tdl: MinderTdl)  extends Rivet(null,null){
}
