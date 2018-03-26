package com.codernauti.gamebank;

import android.app.Activity;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.luolc.emojirain.EmojiRainLayout;

import java.lang.reflect.Field;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

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
}
