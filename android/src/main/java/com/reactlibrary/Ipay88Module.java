package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.ipay.IPayIH;
import com.ipay.IPayIHPayment;
import com.ipay.IPayIHResultDelegate;

import java.io.Serializable;

public class Ipay88Module extends ReactContextBaseJavaModule {

    private static final int PAY_REQUEST_ID = 1000;
    private static ReactApplicationContext reactContext;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == PAY_REQUEST_ID) {
                if (resultCode == Activity.RESULT_OK) {
                    sendEventAndRemoveContext("ipay88:success", null);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    sendEventAndRemoveContext("ipay88:canceled", null);
                } else {
                    sendEventAndRemoveContext("ipay88:failed", null);
                }
            }
        }
    };

    public Ipay88Module(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Ipay88";
    }

    @ReactMethod
    public void pay(ReadableMap data) {
        reactContext = getReactApplicationContext();

        // Precreate payment
        IPayIHPayment payment = new IPayIHPayment();
        payment.setMerchantKey(data.getString("merchantKey"));
        payment.setMerchantCode(data.getString("merchantCode"));
        payment.setPaymentId(data.getString("paymentId"));
        payment.setCurrency(data.getString("currency"));
        payment.setRefNo(data.getString("referenceNo"));
        payment.setAmount(data.getString("amount"));
        payment.setProdDesc(data.getString("productDescription"));
        payment.setUserName(data.getString("userName"));
        payment.setUserEmail(data.getString("userEmail"));
        payment.setUserContact(data.getString("userContact"));
        payment.setRemark(data.getString("remark"));
        payment.setLang(data.getString("utfLang"));
        payment.setCountry(data.getString("country"));
        payment.setBackendPostURL(data.getString("backendUrl"));

        Intent checkoutIntent = IPayIH.getInstance().checkout(payment, reactContext, new ResultDelegate(), IPayIH.PAY_METHOD_CREDIT_CARD);
        reactContext.addActivityEventListener(mActivityEventListener);
        reactContext.startActivityForResult(checkoutIntent, PAY_REQUEST_ID, null);
    }

    static public class ResultDelegate implements IPayIHResultDelegate, Serializable {
        public void onPaymentSucceeded(String transId, String refNo, String amount, String remarks, String authCode) {

            WritableMap params = Arguments.createMap();
            params.putString("transactionId", transId);
            params.putString("referenceNo", refNo);
            params.putString("amount", amount);
            params.putString("remark", remarks);
            params.putString("authorizationCode", authCode);
            sendEventAndRemoveContext("ipay88:success", params);
        }

        public void onPaymentFailed(String transId, String refNo, String amount, String remarks, String err) {

            WritableMap params = Arguments.createMap();
            params.putString("transactionId", transId);
            params.putString("referenceNo", refNo);
            params.putString("amount", amount);
            params.putString("remark", remarks);
            params.putString("error", err);
            sendEventAndRemoveContext("ipay88:failed", params);
        }

        public void onPaymentCanceled(String transId, String refNo, String amount, String remarks, String errDesc) {

            WritableMap params = Arguments.createMap();
            params.putString("transactionId", transId);
            params.putString("referenceNo", refNo);
            params.putString("amount", amount);
            params.putString("remark", remarks);
            params.putString("error", errDesc);
            sendEventAndRemoveContext("ipay88:canceled", params);
        }

        public void onRequeryResult(String merchantCode, String refNo, String amount, String result) {
            // No need to implement
        }

        @Override
        public void onConnectionError(String merchantCode, String merchantKey,
                                      String RefNo, String Amount, String Remark, String lang, String country) {

            WritableMap params = Arguments.createMap();
            sendEventAndRemoveContext("ipay88:canceled", params);
        }
    }

    private static void sendEventAndRemoveContext(String eventName, WritableMap params) {
        if (reactContext != null) {
            sendEvent(reactContext, eventName, params);
            reactContext = null;
        }
    }

    static void sendEvent(ReactContext reactContext, String eventName, WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

}
