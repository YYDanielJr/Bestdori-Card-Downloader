package com.yydaniel.bestdoricarddownloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yydaniel.bestdoricarddownloader.CardBundle;

import java.util.List;

public class WorklistAdapter extends ArrayAdapter<CardBundle> {
    private final int resourceId;
    public WorklistAdapter(@NonNull Context context, int resource, @NonNull List<CardBundle> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);

        CardBundle data = getItem(position);
        TextView tv_cardid = itemView.findViewById(R.id.tv_item_id);
        TextView tv_region = itemView.findViewById(R.id.tv_item_region);
        TextView tv_state = itemView.findViewById(R.id.tv_item_training_state);
        if(data != null) {
            tv_cardid.setText(String.valueOf(data.getCardId()));
            tv_region.setText(data.getRegion());
            if(data.isTrained()) {
                tv_state.setText("特训后");
            } else {
                tv_state.setText("特训前");
            }
        }

        return itemView;
    }
}
