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

abstract class MinderTdl {
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

  var thisPackage: String = ""

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

  def use(signal: SignalSlot)(list: List[ParameterPipe]) = {
    if (!(signal.isInstanceOf[SignalImpl])) {
      throw new IllegalArgumentException(signal.signature + " is not a signal.")
    }

    //create an empty mutable list that we can populate the parameters
    val ml: mutable.MutableList[ParameterPipe] = MutableList[ParameterPipe]()


    //if pipe is empty, the signal is automatically bound to the slot
    //so we should fill each param.

    var actualList = list;

    if (actualList.isEmpty) {
      actualList = signal.params.map(p => {
        ParameterPipe(p.index, p.index)
      }).toList
    }

    for (pipe <- actualList) {
      //verify that the signal has the in port of the pipe
      if (!(signal hasParam pipe.in)) {
        throw new Exception("singal {" + signal + "} does not have param <" + (pipe.in + 1) + ">")
      }

      pipe.inRef = signal params pipe.in

      ml += pipe
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
  val buffer: Array[Byte] = Array.ofDim(2048)
  val cache = new java.util.HashMap[String, (AnyRef, java.lang.reflect.Field)]

  def of(wrapperId: String): SignalSlot = {
    SignalSlotInfoProvider.getSignalSlot(wrapperId, vall)
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
        tdl.thisPackage + "." + testCase
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
      refObj = clazz.newInstance().asInstanceOf[AnyRef]
      field = clazz.getDeclaredField(vall);
      field setAccessible true
      cache.put(actualClassName, (refObj, field));
    }
    rivet = field.get(refObj).asInstanceOf[Rivet]
    tdl.SlotDefs.add(rivet);
    rivet
  }

  def download(url: String)(implicit tdl: MinderTdl) = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream();

    //first check the cache for the url.
    val cacheKey = url.replaceAll("\\/|\\.|\\:|\\-", "_")
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

  def under(repo: String)(implicit tdl: MinderTdl): Array[Byte] = {
    //TODO: caching mechanism

    //check if the repo is a zip file
    //TODO: support jar archives too

    if (repo.endsWith(".zip")) {
      extractFromZip(repo, vall)
    } else {
      download(repo + "/" + vall)
    }
  }

  /**
   * Searches and extracts the entry with the given name from the acrhive given in zip.
   * @param repo the remote repo that the zip will be downloaded from
   * @param entry the entry name that will be searched in the zip
   * @return
   * the byte array that conatins the zip entry if found
   * @throws IllegalArgumentException if the zip does not contain the given entry
   */
  def extractFromZip(repo: String, entry: String)(implicit tdl: MinderTdl): Array[Byte] = {

    val zip = download(repo);

    val zisTream = new ZipInputStream(new ByteArrayInputStream(zip))

    var zipEntry: ZipEntry = null

    var baos: ByteArrayOutputStream = null

    zipEntry = zisTream.getNextEntry

    var found = false
    while (zipEntry != null) {
      if (zipEntry.getName == vall) {
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
      throw new IllegalArgumentException(repo + "/" + vall + " was not found")

    baos.toByteArray
  }
}

case class MinderInt(in: Int) {
  def onto(out: Int) = ParameterPipe((in - 1), (out - 1))
}

case class MinderAny(dst: AnyRef) {
  def onto(destination: Int) = {
    val p = ParameterPipe(-1, destination - 1);
    p.using((a: AnyRef) => dst);
    p
  }
}
