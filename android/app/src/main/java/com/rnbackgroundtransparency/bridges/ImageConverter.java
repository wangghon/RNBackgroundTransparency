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

    private String BitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, finalOut);
            //convert to base64 string
        byte[] imageBytes = finalOut.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private Bitmap Base64ToBitmap(String imageStr) {
        final byte[] decodedBytes = Base64.decode(imageStr, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        return decodedBitmap;
    }

    private Bitmap TransparencyBitmapBGByMask (Bitmap bitmap, double[] colorMask) {
        Bitmap decoded = bitmap.copy(Bitmap.Config.ARGB_8888 , true);
        decoded.setHasAlpha(true);

        int width = decoded.getWidth();
        int height = decoded.getHeight();

        int[] pixels = new int[width];
        for (int y = 0; y < height; y++) {
            decoded.getPixels(pixels, 0, width, 0, y, width, 1);

            for (int x = 0; x < width; x++) {
                // Replace the alpha channel with the r value from the bitmap.
                if (ShouldBeTransparent(pixels[x], colorMask)) {
                    pixels[x] = (pixels[x] & 0x00FFFFFF);
                }
            }
            decoded.setPixels(pixels, 0, width, 0, y, width, 1);
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

    @ReactMethod
    public void maskImage(final String originalImageStr, final ReadableArray colorMask, final Promise promise) {

         class MaskImageAsyncTask extends AsyncTask<String, String, String> {
            @Override
            protected String doInBackground(String... URL) {
                String imageBase64 = null;
                try {
                    Bitmap bitmap = Base64ToBitmap(originalImageStr);

                    double[] colorMasking = ResolveReadableArray(colorMask);
                    bitmap = TransparencyBitmapBGByMask(bitmap, colorMasking);

                    imageBase64 = BitmapToBase64(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject(e);
                }
                return imageBase64;
            }

            @Override
            protected void onPostExecute(String result)
            {
                promise.resolve(result);
            }
        }
        try {
            new MaskImageAsyncTask().execute(originalImageStr);
        } catch (Exception e) {
            promise.reject(e);
        }
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
                    imageBase64 = BitmapToBase64(bitmap);
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
        }
        try {
            new ImageAsyncTask().execute(imageURI);
        } catch (Exception e) {
            promise.reject(e);
        }
    }
}
