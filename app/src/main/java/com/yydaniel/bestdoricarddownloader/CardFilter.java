package com.yydaniel.bestdoricarddownloader;

import java.util.ArrayList;

public class CardFilter {
    ArrayList<Integer> characterIdList;
    ArrayList<Integer> rarityList;
    ArrayList<String> attributeList;
    ArrayList<String> typeList;

    public CardFilter(ArrayList<Integer> characterIdList, ArrayList<Integer> rarityList, ArrayList<String> attributeList, ArrayList<String> typeList) {
        this.characterIdList = characterIdList;
        this.rarityList = rarityList;
        this.attributeList = attributeList;
        this.typeList = typeList;
    }

    public boolean isSatisfied(int characterId, int rarity, String attribute, String type) {
        boolean isCharacterIdSatisfied = false;
        boolean isRaritySatisfied = false;
        boolean isAttributeSatisfied = false;
        boolean isTypeSatisfied = false;
        for(Integer i : characterIdList) {
            if(i == characterId) {
                isCharacterIdSatisfied = true;
                break;
            }
        }
        if(!isCharacterIdSatisfied)
            return false;

        for(Integer i : rarityList) {
            if(i == rarity) {
                isRaritySatisfied = true;
                break;
            }
        }
        if(!isRaritySatisfied)
            return false;

        for(String i : attributeList) {
            if(i.equals(attribute)) {
                isAttributeSatisfied = true;
                break;
            }
        }
        if(!isAttributeSatisfied)
            return false;

        for(String i: typeList) {
            if(i.equals(type)) {
                isTypeSatisfied = true;
                break;
            }
        }
        return isTypeSatisfied;
    }
}
