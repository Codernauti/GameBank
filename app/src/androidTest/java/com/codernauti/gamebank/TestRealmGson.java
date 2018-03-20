package com.codernauti.gamebank;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.MatchSerializer;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.PlayerSerializer;
import com.codernauti.gamebank.database.TransactionSerializer;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Eduard on 15-Mar-18.
 */

@RunWith(AndroidJUnit4.class)
public class TestRealmGson {

    private static final String TAG = "TestRealmGson";

    private Gson gson;
    private Realm realm;

    @Test
    public void testA() throws ClassNotFoundException {
        Context appContext = InstrumentationRegistry.getTargetContext();

        // Init variable
        Realm.init(appContext);
        realm = Realm.getDefaultInstance();

        gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(Class.forName("io.realm.MatchRealmProxy"), new MatchSerializer())
                .registerTypeAdapter(Class.forName("io.realm.PlayerRealmProxy"), new PlayerSerializer())
                .registerTypeAdapter(Class.forName("io.realm.TransactionRealmProxy"), new TransactionSerializer())
                .create();


        final int matchId = 301;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Player pippo = realm.createObject(Player.class, UUID.randomUUID().toString());
                pippo.setUsername("Pippo");

                Match match = realm.createObject(Match.class, matchId);
                match.setMatchName("A casa di zio pippo");
                match.getPlayerList().add(pippo);
            }
        });

        final Match myMatchBeforeChange = realm.where(Match.class)
                .equalTo("mId", matchId)
                .findFirst();

        Log.d(TAG, "Match after creation: \n" + gson.toJson(myMatchBeforeChange));


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                final Match match = realm.where(Match.class)
                        .equalTo("mId", matchId)
                        .findFirst();

                Player pluto = realm.createObject(Player.class, UUID.randomUUID().toString());
                pluto.setUsername("Pluto");

                match.getPlayerList().add(pluto);
            }
        });

        // Test Match to json from json

        final Match myMatchAfterChange = realm.where(Match.class)
                .equalTo("mId", matchId)
                .findFirst();

        String matchJson = gson.toJson(myMatchAfterChange);
        Log.d(TAG, "Match after change: \n" + matchJson);

        Match matchFromJson = gson.fromJson(matchJson, Match.class);

        StringBuilder players = new StringBuilder();
        for (Player p : matchFromJson.getPlayerList()) {
            players.append(p.getUsername());
        }
        Log.d(TAG, "Match from json: \n" + matchFromJson.getId() + "\n" +
                        matchFromJson.getPlayerList().size() + "\n" +
                        matchFromJson.getMatchName() + "\n" +
                        players.toString());


        // Test Player to json from json

        final Player pippo = realm.where(Player.class)
                .equalTo("mUsername", "Pippo")
                .findFirst();

        String pippoJson = gson.toJson(pippo);
        Log.d(TAG, "Pippo taken from db: \n" + pippoJson);

        Player pippoFromJson = gson.fromJson(pippoJson, Player.class);
        Log.d(TAG, "Pippo from Json: \n" + pippoFromJson.getPlayerId() + "\n" +
                        pippoFromJson.getUsername());



    }

}
