package cn.csfz.wxpaypoint.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyTask extends AsyncTask<String, Integer, Bitmap> {
        private String pathUrl;
        private ImageView imageView;
        public MyTask(String pathUrl, ImageView imageView){
            this.pathUrl = pathUrl;
            this.imageView = imageView;
        }


        @Override
        protected Bitmap doInBackground(String... params) {
            String path = pathUrl;
            ByteArrayOutputStream outputStream = null;
            try {
                URL url = new URL(path);
                //开启连接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(60000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    InputStream inputStream = conn.getInputStream();
                    outputStream = new ByteArrayOutputStream();
                    byte[] bt = new byte[8 * 1024];
                    int len = 0;
                    while ((len = inputStream.read(bt)) != -1) {
                        outputStream.write(bt, 0, len);
                        outputStream.flush();
                    }
                    byte[] array = outputStream.toByteArray();
                    return BitmapFactory.decodeByteArray(array, 0, array.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }