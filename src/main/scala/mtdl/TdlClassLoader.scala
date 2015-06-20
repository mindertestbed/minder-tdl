package mtdl

import java.net.{URL, URLClassLoader}
import java.util

import dependencyutils.DependencyService

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object TdlClassLoader {
  val classLoaders = new util.ArrayList[ClassLoader];

  val extraLibClassLoader = {
    DependencyService.getInstance().getClassPathString("minder:AS4Utils:1.0.3-a5", "");
    var dependecyBufferList = new ListBuffer[URL]()
    val additionalJars: util.List[String] = DependencyService.getInstance().allResolvedDependencies
    if (additionalJars != null) {
      for (currentJar <- additionalJars) {
        val currentJar2 = "file://" + currentJar
        dependecyBufferList += str2URL(currentJar2)
      }
    }
    new URLClassLoader(dependecyBufferList.toArray){
      override def loadClass(name: String, resolve: Boolean): Class[_] ={
        println("Extra Resolve " + name)
        super.loadClass(name, resolve)
      }

      override def toString() = "MTDL dependency class loader"
    }
  }

  def appendClassLoader(clzL: ClassLoader): Unit = {
    classLoaders.add(clzL)
  }

  val dir = new java.io.File("tdlcls/");
  dir.mkdirs()

  implicit def str2URL(s: String) = new URL(s)

  def loadClass(name: String): Class[_] = {
    var dependecyBufferList = new ListBuffer[URL]()
    dependecyBufferList += dir.toURI.toURL
    val cl = new Ldr(dependecyBufferList.toArray)
    cl.loadClass(name)
  }

  class Ldr(urls: Array[URL]) extends URLClassLoader(urls) {
    override def loadClass(name: String, resolve: Boolean): Class[_] = {
      if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
        println(name + " superload")
        super.loadClass(name, resolve)
      } else {
        println(name + " defaultload")

        import scala.util.control.Breaks._

        var clz: Class[_] = null
        breakable {
          for (cl <- classLoaders) {
            println("Try " + cl.getClass.getName + " for " + name)
            try {
              clz = Class.forName(name, true, cl);
            } catch {
              case th: Throwable => {}
            }
            if (clz != null) {
              println(name + " hit " + cl)
              break;
            }
          }
        }

        if (clz == null) {
          //try me one last time
          println("Probably this is a group dependency, Try dependency loader")
          clz = extraLibClassLoader.loadClass(name)
        }
        clz
      }
    }
  }

}