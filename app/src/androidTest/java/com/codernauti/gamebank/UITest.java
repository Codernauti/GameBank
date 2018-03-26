package com.codernauti.gamebank;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.view.View;
import android.widget.ListView;

import com.codernauti.gamebank.pairing.server.CreateMatchActivity;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.not;

/**
 * Created by dpolonio on 22/03/18.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITest {

    private static final String MATCH_NAME = "test123";
    private static final int INIT_BUDGET = 5;
    private static final int STARTING_HOST_INITIAL_MEMBER_NUMBER = 1;

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(
            MainActivity.class);

    @Rule
    public ActivityTestRule<CreateMatchActivity> matchActivityActivityTestRule = new ActivityTestRule<>(
            CreateMatchActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule
            .grant(
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            );

    private void fillHostGame() {
        onView(withId(R.id.match_name))
                .perform(typeText(MATCH_NAME), closeSoftKeyboard());

        onView(withId(R.id.match_init_budget_form))
                .perform(typeText(String.valueOf(INIT_BUDGET)));
    }

    private void openLobby() throws UiObjectNotFoundException {
        fillHostGame();

        onView(withId(R.id.open_lobby))
                .perform(click());

        Util.allowBluetoothDiscovery();
    }

    @Test
    public void hostGameFromMainActivity() throws NoSuchFieldException, IllegalAccessException, UiObjectNotFoundException, InterruptedException {

        Util.stopEmojiRain(mainActivityActivityTestRule.getActivity());

        onView(withId(R.id.lobby_button))
                .perform(click());

        onView(withId(R.id.fab))
                .perform(click());

        openLobby();

        // ASSERTION: check the open lobby button gets locked
        onView(withId(R.id.open_lobby))
                .check(matches(not(isEnabled())));
    }

    @Test
    public void hasDesiredNumberOfMembersWhenANewMatchStarts() throws InterruptedException, UiObjectNotFoundException {

        matchActivityActivityTestRule.launchActivity(new Intent());

        openLobby();

        onView(withId(R.id.member_list_joined))
                .check(matches(new TypeSafeMatcher<View>() {
                    @Override
                    protected boolean matchesSafely(View item) {

                        ListView listView = (ListView) item;

                        Assert.assertEquals(STARTING_HOST_INITIAL_MEMBER_NUMBER, listView.getCount());

                        return true;
                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                }));
    }

    @Test
    public void canStartMatch() {

        // Check if we are in the right build configuration
        Assert.assertEquals(BuildConfig.BUILD_TYPE, "debug");
        // Check if the user set the bt mac address
        Assert.assertNotNull(BuildConfig.deviceHostMAC);


    }

    @Test
    public void hostGameFromMainActivityFromSaveGame() {

    }
}
