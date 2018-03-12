package com.codernauti.gamebank.pairing;

import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.GameBank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by dpolonio on 27/02/18.
 */

public class ImagePlayerProfile implements Serializable {

    private static final String TAG = "ImagePlayerProfile";
    private final String mNickname;
    private final UUID mId;
    private String mImageName;
    private byte[] mImage;

    public ImagePlayerProfile(@NonNull String nickname,
                       @NonNull UUID id,
                       @NonNull String imageName) {

        this.mNickname = nickname;
        this.mId = id;
        this.mImageName = imageName;
    }

    @NonNull
    public UUID getId() {
        return mId;
    }

    @NonNull
    public String getNickname() {
        return mNickname;
    }

    @NonNull
    public String getImageName() {
        return mImageName;
    }

    private void loadImageIntoMemory() {

        final File toLoad = new File(GameBank.FILES_DIR, mImageName);

        Log.d(TAG, "File path: " + toLoad.getAbsolutePath());

        if (toLoad.exists() && toLoad.isFile() && (toLoad.length() < Integer.MAX_VALUE)) {

            mImage = new byte[(int)toLoad.length()];
            try (final FileInputStream fis = new FileInputStream(toLoad)) {

                int read;
                read = fis.read(mImage);

                Log.d(TAG, "Read " + read + " bytes");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Impossible to load image into memory");
        }
    }

    private void purgeMemory() {

        writeToPersistentStorage();
        mImage = null;
    }

    private void writeToPersistentStorage() {

        Log.d(TAG, "on writeToPersistentStorage()");

        if (mImage != null) {

            File toWrite = new File(GameBank.FILES_DIR, mImageName);

            Log.d(TAG, "Writing into memory, in: " + toWrite.getAbsolutePath());

            try(final FileOutputStream fos = new FileOutputStream(toWrite, false)) {
                fos.write(mImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        // write into
        Log.d(TAG, "Serializing...");

        loadImageIntoMemory();
        out.defaultWriteObject();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        // restore
        Log.d(TAG, "Deserializing...");

        in.defaultReadObject();
        purgeMemory();
    }
    private void readObjectNoData() throws ObjectStreamException {

        Log.wtf(TAG, "Nothing to do in readObjectNoData()!");
        // For debug purposes
        throw new RuntimeException("Error in readObjectNoData()");
    }


}