package mtdl

import java.io._
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter
import dependencyutils.{DependencyClassLoaderCache, DependencyService}

import scala.io.Source

/**
 * Created by yerlibilgin on 07/12/14.
 */
object TdlCompiler {
  private val lock = new Object
  val MINDERTDL_PACKAGE_NAME = "minderTdl"

  val SCALA_COMPILER = System.getProperty("SCALA_COMPILER", "scalac")

  /**
   * Compile the provided TDL source code and return a java class for it.
   * @param assetPath
   * The path that the corresponding MTDL script will resolve the assets from.
   * @param packageInfo
   * the package that the MTDL will be compiled into.
   *
   * @param dependencyString
   * The list of the maven dependencies that will be included in the compilation and class resolution process
   * @param className
   * The name of the class (corresponds to the test case in Minder)
   * @param source
   * The MTDL script to be compiled.
   * @return
   * The Class<MinderTdl> instance.
   */
  def compileTdl(assetPath: String, packageInfo: String, dependencyString: String, className: String, source: String, version: String): Class[MinderTdl] = {
    val packagePath = MINDERTDL_PACKAGE_NAME + "/" + packageInfo;
    val packageName = packagePath.replaceAll("/", ".")

    val packageNameSplit = packageName.split("\\.");
    val groupId = packageNameSplit(1);
    println("CompileTdl.groupId", groupId)
    //resolution is here

    println("Dependency String: [" + dependencyString + "]")
    val dependencyClassLoader = if (dependencyString != null && dependencyString.length != 0) DependencyClassLoaderCache.getDependencyClassLoader(dependencyString)
    else null

    //now at this point, check the hash of the tdl and make sure that we are not recomping over and over

    //check the hash to prevent unnecessary recompilation
    if (checkHash(packagePath + "/" + className, source)) {
      println("Class not changed, load directly")
      TDLClassLoaderProvider.loadClass(packageName + "." + className, dependencyClassLoader).asInstanceOf[Class[MinderTdl]]
    } else {
      val srcDir = new File("tdlsrc");
      srcDir.mkdirs()
      new File("tdlcls").mkdirs();

      lock.synchronized {
        val pw = new PrintWriter(new FileOutputStream("tdlsrc/" + className + ".scala"))

        try {
          pw.println("package " + packageName);
          pw.println()
          pw.println("import  mtdl._")

          //import the root package of this packageInfo

          if (packageInfo.indexOf('/') > 0) {
            pw.println("import  " + MINDERTDL_PACKAGE_NAME + "." + packageInfo.substring(0, packageInfo.indexOf('/')) + "._")
          }
          pw.println()

          pw.println("class " + className + "(override val variableWrapperMapping: scala.collection.mutable.Map[String,String], run: Boolean) extends MinderTdl(variableWrapperMapping, run){")
          pw.println("ThisPackage = \"" + packageName + "\"")
          pw.println("AssetPath = \"" + assetPath + "\"")
          pw.println("Version = \"" + version + "\"")

          pw.println(source)
          pw.println("}")
        }
        finally {
          pw.close()
        }


        val depedencyClasspath =
          if (dependencyClassLoader != null) {
            dependencyClassLoader.getClassPathString + File.pathSeparator
          } else {
            ""
          }

        val executeString: String =
          SCALA_COMPILER + " -d ../tdlcls/ -language:postfixOps -feature -classpath " + depedencyClasspath +
            "../target/scala-2.11/classes/" +
            File.pathSeparatorChar + "./tdlcls/" +
            File.pathSeparatorChar + "../tdlcls/" +
            File.pathSeparatorChar + "mtdl.jar" +
            File.pathSeparatorChar + "../mtdl.jar " +
            className + ".scala"

        println(executeString);
        val process = Runtime.getRuntime.exec(executeString, null, srcDir)
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

        updateHash(packagePath + "/" + className, source)

        TDLClassLoaderProvider.loadClass(packageName + "." + className, dependencyClassLoader).asInstanceOf[Class[MinderTdl]]
      }
    }
  }

  def compileUtil(assetPath: String, packageInfo: String, dependencyString: String, className: String, source: String): Unit = {
    val packagePath = MINDERTDL_PACKAGE_NAME + "/" + packageInfo;
    val packageName = packagePath.replaceAll("/", ".")

    println("PackagePath " + packagePath);
    println("Package Name " + packageName)

    println("Dependency String: [" + dependencyString + "]")
    val dependencyClassLoader = if (dependencyString != null && dependencyString.length != 0) DependencyClassLoaderCache.getDependencyClassLoader(dependencyString)
    else null

    //now at this point, check the hash of the tdl and make sure that we are not recomping over and over

    if (!checkHash(packagePath + "/" + className, source)) {
      val srcDir = new File("tdlsrc");
      srcDir.mkdirs()
      new File("tdlcls").mkdirs();

      lock.synchronized {
        val fullFileName: String = packageName + "." + className + ".scala"
        val pw = new PrintWriter(new FileOutputStream("tdlsrc/" + fullFileName))

        try {
          pw.println("package " + packageName);
          pw.println()
          pw.println("import  mtdl._")
          pw.println()
          pw.println("object " + className + " extends mtdl.Utils{")
          pw.println("  AssetPath = \"" + assetPath + "\"")
          pw.println(source)
          pw.println("}")
        }
        finally {
          pw.close()
        }


        val depedencyClasspath =
          if (dependencyClassLoader != null) {
            dependencyClassLoader.getClassPathString + File.pathSeparator
          } else {
            ""
          }

        val process = Runtime.getRuntime.exec(SCALA_COMPILER + " -d ../tdlcls/ -language:postfixOps -feature -classpath " + depedencyClasspath + "../target/scala-2.11/classes/" + File.pathSeparatorChar + "./tdlcls/" + File.pathSeparatorChar + "../tdlcls/" + File.pathSeparatorChar + "mtdl.jar" + File.pathSeparatorChar + "../mtdl.jar " + fullFileName, null, srcDir)
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

        updateHash(packagePath + "/" + className, source)
      }
    }
  }

  def checkHash(fullName: String, tdl: String): Boolean = {
    val cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(tdl.getBytes("utf8"));
    val hash = DatatypeConverter.printHexBinary(cript.digest())

    //check the hash from clases
    val file = new File("tdlcls/" + fullName + ".hash")
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

    val pw = new PrintWriter(new FileWriter("tdlcls/" + fullName + ".hash"))
    pw.print(hash)
    pw.close()
  }
}

