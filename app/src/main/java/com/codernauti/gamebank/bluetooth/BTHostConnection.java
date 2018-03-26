package com.codernauti.gamebank.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.GameBank;
import com.codernauti.gamebank.database.Player;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by dpolonio on 26/02/18.
 */

public class BTHostConnection extends BTConnection {

    private static final String TAG = "BTHostConnection";

    private final int mAcceptedConnections;
    private final BluetoothServerSocket mBtServerSocket;
    private final String mPicturePath;

    private volatile boolean mServerSocketOpen;


    BTHostConnection(int acceptedConnections, @NonNull String picturePath,
                     @NonNull BluetoothServerSocket btServerSocket,
                     @NonNull LocalBroadcastManager localBroadcastManager,
                     @NonNull String logPath) {
        super(localBroadcastManager, Executors.newCachedThreadPool(), logPath);

        mPicturePath = picturePath;

        if (acceptedConnections > 0 && acceptedConnections < 8) {

            this.mAcceptedConnections = acceptedConnections;
            this.mBtServerSocket = btServerSocket;
        } else {
            throw new RuntimeException();
        }

    }

    void acceptConnections() {

        Log.d(TAG, "Accepting connections");
        mServerSocketOpen = true;

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < mAcceptedConnections && mServerSocketOpen; i++) {
                    try {
                        Log.d(TAG, "Waiting for a new connection...");
                        final BluetoothSocket clientSocket = mBtServerSocket.accept();
                        Log.d(TAG, "New connection!");

                        // Read init information from client just connected
                        ObjectInputStream bundleInputStream = new ObjectInputStream(
                                clientSocket.getInputStream());
                        BTBundle btBundle = (BTBundle) bundleInputStream.readObject();

                        Log.d(TAG, "Read rendezvous, action: " +
                                btBundle.getBluetoothAction());

                        if (BTEvent.MEMBER_CONNECTED.equals(btBundle.getBluetoothAction())) {

                            Player newPlayerRealm = GameBank.gsonConverter.fromJson(
                                    (String) btBundle.get(String.class.getName()), Player.class);

                            Log.d(TAG, "Player from Realm connected: " +
                                    newPlayerRealm.getPlayerId());

                            addConnection(UUID.fromString(newPlayerRealm.getPlayerId()), clientSocket);
                            startListeningRunnable(UUID.fromString(newPlayerRealm.getPlayerId()));

                            Intent intentJoin = btBundle.getIntent();
                            mLocalBroadcastManager.sendBroadcast(intentJoin);
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Socket closed, connection accept abort.\nAutomatically retry.");
                        i--;
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "Init data from client is not a UUID");
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "Ended waiting for new connections");
                try {
                    mBtServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void readPicture(InputStream fileInputStream, String pictureName) throws IOException {
        Log.d(TAG, "Test phase");
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        int bytesCount = dataInputStream.readInt();
        Log.d(TAG, "Bytes count: " + bytesCount);


        // Write file into internal storage
        File newFile = new File(mPicturePath, pictureName);
        FileOutputStream fileOutputStream = new FileOutputStream(newFile);


        byte[] buffer = new byte[1024];
        int bytesRead;
        int totBytesRead = 0;
        while (totBytesRead < bytesCount) {
            bytesRead = fileInputStream.read(buffer, 0, buffer.length);
            totBytesRead += bytesRead;
            fileOutputStream.write(buffer, 0, bytesRead);

            Log.d(TAG, "Read bytes: " + bytesRead);
        }
        Log.d(TAG, "Total read bytes: " + totBytesRead);

        fileOutputStream.close();
    }

    @Override
    void onStopReadingDataFrom(UUID who) {
        Intent intent = BTBundle.makeIntentFrom(
                new BTBundle(BTEvent.MEMBER_DISCONNECTED)
                        .append(who)
        );
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void close() {
        super.close();
        closeServerSocket();
    }

    void closeServerSocket() {
        try {
            mServerSocketOpen = false;
            mBtServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Impossible to close server socket: " +
                    mBtServerSocket.toString() + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
