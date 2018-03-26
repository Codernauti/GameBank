package com.codernauti.gamebank.uiTest;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.view.View;
import android.widget.ListView;

import com.codernauti.gamebank.MainActivity;
import com.codernauti.gamebank.R;
import com.codernauti.gamebank.pairing.CreateMatchActivity;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

/**
 * Created by dpolonio on 26/03/18.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HostGameLoaderTest implements CommonVariables {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(
            MainActivity.class, true, true);

    @Rule
    public ActivityTestRule<CreateMatchActivity> matchActivityActivityTestRule = new ActivityTestRule<>(
            CreateMatchActivity.class, true, false);


    @Test
    public void hostGameFromMainActivity() throws NoSuchFieldException, IllegalAccessException, UiObjectNotFoundException, InterruptedException {

        Util.stopEmojiRain(mainActivityActivityTestRule.getActivity());

        onView(withId(R.id.lobby_button))
                .perform(click());

        onView(withId(R.id.fab))
                .perform(click());

        Util.openLobby(MATCH_NAME, INIT_BUDGET);

        // ASSERTION: check the open lobby button gets locked
        onView(withId(R.id.open_lobby))
                .check(matches(not(isEnabled())));
    }

    @Test
    public void hasDesiredNumberOfMembersWhenANewMatchStarts() throws InterruptedException, UiObjectNotFoundException {

        matchActivityActivityTestRule.launchActivity(new Intent());

        Util.openLobby(MATCH_NAME, INIT_BUDGET);

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
}
