package apigateway

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import apigateway.models.{APIRequest, APIResponse}
import akka.pattern.ask
import akka.util.Timeout
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import java.util.concurrent.TimeUnit

import apigateway.actors.{APIGatewayActorSystem, AddToQueue, Loop}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class APIGatewayManager(apiGatewayActor: ActorRef, apiGatewayBatchingActor: ActorRef, apiGatewayPulseActor: ActorRef)  {
  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val ec: ExecutionContext = APIGatewayActorSystem.system.dispatcher

  def processRequests(apiRequests: List[APIRequest]): Future[List[APIResponse]] = {
    Future.sequence(apiRequests.map { req =>
      (apiGatewayActor ? req).mapTo[APIResponse]
    })
  }

  def processRequests5AtATime(apiRequest: APIRequest): Future[APIResponse] = {
    (apiGatewayBatchingActor ? AddToQueue(apiRequest)).mapTo[APIResponse]
  }

  def processEvery5SecondsOrRequests = apiGatewayPulseActor ! Loop
}