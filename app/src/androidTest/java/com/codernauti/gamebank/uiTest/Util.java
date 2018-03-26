package com.codernauti.gamebank.uiTest;

import android.app.Activity;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.codernauti.gamebank.R;
import com.luolc.emojirain.EmojiRainLayout;

import java.lang.reflect.Field;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dpolonio on 22/03/18.
 */

class Util {

    static void stopEmojiRain(Activity activity) throws NoSuchFieldException, IllegalAccessException {

        // Stop emojirain animation
        Field f = activity.getClass().getDeclaredField("mEmojiRainLayout"); //NoSuchFieldException
        f.setAccessible(true);
        EmojiRainLayout emojiRainLayout = (EmojiRainLayout) f.get(activity); //IllegalAccessException
        emojiRainLayout.stopDropping();
    }

    static void allowBluetoothDiscovery() throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject allowPermissions = device.findObject(new UiSelector().clickable(true).checkable(false).index(1));
        if (allowPermissions.exists()) {
            allowPermissions.click();
        }
    }

    static void fillHostGame(String matchName, int initBudget) {
        onView(ViewMatchers.withId(R.id.match_name))
                .perform(typeText(matchName), closeSoftKeyboard());

        onView(withId(R.id.match_init_budget_form))
                .perform(typeText(String.valueOf(initBudget)));
    }

    static void openLobby(String matchName, int initBudget) throws UiObjectNotFoundException {
        fillHostGame(matchName, initBudget);

        onView(withId(R.id.open_lobby))
                .perform(click());

        Util.allowBluetoothDiscovery();
    }
}
