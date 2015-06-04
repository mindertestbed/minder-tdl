package mtdl

import java.net.{URL, URLClassLoader}

import dependencyutils.DependencyService

import scala.collection.mutable.ListBuffer

import scala.collection.JavaConversions._

object TdlClassLoader {
  val dir = new java.io.File("tdlcls/");
  dir.mkdirs()

  val lst = List

  implicit def str2URL(s: String) = new URL(s)

  def loadClass(name: String): Class[_] = {
    val allDependencyJars = DependencyService.getInstance().allResolvedDependencies;

    var dependecyBufferList = new ListBuffer[URL]()
    dependecyBufferList += dir.toURI.toURL

    for (currentJar <- allDependencyJars) {
      val currentJar2 = "file://" + currentJar
      println("currentJar: " + currentJar2)
      dependecyBufferList += str2URL(currentJar2)
    }

    //allDependencyJars.toArray(new Array[URL](0))
    val cl = new Ldr(dependecyBufferList.toArray)
    cl.loadClass(name)
  }

  class Ldr(urls: Array[URL]) extends URLClassLoader(urls: Array[URL]) {
    override def loadClass(name: String): Class[_] = {
      //(name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME))
      try {
        TdlClassLoader.getClass.getClassLoader.loadClass(name)
      } catch {
        case ce: ClassNotFoundException => {
          super.loadClass(name)
        }
      }
    }

    override def loadClass(name: String, resolve: Boolean): Class[_] = {
      try {
        TdlClassLoader.getClass.getClassLoader.loadClass(name)
      } catch {
        case ce: ClassNotFoundException => {
          super.loadClass(name, resolve)
        }
      }
    }
  }

}