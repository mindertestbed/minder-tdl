package mtdl

import java.io._
import java.net.URL
import java.util.Properties
import java.util.zip.{ZipEntry, ZipInputStream}
import javax.net.ssl.HttpsURLConnection

/**
 * Created by yerlibilgin on 18/05/15.
 */
class Utils {
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

  def getParameter(key: String, default: String = ""): String = parameters.getProperty(key, default)

  def addParameter(key: String, value: String) = parameters.put(key, value);

  def setParams(parms: String): Unit = {
    parameters.clear();
    parameters.load(new ByteArrayInputStream(parms.getBytes()));
  }

  var dlCache = new File("dlcache");
  dlCache.mkdirs()

  def getAsset(asset: String) = new AssetProvider(AssetPath + "/" + asset)

  def download(url: String) = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream();

    //first check the cache for the url.
    val cacheKey = url.replaceAll("\\p{Punct}", "_")
    val fl = new File(dlCache.getAbsolutePath, cacheKey);
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

  //utility functions

  def bA2Hex(array: Array[Byte]): String = {
    javax.xml.bind.DatatypeConverter.printHexBinary(array)
  }


  def hex2bA(hex: String): Array[Byte] = {
    javax.xml.bind.DatatypeConverter.parseHexBinary(hex)
  }

  def sha256(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("SHA-256");
    md.digest(array)
  }

  def sha128(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("SHA-1");
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


class MinderNull{
  /**
   * When a value is mapped onto <code>out</code>, then
   * it is returned in the default converter function.
   * @param out
   * @return
   */
  def onto(out: Int) = {
    val p = ParameterPipe(-1, out - 1);
    //whatever happens, return null.
    p.select = (a: Any) => null;
    p
  }
}

