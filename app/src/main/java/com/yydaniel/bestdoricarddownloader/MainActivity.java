package com.yydaniel.bestdoricarddownloader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.yydaniel.bestdoricarddownloader.AddCardManuallyDialog;
import com.yydaniel.bestdoricarddownloader.CardBundle;
import com.yydaniel.bestdoricarddownloader.WorklistAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddCardManuallyDialog.OnDialogInteractionListener {

    private List<CardBundle> cardList;
    private ListView lv_worklist;

    // 读取来自AddFromBestdori的浏览器Activity的返回值
    private ActivityResultLauncher<Intent> addFromBestdoriResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    List<CardBundle> returnedData = (List<CardBundle>)result.getData().getSerializableExtra("CARD_LIST");

                    if(cardList != null && returnedData != null) {
                        cardList.addAll(returnedData);

                        WorklistAdapter adapter = new WorklistAdapter(MainActivity.this, R.layout.worklist_item, cardList);
                        if(lv_worklist != null) {
                            lv_worklist.setAdapter(adapter);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        cardList = new ArrayList<>();
        lv_worklist = findViewById(R.id.lv_worklist);
        final Button button_addCardManually = findViewById(R.id.button_add_card_manually);
        button_addCardManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 手动输入卡面编号的对话框
                AddCardManuallyDialog addCardManuallyDialog = new AddCardManuallyDialog();
                addCardManuallyDialog.setListener(MainActivity.this);
                addCardManuallyDialog.show(getSupportFragmentManager(), "ADD_CARD_MANUALLY_DIALOG");
            }
        });

        final CheckBox checkBox = findViewById(R.id.checkBox_if_realesrgan);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    // 弹窗提醒算法吃性能
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.alert_realesrgan_title)
                            .setMessage(R.string.alert_realesrgan)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            }).show();
                }
            }
        });

        final Button button_start = findViewById(R.id.button_start_jobs);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cardList.isEmpty()) {
                    // 开始下载，跳转到下载Activity
                    Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                    intent.putExtra("CARD_LIST", (Serializable)cardList);
                    boolean isUpscaleEnabled = checkBox.isChecked();
                    intent.putExtra("UPSCALE", isUpscaleEnabled);
                    startActivity(intent);

                    cardList.clear();
                    WorklistAdapter adapter = new WorklistAdapter(MainActivity.this, R.layout.worklist_item, cardList);
                    if(lv_worklist != null) {
                        lv_worklist.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_nocard, Toast.LENGTH_LONG).show();
                }

            }
        });

        final Button button_exploreBestdori = findViewById(R.id.button_explore_bestdori);

        button_exploreBestdori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启Webview Activity
                 Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
//                 startActivity(intent);
                addFromBestdoriResultLauncher.launch(intent);
            }
        });

    }

    // 实现接口回调方法
    @Override
    public void onButtonActionRequested(CardBundle bundle) {
        if(cardList != null) {
            cardList.add(bundle);

            WorklistAdapter adapter = new WorklistAdapter(MainActivity.this, R.layout.worklist_item, cardList);
            if(lv_worklist != null) {
                lv_worklist.setAdapter(adapter);
            }
        }
    }
}