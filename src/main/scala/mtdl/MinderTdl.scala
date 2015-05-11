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

abstract class MinderTdl(val variableWrapperMapping: Map[String, String], val run: java.lang.Boolean) {
  var dlCache = new File("dlcache");
  dlCache.mkdirs()
  //https://joinup.ec.europa.eu/system/files/project/94/f7/9e/ADMS_XML_Schema_v1.01.zip

  val JOINUP_CORE = "https://joinup.ec.europa.eu/site/core_location/"

  implicit val tdl = this;

  implicit def str2MinderStr(str: String) = MinderStr(str)

  implicit def int2MinderInt(int: Int) = MinderInt(int)

  implicit def anyRef2MinderAnyRef(anyRef: AnyRef) = MinderAny(anyRef)

  var TestCase: String = ""
  var Author: String = ""
  var Created: Date = null
  var Revision: Int = 0

  var ThisPackage: String = ""
  var AuthorMail: String = ""

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

  def NULLSLOT = new NullSlot()

  var debug: Any => Unit = (any: Any) => println(any)
  var debugThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {println(any);throwable.printStackTrace()}
  var info: Any => Unit = (any: Any) => println(any)
  var infoThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {println(any);throwable.printStackTrace()}
  var error: Any => Unit = (any: Any) => println(any)
  var errorThrowable: (Any, Throwable) => Unit = (any: Any, throwable: Throwable) => {println(any);throwable.printStackTrace()}


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

  def getAsset(asset: String): Array[Byte] = {
    //don't do anthing in description mode.
    if (!tdl.run)
      return null;



    val bis = new BufferedInputStream(new FileInputStream("assets/" + AuthorMail + "/" + asset))
    val bArray = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    bArray
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
      pipe.inRef = Param(-1, null, signal)
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

  def download(url: String) = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream();

    //first check the cache for the url.
    val cacheKey = url.replaceAll("\\p{Punct}", "_")
    val fl = new File(tdl.dlCache.getAbsolutePath, cacheKey);
    if (fl.exists()) {
      var len: Int = 0
      val fis = new FileInputStream(fl)
      len = fis.read(buffer)
      while (len > 0) {
        stream.write(buffer, 0, len);
        len = fis.read(buffer)
      }
      fis.close()
      stream toByteArray
    } else {
      if (url.startsWith("https"))
        downloadHttps(url, stream)
      else
        downloadHttp(url, stream)

      val streamArr = stream.toByteArray
      val outs = new FileOutputStream(fl)
      outs write streamArr;
      outs close;
      streamArr
    }
  }

  /**
   * Download a file from an HTTPS server
   * @param httpsURL
   * @param stream
   */
  def downloadHttps(httpsURL: String, stream: OutputStream) = {
    val myurl = new URL(httpsURL);
    val con = myurl.openConnection().asInstanceOf[HttpsURLConnection]
    val ins = con.getInputStream()
    val buffer: Array[Byte] = Array.ofDim(2048)

    var len = ins.read(buffer)
    while (len > 0) {
      stream.write(buffer, 0, len);
      len = ins.read(buffer)
    }
    ins.close();
  }

  /**
   * Download from a regular server
   * @param url
   * @param stream
   * @return
   */
  def downloadHttp(url: String, stream: OutputStream) = {
    import sys.process._
    import java.net.URL

    new URL(url) #> stream
  }


  val buffer: Array[Byte] = Array.ofDim(2048)

  /**
   * Searches and extracts the entry with the given name from the acrhive given in zip.
   * @param repo the remote repo that the zip will be downloaded from
   * @param entry the entry name that will be searched in the zip
   * @return
   * the byte array that conatins the zip entry if found
   * @throws IllegalArgumentException if the zip does not contain the given entry
   */
  def extractFromZip(repo: String, entry: String): Array[Byte] = {

    val zip = download(repo);

    val zisTream = new ZipInputStream(new ByteArrayInputStream(zip))

    var zipEntry: ZipEntry = null

    var baos: ByteArrayOutputStream = null

    zipEntry = zisTream.getNextEntry

    var found = false
    while (zipEntry != null) {
      if (zipEntry.getName == entry) {
        baos = new ByteArrayOutputStream()

        var len: Int = 0
        len = zisTream.read(buffer)
        while (len > 0) {
          baos.write(buffer, 0, len);
          len = zisTream.read(buffer)
        }
        zipEntry = null
        found = true
      } else {
        zipEntry = zisTream.getNextEntry
      }
    }

    if (!found)
      throw new IllegalArgumentException(repo + "/" + entry + " was not found")

    baos.toByteArray
  }

  val wrapperDefs: mutable.Set[String] = mutable.Set[String]()
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

/**
 * Used to provide additional methods to Int.
 *
 * @param in
 */
case class MinderInt(in: Int) {

  /**
   * creates a pipe that has in as input, out as target.
   * If no input signal is specified, then in will be returned
   * as the default output if this pipe.
   *
   * This default behaviour fixes the int input ambiguity bug.
   */
  def onto(out: Int) = {
    val p = ParameterPipe((in - 1), (out - 1))
    //the default selection for a parameter pipe is to return the in value.
    //later the signal value might be passed.
    p.select = (a: Any) => in;
    p
  }
}

/**
 * Used to provide additional methods to Any.
 * @param src
 */
case class MinderAny(src: Any) {
  /**
   * When a value is mapped onto <code>out</code>, then
   * it is returned in the default converter function.
   * @param out
   * @return
   */
  def onto(out: Int) = {
    val p = ParameterPipe(-1, out - 1);
    //whatever happens, return the value.
    p.select = (a: Any) => src;
    p
  }
}
