package com.yydaniel.bestdoricarddownloader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

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

    private void chooseAllOrReverse(ArrayList<CheckBox> boxes) {
        boolean isAllSelected = true;
        // 第一遍循环寻找是否有没有选中的项
        for(CheckBox i : boxes) {
            if(!i.isChecked()) {
                isAllSelected = false;
                break;
            }
        }

        // 第二遍循环设置状态
        for(CheckBox i : boxes) {
            i.setChecked(!isAllSelected);
        }
    }

    // 从json内获取数据的辅助函数
    private int getCharacterId(JSONObject o) throws JSONException {
        return o.getInt("characterId");
    }

    private int getRarity(JSONObject o) throws JSONException {
        return o.getInt("rarity");
    }

    private String getAttribute(JSONObject o) throws JSONException {
        return o.getString("attribute");
    }

    private String getType(JSONObject o) throws JSONException {
        return o.getString("type");
    }

    // 传入character all的json和四组checkbox
    private ArrayList<CardBundle> cardFilter(JSONObject root,
                                             ArrayList<CheckBox> nameBoxes,
                                             ArrayList<CheckBox> attributeBoxes,
                                             ArrayList<CheckBox> rarityBoxes,
                                             ArrayList<CheckBox> typeBoxes) {
        ArrayList<Integer> characterIdGroup = new ArrayList<>();
        ArrayList<Integer> rarityGroup = new ArrayList<>();
        ArrayList<String> attributeGroup = new ArrayList<>();
        ArrayList<String> typeGroup = new ArrayList<>();

        ArrayList<CardBundle> ret = new ArrayList<>();
        for(int i = 0; i < nameBoxes.size(); i++) {
            if(nameBoxes.get(i).isChecked()) {
                characterIdGroup.add(i+1);
            }
        }
        for(int i = 0; i < rarityBoxes.size(); i++) {
            if(rarityBoxes.get(i).isChecked()) {
                rarityGroup.add(i+1);
            }
        }

        if(attributeBoxes.get(0).isChecked()) {
            attributeGroup.add("powerful");
        }
        if(attributeBoxes.get(1).isChecked()) {
            attributeGroup.add("cool");
        }
        if(attributeBoxes.get(2).isChecked()) {
            attributeGroup.add("happy");
        }
        if(attributeBoxes.get(3).isChecked()) {
            attributeGroup.add("pure");
        }

        if(typeBoxes.get(0).isChecked()) {
            typeGroup.add("permanent");
        }
        if(typeBoxes.get(1).isChecked()) {
            typeGroup.add("limited");
        }
        if(typeBoxes.get(2).isChecked()) {
            typeGroup.add("dreamfes");
        }
        if(typeBoxes.get(3).isChecked()) {
            typeGroup.add("kirafes");
        }
        if(typeBoxes.get(4).isChecked()) {
            typeGroup.add("birthday");
        }
        if(typeBoxes.get(5).isChecked()) {
            typeGroup.add("event");
        }
        if(typeBoxes.get(6).isChecked()) {
            typeGroup.add("campaign");
        }
        if(typeBoxes.get(7).isChecked()) {
            typeGroup.add("initial");
        }
        if(typeBoxes.get(8).isChecked()) {
            typeGroup.add("others");
        }

        CardFilter filter = new CardFilter(characterIdGroup, rarityGroup, attributeGroup, typeGroup);
        Iterator<String> it = root.keys();
        while(it.hasNext()) {
            String key = it.next();
            try {
                JSONObject character = root.getJSONObject(key);
                int characterId = character.getInt("characterId");
                int rarity = character.getInt("rarity");
                String attribute = character.getString("attribute");
                String type = character.getString("type");

                if(filter.isSatisfied(characterId, rarity, attribute, type)) {
                    ret.add(new CardBundle(Integer.parseInt(key), "jp", false));
                    ret.add(new CardBundle(Integer.parseInt(key), "jp", true));
                }
            } catch (JSONException e) {
                continue;
            }
        }
        return ret;
    }

    private void afterFetching(StringBuilder jsonCards, StringBuilder jsonCharacters) {
        try {
            JSONObject cards = new JSONObject(jsonCards.toString());
            JSONObject characters = new JSONObject(jsonCharacters.toString());

            int cardAmount = cards.length();
            int characterAmount = characters.length();
            Toast.makeText(BatchAddActivity.this, "卡片个数：" + cardAmount + "，成员个数：" + characterAmount, Toast.LENGTH_LONG).show();
            TextView tv_currentCards = findViewById(R.id.textView_current_cards);
            tv_currentCards.setText("共获取 " + cardAmount + " 张卡面");
            FlexboxLayout parent = findViewById(R.id.flexbox_characters);
            ArrayList<CheckBox> nameBoxes = new ArrayList<>();
            ArrayList<CheckBox> attributeBoxes = new ArrayList<>();
            ArrayList<CheckBox> rarityBoxes = new ArrayList<>();
            ArrayList<CheckBox> typeBoxes = new ArrayList<>();

            // 打包所有同类checkbox
            attributeBoxes.add(findViewById(R.id.checkBox_attribute_powerful));
            attributeBoxes.add(findViewById(R.id.checkBox_attribute_cool));
            attributeBoxes.add(findViewById(R.id.checkBox_attribute_happy));
            attributeBoxes.add(findViewById(R.id.checkBox_attribute_pure));

            rarityBoxes.add(findViewById(R.id.checkBox_star1));
            rarityBoxes.add(findViewById(R.id.checkBox_star2));
            rarityBoxes.add(findViewById(R.id.checkBox_star3));
            rarityBoxes.add(findViewById(R.id.checkBox_star4));
            rarityBoxes.add(findViewById(R.id.checkBox_star5));

            typeBoxes.add(findViewById(R.id.checkBox_type_pernament));
            typeBoxes.add(findViewById(R.id.checkBox_type_limited));
            typeBoxes.add(findViewById(R.id.checkBox_type_dreamfes));
            typeBoxes.add(findViewById(R.id.checkBox_type_kirafes));
            typeBoxes.add(findViewById(R.id.checkBox_type_birthday));
            typeBoxes.add(findViewById(R.id.checkBox_type_event));
            typeBoxes.add(findViewById(R.id.checkBox_type_campaign));
            typeBoxes.add(findViewById(R.id.checkBox_type_initial));
            typeBoxes.add(findViewById(R.id.checkBox_type_others));

            for(int i = 0; i < characterAmount; i++) {
                JSONObject character = characters.getJSONObject(String.valueOf(i+1));
                JSONArray names = character.getJSONArray("characterName");
                String targetName = names.getString(3).replaceAll(" ", "");
                CheckBox newCheckBox = new CheckBox(this);
                nameBoxes.add(newCheckBox);
                newCheckBox.setText(targetName);

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                );

                newCheckBox.setLayoutParams(params);

                parent.addView(newCheckBox);

            }

            Button button_characters = findViewById(R.id.button_choose_all_chars);
            button_characters.setOnClickListener((v) -> {
                chooseAllOrReverse(nameBoxes);
            });

            Button button_attributes = findViewById(R.id.button_choose_all_attributes);
            button_attributes.setOnClickListener((v) -> {
                chooseAllOrReverse(attributeBoxes);
            });

            Button button_rarities = findViewById(R.id.button_choose_all_stars);
            button_rarities.setOnClickListener((v) -> {
                chooseAllOrReverse(rarityBoxes);
            });

            Button button_types = findViewById(R.id.button_choose_all_types);
            button_types.setOnClickListener((v) -> {
                chooseAllOrReverse(typeBoxes);
            });

            // 返回的判断逻辑
            Button button_finish = findViewById(R.id.button_batch_finish);
            button_finish.setOnClickListener((v) -> {
                ArrayList<CardBundle> bundles = cardFilter(cards, nameBoxes, attributeBoxes, rarityBoxes, typeBoxes);
                Intent intent = new Intent();
                if(!bundles.isEmpty()) {
                    intent.putExtra("CARD_LIST", (Serializable)bundles);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
            });

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