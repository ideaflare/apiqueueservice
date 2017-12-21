package apigateway.actors

import akka.actor.{Actor, ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, SECONDS}

case object Loop

class APIGatewayPulseActor(apiGatewayBatchingActor: ActorRef, actorSystem: ActorSystem) extends Actor {
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  override def receive: Receive = {
    case Loop =>
      apiGatewayBatchingActor ! FlushAllQueues
      actorSystem.scheduler.scheduleOnce(FiniteDuration(5, SECONDS), self, Loop)
  }
}
