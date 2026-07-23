/*************************************************************************/
/*  GooglePlayBillingUtils.java                                                    */
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/* Copyright (c) 2007-2020 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2020 Godot Engine contributors (cf. AUTHORS.md).   */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */

package org.godotengine.godot.plugin.googleplaybilling.utils;

import androidx.annotation.NonNull;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

import org.godotengine.godot.Dictionary;

import java.util.List;

public class GooglePlayBillingUtils {
	public static Dictionary convertPurchaseToDictionary(Purchase purchase) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("original_json", purchase.getOriginalJson());
		dictionary.put("order_id", purchase.getOrderId());
		dictionary.put("package_name", purchase.getPackageName());
		dictionary.put("purchase_state", purchase.getPurchaseState());
		dictionary.put("purchase_time", purchase.getPurchaseTime());
		dictionary.put("purchase_token", purchase.getPurchaseToken());
		dictionary.put("quantity", purchase.getQuantity());
		dictionary.put("signature", purchase.getSignature());
		
		List<String> products = purchase.getProducts();
		String[] productsArray = products.toArray(new String[0]);
		dictionary.put("sku", products.get(0));
		dictionary.put("skus", productsArray);
		dictionary.put("products", productsArray);

		dictionary.put("is_acknowledged", purchase.isAcknowledged());
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing());
		return dictionary;
	}

	public static Dictionary convertProductDetailsToDictionary(ProductDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("product_id", details.getProductId());
		dictionary.put("sku", details.getProductId()); // Maintain compatibility with older plugin versions
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		dictionary.put("type", details.getProductType());

		if (details.getOneTimePurchaseOfferDetails() != null) {
			ProductDetails.OneTimePurchaseOfferDetails offer = details.getOneTimePurchaseOfferDetails();
			dictionary.put("price", offer.getFormattedPrice());
			dictionary.put("price_currency_code", offer.getPriceCurrencyCode());
			dictionary.put("price_amount_micros", offer.getPriceAmountMicros());
		} else if (details.getSubscriptionOfferDetails() != null) {
			List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
			Object[] subscriptionOffers = new Object[offers.size()];

			for (int i = 0; i < offers.size(); i++) {
				ProductDetails.SubscriptionOfferDetails offer = offers.get(i);

				Dictionary offerDict = new Dictionary();
				offerDict.put("base_plan_id", offer.getBasePlanId());
				offerDict.put("offer_id", offer.getOfferId());
				offerDict.put("offer_token", offer.getOfferToken());
				offerDict.put("offer_tags", offer.getOfferTags().toArray(new String[0]));

				List<ProductDetails.PricingPhase> phases = offer.getPricingPhases().getPricingPhaseList();
				Object[] pricingPhases = new Object[phases.size()];
				for (int j = 0; j < phases.size(); j++) {
					Dictionary phaseDict = getDictionary(phases, j);
					pricingPhases[j] = phaseDict;
				}
				offerDict.put("pricing_phases", pricingPhases);
				subscriptionOffers[i] = offerDict;
			}
			dictionary.put("subscription_offer_details", subscriptionOffers);

			// Compatibility for simple cases (first base plan)
			if (!offers.isEmpty()) {
				ProductDetails.SubscriptionOfferDetails firstOffer = offers.get(0);
				if (!firstOffer.getPricingPhases().getPricingPhaseList().isEmpty()) {
					ProductDetails.PricingPhase firstPhase = firstOffer.getPricingPhases().getPricingPhaseList().get(0);
					dictionary.put("price", firstPhase.getFormattedPrice());
					dictionary.put("price_currency_code", firstPhase.getPriceCurrencyCode());
					dictionary.put("price_amount_micros", firstPhase.getPriceAmountMicros());
					dictionary.put("subscription_period", firstPhase.getBillingPeriod());
				}
			}
		}
		return dictionary;
	}

	@NonNull
	private static Dictionary getDictionary(List<ProductDetails.PricingPhase> phases, int j) {
		ProductDetails.PricingPhase phase = phases.get(j);
		Dictionary phaseDict = new Dictionary();
		phaseDict.put("formatted_price", phase.getFormattedPrice());
		phaseDict.put("price_currency_code", phase.getPriceCurrencyCode());
		phaseDict.put("price_amount_micros", phase.getPriceAmountMicros());
		phaseDict.put("billing_period", phase.getBillingPeriod());
		phaseDict.put("billing_cycle_count", phase.getBillingCycleCount());
		phaseDict.put("recurrence_mode", phase.getRecurrenceMode());
		return phaseDict;
	}

	public static Object[] convertPurchaseListToDictionaryObjectArray(List<Purchase> purchases) {
		Object[] purchaseDictionaries = new Object[purchases.size()];

		for (int i = 0; i < purchases.size(); i++) {
			purchaseDictionaries[i] = GooglePlayBillingUtils.convertPurchaseToDictionary(purchases.get(i));
		}

		return purchaseDictionaries;
	}

	public static Object[] convertProductDetailsListToDictionaryObjectArray(List<ProductDetails> productDetails) {
		Object[] productDetailsDictionaries = new Object[productDetails.size()];

		for (int i = 0; i < productDetails.size(); i++) {
			productDetailsDictionaries[i] = GooglePlayBillingUtils.convertProductDetailsToDictionary(productDetails.get(i));
		}

		return productDetailsDictionaries;
	}
}
