package com.yydaniel.bestdoricarddownloader;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.*;

public class AssetUtils {

    /**
     * 递归复制 assets 目录到内部存储
     * @param context 上下文
     * @param assetPath assets 中的相对路径（空字符串表示根目录）
     * @param targetDir 目标目录（内部存储路径）
     */
    public static void copyAssetsToDir(Context context, String assetPath, File targetDir) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] assetList = assetManager.list(assetPath);

        if (assetList == null || assetList.length == 0) {
            // 处理单个文件
            copySingleFile(assetManager, assetPath, targetDir);
        } else {
            // 处理目录
            for (String item : assetList) {
                String sourcePath = assetPath.isEmpty() ? item : assetPath + File.separator + item;
                String[] childList = assetManager.list(sourcePath);

                if (childList != null && childList.length > 0) {
                    // 是目录：递归复制
                    File newDir = new File(targetDir, item);
                    if (!newDir.exists()) newDir.mkdir();
                    copyAssetsToDir(context, sourcePath, newDir);
                } else {
                    // 是文件：直接复制
                    copySingleFile(assetManager, sourcePath, targetDir);
                }
            }
        }
    }

    private static void copySingleFile(AssetManager assetManager, String assetPath, File targetDir) throws IOException {
        try (InputStream in = assetManager.open(assetPath);
             OutputStream out = new FileOutputStream(new File(targetDir, getFilename(assetPath)))) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        }
    }

    private static String getFilename(String path) {
        return path.contains(File.separator)
                ? path.substring(path.lastIndexOf(File.separator) + 1)
                : path;
    }

    /**
     * 设置文件可执行权限（仅对二进制文件必要）
     * @param file 目标文件
     */
    public static void setExecutable(File file) {
        if (file.exists()) {
            file.setExecutable(true);  // 设置可执行权限
            file.setReadable(true);    // 同时确保可读
        }
    }
}