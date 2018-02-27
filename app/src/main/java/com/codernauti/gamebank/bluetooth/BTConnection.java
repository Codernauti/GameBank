package com.codernauti.gamebank.bluetooth;

import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by dpolonio on 27/02/18.
 */

abstract class BTConnection {

    final LocalBroadcastManager mLocalBroadcastManager;

    BTConnection (LocalBroadcastManager mLocalBroadcastManager) {
        this.mLocalBroadcastManager = mLocalBroadcastManager;
    }
}
