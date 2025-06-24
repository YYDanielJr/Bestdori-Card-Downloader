package com.yydaniel.bestdoricarddownloader;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecycleViewLogAdapter extends RecyclerView.Adapter<RecycleViewLogAdapter.LogViewHolder> {
    private static final int MAX_LOG_ITEMS = 1000;
    private final List<String> logItems = new LinkedList<>();
    private boolean autoScrollEnabled = true;

    public void addLogItem(String log) {
        logItems.add(log);
        // 如果超过最大行数，移除最旧的日志
        if (logItems.size() > MAX_LOG_ITEMS) {
            int itemsToRemove = logItems.size() - MAX_LOG_ITEMS;
            logItems.subList(0, itemsToRemove).clear();
            notifyDataSetChanged();
        } else {
            notifyItemInserted(logItems.size() - 1);
        }
    }

    public void clearLogs() {
        logItems.clear();
        notifyDataSetChanged();
    }

    public void setAutoScrollEnabled(boolean enabled) {
        autoScrollEnabled = enabled;
    }

    public boolean isAutoScrollEnabled() {
        return autoScrollEnabled;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        String logItem = logItems.get(position);
        holder.tvLogItem.setText(logItem);

        // 根据内容类型设置不同颜色
        if (logItem.contains("错误") || logItem.contains("Error") || logItem.contains("fail")) {
            holder.tvLogItem.setTextColor(Color.RED);
        } else if (logItem.contains("警告") || logItem.contains("Warning")) {
            holder.tvLogItem.setTextColor(Color.parseColor("#C17B0F")); // 橙色
        } else if (logItem.contains("完成") || logItem.contains("Success")) {
            holder.tvLogItem.setTextColor(Color.parseColor("#25711F"));
        } else {
            holder.tvLogItem.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return logItems.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogItem;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogItem = itemView.findViewById(R.id.tv_log_item);
        }
    }
}