package com.saacole.checkoutkata

import scala.util.{Success, Try}

class Checkout(priceRules: Map[String, Prices]) {

  /** Scanning one item
    *
    *	Calculates the price of given, eligible SKU and returns
    * the total price for given quantity.
    * Throws an error when a sku is passed that doesn't exist. We 
    * cannot calculate a transaction if a product doesn't exist.
    */
  def scanItem(sku: String, quantity: Int): BigDecimal = priceRules get sku match {
    case Some(Prices(price 		, None			 )) => price * quantity
    case Some(Prices(unitPrice, Some(offer))) => calcSpecialPrice(unitPrice, offer, quantity)
    case None                                 => throw SKUNotFound(s"SKU $sku not found")
  }

  /** Scanning items
    *
    *	Collecting a list of prices from a list of SKUs.
    */
  def scanItems(skus: List[String]): List[BigDecimal] =
    skus
      .groupBy(x=>x)              // Maps by key to List of those matching values.
      .mapValues(x=>x.length)     // Maps the values (lists) to the length of those lists.
      .map {
        case (sku, quantity) => scanItem(sku, quantity)
      }.toList

  /** Scanning items
    *
    *	Sums a list of prices.
    */
  def calcTotal(prices: List[BigDecimal]): BigDecimal = prices.sum

  /** Calculates for Special Prices
    *
    *	Calculates the 'special' price of given, eligible SKU and returns
    * the total price for given quantity.
    *
    * A few mathematical calculations here, so left quite verbose for clarity.
    */
  def calcSpecialPrice(unitPrice: BigDecimal, offer: String, quantity: Int): BigDecimal = {
    val parsedOffer        = parseOffer(offer)

    val ineligibleQuantity = quantity % parsedOffer.eligibleQuantity
    val costForIneligible  = ineligibleQuantity * unitPrice

    val eligibleQuantity   = quantity / parsedOffer.eligibleQuantity
    val costForEligible    = eligibleQuantity * parsedOffer.price

    costForEligible + costForIneligible
  }

  /** Parses the offer string
    *
    * Assumption here that all offer strings follow the same format as example.
    * Deconstructs the offer string and returns the useful information as an Offer.
    */
  private def parseOffer(offer: String): Offer = {
    val strings = offer.split(" ")

    (Try(strings(0).toInt), Try(BigDecimal(strings(2)))) match {
      case (Success(x), Success(y)) if x > 0 => Offer(x, y)
      case _ => throw ParsingError(s"Cannot read offer '$offer'. Read documentation for correct format")
    }
  }
}
