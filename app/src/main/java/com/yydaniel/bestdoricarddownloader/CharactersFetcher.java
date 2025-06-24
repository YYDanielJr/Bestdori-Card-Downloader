package com.yydaniel.bestdoricarddownloader;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CharactersFetcher {
    static final String charactersUrl = "https://bestdori.com/api/characters/main.2.json";
    private static StringBuilder fetchJson(URL url) throws IOException {
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

    public static JSONObject getCharacters(Context context) {
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, "characters.json");
        JSONObject ret = null;
        String fileContent = null;
        if(file.exists()) {
            try {
                Path path = Paths.get(file.getPath());
                fileContent = new String(Files.readAllBytes(path));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                if(fileContent != null) {
                    ret = new JSONObject(fileContent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            return ret;
        } else {
            try {
                URL url = new URL(charactersUrl);
                StringBuilder characters = fetchJson(url);
                // 将文件写入characters.json
                if(characters != null) {
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(characters.toString());
                        fileWriter.flush();
                    }
                    ret = new JSONObject(characters.toString());
                    return ret;
                }
                return null;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
