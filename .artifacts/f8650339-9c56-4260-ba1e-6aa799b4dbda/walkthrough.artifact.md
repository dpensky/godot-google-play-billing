# Walkthrough - Updated Google Play Billing Integration (v9.1.0)

I have refactored the plugin code to fully support the Google Play Billing Library version 9.1.0. This involved migrating from the deprecated `SkuDetails` API to the modern `ProductDetails` API.

## Changes Made

### Refactored GooglePlayBillingUtils.java
- Updated `convertPurchaseToDictionary` to use `purchase.getProducts()` (replaces `getSkus()`).
- Replaced `convertSkuDetailsToDictionary` with `convertProductDetailsToDictionary`.
- Added support for both **One-Time Products** and **Subscriptions**.
- For subscriptions, the dictionary now includes detailed information about base plans and offers in a new `subscription_offer_details` array.
- > [!TIP]
  > I maintained compatibility by keeping the `sku` key in the dictionaries, mapping it to the `productId`.

### Refactored GodotGooglePlayBilling.java
- Switched `skuDetailsCache` to `productDetailsCache` storing `ProductDetails` objects.
- Updated `querySkuDetails` to use `billingClient.queryProductDetailsAsync`.
- Updated `queryPurchases` to use the new `QueryPurchasesParams` API.
- Refactored the purchase flow (`purchaseInternal`) to use `BillingFlowParams.ProductDetailsParams`.
- > [!IMPORTANT]
  > For subscriptions, the code now automatically selects the first available offer token if one isn't specified, ensuring the purchase flow continues to work for simple cases.

### Compilation and Build
- Updated imports to match the new library structure.
- Corrected the `ProductDetailsResponseListener` callback signature to use `QueryProductDetailsResult`.

## Verification Results

### Automated Tests
- Successfully ran `./gradlew :godot-google-play-billing:assembleDebug`.
- The project compiles without errors with the new Billing Library.

### Manual Verification
- Verified that the dictionary mapping for `ProductDetails` covers all major fields needed for Godot to display product information.
