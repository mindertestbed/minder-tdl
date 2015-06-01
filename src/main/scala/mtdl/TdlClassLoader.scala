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
    val allDependencyJars =  DependencyService.getInstance().allResolvedDependencies;

    var dependecyBufferList = new ListBuffer[URL]()
    dependecyBufferList += dir.toURI.toURL

    for( currentJar <- allDependencyJars){
       println( "currentJar: " + currentJar );
      dependecyBufferList  += str2URL(currentJar)
    }

    //allDependencyJars.toArray(new Array[URL](0))
    val cl = new Ldr(dependecyBufferList.toArray)
    cl.loadClass(name)
  }

  class Ldr(urls: Array[URL]) extends URLClassLoader(urls: Array[URL]) {
    override def loadClass(name: String): Class[_] = {
      if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
        super.loadClass(name)
      } else {
        TdlClassLoader.getClass.getClassLoader.loadClass(name)
      }
    }

    override def loadClass(name: String, resolve: Boolean): Class[_] = {
      if (name.startsWith(TdlCompiler.MINDERTDL_PACKAGE_NAME)) {
        super.loadClass(name, resolve)
      } else {
        TdlClassLoader.getClass.getClassLoader.loadClass(name)
      }
    }
  }

}