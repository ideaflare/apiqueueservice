package apigateway

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import apigateway.actors._
import apigateway.models._
import org.json4s.JsonAST.{JObject, JString}
import org.scalatest._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class APIGatewayManagerSpec extends TestKit(ActorSystem("apiGatewayActorSystem"))
  with WordSpecLike with Matchers with BeforeAndAfterAll {


  implicit val ec: ExecutionContext = system.dispatcher

  val apiGatewayActorMock: ActorRef = system.actorOf(Props(new APIGatewayActor()), "apiGatewayActor")
  val apiGatewayBatchingActorMock: ActorRef = system.actorOf(Props(new APIGatewayBatchingActor(apiGatewayActorMock)), "apiGatewayBatchingActor")
  val apiGatewayPulseActorMock: ActorRef = system.actorOf(Props(new APIGatewayPulseActor(apiGatewayBatchingActorMock, system)))

  val pricingAPIRequest = PricingAPIRequest(List("NL", "CN"))
  val orderAPIRequest = OrderAPIRequest(List("109347263", "123456891"))
  val productAPIRequest = ProductAPIRequest(List("109347263", "123456891"))

  def apiResponse(req: APIRequest, expectedResponse: String) = APIResponse(req, JObject((expectedResponse, JString("OK"))))
  val apiGatewayManager = APIGatewayManager(apiGatewayActorMock, apiGatewayBatchingActorMock, apiGatewayPulseActorMock)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "The APIGatewayManager" must {
    "process all requests asynchronously" in {
      val requests: List[APIRequest] = List(
        pricingAPIRequest,
        orderAPIRequest,
        productAPIRequest
      )

      val expectedResponse = List(apiResponse(pricingAPIRequest, "pricingAPIResponse"),
        apiResponse(orderAPIRequest, "orderAPIResponse"),
        apiResponse(productAPIRequest, "productAPIResponse")
      )

      val eventualResponse = apiGatewayManager.processRequests(requests)
      val response = Await.result(eventualResponse, Duration(10, TimeUnit.SECONDS))
      expectedResponse.foreach { e =>
        response.contains(e) shouldEqual true
      }
    }

    "process in batches of 5" in {
      val requests: List[APIRequest] = List(
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest
      )

      val requests2: List[APIRequest] = List(
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest
      )

      val eventualResponses = requests.map { r =>
        apiGatewayManager.processRequests5AtATime(r)
      }

      val eventualResponses2 = requests2.map { r =>
        apiGatewayManager.processRequests5AtATime(r)
      }

      val response1 = Await.result(Future.sequence(eventualResponses), Duration(10, TimeUnit.SECONDS))
      response1.size shouldEqual 6
      val response2 = Await.result(Future.sequence(eventualResponses2), Duration(10, TimeUnit.SECONDS))
      response2.size shouldEqual 4
    }

    "flush all queues every 5 seconds" in {
      val requests: List[APIRequest] = List(
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest,
        pricingAPIRequest
      )

      val eventualResponses = requests.map { r =>
        apiGatewayManager.processRequests5AtATime(r)
      }

      apiGatewayManager.processEvery5SecondsOrRequests

      val response = Await.result(Future.sequence(eventualResponses), Duration(10, TimeUnit.SECONDS))
      response.size shouldEqual 4
    }
  }
}
