package com.yydaniel.bestdoricarddownloader;

import java.io.Serializable;

public class CardBundle implements Serializable {
    public int cardId;
    public String region;
    public boolean isTrained;

    public CardBundle(int cardId, String region, boolean isTrained) {
        this.cardId = cardId;
        this.region = region;
        this.isTrained = isTrained;
    }

    public String getRegion() {
        return region;
    }

    public boolean isTrained() {
        return isTrained;
    }

    public int getCardId() {
        return cardId;
    }
}
