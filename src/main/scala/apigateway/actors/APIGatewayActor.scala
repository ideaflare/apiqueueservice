package apigateway.actors

import akka.actor.Actor
import apigateway.models._
import org.json4s.JsonAST.{JObject, JString}

class APIGatewayActor extends Actor {
  override def receive: Receive = {
    case p: ProductAPIRequest =>
      val response = APIResponse(p, JObject(("productAPIResponse", JString("OK"))))
      sender ! response

    case o: OrderAPIRequest =>
      val response = APIResponse(o, JObject(("orderAPIResponse", JString("OK"))))
      sender ! response

    case p: PricingAPIRequest =>
      val response = APIResponse(p, JObject(("pricingAPIResponse", JString("OK"))))
      sender ! response
  }
}