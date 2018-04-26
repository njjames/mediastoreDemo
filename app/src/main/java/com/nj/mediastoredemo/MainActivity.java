package com.nj.mediastoredemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isUpdate;

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static String filename = "1.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_load).setOnClickListener(this);
        findViewById(R.id.btn_loadandupdate1).setOnClickListener(this);
        findViewById(R.id.btn_update2).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_broadcastupdate_old).setOnClickListener(this);
        findViewById(R.id.btn_broadcastupdate_new).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_load:
                isUpdate = false;
                new BimapTask().execute("http://192.168.16.122:8080/pic/1.jpg");
                break;
            case R.id.btn_loadandupdate1:
                isUpdate = true;
                new BimapTask().execute("http://192.168.16.122:8080/pic/1.jpg");
                break;
            case R.id.btn_update2:
                updateIamage();
                break;
            case R.id.btn_delete:
                deleteImage();
                break;
            case R.id.btn_broadcastupdate_old:
                broadcastUpdateOld();
                break;
            case R.id.btn_broadcastupdate_new:
                broadcastUpdateNew();
                break;
        }
    }

    private void broadcastUpdateNew() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(path, filename)));
        sendBroadcast(intent);
        Toast.makeText(MainActivity.this, "新的更新广播发送了", Toast.LENGTH_SHORT).show();
    }

    private void broadcastUpdateOld() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
        intent.setData(Uri.fromFile(new File(path, filename)));
        sendBroadcast(intent);
        Toast.makeText(MainActivity.this, "旧的更新广播发送了", Toast.LENGTH_SHORT).show();
    }

    private void updateIamage() {
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), path + "/" + filename, "this is title2", "update by method2");
            Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deleteImage() {
        File file = new File(path, filename);
        if (file.exists()) {
            file.delete();
            Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    }

    class BimapTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadBitmap(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (!isUpdate) {
                try {
                    File file = new File(path, filename);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
                    Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else {
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "this is title", "update by method1");
                Toast.makeText(MainActivity.this, "下载并更新成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap downloadBitmap(String string) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(string).openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                return BitmapFactory.decodeStream(conn.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
