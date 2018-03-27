package com.codernauti.gamebank.uiTest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.codernauti.gamebank.BuildConfig;
import com.codernauti.gamebank.MainActivity;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.bluetooth.BTClientService;
import com.codernauti.gamebank.bluetooth.BTHostService;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.lobby.BTHostAdapter;
import com.codernauti.gamebank.pairing.CreateMatchActivity;
import com.codernauti.gamebank.pairing.RoomPlayerAdapter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;

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

    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((ListView) view).getCount () == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("ListView should have " + size + " items");
            }
        };
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


            // Waiting that everyone connects
            //Thread.sleep(60000);

            getActivityInstance();

            ListView lv = currentActivity.findViewById(R.id.member_list_joined);

            while (lv.getCount() < getInstrumentation().getContext().getResources().getInteger(R.integer.test_room_size)) {
                Thread.sleep(1000);
                Log.d(TAG, "Waiting...count: " + lv.getCount());
            }

            // Now we can start the match
            onView(withId(R.id.start_match))
                    .perform(click());

            getActivityInstance();

            Log.d(TAG, "I'm in the activity: " + currentActivity.getClass().getName());

            // Check that I'm in the Dashboard activity
            Assert.assertNotEquals(currentActivity.getClass().getName(), CreateMatchActivity.class.getName());

            saveLog(BTHostService.class);
        } else {
            // Client actions
            BluetoothAdapter btAdapter;

            btAdapter = BluetoothAdapter.getDefaultAdapter();

            getActivityInstance();

            while(btAdapter.isDiscovering()) {Thread.sleep(1000);}

            Assert.assertNotNull(currentActivity);

            Field f = currentActivity.getClass().getDeclaredField("mAdapter"); //NoSuchFieldException
            f.setAccessible(true);
            BTHostAdapter bluetoothAdapter = (BTHostAdapter) f.get(currentActivity); //IllegalAccessException

            Assert.assertNotNull(bluetoothAdapter);

            Thread.sleep(7500);

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

            Thread.sleep(15000);

            /*getActivityInstance();

            Field mMembersAdapter = currentActivity.getClass().getDeclaredField("mMembersAdapter");
            mMembersAdapter.setAccessible(true);
            RoomPlayerAdapter roomPlayerAdapter = (RoomPlayerAdapter) mMembersAdapter.get(currentActivity);

            Assert.assertNotNull(roomPlayerAdapter);

            while (roomPlayerAdapter.getCount() == 0) {
                Thread.sleep(1000);
            }

            final AtomicInteger value = new AtomicInteger(0);

            getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    *//*Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {

                        @Override
                        public void execute(Realm realm) {

                            int i = 0;

                            while (i == 0) {
                                i = realm.where(Player.class)
                                        .findAll().size();
                            }

                            value.set(i);
                        }
                    });*//*

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {

                        @Override
                        public void execute(Realm realm) {

                            int i = 0;

                            while (i == 0) {
                                i = realm.where(Player.class)
                                        .findAll().size();
                            }

                            value.set(i);
                        }
                    });

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }).start();
                }
            });

            while (value.get() == 0) {
                Thread.sleep(500);
            }*/

            getActivityInstance();

            ListView lv = currentActivity.findViewById(R.id.room_members);

            while (lv.getCount() == 0) {
                Thread.sleep(1000);
                Log.d(TAG, "Waiting...count: " + lv.getCount());
            }

            /*onView(withId(R.id.room_members))
                    .check(ViewAssertions.matches(not(withListSize(0))));*/

            // Set I'm ready
            onView(withId(R.id.member_set_status))
                    .perform(click());

            // Test poke button
            onView(withId(R.id.room_poke_fab))
                    .perform(click());

            getActivityInstance();
            Assert.assertNotNull(currentActivity);

            String activityName = currentActivity.getClass().getName();

            while (activityName.equals(currentActivity.getClass().getName())) {
                getActivityInstance();
                Assert.assertNotNull(currentActivity);
            }

            Log.d(TAG, "I'm in the game");
            saveLog(BTClientService.class);
        }
    }

    private void saveLog(Class service) {
        Context context = getInstrumentation().getContext();

        context.stopService(new Intent(context, service));
    }

    /*@Test
    public void hostGameFromMainActivityFromSaveGame() {

    }*/
}
