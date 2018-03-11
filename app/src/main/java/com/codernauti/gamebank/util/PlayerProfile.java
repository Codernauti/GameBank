package com.codernauti.gamebank.util;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.GameBank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by dpolonio on 27/02/18.
 */

public class PlayerProfile implements Serializable {

    private String mNickname;
    private final UUID mId;

    //private final ImageProxy mImage;

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id,
                         @NonNull String imageName) {

        this.mNickname = nickname;
        this.mId = id;
        //mImage = new ImageProxy(imageName);
    }

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id) {
        this.mNickname = nickname;
        this.mId = id;
        //mImage = new ImageProxy();
    }

    @NonNull
    public UUID getId() {
        return mId;
    }

    @NonNull
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String newNickname) {
        this.mNickname = newNickname;
    }

    /*public void getImageName() {
        mImage.getImageName();
    }*/
}

class ImageProxy implements Serializable {

    private static final String TAG = "ImageProxy";

    private static final String NO_IMAGE = "no_image";

    private String mImageName;

    ImageProxy(String imageName) {
        mImageName = imageName;
    }

    ImageProxy() {
        mImageName = NO_IMAGE;
    }

    private void readObject(ObjectInputStream inputStream)
            throws ClassNotFoundException, IOException {
        Log.d(TAG, "readObject() -> deserialize");

        mImageName = (String) inputStream.readObject();

        String pathIntoWrite = GameBank.FILES_DIR + "/" + mImageName;
        Log.d(TAG, "Read image absolute path:\n" + pathIntoWrite);


        FileOutputStream fileOutputStream = new FileOutputStream(pathIntoWrite);
        Log.d("PlayerProfile", "outputStream get");

        if (!mImageName.equals(NO_IMAGE)) {

            boolean flag = true;
            Log.d("PlayerProfile", "into the loop :O");
            while (flag) {
                Log.d(TAG, "Read 1 byte");
                byte singleRead = inputStream.readByte();

                if (singleRead == Byte.MIN_VALUE) {
                    flag = false;
                    Log.d(TAG, "exit");
                } else {
                    fileOutputStream.write(singleRead);
                }
            }
        } else {
            Log.d(TAG, "Image not set");
        }

        Log.d("PlayerProfile", "readObject() finished");
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        Log.d("PlayerProfile", "writeObject() -> serialize");
        Log.d(TAG, "From path: " + getFilePath());

        outputStream.writeObject(mImageName);

        if (!mImageName.equals(NO_IMAGE) && new File(getFilePath()).exists()) {

            FileInputStream fileInputStream = new FileInputStream(getFilePath());
            byte[] buffer = new byte[(int) fileInputStream.getChannel().size() + 1];

            buffer[buffer.length] = Byte.MIN_VALUE;
            int byteRead = fileInputStream.read(buffer);
            outputStream.write(buffer);
        } else {
            outputStream.write(Byte.MIN_VALUE);
        }

        Log.d("PlayerProfile", "writeObject() finished");

    }

    @NonNull
    private String getFilePath() {
        if (!mImageName.equals(NO_IMAGE)) {

            return GameBank.FILES_DIR + "/" + mImageName;
        } else {

            return NO_IMAGE;
        }
    }

    String getImageName() {
        return Environment.getExternalStorageState() + "/" + mImageName;
    }
}
