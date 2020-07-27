package com.reactlibrary;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;

import java.io.UnsupportedEncodingException;
import android.util.Base64;
import com.ipay.IPayIH;
import com.ipay.IPayIHPayment;
import com.ipay.IPayIHR;
import com.ipay.IPayIHResultDelegate;
import com.ipay.constants.ConnectAddress;

import java.io.Serializable;

public class Ipay88Module extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;

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
        reactContext.startActivity(checkoutIntent);
    }

    public String decodeBase64(String text) {
        String result = "";
        byte[] data = Base64.decode(text, Base64.DEFAULT);
        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    static public class ResultDelegate implements IPayIHResultDelegate, Serializable {
        public void onPaymentSucceeded(String transId, String refNo, String amount, String remarks, String authCode) {

            Log.d("JASON", "OK");
            WritableMap params = Arguments.createMap();
            params.putString("transactionId", transId);
            params.putString("referenceNo", refNo);
            params.putString("amount", amount);
            params.putString("remark", remarks);
            params.putString("authorizationCode", authCode);
            sendEvent(reactContext, "ipay88:success", params);
            reactContext = null;
        }

        public void onPaymentFailed(String transId, String refNo, String amount, String remarks, String err) {

            Log.d("JASON", "NOK : " + err);
            WritableMap params = Arguments.createMap();
            params.putString("transactionId", transId);
            params.putString("referenceNo", refNo);
            params.putString("amount", amount);
            params.putString("remark", remarks);
            params.putString("error", err);
            sendEvent(reactContext, "ipay88:failed", params);
            reactContext = null;
        }

        public void onPaymentCanceled(String transId, String refNo, String amount, String remarks, String errDesc) {

            Log.d("JASON", "CANCEL");
            WritableMap params = Arguments.createMap();
            params.putString("transactionId", transId);
            params.putString("referenceNo", refNo);
            params.putString("amount", amount);
            params.putString("remark", remarks);
            params.putString("error", errDesc);
            sendEvent(reactContext, "ipay88:canceled", params);
            reactContext = null;
        }

        public void onRequeryResult(String merchantCode, String refNo, String amount, String result) {
            // No need to implement
        }

        @Override
        public void onConnectionError(String merchantCode, String merchantKey,
                                    String RefNo, String Amount, String Remark, String lang, String country) {
            
            WritableMap params = Arguments.createMap();
            sendEvent(reactContext, "ipay88:canceled", params);
            reactContext = null;
        }
    }

    static void sendEvent(ReactContext reactContext, String eventName, WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

}
