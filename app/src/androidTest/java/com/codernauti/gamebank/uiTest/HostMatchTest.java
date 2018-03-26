package com.codernauti.gamebank.uiTest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.util.Log;
import android.view.View;

import com.codernauti.gamebank.BuildConfig;
import com.codernauti.gamebank.MainActivity;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.lobby.BTHostAdapter;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Collection;

import io.realm.Realm;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.CoreMatchers.anything;

/**
 * Created by dpolonio on 22/03/18.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HostMatchTest implements CommonVariables {

    private static final String TAG = "HostMatchTest";

    private volatile boolean isReady = false;
    private Activity currentActivity;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(
            MainActivity.class, true, true);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule
            .grant(
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            );


    private Activity getCurrentActivity() {
        final Activity[] activity = new Activity[1];
        onView(isRoot()).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                activity[0] = (Activity) view.findViewById(android.R.id.content).getContext();
            }
        });
        return activity[0];
    }

    public void getActivityInstance(){

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {

                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()){
                    currentActivity = (Activity) resumedActivities.iterator().next();
                }
            }
        });
    }

    @Test
    public void canStartMatch() throws UiObjectNotFoundException, NoSuchFieldException, IllegalAccessException, InterruptedException {

        final String macAddress = android.provider.Settings.Secure.getString(
                InstrumentationRegistry.getTargetContext().getContentResolver(),
                "bluetooth_address");

        final String savedAddress = InstrumentationRegistry
                .getTargetContext().getString(R.string.test_host_mac_address);

        Log.d(TAG, "Device MAC address: " + macAddress);
        Log.d(TAG, "Defined host MAC address: " + savedAddress);

        // Check if we are in the right build configuration
        Assert.assertEquals(BuildConfig.BUILD_TYPE, "debug");
        // Check if the user set the bt mac address
        Assert.assertNotNull(savedAddress);


        onView(withId(R.id.lobby_button))
                .perform(click());
        if (savedAddress.equals(macAddress)) {
            // Host actions
            onView(withId(R.id.fab))
                    .perform(click());

            Util.openLobby(MATCH_NAME, INIT_BUDGET);

            getActivityInstance();

            Assert.assertNotNull(currentActivity);

            Field f = currentActivity.getClass().getDeclaredField("mMembersAdapter");
            f.setAccessible(true);
            final RoomPlayerAdapter roomPlayerAdapter = (RoomPlayerAdapter) f.get(currentActivity);

            Assert.assertNotNull(roomPlayerAdapter);

            final int room_size = InstrumentationRegistry
                    .getTargetContext().getResources().getInteger(R.integer.test_room_size) + 1;

            Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    while (room_size != roomPlayerAdapter.getCount()) {

                        Log.d(TAG, "Waiting, roomPlayerCount is: " + roomPlayerAdapter.getCount());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isReady = true;
                }
            });

            while (!isReady) {
                Thread.sleep(500);
            }

            Thread.sleep(2000);

            // Now we can start the match
            onView(withId(R.id.start_match))
                    .perform(click());

        } else {
            // Client actions
            BluetoothAdapter btAdapter;

            btAdapter = BluetoothAdapter.getDefaultAdapter();

            getActivityInstance();

            while(btAdapter.isDiscovering()) {Thread.sleep(500);}

            Assert.assertNotNull(currentActivity);

            Field f = currentActivity.getClass().getDeclaredField("mAdapter"); //NoSuchFieldException
            f.setAccessible(true);
            BTHostAdapter bluetoothAdapter = (BTHostAdapter) f.get(currentActivity); //IllegalAccessException

            Assert.assertNotNull(bluetoothAdapter);

            int match = 0;
            boolean flag = true;
            for (; match < bluetoothAdapter.getCount() && flag; match++) {
                if (bluetoothAdapter.getItem(match).getAddress().equals(savedAddress)) {
                    flag = false;
                }
            }

            Log.d(TAG, "Match value: " + match);
            onData(anything()).inAdapterView(withId(R.id.list))
                    .atPosition(match - 1).perform(click());

            Thread.sleep(7500);

            // Set I'm ready
            onView(withId(R.id.member_set_status))
                    .perform(click());

            // Test poke button
            onView(withId(R.id.room_poke_fab))
                    .perform(click());
        }
    }

    /*@Test
    public void hostGameFromMainActivityFromSaveGame() {

    }*/
}
