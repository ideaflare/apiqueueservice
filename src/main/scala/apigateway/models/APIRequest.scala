package apigateway.models

import org.json4s._

case class APIResponse(request: APIRequest, response: JObject)

sealed trait APIRequest {
  val queries: List[String]
}
case class ProductAPIRequest(queries: List[String]) extends APIRequest
case class OrderAPIRequest(queries: List[String]) extends APIRequest
case class PricingAPIRequest(queries: List[String]) extends APIRequest