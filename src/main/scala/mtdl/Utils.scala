package mtdl

import java.io._
import java.net.URL
import java.security.MessageDigest
import java.util.Properties
import java.util.zip.{GZIPInputStream, GZIPOutputStream, ZipEntry, ZipInputStream}
import javax.net.ssl.HttpsURLConnection
import javax.xml.bind.DatatypeConverter
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, Transformer, TransformerFactory}
import javax.xml.xpath.{XPath, XPathConstants, XPathExpressionException, XPathFactory}

import org.slf4j.LoggerFactory
import org.w3c.dom._


object Utils{
  val MINDER_REPORT_LOGGER_NAME = "mtdl.report.logger"
}

/**
  * Created by yerlibilgin on 18/05/15.
  */
class Utils {

  val REPORT_LOGGER = LoggerFactory.getLogger(Utils.MINDER_REPORT_LOGGER_NAME)

  /**
    * Added after the task: 187 : Migrate the ASSETS from the user to the groups
    */
  var AssetPath: String = ""
  var ThisPackage: String = ""
  var Version: String = ""

  /**
    *
    * SINCE TASK:  #189 Add Argument (parameter) support to Scripts
    *
    * This collection holds parameters for the script when
    * the script is going to run.
    */
  val parameters = new Properties

  def getParameter(key: String): String = parameters.getProperty(key)

  def addParameter(key: String, value: String) = parameters.put(key, value)

  def setParams(parms: String): Unit = {
    parameters.clear()
    parameters.load(new ByteArrayInputStream(parms.getBytes()))
  }


  def DEBUG(any: Any): Unit = {
    REPORT_LOGGER.debug("{}", any)
  }

  def DEBUG(any: Any, throwable: Throwable): Unit = {
    REPORT_LOGGER.debug("{} {}", any, throwable)
  }

  def WARN(any: Any): Unit = {
    REPORT_LOGGER.warn("{}", any)
  }

  def WARN(any: Any, throwable: Throwable): Unit = {
    REPORT_LOGGER.warn("{} {}", any, throwable)
  }

  def INFO(any: Any): Unit = {
    REPORT_LOGGER.info("{}", any)
  }

  def INFO(any: Any, throwable: Throwable): Unit = {
    REPORT_LOGGER.info("{} {}", any, throwable)
  }

  def ERROR(any: Any): Unit = {
    REPORT_LOGGER.error("{}", any)
  }

  def ERROR(any: Any, throwable: Throwable): Unit = {
    REPORT_LOGGER.error("{} {}", any, throwable)
  }


  var dlCache = new File("dlcache")
  dlCache.mkdirs()

  def getAsset(asset: String) = new AssetProvider(AssetPath + "/" + asset)

  def getHash(fullUrl: String): String = {
    val cript = MessageDigest.getInstance("SHA-1")
    cript.reset();
    cript.update(fullUrl.getBytes("utf8"))
    val hash = DatatypeConverter.printHexBinary(cript.digest())

    hash.toString
  }

  def download(url: String) = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()

    //CREATE DOWNLOAD PATH
    var downloadLocation: String = dlCache.getAbsolutePath

    //Remove "http://" from the url and split acc. to the /
    var splittedUrl: Array[String] = null
    if (url.startsWith("https"))
      splittedUrl = url.substring(8).split("/")
    else
      splittedUrl = url.substring(7).split("/")

    //If there is port, split that too
    if (splittedUrl(0).contains(":")) {
      val address: Array[String] = splittedUrl(0).split(":")
      var addressWthPunct = address(0).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator + addressWthPunct
      var tmp = new File(downloadLocation)
      tmp.mkdirs()
      println(downloadLocation)

      addressWthPunct = address(1).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator + addressWthPunct
      tmp = new File(downloadLocation)
      tmp.mkdirs()
      println(downloadLocation)

    } else {
      var addressWthPunct = splittedUrl(0).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator + addressWthPunct
      var tmp = new File(downloadLocation)
      tmp.mkdirs()
    }

    var arraySize: Int = splittedUrl.length
    for (i <- 1 until arraySize - 1) {
      var addressWthPunct = splittedUrl(i).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator + splittedUrl(i)
      println(downloadLocation)
      var tmp = new File(downloadLocation)
      tmp.mkdirs()
    }

    var fileName = splittedUrl(arraySize - 1).replaceAll("\\p{Punct}", "_")
    println(fileName)


    val cacheKeyHash = getHash(fileName)
    val fl = new File(downloadLocation, cacheKeyHash)
    if (fl.exists()) {
      var len: Int = 0
      val fis = new FileInputStream(fl)
      len = fis.read(buffer)
      while (len > 0) {
        stream.write(buffer, 0, len)
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
    *
    * @param httpsURL
    * @param stream
    */
  def downloadHttps(httpsURL: String, stream: OutputStream) = {
    val myurl = new URL(httpsURL)
    val con = myurl.openConnection().asInstanceOf[HttpsURLConnection]
    val ins = con.getInputStream()
    val buffer: Array[Byte] = Array.ofDim(2048)

    var len = ins.read(buffer)
    while (len > 0) {
      stream.write(buffer, 0, len)
      len = ins.read(buffer)
    }
    ins.close();
  }

  /**
    * Download from a regular server
    *
    * @param url
    * @param stream
    * @return
    */
  def downloadHttp(url: String, stream: OutputStream) = {
    import scala.io.Source
    val html = Source.fromURL(url)
    val s = html.mkString
    //println(s)

    stream.write(s.getBytes())
  }


  val buffer: Array[Byte] = Array.ofDim(2048)

  /**
    * Searches and extracts the entry with the given name from the acrhive given in zip.
    *
    * @param repo  the remote repo that the zip will be downloaded from
    * @param entry the entry name that will be searched in the zip
    * @return
    * the byte array that conatins the zip entry if found
    * @throws IllegalArgumentException if the zip does not contain the given entry
    */
  def extractFromZip(repo: String, entry: String): Array[Byte] = {

    val zip = download(repo)

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
          baos.write(buffer, 0, len)
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

  //utility functions

  def bA2Hex(array: Array[Byte]): String = {
    javax.xml.bind.DatatypeConverter.printHexBinary(array)
  }


  def hex2bA(hex: String): Array[Byte] = {
    javax.xml.bind.DatatypeConverter.parseHexBinary(hex)
  }

  def sha256(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("SHA-256")
    md.digest(array)
  }

  def sha128(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("SHA-1")
    md.digest(array)
  }


  def compareArray(array1: Array[Byte], array2: Array[Byte]): Boolean = {
    if (array1 == null && array2 == null) {
      true
    } else if (array1 == null) {
      false
    } else if (array2 == null) {
      false
    } else {
      if (array1.length != array2.length)
        false
      else {
        try {
          for (i <- 0 until array1.length) {
            if (array1(i) != array2(i))
              throw new RuntimeException()
          }
          true
        } catch {
          case _: Throwable =>
            false
        }
      }
    }
  }

  /**
    * compress the given byte array
    *
    * @param plain
    * @return
    */
  def gzip(plain: Array[Byte]): Array[Byte] = {
    val bais: ByteArrayInputStream = new ByteArrayInputStream(plain)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    gzip(bais, baos)
    baos.toByteArray
  }

  def gunzip(compressed: Array[Byte]): Array[Byte] = {
    val bais: ByteArrayInputStream = new ByteArrayInputStream(compressed)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    gunzip(bais, baos)
    baos.toByteArray
  }

  /**
    * Compress the given stream as GZIP
    *
    * @param inputStream
    * @param outputStream
    */
  def gzip(inputStream: InputStream, outputStream: OutputStream) {
    try {
      val gzipOutputStream: GZIPOutputStream = new GZIPOutputStream(outputStream, true)
      transferData(inputStream, gzipOutputStream)
      gzipOutputStream.close
    }
    catch {
      case e: Exception => {
        throw new RuntimeException("GZIP Compression failed")
      }
    }
  }

  /**
    * Decompress the given stream that contains gzip data
    *
    * @param inputStream
    * @param outputStream
    */
  def gunzip(inputStream: InputStream, outputStream: OutputStream) {
    try {
      val gzipInputStream: GZIPInputStream = new GZIPInputStream(inputStream)
      transferData(gzipInputStream, outputStream)
      gzipInputStream.close
    }
    catch {
      case e: Exception => {
        throw new RuntimeException("GZIP decompression failed")
      }
    }
  }

  @throws(classOf[Exception])
  def transferData(inputStream: InputStream, outputStream: OutputStream) {
    val chunk: Array[Byte] = new Array[Byte](1024)
    var read: Int = -1
    while ((({
      read = inputStream.read(chunk, 0, chunk.length);
      read
    })) > 0) {
      outputStream.write(chunk, 0, read)
    }
  }

  def prettyPrint(node: Node, indentAmount: Int = 2): String = {
    var transformer: Transformer = null
    try {
      transformer = TransformerFactory.newInstance.newTransformer
      transformer.setOutputProperty(OutputKeys.INDENT, "yes")
      transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes")
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indentAmount)
      val result: StreamResult = new StreamResult(new StringWriter)
      val source: DOMSource = new DOMSource(node)
      transformer.transform(source, result)
      val xmlString: String = result.getWriter.toString
      xmlString
    }
    catch {
      case e: Exception =>
        e.printStackTrace
        return ""
    }
  }

  val factory = DocumentBuilderFactory.newInstance
  factory.setNamespaceAware(true)
  val documentBuilder = factory.newDocumentBuilder()

  def parseXml(xml: String): Document = {
    documentBuilder.parse(new ByteArrayInputStream(xml.getBytes))
  }

  def parseXmlByteArray(xml: Array[Byte]): Document = {
    documentBuilder.parse(new ByteArrayInputStream(xml))
  }

  private val xPath: XPath = {
    val xPath: XPath = XPathFactory.newInstance.newXPath
    xPath.setNamespaceContext(new NamespaceContext {
      def getNamespaceURI(prefix: String) = "*"

      def getPrefix(namespace: String) = null

      def getPrefixes(namespace: String) = null
    })
    xPath
  }

  def xpathQuerySingleNode(node: Node, xpath: String): Node = {
    try {
      val o: AnyRef = xPath.evaluate(xpath, node, XPathConstants.NODE)
      if (o == null) throw new RuntimeException("No match for [" + xpath + "]")
      o.asInstanceOf[Node]
    }
    catch {
      case e: XPathExpressionException => {
        throw new RuntimeException(e)
      }
    }
  }

  def xpathQueryList(node: Node, xpath: String): List[Node] = {
    try {
      val o: AnyRef = xPath.evaluate(xpath, node, XPathConstants.NODESET)
      if (o == null) throw new RuntimeException("No match for [" + xpath + "]")
      val list: NodeList = o.asInstanceOf[NodeList]

      val els = new scala.collection.mutable.MutableList[Node]
      var i = 0
      while (i < list.getLength) {
        els += (o.asInstanceOf[NodeList]).item(i)
        i += 1;
      }

      els.toList
    } catch {
      case e: XPathExpressionException => {
        throw new RuntimeException(e)
      }
    }
  }


  def createProperties(tuples: Tuple2[String, String]*): Properties = {
    val properties = new Properties
    for (tuple <- tuples) {
      properties.put(tuple._1, tuple._2)
    }

    properties
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
    p.select = (a: Any) => in
    p
  }

  def -->(out: Int) = onto(out)
}

/**
  * Used in cases where a parameter value will be ready only
  * during the execution of the test
  *
  * @param vall
  */
case class invokeLater(vall: () => Any) {
  def onto(out: Int) = {
    val p = ParameterPipe(-1, out - 1)
    //whatever happens, return the value.
    p.select = (a: Any) => {
      vall()
    }
    p
  }

  def -->(out: Int) = onto(out)
}

/**
  * Used to provide additional methods to Any.
  *
  * @param src
  */
case class MinderAny(src: Any) {
  /**
    * When a value is mapped onto <code>out</code>, then
    * it is returned in the default converter function.
    *
    * @param out
    * @return
    */
  def onto(out: Int) = {
    val p = ParameterPipe(-1, out - 1);
    //whatever happens, return the value.
    p.select = (a: Any) => src
    p
  }

  def -->(out: Int) = onto(out)

}

class MinderNull {
  /**
    * When a value is mapped onto <code>out</code>, then
    * it is returned in the default converter function.
    *
    * @param out
    * @return
    */
  def onto(out: Int) = {
    val p = ParameterPipe(-1, out - 1)
    //whatever happens, return null.
    p.select = (a: Any) => null;
    p
  }

  def -->(out: Int) = onto(out)
}

