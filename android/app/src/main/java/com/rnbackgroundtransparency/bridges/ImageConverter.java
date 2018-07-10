package com.rnbackgroundtransparency.bridges;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageConverter extends ReactContextBaseJavaModule {

    private ReactApplicationContext mContext;

    public ImageConverter(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }

    @Override
    public String getName() {
        return "ImageConverter";
    }


    private void sendEvent(String eventName, WritableMap params) {
        mContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void convertImage(String imageURI, final Promise promise) {

         class ImageAsyncTask extends AsyncTask<String, String, String> {
            @Override
            protected String doInBackground(String... URL) {

                String imageURL = URL[0];
                String imageBase64 = null;
                try {
                    // Download Image from URL
                    InputStream input = new java.net.URL(imageURL).openStream();

                    //Decode bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    bitmap = TransparencyBitmapBG(bitmap);

                    //convert to base64 string
                    imageBase64 = BitMapToString(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject(e);
                }
                return imageBase64;
            }

             @Override
             protected void onProgressUpdate(String... progress) {

                 WritableMap params = Arguments.createMap();;
                 params.putString("image", progress[0]);

                 sendEvent("onImageConvert", params);
             }

            @Override
            protected void onPostExecute(String result)
            {
                promise.resolve(result);
            }

            private String BitMapToString(Bitmap bitmap) {

                ByteArrayOutputStream finalOut = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, finalOut);
                //convert to base64 string
                byte[] imageBytes = finalOut.toByteArray();

                return Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }

            private Bitmap TransparencyBitmapBG (Bitmap bitmap) {
                Bitmap decoded = bitmap.copy(Bitmap.Config.ARGB_8888 , true);
                decoded.setHasAlpha(true);

                int white = 0xffffffff;

                for(int x = 0; x < decoded.getWidth(); x++) {

                    for(int y = 0; y < decoded.getHeight(); y++) {

                        //if(ColorDistance(decoded.getPixel(x, y), white) < threshold) {
                        if (ShouldBeTransparent(decoded.getPixel(x, y))) {
                            decoded.setPixel(x, y,Color.TRANSPARENT);
                        }
                    }

//                    if ( x % 100 == 0 ) {
//
//                        String temp = BitMapToString(decoded);
//                        publishProgress(temp);
//                    }
                }
                return decoded;
            }

            private double ColorDistance(int c1, int c2) {
                return Math.sqrt(Math.pow(Color.red(c1) - Color.red(c2), 2) + Math.pow(Color.green(c1) - Color.green(c2), 2) + Math.pow(Color.blue(c1) - Color.blue(c2), 2)); // + Math.pow(c1.alpha - c2.alpha, 2));
            }

            private boolean ShouldBeTransparent(int c1) {
                int[] color = {240, 255};
                return Color.red(c1) > color[0]
                        && Color.red(c1) <= color[1]
                        && Color.green(c1) > color[0]
                        && Color.green(c1) <= color[1]
                        && Color.blue(c1) > color[0]
                        && Color.blue(c1) <= color[1];
            }


         }
        try {
            new ImageAsyncTask().execute(imageURI);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


}
