package mtdl

import java.io._
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

import scala.io.Source

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TdlCompiler {
  private val lock = new Object

  val MINDERTDL_PACKAGE_NAME = "minderTdl"

  val SCALA_COMPILER = System.getProperty("SCALA_COMPILER", "scalac")

  def compileTdl(userEmail: String, tcName: String, tdlStr: String): Class[MinderTdl] = {
    val uMail = userEmail.replaceAll("(@|\\.|\\-)", "_")
    //val regex = "TestCase\\s*=\\s*((\"[a-zA-Z_][a-zA-Z_0-9]*\")|([a-zA-Z_][a-zA-Z_0-9]*))".r

    //val classArray = (regex findAllIn tdlStr).toArray

    //if (classArray.length != 1)
    //  throw new IllegalArgumentException("The tdl definition must declare a TestCaseName")

    //val nameDeclaration = classArray(0).replaceAll("=|\"", "").trim;
    //val wordArray = nameDeclaration.split("\\s+")
    //if (wordArray.length != 2)
    //  throw new IllegalArgumentException("The tdl definition is invalid [" + classArray(0) + "]")

    //val tcName = wordArray(1);
    val tcPackage = MINDERTDL_PACKAGE_NAME + "." + uMail

    //now at this point, check the hash of the tdl and make sure that we are not recomping over and over

    if (checkHash(tcPackage + "." + tcName, tdlStr)) {
      println("Class not changed, load directly")
      TdlClassLoader.loadClass(tcPackage + "." + tcName).asInstanceOf[Class[MinderTdl]]
    } else {
      val srcDir = new File("tdlsrc");
      srcDir.mkdirs()
      new File("tdlcls").mkdirs();

      lock.synchronized {
        val pw = new PrintWriter(new FileOutputStream("tdlsrc/" + tcName + ".scala"))

        try {
          pw.println("package " + tcPackage);
          pw.println()
          pw.println("import  mtdl._")
          pw.println()
          pw.println("class " + tcName + "(override val variableWrapperMapping: Map[String,String], run: Boolean) extends MinderTdl(variableWrapperMapping, run){")
          pw.println("ThisPackage = \"" + tcPackage + "\"")
          pw.println("AuthorMail = \"" + uMail + "\"")
          pw.println("TestCase = \"" + tcName + "\"")
          pw.println(tdlStr)
          pw.println("}")
        }
        finally {
          pw.close()
        }


        val process = Runtime.getRuntime.exec(SCALA_COMPILER + " -d ../tdlcls/ -language:postfixOps -feature -classpath ../target/scala-2.11/classes/" + File.pathSeparatorChar + "mtdl.jar" + File.pathSeparatorChar + "../mtdl.jar " + tcName + ".scala", null, srcDir)
        process.waitFor()

        val out = Source.fromInputStream(process.getInputStream).mkString
        val err = Source.fromInputStream(process.getErrorStream).mkString

        if (out != null && out.trim.length != 0)
          println(out)
        if (err != null && err.trim.length != 0)
          System.err.println(err)

        if (err != null && err.length > 0) {
          throw new IllegalArgumentException(err);
        }

        updateHash(tcPackage + "." + tcName, tdlStr)
        TdlClassLoader.loadClass(tcPackage + "." + tcName).asInstanceOf[Class[MinderTdl]]
      }
    }
  }

  def checkHash(fullName: String, tdl: String): Boolean = {
    val cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(tdl.getBytes("utf8"));
    val hash = DatatypeConverter.printHexBinary(cript.digest())

    //check the hash from clases
    val file = new File("tdlcls/" + fullName)
    if (file.exists()) {
      val origHash = Source.fromFile(file).mkString
      if (origHash.equals(hash))
        true
      else
        false
    } else
      false

  }

  def updateHash(fullName: String, tdl: String) {
    val cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(tdl.getBytes("utf8"));
    val hash = DatatypeConverter.printHexBinary(cript.digest())

    val pw = new PrintWriter(new FileWriter("tdlcls/" + fullName))
    pw.print(hash)
    pw.close()
  }

  def compileTdl(uMail: String, file: File): Class[MinderTdl] = {
    compileTdl(uMail, {
      var fn = file.getName
      if (fn.contains('/')){
        fn = fn.substring(fn.lastIndexOf('/')+1)
      }
      if (fn.contains('.')){
        fn = fn.substring(0, fn.indexOf('.'))
      }
      fn
    }, Source.fromFile(file).mkString)
  }

  def getSignatures(tdl: String, wrapperName: String): Unit = {

  }
}
