package mtdl

import java.io._

import scala.io.Source

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TdlCompiler {
  private val lock = new Object

  val MINDERTDL_PACKAGE_NAME = "minterTdl"

  def compileTdl(userEmail: String, tdlStr: String): Class[MinderTdl] = {
    val uMail = userEmail.replaceAll("(@|\\.)", "_")
    val regex = "TestCase\\s*=\\s*((\"[a-zA-Z_][a-zA-Z_0-9]*\")|([a-zA-Z_][a-zA-Z_0-9]*))".r

    val classArray = (regex findAllIn tdlStr).toArray

    if (classArray.length != 1)
      throw new IllegalArgumentException("The tdl definition must declare a TestCaseName")

    val nameDeclaration = classArray(0).replaceAll("=|\"", "").trim;
    val wordArray = nameDeclaration.split("\\s+")
    if (wordArray.length != 2)
      throw new IllegalArgumentException("The tdl definition is invalid [" + classArray(0) + "]")

    val tcName = wordArray(1);
    val tcPackage = MINDERTDL_PACKAGE_NAME + "." + uMail

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
        pw.println("class " + tcName + " extends MinderTdl{")
        pw.println("thisPackage = \"" + tcPackage + "\"")
        pw.println(tdlStr)
        pw.println("}")
      }
      finally {
        pw.close()
      }
      val process = Runtime.getRuntime.exec("scalac -d ../tdlcls/ -language:postfixOps -feature -classpath ../target/scala-2.11/classes/:../target/scala-2.11/minder-tdl_2.11-0.0.1.jar:../minder-tdl_2.11-0.0.1.jar:minder-tdl_2.11-0.0.1.jar " + wordArray(1) + ".scala", null, srcDir)
      process.waitFor()

      val out = Source.fromInputStream(process.getInputStream).mkString
      val err = Source.fromInputStream(process.getErrorStream).mkString

      if (out != null && out.trim.length != 0)
        println(out)
      if (err != null && err.trim.length != 0)
        System.err.println(err)

      TdlClassLoader.loadClass(tcPackage + "." + tcName).asInstanceOf[Class[MinderTdl]]
    }
  }

  def compileTdl(uMail: String, file: File): Class[MinderTdl] = {
    compileTdl(uMail, Source.fromFile(file).mkString)
  }

  /**
   * Runs the given test case
   * Created by yerlibilgin on 07/12/14.
   *
   * @param sessionId the user if of the TS that is running the test
   * @param tdl The test definition
   */
  def runTest(sessionId: String, tdl: MinderTdl): Unit = {
  }
}
