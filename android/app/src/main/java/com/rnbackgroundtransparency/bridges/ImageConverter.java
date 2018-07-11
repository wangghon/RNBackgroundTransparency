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
import com.facebook.react.bridge.ReadableArray;
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
    public void convertImage(String imageURI, final ReadableArray colorMask, final Promise promise) {

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
                    double[] colorMasking = ResolveReadableArray(colorMask);
                    bitmap = TransparencyBitmapBGByMask(bitmap, colorMasking);

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

            private Bitmap TransparencyBitmapBGByMask (Bitmap bitmap, double[] colorMask) {
                Bitmap decoded = bitmap.copy(Bitmap.Config.ARGB_8888 , true);
                decoded.setHasAlpha(true);

                for(int x = 0; x < decoded.getWidth(); x++) {

                    for(int y = 0; y < decoded.getHeight(); y++) {

                        if (ShouldBeTransparent(decoded.getPixel(x, y), colorMask)) {
                            decoded.setPixel(x, y,Color.TRANSPARENT);
                        }
                    }
                }
                return decoded;
            }

            private boolean ShouldBeTransparent(int c1, double[] colorMask) {
                return Color.red(c1) > colorMask[0]
                        && Color.red(c1) <= colorMask[1]
                        && Color.green(c1) > colorMask[2]
                        && Color.green(c1) <= colorMask[3]
                        && Color.blue(c1) > colorMask[4]
                        && Color.blue(c1) <= colorMask[5];
            }

            private double[] ResolveReadableArray(ReadableArray jsArray) {
                assert(jsArray.size() == 6); //[minRed, maxRed, minGreen, maxGreen, minBlue, maxBlue]

                double[] colorMask = new double[6];
                for(int i = 0; i < jsArray.size(); i++) {
                    colorMask[i] = jsArray.getDouble(i);
                }
                return colorMask;
            }


         }
        try {
            new ImageAsyncTask().execute(imageURI);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


}
