package com.rnbackgroundtransparency.bridges;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.ByteArrayInputStream;
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

    @ReactMethod
    public void convertImage(String imageURI, final Integer threshold, final Promise promise) {

         class ImageAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... URL) {

                String imageURL = URL[0];
                String imageBase64 = null;
                try {
                    // Download Image from URL
                    InputStream input = new java.net.URL(imageURL).openStream();

                    //Decode bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(input);

                    //
                    bitmap = TransparencyBitmapBG(bitmap, threshold);

                    //convert to base64 string
                    imageBase64 = BitMapToString(bitmap);
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

            private String BitMapToString(Bitmap bitmap) {

                ByteArrayOutputStream finalOut = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, finalOut);
                //convert to base64 string
                byte[] imageBytes = finalOut.toByteArray();

                return Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }

            private Bitmap TransparencyBitmapBG (Bitmap bitmap, Integer threshold) {
                Bitmap decoded = bitmap.copy(Bitmap.Config.ARGB_8888 , true);
                decoded.setHasAlpha(true);

                int white = 0xffffffff;

                for(int x=0;x< 1024 /*decoded.getWidth()*/;x++){
                    for(int y=0;y< 768 /*decoded.getHeight()*/;y++){

                        if(ColorDistance(decoded.getPixel(x, y), white) < threshold)
                        {
                            decoded.setPixel(x, y,Color.TRANSPARENT);
                        }
                    }
                }
                return decoded;
            }

            private double ColorDistance(int c1, int c2) {
                return Math.sqrt(Math.pow(Color.red(c1) - Color.red(c2), 2) + Math.pow(Color.green(c1) - Color.green(c2), 2) + Math.pow(Color.blue(c1) - Color.blue(c2), 2)); // + Math.pow(c1.alpha - c2.alpha, 2));
            }
         }
        try {
            new ImageAsyncTask().execute(imageURI);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


}
