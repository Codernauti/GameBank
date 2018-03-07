package com.codernauti.gamebank;

import java.io.Serializable;
import java.util.UUID;

public class TransModel implements Serializable {

    private final String fromUser;
    private final String toUser;

    private final UUID fromUUID;
    private final UUID toUUID;

    private final int cash;

    public TransModel(String fromU, String toU, UUID fromUUID, UUID toUUID, int cash) {
        this.fromUser = fromU;
        this.toUser = toU;

        this.fromUUID = fromUUID;
        this.toUUID = toUUID;
        this.cash = cash;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public int getCash() {
        return cash;
    }

    public UUID getToUUID() {
        return toUUID;
    }

    public UUID getFromUUID() {
        return fromUUID;
    }

}
