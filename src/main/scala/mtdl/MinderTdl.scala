package mtdl

import java.util
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable
import scala.collection.mutable.MutableList

/**
  *
  * a map that contains the parameter adapter names and their actual mappings.
  * The map keys are parameter names, the values are:
  * AdapterName|Version (separated by a |)
  *
  * @param run
  */
abstract class MinderTdl(val run: java.lang.Boolean) extends Utils {
  implicit val tdl = this

  implicit def str2MinderStr(str: String) = MinderStr(str)

  implicit def int2MinderInt(int: Int) = MinderInt(int)

  implicit def anyRef2MinderAnyRef(anyRef: AnyRef) = MinderAny(anyRef)

  val automatically = List[ParameterPipe]()

  var RivetDefs = new util.ArrayList[Rivet]()

  var currentRivetIndex: Int = 0

  val adapterDefs: mutable.Set[String] = mutable.Set[String]()

  def NULLSLOT = new NullSlot()

  val NULL = new MinderNull()


  var exception: Throwable = null

  def THROWLATER(exception: Throwable): Unit = {
    this.exception = exception
  }

  def THROWLATER(message: String, exception: Throwable): Unit = {
    this.exception = new Exception(message, exception)
  }

  def THROWLATER(message: String): Unit = {
    this.exception = new RuntimeException(message)
  }

  var debug: Any => Unit = (any: Any) => println(any)
  var debugThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any)
    throwable.printStackTrace()
  }
  var warn: Any => Unit = (any: Any) => println(any)
  var warnThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any)
    throwable.printStackTrace()
  }
  var info: Any => Unit = (any: Any) => println(any)
  var infoThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any)
    throwable.printStackTrace()
  }
  var error: Any => Unit = (any: Any) => println(any)

  var errorThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any)
    throwable.printStackTrace()
  }

  /**
    * Provides a skeleton method for the mtdl implementors to implement. The test script
    * will be able to send key value pairs to the handlers that might use those keys-values
    * in further processes such as reporting.
    */
  var addReportMetadata: (String, String) => Unit = (key: String, value: String) => {
    //do nothing
  }


  def DEBUG(any: Any): Unit = {
    debug(any)
  }

  def DEBUG(any: Any, throwable: Throwable): Unit = {
    debug(any, throwable)
  }

  def WARN(any: Any): Unit = {
    warn(any)
  }

  def WARN(any: Any, throwable: Throwable): Unit = {
    warn(any, throwable)
  }

  def INFO(any: Any): Unit = {
    info(any)
  }

  def INFO(any: Any, throwable: Throwable): Unit = {
    info(any, throwable)
  }

  def ERROR(any: Any): Unit = {
    error(any)
  }

  def ERROR(any: Any, throwable: Throwable): Unit = {
    error(any, throwable)
  }

  def ADD_REPORT_METADATA(key: String, value: String): Unit = {
    addReportMetadata(key, value);
  }

  def use(signal: AdapterFunction)(list: List[ParameterPipe]) = {
    //if (run && !(signal.isInstanceOf[SignalImpl])) {
    //  throw new IllegalArgumentException(signal.signature + " is not a signal.")
    //}

    //create an empty mutable list that we can populate the parameters
    val ml: mutable.MutableList[ParameterPipe] = MutableList[ParameterPipe]()


    var actualList = list
    //if pipe is empty, the signal is automatically bound to the slot
    //so we should fill each param automatically.
    if (actualList.isEmpty) {
      actualList = signal.params.map(p => {
        ParameterPipe(p.index, p.index)
      }).toList
    }

    //if the actual list is still empty then the signal does not have any param.
    //so put a special param to make sure that this signal does not get lost
    //BUG FIX: BUG-1
    if (actualList.isEmpty) {
      val pipe = ParameterPipe(-1, -1)
      pipe.inRef = Param(-1, signal)
      ml += pipe
    } else {
      for (pipe <- actualList) {
        //verify that the signal has the in port of the pipe
        if (!(signal hasParam pipe.in)) {
          throw new Exception("singal {" + signal + "} does not have param <" + (pipe.in + 1) + ">")
        }

        pipe.inRef = signal params pipe.in

        //update the selector function to pass-through whatever we provide. (cos we will
        //provide the actual signal argument later.
        pipe.select = (a: Any) => a

        ml += pipe
      }
    }
    ml.toList
  }

  def map(paramTriple: ParameterPipe*): List[ParameterPipe] = {
    paramTriple.toList
  }

  def mapping(paramTriple: ParameterPipe*): List[ParameterPipe] = {
    paramTriple.toList
  }

  /**
    * Added since version 0.2.3 in order to omit the unreadable rivet syntax for NULL rivets
    */
  def runAsRivet(func: () => Unit): Unit = {
    NULLSLOT shall map(NULL onto 1 using { (any: Any) => {
      func()
      any
    }
    })
  }

  /**
    * Added since version 0.2.3 in order to omit unreadable rivet syntax where
    * only one signal is used and forwarded to NULLSLOT.
    *
    * @param signalSlot
    * @param f
    * @return
    */
  def waitForSignal(signalSlot: AdapterFunction)(f: (Any) => Any): Unit = {
    NULLSLOT shall (use(signalSlot)) (mapping(1 onto 1 using f))
  }

  /**
    * A simply forwarding method for increasing readability.
    *
    * @param f
    * @return
    */
  def using(f: (Any) => Any): (Any => Any) = f


  /**
    * Section added for initializing and auto incrementing the test case IDS.
    */
  var rivetIdGenerator: AtomicInteger = new AtomicInteger(0)

  def getNextRivetId(): Int = {
    rivetIdGenerator.getAndIncrement()
  }


  def suspend() = {
    RivetDefs.add(new Suspend())
  }
}

case class MinderStr(vall: String) {
  val cache = new java.util.HashMap[String, (AnyRef, java.lang.reflect.Field)]

  def of(adapterId: String)(implicit tdl: MinderTdl): AdapterFunction = {
    tdl.adapterDefs += adapterId

    return AdapterFunction(adapterId, vall)
  }

  def %(subRepo: String): String = {
    vall.concat("/").concat(subRepo)
  }

  def from(testCase: String)(implicit tdl: MinderTdl): Rivet = {
    var rivet: Rivet = null
    var refObj: AnyRef = null
    var field: java.lang.reflect.Field = null

    //check the class name and format
    val actualClassName =
      if (!testCase.contains('.')) {
        //this has to be in the same package. But still, lets add the full package name
        tdl.ThisPackage + "." + testCase
      } else {
        val index = testCase.lastIndexOf('.')
        var email = testCase.substring(0, index)
        val className = testCase.substring(index + 1)
        email = email.replaceAll("(@|\\.)", "_")
        TdlCompiler.MINDERTDL_PACKAGE_NAME + "." + email + "." + className
      }

    if (cache.containsKey(actualClassName)) {
      val tpl = cache.get(actualClassName)
      refObj = tpl._1
      field = tpl._2
      field.get(refObj).asInstanceOf[Rivet]
    } else {
      val clazz: Class[_] = this.getClass.getClassLoader.loadClass(actualClassName)
      refObj = clazz.getConstructors()(0).newInstance(tdl.run).asInstanceOf[AnyRef]
      field = clazz.getDeclaredField(vall)
      field setAccessible true
      cache.put(actualClassName, (refObj, field))
    }
    rivet = field.get(refObj).asInstanceOf[Rivet]
    tdl.RivetDefs.add(rivet)
    rivet
  }


  def under(repo: String)(implicit tdl: MinderTdl): Array[Byte] = {
    //don't do anthing in description mode.
    if (!tdl.run)
      return null

    //TODO: caching mechanism
    //check if the repo is a zip file
    //TODO: support jar archives too

    if (repo.endsWith(".zip")) {
      tdl.extractFromZip(repo, vall)
    } else {
      tdl.download(repo + "/" + vall)
    }
  }
}

object MinderTdl {
  val NULL_ADAPTER_NAME: String = "NULLADAPTER"
  val NULL_SLOT_NAME = "NULLSLOT"
}