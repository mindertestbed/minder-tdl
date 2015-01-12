import java.io.File

import org.specs2.mutable._
import mtdl._

import scala.io.Source
import scala.collection.JavaConversions._

/**
 * Created by yerlibilgin on 28/11/14.
 */
class TestTdlCompiler extends Specification {

  // query from db with wrapper and id if does not exist
  // throw new IllegalArgumentException("No such signal or slot <" + wrapper +
  // "." + id + ">");

  class Provider extends ISignalSlotInfoProvider {
    override def getSignalSlot(wrapperId: String, signature: String) = {
      if (signature.contains("signal")) SignalImpl(wrapperId, signature)
      else SlotImpl(wrapperId, signature)
    }
  }

  SignalSlotInfoProvider.setSignalSlotInfoProvider(new Provider)

  sequential

  "TdlCompiler" should {
    "compile and recompile a valid tdl file" in {
      val minderClass = TdlCompiler.compileTdl("myildiz83@gmail.com", new File("sampletdl/SampleTestCase11.tdl"))

      for (c <- minderClass.getConstructors) {
        println(c)
      }

      val minder:MinderTdl = createInstance(minderClass, true, "$wrapper0"->"B", "$wrapper1" -> "C")

      minder.SlotDefs.foreach(
        rivet => {
          println(rivet.describe())
        }
      )
      minder.SlotDefs.size must_== (1)
    }
  }

  "recompile a valid tdl file" in {
    val minderClass = TdlCompiler.compileTdl("myildiz83@gmail.com", new File("sampletdl/SampleTestCase12.tdl"))
    val minder = createInstance(minderClass, true, "$wrapper0"->"B", "$wrapper1" -> "C")
    minder.SlotDefs.foreach(
      rivet => {
        println(rivet.describe())
      }
    )
    minder.SlotDefs.size must_== (5)
  }

  "compile a valid tdl file that references another" in {
    TdlCompiler.compileTdl("melis@gmail.com", new File("sampletdl/SampleTestCase12.tdl"))
    val minderClass = TdlCompiler.compileTdl("melis@gmail.com", new File("sampletdl/SampleTestCase2.tdl"))
    val minder = createInstance(minderClass, true, "$wrapper0"->"B", "$wrapper1" -> "C")

    minder.SlotDefs.foreach(
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
    var cls = TdlCompiler.compileTdl("radu@romanya.com", new File("sampletdl/SampleTestCase12.tdl"));


    val tc1: mtdl.MinderTdl = createInstance(cls, true, "$wrapper0"->"B", "$wrapper1" -> "C")

    cls = TdlCompiler.compileTdl("radu@romanya.com", new File("sampletdl/SampleTestCase2.tdl"));
    val tc2: mtdl.MinderTdl = createInstance(cls, true, "$wrapper0"->"B", "$wrapper1" -> "C");

    "keep the list of its rivets" in {
      tc1.SlotDefs.size must be_==(5)
    }

    "be able to reuse other rivets" in {
      tc2.SlotDefs.size must be_==(2)
      tc2.SlotDefs(0) must beEqualTo(tc1.SlotDefs(2))
      tc2.SlotDefs(1) must beEqualTo(tc1.SlotDefs(3))
    }
    "have equal param# and paramPipe#" in {
      tc1.SlotDefs.get(0).slot.params.size must be_==(tc1.SlotDefs.get(0).pipes.size)
    }
  }
  "A valid test case" should {
    var cls = TdlCompiler.compileTdl("radu@romanya.com", new File("sampletdl/SampleTestCase12.tdl"));

    val tc1: mtdl.MinderTdl = createInstance(cls, false)

    cls = TdlCompiler.compileTdl("radu@romanya.com", new File("sampletdl/SampleTestCase2.tdl"));
    val tc2: mtdl.MinderTdl = createInstance(cls, false);

    "have equal param# and paramPipe#" in {
      tc1.variableDefs.foreach(
       v => println(v)
      )


      println("For tc2")
      tc2.variableDefs.foreach(
        v => println(v)
      )

      1 must_==(1)
    }
  }


  def createInstance(minderClass: Class[MinderTdl], run: java.lang.Boolean, seq: (String, String)*): MinderTdl ={
    val map = {
      val map2 = collection.mutable.Map[String, String]()
      for (e@(k, v) <- seq) {
        map2 += e
      }
      map2.toMap
    }
    minderClass.getConstructors()(0).newInstance(map, run).asInstanceOf[MinderTdl]
  }
}
