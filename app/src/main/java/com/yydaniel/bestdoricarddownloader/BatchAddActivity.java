package com.yydaniel.bestdoricarddownloader;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class BatchAddActivity extends AppCompatActivity {
    private StringBuilder fetchJson(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求参数
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应流
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // 关闭资源
            reader.close();
            inputStream.close();

            return response;
        }
        return null;
    }

    private void afterFetching(StringBuilder jsonCards, StringBuilder jsonCharacters) {
        try {
            JSONObject cards = new JSONObject(jsonCards.toString());
            JSONObject characters = new JSONObject(jsonCharacters.toString());

            int cardAmount = cards.length();
            int characterAmount = characters.length();
            Toast.makeText(BatchAddActivity.this, "卡片个数：" + cardAmount + "，成员个数：" + characterAmount, Toast.LENGTH_LONG).show();

            LinearLayout parent = findViewById(R.id.linearLayout_batch_main);
            TextView viewAbove = findViewById(R.id.tv_characters);
            TextView viewBelow = findViewById(R.id.tv_stars);

//            for(int i = 0; i < characterAmount; i++) {
//                JSONObject
//                CheckBox newCheckBox = new CheckBox(this);
//                newCheckBox.setText();
//            }

        } catch (JSONException e) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_batch_add_loading);
        String apiUrl = "https://bestdori.com/api/cards/all.5.json";
        String charactersUrl = "https://bestdori.com/api/characters/main.2.json";

        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                StringBuilder cards = fetchJson(url);

                url = new URL(charactersUrl);
                StringBuilder characters = fetchJson(url);

                if(cards != null && characters != null) {
                    runOnUiThread(() -> {
                        setContentView(R.layout.activity_batch_add);
                        afterFetching(cards, characters);
                    });
                } else {
                    finish();
                }
            } catch(Exception e) {
                e.printStackTrace();
                finish();
            }
        }).start();
    }
}