package com.kitlabs.broadcastreceiverdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.kitlabs.broadcastreceiverdemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
//    private var price: String? = null
//    private var currentCurrency: String? = null
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d("log","1")
        billingClientSetup()

        val btn = findViewById<Button>(R.id.submitbtn)
        btn.setOnClickListener {

            setupGooglePlayFlow()

//            val flowParams = productDetails?.let { pd ->
//                val offerToken = pd.subscriptionOfferDetails?.get(0)?.offerToken
//                val productDetailsParamsList = listOf(offerToken?.let { offerT ->
//                    BillingFlowParams.ProductDetailsParams.newBuilder()
//                        .setProductDetails(pd)
//                        .setOfferToken(offerT)
//                        .build()
//                })
//                BillingFlowParams
//                    .newBuilder()
//                    .setProductDetailsParamsList(productDetailsParamsList)
//                    .build()
//            }
//
//            flowParams?.let { it1 ->
//                billingClient?.launchBillingFlow(this@MainActivity, it1)
//            }
        }
    }

    private fun setupGooglePlayFlow() {
        try {
//            com.kitlabs.broadcastreceiverdemo_basic1
//            com-kitlabs-basic1
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("com.kitlabs.broadcastreceiverdemo_basic")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            )


            val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

            billingClient?.queryProductDetailsAsync(params){_,p1 ->

                productDetails = p1[0]

                Log.d("Product Details","${productDetails.toString()}")
//                val priceAmount = productDetails?.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.priceAmountMicros?.div(1000)
//                price = StringBuilder(priceAmount.toString()).insert(priceAmount.toString().length - 2, ".").toString()
//                currentCurrency = productDetails?.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.priceCurrencyCode

                val flowParams = productDetails?.let { pd ->
                    val offerToken = pd.subscriptionOfferDetails?.get(0)?.offerToken
                    val productDetailsParamsList = listOf(offerToken?.let { offerT ->
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(pd)
                            .setOfferToken(offerT)
                            .build()
                    })
                    BillingFlowParams
                        .newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()
                }

                flowParams?.let { it1 ->
                    billingClient?.launchBillingFlow(this@MainActivity, it1)
                }
            }

        } catch (e : Exception){
            Log.d("setup Error ","Exception in setupGooglePlayFlow() => $e")
        }

    }

    private fun billingClientSetup() {
        Log.d("log","2")
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        try{
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("Google Setup", "Connection established with BillingClient")
                    } else {
                        Log.d("Google Setup", "Billing Service Disconnected")
                    }
                }

                override fun onBillingServiceDisconnected() {
                }
            })
        }catch (e : Exception){
            Log.d("Google Setup Error", "$e")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchaseList: MutableList<Purchase>?) {
        Log.d("log","3")
        try {
            Log.d("log","4")
            when(billingResult.responseCode){
                BillingClient.BillingResponseCode.OK ->{
                    Log.d("onPurchasesUpdated","onPurchasesUpdated => OK")
                    purchaseList?.let { handlePurchases(purchaseList) }
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Log.d("onPurchasesUpdated","onPurchasesUpdated => ITEM_ALREADY_OWNED")
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.d("onPurchasesUpdated", "onPurchasesUpdated => USER_CANCELED")
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Log.d("onPurchasesUpdated", "onPurchasesUpdated => SERVICE_DISCONNECTED")
                }
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                    Log.d("onPurchasesUpdated","onPurchasesUpdated => SERVICE_UNAVAILABLE")
                }
                else -> {
                    Log.d("onPurchasesUpdated","Error while purchasing membership")
                }
            }
        }catch (e :Exception){
            Log.d("onPurchasesUpdated Error","$e")

        }
    }

    private fun handlePurchases(purchaseList: MutableList<Purchase>) {
        Log.d("log","5")
        for (purchase in purchaseList){
            Log.d("log","6")
            if (purchase.products.contains("com.kitlabs.broadcastreceiverdemo_basic") && // This check is very important otherwise same
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                Log.d("log","7")
                if (!purchase.isAcknowledged) {
                    Log.d("log","8")
                    val params = AcknowledgePurchaseParams
                        .newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    Log.d("log","9")
                    billingClient?.acknowledgePurchase(params){
                        Log.d("log","10")
                        if (it.responseCode == BillingClient.BillingResponseCode.OK){
                            Log.d("Purchase Handler","${purchase.orderId}")
                            Log.d("Purchase Handler","${purchase.originalJson}")
                            Log.d("Purchase Handler","${purchase.purchaseToken}")
                            val intent = Intent(this, MainActivity2::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }

        }
    }
}