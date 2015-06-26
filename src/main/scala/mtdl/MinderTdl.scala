package mtdl

import java.io._
import java.net.URL
import java.util
import java.util.zip.{ZipEntry, ZipInputStream}
import javax.net.ssl.HttpsURLConnection

import minderengine.{MinderUtils, Wrapper}

import scala.collection.mutable
import scala.collection.mutable.MutableList
import java.util.Date

abstract class MinderTdl(val variableWrapperMapping: Map[String, String], val run: java.lang.Boolean) extends Utils {
  //https://joinup.ec.europa.eu/system/files/project/94/f7/9e/ADMS_XML_Schema_v1.01.zip

  val JOINUP_CORE = "https://joinup.ec.europa.eu/site/core_location/"

  implicit val tdl = this;

  implicit def str2MinderStr(str: String) = MinderStr(str)

  implicit def int2MinderInt(int: Int) = MinderInt(int)

  implicit def anyRef2MinderAnyRef(anyRef: AnyRef) = MinderAny(anyRef)

  var ThisPackage: String = ""

  val param1: Int = 1
  val param2: Int = 2
  val param3: Int = 3
  val param4: Int = 4
  val param5: Int = 5
  val param6: Int = 6
  val param7: Int = 7
  val param8: Int = 8
  val param9: Int = 9
  val param10: Int = 10
  val param11: Int = 11
  val param12: Int = 12
  val param13: Int = 13
  val param14: Int = 14
  val param15: Int = 15
  val param16: Int = 16
  val param17: Int = 17
  val param18: Int = 18
  val param19: Int = 19
  val param20: Int = 20


  val automatically = List[ParameterPipe]()

  var SlotDefs = new util.ArrayList[Rivet]()

  val wrapperDefs: mutable.Set[String] = mutable.Set[String]()


  def NULLSLOT = new NullSlot()

  val NULL = new MinderNull()

  var debug: Any => Unit = (any: Any) => println(any)
  var debugThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any); throwable.printStackTrace()
  }
  var info: Any => Unit = (any: Any) => println(any)
  var infoThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any); throwable.printStackTrace()
  }
  var error: Any => Unit = (any: Any) => println(any)
  var errorThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {
    println(any); throwable.printStackTrace()
  }

  var exception: RuntimeException = null;

  def THROWLATER(exception: RuntimeException): Unit ={
    this.exception = exception;
  }

  def DEBUG(any: Any): Unit = {
    debug(any)
  }

  def DEBUG(any: Any, throwable: Throwable): Unit = {
    debug(any, throwable)
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

  def use(signal: SignalSlot)(list: List[ParameterPipe]) = {
    if (run && !(signal.isInstanceOf[SignalImpl])) {
      throw new IllegalArgumentException(signal.signature + " is not a signal.")
    }

    //create an empty mutable list that we can populate the parameters
    val ml: mutable.MutableList[ParameterPipe] = MutableList[ParameterPipe]()


    var actualList = list;
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
}

case class MinderStr(vall: String) {
  val cache = new java.util.HashMap[String, (AnyRef, java.lang.reflect.Field)]

  def of(wrapperId: String)(implicit tdl: MinderTdl): SignalSlot = {
    if (tdl.run == false) {
      //description mode
      //we need to use .shall function of SLotImpl to get the rivet.
      //so always return slotImpl.

      return SlotImpl(wrapperId, vall)
    } else {
      //if the wrapper id is a variable, then we have to find the matching actual wrapper from the wrapper map
      val searchKey = if (wrapperId.startsWith("$")) tdl.variableWrapperMapping(wrapperId) else wrapperId
      val signalOrSlot = SignalSlotInfoProvider.getSignalSlot(searchKey, vall)

      //now add the wrapper to the actual wrapper list
      tdl.wrapperDefs += signalOrSlot.wrapperId

      signalOrSlot
    }
  }

  def %(subRepo: String): String = {
    vall.concat("/").concat(subRepo)
  }

  def from(testCase: String)(implicit tdl: MinderTdl): Rivet = {
    var rivet: Rivet = null;
    var refObj: AnyRef = null;
    var field: java.lang.reflect.Field = null;

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
      val tpl = cache.get(actualClassName);
      refObj = tpl._1;
      field = tpl._2;
      field.get(refObj).asInstanceOf[Rivet]
    } else {
      val clazz: Class[_] = TdlClassLoader.loadClass(actualClassName)
      refObj = clazz.getConstructors()(0).newInstance(tdl.variableWrapperMapping, tdl.run).asInstanceOf[AnyRef]
      field = clazz.getDeclaredField(vall);
      field setAccessible true
      cache.put(actualClassName, (refObj, field));
    }
    rivet = field.get(refObj).asInstanceOf[Rivet]
    tdl.SlotDefs.add(rivet);
    rivet
  }


  def under(repo: String)(implicit tdl: MinderTdl): Array[Byte] = {
    //don't do anthing in description mode.
    if (!tdl.run)
      return null;

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
