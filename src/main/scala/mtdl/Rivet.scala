package mtdl

import scala.collection.mutable

/**
 * Created by yerlibilgin on 05/12/14.
 */
class Rivet(val slot: SlotImpl, pipeListList: List[List[ParameterPipe]]) {
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

  //if the pipe list is empty, then the slot should also be zero-param
  if (pipes.isEmpty && !slot.params.isEmpty)
    throw new IllegalArgumentException("The slot requires arguments but none is supplied")

  //the length of the parameters has to be the same as the number of slots if the signals and slots are not zero param.
  if (pipes.size != slot.params.size && pipes.size != 1 && (pipes(0).in != -1 || pipes(0).out != -1))
    throw new IllegalArgumentException("The number of slot arguments has to match the number of parameter pipes")

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
    "Rivet for " + slot.wrapperId + "." + slot.signature
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
      if (slot == null) {
        rvt.slot == null
      } else {
        if (slot != rvt.slot)
          false
        else {
          slot.signature == rvt.slot.signature && this.describe() == rvt.describe()
        }
      }
    } else
      false
  }

  override def hashCode(): Int = slot.signature.hashCode
}
