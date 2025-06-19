package com.yydaniel.bestdoricarddownloader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebviewActivity extends AppCompatActivity implements AddFromBestdoriDialog.OnDialogInteractionListener {

    List<CardBundle> cardList;
    private int getCardId(String url) {
        // 正则表达式匹配 cards/ 后的连续数字
        Pattern pattern = Pattern.compile("cards/(\\d+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
        } else {
            return 0;
        }
    }

    private void returnToMain() {
        Intent resultIntent = new Intent();
        if(cardList != null && !cardList.isEmpty()) {
            resultIntent.putExtra("CARD_LIST", (Serializable)cardList);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED, resultIntent);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview);

        cardList = new ArrayList<>();

        final WebView webview = findViewById(R.id.Webview_bestdori);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.loadUrl("https://bestdori.com/info/cards");

        FloatingActionButton btn_save = findViewById(R.id.fab_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = webview.getUrl();
                URI uri = null;
                try {
                    uri = new URI(url);
                } catch (URISyntaxException e) {
                    Toast.makeText(WebviewActivity.this, "获取URL出错。", Toast.LENGTH_LONG).show();
                }
                if("https".equals(uri.getScheme()) &&
                        "bestdori.com".equals(uri.getHost()) &&
                        uri.getPath().matches("^/info/cards/\\d+.*")) {
                    AddFromBestdoriDialog dialog = new AddFromBestdoriDialog();
                    Bundle bundle = new Bundle();
                    bundle.putInt("CARD_ID", getCardId(url));
                    dialog.setArguments(bundle);
                    dialog.setListener(WebviewActivity.this);
                    dialog.show(getSupportFragmentManager(), "ADD_FROM_BESTDORI_DIALOG");
                } else {
                    new AlertDialog.Builder(WebviewActivity.this)
                            .setTitle(R.string.alert_choose_card)
                            .setMessage(R.string.alert_not_valid_card)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            }).show();
                }
            }
        });

        FloatingActionButton btn_back = findViewById(R.id.fab_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webview.canGoBack()) {
                    webview.goBack();
                } else {
                    returnToMain();
                }
            }
        });

        FloatingActionButton btn_exit = findViewById(R.id.fab_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMain();
            }
        });

        OnBackPressedCallback back_cb = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(webview.canGoBack()) {
                    webview.goBack();
                } else {
                    returnToMain();
                }
            }
        };
        WebviewActivity.this.getOnBackPressedDispatcher().addCallback(back_cb);

    }

    @Override
    public void onButtonActionRequested(CardBundle bundle) {
        if(cardList != null) {
            cardList.add(bundle);
        }
    }
}