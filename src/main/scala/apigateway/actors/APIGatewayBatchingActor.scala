package apigateway.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}
import apigateway.models._
import akka.pattern.ask
import akka.util.Timeout
import akka.pattern.pipe

import scala.collection.mutable
import scala.concurrent.ExecutionContext

case class AddToQueue(request: APIRequest)
case object FlushAllQueues

case class SenderRequest[A<: APIRequest](apiRequest: A, sender: ActorRef)

class APIGatewayBatchingActor(apiGatewayActor: ActorRef) extends Actor {
  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val ec: ExecutionContext = APIGatewayActorSystem.system.dispatcher

  val productAPIQueue: mutable.Queue[SenderRequest[ProductAPIRequest]] = mutable.Queue.empty[SenderRequest[ProductAPIRequest]]
  val orderAPIQueue: mutable.Queue[SenderRequest[OrderAPIRequest]] = mutable.Queue.empty[SenderRequest[OrderAPIRequest]]
  val pricingAPIQueue: mutable.Queue[SenderRequest[PricingAPIRequest]] = mutable.Queue.empty[SenderRequest[PricingAPIRequest]]

  def flushQueue[A<: APIRequest](queue: mutable.Queue[SenderRequest[A]]): Unit = {
    queue.dequeueAll(_ => true).foreach { senderRequest =>
      val response = (apiGatewayActor ? senderRequest.apiRequest).mapTo[APIResponse]
      response pipeTo senderRequest.sender
    }
  }

  override def receive: Receive = {
    case AddToQueue(req) =>
      val currentSender = sender()
      req match {
        case p: ProductAPIRequest => productAPIQueue.enqueue(SenderRequest(p, currentSender))
        case o: OrderAPIRequest => orderAPIQueue.enqueue(SenderRequest(o, currentSender))
        case p: PricingAPIRequest => pricingAPIQueue.enqueue(SenderRequest(p, currentSender))
      }

      if (productAPIQueue.size == 5) {
        flushQueue(productAPIQueue)
      }
      if (orderAPIQueue.size == 5) {
        flushQueue(orderAPIQueue)
      }
      if (pricingAPIQueue.size == 5) {
        flushQueue(pricingAPIQueue)
      }

    case FlushAllQueues =>
      flushQueue(productAPIQueue)
      flushQueue(orderAPIQueue)
      flushQueue(pricingAPIQueue)
  }
}
