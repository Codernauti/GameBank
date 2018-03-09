package com.codernauti.gamebank.util;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by dpolonio on 27/02/18.
 */

public class PlayerProfile implements Serializable {

    private static final String NO_IMAGE = "vfdjkvfdmjdk";

    private String mNickname;
    private final UUID mId;

    private String mImageName;
    private ImageProxy mImage = new ImageProxy();

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id,
                         @NonNull String pictureName) {

        this.mNickname = nickname;
        this.mId = id;
        this.mImageName = pictureName;
    }

    public PlayerProfile(@NonNull String nickname,
                         @NonNull UUID id) {
        this.mNickname = nickname;
        this.mId = id;
        this.mImageName = NO_IMAGE;
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

    @NonNull
    private String getFilePath() {
        if (mImageName.equals(NO_IMAGE)) {

            return Environment.getExternalStorageDirectory() + "/" + mImageName;
        } else {

            return NO_IMAGE;
        }
    }

    private class ImageProxy implements Serializable {

        private void readObject(ObjectInputStream aInputStream)
                throws ClassNotFoundException, IOException {

            boolean flag = true;
            FileOutputStream fo = new FileOutputStream(getFilePath());

            if (!getFilePath().equals(NO_IMAGE)) {

                while (flag) {

                    byte singleRead = aInputStream.readByte();

                    if (singleRead == Byte.MIN_VALUE) {
                        flag = false;
                    } else {
                        fo.write(singleRead);
                    }
                }
            }
        }

        private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

            if (!getFilePath().equals(NO_IMAGE) && new File(getFilePath()).exists()) {

                FileInputStream fileInputStream = new FileInputStream(getFilePath());
                byte[] buffer = new byte[(int) fileInputStream.getChannel().size() + 1];

                buffer[buffer.length] = Byte.MIN_VALUE;
                int byteRead = fileInputStream.read(buffer);

                aOutputStream.write(buffer);
            } else {
                aOutputStream.write(Byte.MIN_VALUE);
            }

        }

    }
}
