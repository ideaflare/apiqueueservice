package apigateway.actors

import akka.actor.{ActorRef, ActorSystem, Props}

object APIGatewayActorSystem {
  val system = ActorSystem("APIGatewayActorSystem")
  val apiGatewayActor: ActorRef = system.actorOf(Props(new APIGatewayActor()), "apiGatewayActor")
  val apiGatewayBatchingActor: ActorRef = system.actorOf(Props(new APIGatewayBatchingActor(apiGatewayActor)), "apiGatewayBatchingActor")
  val apiGatewayPulseActor: ActorRef = system.actorOf(Props(new APIGatewayPulseActor(apiGatewayBatchingActor, system)), "apiGatewayPulseActor")
}