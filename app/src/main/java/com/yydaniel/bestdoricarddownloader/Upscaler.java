package com.yydaniel.bestdoricarddownloader;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Upscaler {
    private final String originalCommand = "realsr-ncnn -i %s -o %s -m models-Real-ESRGAN-anime";
    private String command;
    private Process process;
    private int commandRunner(Context context, String command) {
        String dir = context.getCacheDir().getAbsolutePath();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh");
            processBuilder.redirectErrorStream(true); // 合并标准输出和标准错误
            Process process = processBuilder.start();

            OutputStream stdin = process.getOutputStream();
            BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String fullCommand = "cd " + dir +              // 切到二进制目录
                    "; chmod 755 *ncnn" +                     // 赋权（最小权限）
                    "; export LD_LIBRARY_PATH=" + dir +         // 设置库路径
                    "\n./" + command +                                // 执行命令
                    "\nexit\n";                                       // 退出shell

            stdin.write(fullCommand.getBytes());
            stdin.flush();
            stdin.close();

            int exitCode = process.waitFor();
            return (exitCode == 0) ? 0 : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Upscaler(Context context, String input, String output) {
        File bin = new File(context.getCacheDir(), "realsr-ncnn");
        // command = String.format(originalCommand, bin.getAbsolutePath(), input, output);
        command = String.format(originalCommand, input, output);
        File file1 = new File("upscale_cache");
        if(!file1.exists()) {
            file1.mkdirs();
        }
        File file2 = new File("upscale_dir_cache");
        if(!file2.exists()) {
            file2.mkdirs();
        }
    }

    public void setExecutablePermission(String filePath) throws IOException, InterruptedException {
        // 通过 chmod 命令设置权限
        Process process = Runtime.getRuntime().exec("chmod 755 " + filePath);
        process.waitFor();
    }

    public void copyBinaryFromAssets(Context context, String assetName, String destPath) throws IOException {
        InputStream is = context.getAssets().open(assetName);
        FileOutputStream fos = new FileOutputStream(destPath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fos.close();
        is.close();
    }

    public static void setExecutable(File file) {
        if (file.exists()) {
            file.setExecutable(true);  // 设置可执行权限
            file.setReadable(true);    // 同时确保可读
        }
    }

    public int upscale(Context context) {
        String assetName = "realsr-ncnn";
        File target = new File(context.getCacheDir(), assetName);
        if(!target.exists()) {
            try {
                AssetUtils.copyAssetsToDir(context, "", context.getCacheDir()); // 空字符串表示 assets 根目录
                if(target.exists()) {
                    AssetUtils.setExecutable(target);
                }
            } catch (IOException e) {
                Log.e("AssetCopy", "Error copying assets", e);
            }
        }

        File filesDir = context.getCacheDir();
        File upscaleCacheDir = new File(filesDir, "upscale_cache");
        if (!upscaleCacheDir.exists()) {
            upscaleCacheDir.mkdirs();
        }
        File cacheDir = new File(filesDir, "cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        return commandRunner(context, command);
    }
}
