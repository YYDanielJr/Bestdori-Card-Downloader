package com.yydaniel.bestdoricarddownloader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadActivity extends AppCompatActivity implements Upscaler.OutputListener {

    private RecycleViewLogAdapter logAdapter;
    private RecyclerView recyclerViewLog;
    private boolean autoScrollEnabled = true;

    private void showOnLog(String log) {
        runOnUiThread(() -> {
            if (logAdapter != null) {
                logAdapter.addLogItem(log);

                // 如果启用了自动滚动，则滚动到底部
                if (autoScrollEnabled) {
                    recyclerViewLog.scrollToPosition(logAdapter.getItemCount() - 1);
                }
            }
        });
    }

    // 实现回调接口方法
    @Override
    public void onNewOutput(final String line) {
        showOnLog(line + "\n");
    }

    private void clean() {
        File dir = getCacheDir();
        File cachedir = new File(dir, "cache");
        File[] files = cachedir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        File cacheUpscaleDir = new File(dir, "upscale_cache");
        File[] filesUpscale = cacheUpscaleDir.listFiles();
        if (filesUpscale != null) {
            for (File file : filesUpscale) {
                file.delete();
            }
        }
    }

    private Uri copyFileToMediaStore(Context context, File sourceFile, String fileName, String mimeType) {
        String relativePath = Environment.DIRECTORY_DCIM + "/BestdoriCardDownloader";
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        if(mimeType.contains("jpg"))
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".jpg");
        else
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri == null) return null;

        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = resolver.openOutputStream(uri)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (outputStream != null) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 标记文件操作完成
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);

            return uri;
        } catch (IOException e) {
            e.printStackTrace();
            resolver.delete(uri, null, null);
            return null;
        }
    }

    public File download(final String path, final String fileName) {
        try {
            // Step 1：构建向图片的连接
            URL url = new URL(path);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                // Step 2：创建目录
                File filesDir = this.getCacheDir();
                if (!filesDir.exists()) {
                    filesDir.mkdirs();
                }
                File cacheFile = new File(filesDir, "cache");
                if (!cacheFile.exists()) {
                    cacheFile.mkdirs();
                }
                File file = new File(cacheFile, fileName);

                // Step 3：写入私有存储
                InputStream is = con.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                IOUtils.copy(is, fos);

                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
            clean();
            return null;
        }
        return null;
    }


    // 辅助方法：获取JSON数据
    private static JSONObject fetchJson(String urlString) throws IOException, JSONException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return !response.toString().isEmpty() ? new JSONObject(response.toString()) : null;
        }
        return null;
    }

    public void fetch(CardBundle bundle, boolean isUpscaleEnabled, AtomicInteger upscaleFailCounter, AtomicInteger failCounter) {
        try {
            runOnUiThread(() -> {
                ProgressBar bar = findViewById(R.id.progressBar2);
                int currentIndex = bar.getProgress() + 1;
                int indexAmount = bar.getMax();
                String log_start = getString(R.string.tv_log_startdownloading);
                showOnLog(String.format(log_start, currentIndex, indexAmount));
            });
            // Step 1：获取卡面JSON文件
            JSONObject cardData = null;
            cardData = fetchJson(String.format("https://bestdori.com/api/cards/%d.json", bundle.getCardId()));
            if (cardData == null) return;

            // Step 2. 提取资源集名称
            String resourceSetName = cardData.optString("resourceSetName");
            if (resourceSetName.isEmpty()) {
                System.err.println("数据缺失: resourceSetName");
                return;
            }
            Log.d("DEBUG_TAG", resourceSetName);

            // Step 3. 构建图片URL
            String imageType = bundle.isTrained() ? "after_training" : "normal";
            String imageUrl = String.format(
                    "https://bestdori.com/assets/jp/characters/resourceset/%s_rip/card_%s.png",
                    resourceSetName,
                    imageType
            );

            // Step 4. 获取图片数据
            // 在download方法中，会直接由网络位置下载到私有目录。
            String fileName = bundle.cardId + "_" + imageType;
            File file = download(imageUrl, fileName + ".png");

            // Step 5：如果有超分就超分
            boolean isUpscaleSuccessful = false;
            if(isUpscaleEnabled) {
                Upscaler upscaler = new Upscaler(DownloadActivity.this, file.getPath(),  "upscale_cache/" + fileName + "_Real_ESRGAN_Anime_4x.jpg");
                if(upscaler.upscale(DownloadActivity.this, DownloadActivity.this) == 0) {
                    isUpscaleSuccessful = true;
                    String fileNameAfterUpscale = fileName + "_Real_ESRGAN_Anime_4x.jpg";
                    File filesDir = this.getCacheDir();
                    File upscaleCacheDir = new File(filesDir, "upscale_cache");
                    if (!upscaleCacheDir.exists()) {
                        upscaleCacheDir.mkdirs();
                    }
                    file = new File(upscaleCacheDir, fileNameAfterUpscale);
                }
            }

            // Step 6：将文件写入DCIM
            if(isUpscaleSuccessful) {
                copyFileToMediaStore(DownloadActivity.this, file, fileName + "_Real_ESRGAN_Anime_4x", "image/jpg");
            } else {
                copyFileToMediaStore(DownloadActivity.this, file, fileName, "image/png");
                if(isUpscaleEnabled) {
                    upscaleFailCounter.getAndIncrement();
                }
            }

            // Step 7：通知主线程更新UI
            runOnUiThread(() -> {
                ProgressBar bar = findViewById(R.id.progressBar2);
                int currentIndex = bar.getProgress() + 1;

                String log_over = getString(R.string.tv_log_downloadover);
                showOnLog(String.format(log_over, currentIndex));

                bar.setProgress(bar.getProgress() + 1);
                TextView tv_progress = findViewById(R.id.tv_progress);
                if (bar.getProgress() < bar.getMax()) {
                    tv_progress.setText(String.format("正在下载文件…… （%d/%d）", bar.getProgress(), bar.getMax()));
                } else {
                    clean();
                    bar.setVisibility(View.INVISIBLE);
                    if(failCounter.get() > 0 && upscaleFailCounter.get() == 0) {
                        tv_progress.setText(failCounter.get() + "张卡面下载失败。");
                    } else if(failCounter.get() == 0 && upscaleFailCounter.get() > 0) {
                        tv_progress.setText(upscaleFailCounter.get() + "张卡面超分失败。");
                    } else if(failCounter.get() > 0 && upscaleFailCounter.get() > 0) {
                        tv_progress.setText(upscaleFailCounter.get() + "张卡面超分失败，" + failCounter.get() + "张卡面下载失败。");
                    } else {
                        tv_progress.setText("完成。");
                    }
                    tv_progress.append("\n\n图片已保存至DCIM/BestdoriCardDownload/目录。");
                    Button btn_finish = findViewById(R.id.button_finish);
                    btn_finish.setVisibility(View.VISIBLE);
                }

            });
        } catch (JSONException e) {
            System.err.println("JSON解析错误: " + e.getMessage());
            showOnLog("JSON解析错误: " + e.getMessage() + "\n");
            failCounter.getAndIncrement();
        } catch (Exception e) {
            System.err.println("未知错误: " + e.getMessage());
            showOnLog("JSON解析错误: " + e.getMessage() + "\n");
            failCounter.getAndIncrement();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Activity入口

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_download);

        Intent intent = getIntent();
        List<CardBundle> cardList = (List<CardBundle>) intent.getSerializableExtra("CARD_LIST");
        boolean isUpscaleEnabled = intent.getBooleanExtra("UPSCALE", false);

        // 进度条
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setMax(cardList.size());
        progressBar.setProgress(0);

        // 完成按钮，在最后一张图片下载完成之前隐藏
        Button btn_finish = findViewById(R.id.button_finish);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tv_progress = findViewById(R.id.tv_progress);

        btn_finish.setVisibility(View.INVISIBLE);
        tv_progress.setText("正在下载文件……");

        // Real-ESRGAN警告，不超分则不提示
        if(!isUpscaleEnabled) {
            TextView tv_upscale = findViewById(R.id.tv_realesrgan_warning);
            tv_upscale.setVisibility(View.GONE);
        }

        // 初始化log日志框
        recyclerViewLog = findViewById(R.id.rv_log);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewLog.setLayoutManager(layoutManager);
        recyclerViewLog.setItemAnimator(null);

        logAdapter = new RecycleViewLogAdapter();
        recyclerViewLog.setAdapter(logAdapter);

        // 添加滚动监听器以控制自动滚动
        recyclerViewLog.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 如果用户手动向上滚动，则禁用自动滚动
                if (dy < 0) {
                    logAdapter.setAutoScrollEnabled(false);
                }
                // 如果用户滚动到底部，则重新启用自动滚动
                else if (!recyclerView.canScrollVertically(1)) {
                    logAdapter.setAutoScrollEnabled(true);
                }
            }
        });

        // 单线程的线程池，让每张图片下载有序进行
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger failCounter = new AtomicInteger(0);
        AtomicInteger upscaleFailCounter = new AtomicInteger(0);

        for (CardBundle bundle : cardList) {
//            fetch(cardList.get(i));
//            progressBar.setProgress(i + 1);
            final int index = counter.getAndIncrement();
            executor.execute(() -> {
                try {
                    // 获取图片的方法
                    fetch(bundle, isUpscaleEnabled, failCounter, upscaleFailCounter);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (index == cardList.size() - 1) {
                        executor.shutdown();
                    }
                }
            });
        }

    }

}