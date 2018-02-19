package com.codernauti.gamebank;

public class TransModel {

    private String fromUser;
    private String toUser;
    private int cash;

    public TransModel() {
    }

    public TransModel(String fromU, String toU, int cash) {
        this.fromUser = fromU;
        this.toUser = toU;
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

}
