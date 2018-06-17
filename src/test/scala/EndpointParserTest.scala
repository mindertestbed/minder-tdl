import mtdl._
import org.specs2.mutable._
import scala.collection.JavaConversions._
/**
  * Created by yerlibilgin on 28/11/14.
  */
class EndpointParserTest extends Specification {

  sequential

  "TdlParser" should {
    "parse" in {

      val (list, newSource) = EndpointParser.detectEndPointIdentifiers("");

      list.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)
      true
    }

    "parse2" in {

      val (list, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |[GET]
          |waitForEP
          |(identifier1, epPacket => {
          |})
        """.stripMargin);

      list.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)
      true
    }

    "parse3" in {
      val (list, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |[GET,56] waitForEP(identifier1, epPacket => {
          |})
        """.stripMargin);

      list.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)
      true
    }
    "parse4" in {
      val (list, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |[POST]
          |waitForEP(identifier1, epPacket => {
          |})
        """.stripMargin);

      list.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)
      true
    }
    "parse6" in {
      val (set, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |[POST,10000]
          |waitForEP(identifier1, eppacket => {
          |  info("doing something nasty");
          |  info("processing endpoint");
          |})
          |
          |runasrivet(()=>{
          |  info("doing something nasty");
          |})
          |
          |[GET]
          |waitForEP(identifier2, eppacket => {
          |  info("doing something nasty2");
          |  info("processing endpoint2");
          |})
          |
        """.stripMargin);

      set.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)

      set.size must_== 2

      val iterator = set.iterator()

      iterator.next() must_== "POST:identifier1"
      iterator.next() must_== "GET:identifier2"
    }

    "parse7" in {
      val (set, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |
          |
          |waitForEP(identifier1, epPacket => {
          |  INFO("Doing something nasty");
          |  INFO("Processing endpoint");
          |})
          |
          |runAsRivet(()=>{
          |  INFO("Doing something nasty");
          |})
          |
          |
          |waitForEP(identifier2, epPacket => {
          |  INFO("Doing something nasty2");
          |  INFO("Processing endpoint2");
          |})
          |
        """.stripMargin);

      set.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)

      set.size must_== 2

      val iterator = set.iterator()

      iterator.next() must_== "GET:identifier1"
      iterator.next() must_== "GET:identifier2"
    }
    "fail1" in {
      val (set, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |
          |[PUT]
          |waitForEP(identifier1, epPacket => {
          |  INFO("Doing something nasty");
          |  INFO("Processing endpoint");
          |})
          |
          |runAsRivet(()=>{
          |  INFO("Doing something nasty");
          |})
          |
          |[DELETE]
          |waitForEP(identifier2, epPacket => {
          |  INFO("Doing something nasty2");
          |  INFO("Processing endpoint2");
          |})
          |
        """.stripMargin);

      set.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)

      set.size must_== 2

      val iterator = set.iterator()

      iterator.next() must_== "PUT:identifier1"
      iterator.next() must_== "DELETE:identifier2"
    }
    "fail2" in {
      val (set, newSource) = EndpointParser.detectEndPointIdentifiers(
        """
          |
          |[GET,9]
          |waitForEP(identifier1, epPacket => {
          |  INFO("Doing something nasty");
          |  INFO("Processing endpoint");
          |})
          |
        """.stripMargin);

      set.foreach(identifier => {
        println(s"identifier: $identifier")
      })

      println("New source")
      println(newSource)

      set.size must_== 1

      val iterator = set.iterator()

      iterator.next() must_== "GET:identifier1"
    }
  }
}
