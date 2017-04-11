/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.patricklobo;

import java.util.TimeZone;
import java.util.Iterator;


import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


import com.google.zxing.BarcodeFormat;
import com.sunmi.controller.ICallback;
import com.sunmi.impl.V1Printer;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


import android.util.Log;

public class MobilePrinter extends CordovaPlugin {
    public static final String TAG = "MobilePrinter";
    public static int imageWidth=48;

    public static String platform;                            // MobilePrinter OS
    public static String uuid;                                // MobilePrinter UUID

    private static final String ANDROID_PLATFORM = "Android";
    private static final String AMAZON_PLATFORM = "amazon-fireos";
    private static final String AMAZON_DEVICE = "Amazon";

    /**
     * Constructor.
     */
    public MobilePrinter() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        MobilePrinter.uuid = getUuid();
    }


    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("getMobilePrinterInfo".equals(action)) {
            JSONObject r = new JSONObject();
            r.put("uuid", MobilePrinter.uuid);
            r.put("version", this.getOSVersion());
            r.put("platform", this.getPlatform());
            r.put("model", this.getModel());
            r.put("manufacturer", this.getManufacturer());
	        r.put("isVirtual", this.isVirtual());
            r.put("serial", this.getSerialNumber());
            callbackContext.success(r);
        }
        else if("getUnicode".equals(action)) {
            JSONObject r = new JSONObject();
            JSONArray resp = new JSONArray();
            JSONObject innerObj = args.getJSONObject(0);
            JSONArray list = innerObj.getJSONArray("post");
            System.out.println("Tamanho: " + list.length());
            for (int i = 0; i < list.length(); i++) {
                    try {
                            JSONObject obj = list.getJSONObject(i);
                            String codigo = obj.getString("codigo");
                                byte[] cod = this.qrcode(codigo);
                            for (int x = 0; x < cod.length; x++) {
                                resp.put(cod[x]);
                            }
                        } catch (JSONException e) {
                        Object aObj = list.get(i);
                    if(aObj instanceof String){
                    System.out.println("String: " + aObj);
            
                        byte[] buf = this.unicode((String)aObj);
                                for (int x = 0; x < buf.length; x++) {
                                resp.put(buf[x]);
                            }
                       
                        
                    } else {
                        resp.put(aObj);

                        
                        }
                        }
                    
                    }
                    
            r.put("text",resp);
            callbackContext.success(r);
        }
        else if("codigo".equals(action)) {
            JSONObject r = new JSONObject();
            JSONObject innerObj = args.getJSONObject(0);
            String codigo = innerObj.getString("codigo");
            byte[] qrcode = this.qrcode(codigo);
            JSONArray resp = new JSONArray();
            for (int x = 0; x < qrcode.length; x++) {
                                resp.put(qrcode[x]);
                            }
            
            r.put("codigo",resp);
            callbackContext.success(r);
        }
        else {
            return false;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    public byte[] qrcode(String qrcode){
          Bitmap btMap = BarcodeCreater.encode2dAsBitmap(qrcode,
          PrintService.imageWidth * 6,
          PrintService.imageWidth * 6, 2);
          return getImage(btMap);
    }

    public byte[] getImage(Bitmap bitmap) {

    int mWidth = bitmap.getWidth();
    int mHeight = bitmap.getHeight();
    bitmap = resizeImage(bitmap, imageWidth * 8, mHeight);
    

    byte[]  bt = PrinterLib.getBitmapData(bitmap);
bitmap.recycle();
return bt;
}

    public byte[] unicode(String string){
    try {
      byte[] bytes = string.getBytes("unicode");
      byte[] bt=new byte[bytes.length-2];

      for (int i = 2,j=0; i < bytes.length - 1; i += 2,j += 2) {
        bt[j]= (byte)(bytes[i + 1] & 0xff);
        bt[j+1] = (byte)(bytes[i] & 0xff);
      }
      return bt;
    }
    catch (Exception e) {
      byte[] bt = string.getBytes();
      return bt;
    }
  }

    /**
     * Get the OS name.
     *
     * @return
     */
    public String getPlatform() {
        String platform;
        if (isAmazonMobilePrinter()) {
            platform = AMAZON_PLATFORM;
        } else {
            platform = ANDROID_PLATFORM;
        }
        return platform;
    }


    private static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        if(width>w)
        {
          float scaleWidth = ((float) w) / width;
          float scaleHeight = ((float) h) / height+24;
          Matrix matrix = new Matrix();
          matrix.postScale(scaleWidth, scaleWidth);
          Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
          height, matrix, true);
          return resizedBitmap;
        }else{
          Bitmap resizedBitmap = Bitmap.createBitmap(w, height+24, Config.RGB_565);
          Canvas canvas = new Canvas(resizedBitmap);
          Paint paint = new Paint();
          canvas.drawColor(Color.WHITE);
          canvas.drawBitmap(bitmap, (w-width)/2, 0, paint);
          return resizedBitmap;
        }
      }

    /**
     * Get the device's Universally Unique Identifier (UUID).
     *
     * @return
     */



    public String getUuid() {
        String uuid = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return uuid;
    }

    public String getModel() {
        String model = android.os.Build.MODEL;
        return model;
    }

    public String getProductName() {
        String productname = android.os.Build.PRODUCT;
        return productname;
    }

    public String getManufacturer() {
        String manufacturer = android.os.Build.MANUFACTURER;
        return manufacturer;
    }

    public String getSerialNumber() {
        String serial = android.os.Build.SERIAL;
        return serial;
    }

    /**
     * Get the OS version.
     *
     * @return
     */
    public String getOSVersion() {
        String osversion = android.os.Build.VERSION.RELEASE;
        return osversion;
    }

    public String getSDKVersion() {
        @SuppressWarnings("deprecation")
        String sdkversion = android.os.Build.VERSION.SDK;
        return sdkversion;
    }

    public String getTimeZoneID() {
        TimeZone tz = TimeZone.getDefault();
        return (tz.getID());
    }

    /**
     * Function to check if the device is manufactured by Amazon
     *
     * @return
     */
    public boolean isAmazonMobilePrinter() {
        if (android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
            return true;
        }
        return false;
    }

    public boolean isVirtual() {
	return android.os.Build.FINGERPRINT.contains("generic") ||
	    android.os.Build.PRODUCT.contains("sdk");
    }

}
