package com.codernauti.gamebank;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.MatchSerializer;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.PlayerDeserializer;
import com.codernauti.gamebank.database.PlayerSerializer;
import com.codernauti.gamebank.database.TransactionSerializer;
import com.codernauti.gamebank.util.SharePrefUtil;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Eduard on 19-Mar-18.
 */

@RunWith(AndroidJUnit4.class)
public class TestGsonPlayer {

    Context appContext;
    Realm realm;
    Gson gson;


    public void init() throws ClassNotFoundException {
        appContext = InstrumentationRegistry.getTargetContext();

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
                .registerTypeAdapter(Class.forName("io.realm.PlayerRealmProxy"), new PlayerSerializer())
                .registerTypeAdapter(Class.forName("io.realm.TransactionRealmProxy"), new TransactionSerializer())
                .registerTypeAdapter(Class.forName("io.realm.MatchRealmProxy"), new MatchSerializer())
                .registerTypeAdapter(Player.class, new PlayerDeserializer())
                .create();
    }

    @Test
    public void shouldDeserializePlayerWithoutImage() throws ClassNotFoundException {

        init();
        GameBank.BT_ADDRESS = UUID.randomUUID();
        final String pippoId = GameBank.BT_ADDRESS.toString();
        final int matchId = 42;

        SharePrefUtil.getProfilePicturePreference(appContext);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Player pippo = realm.createObject(Player.class, pippoId);
                pippo.setUsername("Pippo");

                Match match = realm.createObject(Match.class, matchId);
                match.setMatchName("A casa di zio pippo");
                match.getPlayerList().add(pippo);
            }
        });



        final Match myMatchBeforeSent = realm.where(Match.class)
                .equalTo("mId", matchId)
                .findFirst();

        String jsonMatch = gson.toJson(myMatchBeforeSent);
        Log.d("TestGsonMatch", "Match after creation: \n" + jsonMatch);

        // reset
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });


        // Deserialization
        Match myMatchAfterSent = gson.fromJson(jsonMatch, Match.class);

        for (Player player : myMatchAfterSent.getPlayerList()) {
            Log.d("TestGsonPlayer", player.getPlayerId());
            Log.d("TestGsonPlayer", player.getUsername());
            Assert.assertEquals("Should be equal", pippoId, player.getPlayerId());
            Assert.assertEquals("Should be empty", "", player.getImageBase64());
        }

    }
}
