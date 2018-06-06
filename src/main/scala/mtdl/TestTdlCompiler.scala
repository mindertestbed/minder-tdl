package mtdl

import java.io._
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import javax.xml.bind.DatatypeConverter

/**
 * Created by yerlibilgin on 28/11/14.
 */
object TestTdlCompiler {//extends Specification {

  /*
  // query from db with adapter and id if does not exist
  // throw new IllegalArgumentException("No such signal or slot <" + adapter +
  // "." + id + ">");

  class Provider extends ISignalSlotInfoProvider {

    val signals = List("payloadSubmitted", "sendAS4Message", "sendAS4Receipt", "deliverPayload");

    //val slots = List("configurePMode", "submitPayload", "receiveAS4Message", "receiveAS4Receipt");

    override def getSignalSlot(adapterId: String, signature: String) = {

      println("SIGNATURE " + signature)

      var isSignal = false;
      signals foreach {
        str => {
          if (signature startsWith str)
            isSignal = true
          println("\n" + str)
        }
      }

      if (isSignal)
        SignalImpl(adapterId, signature)
      else if (signature.contains("signal")) SignalImpl(adapterId, signature)
      else SlotImpl(adapterId, signature)
    }
  }


  AdapterFunctionInfoProvider.setSignalSlotInfoProvider(new Provider)

  sequential

  "TdlCompiler" should {

    "compile and recompile a valid util class" in {
      TdlCompiler.compileUtil(".", "tg", "Hodo", "def hello()={println(\"hello\")}")
      1 must_== 1
    }

    "compile and recompile a valid tdl file" in {
      val minderClass = TdlCompiler.compileTdl("myildiz83@gmail.com", new File("initialdata/SampleTestCase11.tdl"))

      for (c <- minderClass.getConstructors) {
        println(c)
      }

      val minder: MinderTdl = createInstance(minderClass, true, "$adapter0" -> "B", "$adapter1" -> "C")

      minder.RivetDefs.foreach(
        rivet => {

          class Abc extends MinderTdl{
            val c = 1 --> 1
          }

          println(rivet.describe())
        }
      )
      minder.RivetDefs.size must_== (1)
    }

    "compile and recompile a valid tdl file containing NULL SLOTS" in {
      val minderClass = TdlCompiler.compileTdl("myildiz83@gmail.com", new File("initialdata/as4_23.tdl"))

      for (c <- minderClass.getConstructors) {
        println(c)
      }

      val minder: MinderTdl = createInstance(minderClass, true, "$C2" -> "Domibus2", "$C3" -> "Domibus3")

      minder.RivetDefs.foreach(
        rivet => {
          println(rivet.describe())
        }
      )
      minder.RivetDefs.size must_== (6)
    }

  }

  "Asset provider" should {
    "late execute" in {
      def hede = new Utils().getAsset("PMode_023")
      val pr: ParameterPipe = (hede onto 1)
      println("Before")
      println(new String(pr.execute().asInstanceOf[Array[Byte]]))
      1 must_== 1
    }
  }

  /*
    "recompile a valid tdl file" in {
      val minderClass = TdlCompiler.compileTdl("myildiz83@gmail.com", new File("initialdata/SampleTestCase12.tdl"))
      val minder = createInstance(minderClass, true, "$adapter0" -> "B", "$adapter1" -> "C")
      minder.RivetDefs.foreach(
        rivet => {
          println(rivet.describe())
        }
      )
      minder.RivetDefs.size must_== (5)
    }

    "compile a valid tdl file that references another" in {
      TdlCompiler.compileTdl("melis@gmail.com", new File("initialdata/SampleTestCase12.tdl"))
      val minderClass = TdlCompiler.compileTdl("melis@gmail.com", new File("initialdata/SampleTestCase2.tdl"))
      val minder = createInstance(minderClass, true, "$adapter0" -> "B", "$adapter1" -> "C")

      minder.RivetDefs.foreach(
        rivet => {
          println(rivet.describe())
        }
      )
      true
    }

    "throw an error for a valid scala code but an invalid tdl" in {
      TdlCompiler.compileTdl("myildiz83@gmail.com", "class Abc{}") must throwA[IllegalArgumentException]
    }

    "throw an error for a valid scala code but an invalid tdl containing multiple tdl definitions" in {
      TdlCompiler.compileTdl("myildiz83@gmail.com", "TestCase = \"Tc1\"\nTestCase=\"Tc2\"") must throwA[IllegalArgumentException]
    }

    "throw an error for a scala code that does not contain TestCase name" in {
      TdlCompiler.compileTdl("myildiz83@gmail.com", "println(\"Hello\")") must throwA[IllegalArgumentException]
    }


    "A valid test case" should {
      var cls = TdlCompiler.compileTdl("radu@romanya.com", new File("initialdata/SampleTestCase12.tdl"));

      val tc1: mtdl.MinderTdl = createInstance(cls, true, "$adapter0" -> "B", "$adapter1" -> "C")

      cls = TdlCompiler.compileTdl("radu@romanya.com", new File("initialdata/SampleTestCase2.tdl"));
      val tc2: mtdl.MinderTdl = createInstance(cls, true, "$adapter0" -> "B", "$adapter1" -> "C");

      "keep the list of its rivets" in {
        tc1.RivetDefs.size must be_==(5)
      }

      "be able to reuse other rivets" in {
        tc2.RivetDefs.size must be_==(2)
        //   tc2.RivetDefs(0) must beEqualTo(tc1.RivetDefs(2))
        //   tc2.RivetDefs(1) must beEqualTo(tc1.RivetDefs(3))
      }
      "have equal param# and paramPipe#" in {
        tc1.RivetDefs.get(0).slot.params.size must be_==(tc1.RivetDefs.get(0).pipes.size)
      }
    }*/


  def createInstance(minderClass: Class[MinderTdl], run: java.lang.Boolean, seq: (String, String)*): MinderTdl = {
    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- seq) {
        map2 += e
      }
      map2.toMap
    }
    minderClass.getConstructors()(0).newInstance(map, run).asInstanceOf[MinderTdl]
  }*/

  /**
   * Download from a regular server
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
  def getHash(fullUrl: String): String = {
    val cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(fullUrl.getBytes("utf8"));
    val hash = DatatypeConverter.printHexBinary(cript.digest())
    hash.toString
  }
  def main (args: Array[String]) {
    val url: String = "http://130.206.118.4:8080/cipa-smp-full-webapp/iso6523-actorid-upis%3A%3A0088%3A5798000000003/services/busdox-docid-qns%3A%3Aurn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2%3A%3AInvoice%23%23urn%3Awww.cenbii.eu%3Atransaction%3Abiicoretrdm010%3Aver1.0%3A%23urn%3Awww.peppol.eu%3Abis%3Apeppol4a%3Aver1.0%3A%3A2.0"
    //val url: String = "https://gist.githubusercontent.com/archie/7076239/raw/13e825b997bfd193f212700854e9bcc530b821b8/Hashing.scala"

    val buffer: Array[Byte] = Array.ofDim(2048)

    val stream: ByteArrayOutputStream = new ByteArrayOutputStream();
    //first check the cache for the url.

    //CREATE DOWNLOAD PATH
    var dlCache = new File("dlcache");
    dlCache.mkdirs()

    var downloadLocation: String = dlCache.getAbsolutePath

    //Remove "http://" from the url and split acc. to the /
    val splittedUrl : Array[String] =url.substring(7).split("/")

    //If there is port, split that too
    if (splittedUrl(0).contains(":")){
      val address : Array[String]=  splittedUrl(0).split(":");
      var addressWthPunct = address(0).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator+ addressWthPunct
      var tmp = new File(downloadLocation);
      tmp.mkdirs()
      println(downloadLocation)

      addressWthPunct = address(1).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator+ addressWthPunct
      tmp = new File(downloadLocation);
      tmp.mkdirs()
      println(downloadLocation)

    }else{
      var addressWthPunct = splittedUrl(0).replaceAll("\\p{Punct}", "_")
      downloadLocation =  downloadLocation + File.separator+ addressWthPunct
      var tmp = new File(downloadLocation);
      tmp.mkdirs()
    }

    var arraySize:Int = splittedUrl.length
    for( i <- 1 until arraySize-1){
      var addressWthPunct = splittedUrl(i).replaceAll("\\p{Punct}", "_")
      downloadLocation = downloadLocation + File.separator+ splittedUrl(i)
      println(downloadLocation)
      var tmp = new File(downloadLocation);
      tmp.mkdirs()
    }

    var fileName = splittedUrl(arraySize-1).replaceAll("\\p{Punct}", "_")
    println(fileName)


    val cacheKeyHash = getHash(fileName)
    val fl = new File(downloadLocation, cacheKeyHash);
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

}
