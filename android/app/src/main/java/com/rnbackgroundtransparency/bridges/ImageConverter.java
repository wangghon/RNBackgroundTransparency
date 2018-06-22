package com.rnbackgroundtransparency.bridges;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
    public void convertImage(String imageURI, final Promise promise) {

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

                    //convert to base64 string
                    imageBase64 = BitMapToString(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return imageBase64;
            }

            @Override
            protected void onPostExecute(String result)
            {
                promise.resolve(result);
            }

            private String BitMapToString(Bitmap bitmap) {
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

                //convert to png
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOutput);

                //convert to base64 string
                byte[] b = byteOutput.toByteArray();
                return Base64.encodeToString(b, Base64.DEFAULT);
            }

        }
        try {
            new ImageAsyncTask().execute(imageURI);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


}
