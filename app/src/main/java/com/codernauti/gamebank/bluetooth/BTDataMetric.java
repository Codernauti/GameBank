package com.codernauti.gamebank.bluetooth;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

class BTDataMetric implements Closeable {

    private final static String FILE_NAME = "BTMetric";
    private static final String TAG = "BTDataMetric";

    private final BufferedWriter mLogger;

    private long mOverallDataTransmitted;

    abstract static class Measurement {

        final String timeStamp;
        String mAction;

        Measurement() {
            this.timeStamp = String.valueOf((int)Calendar.getInstance().getTimeInMillis()/1000L);
        }

        abstract int getCount();
        String getReport() {
            return "Action: " + mAction;
        }
    }

    static class InputMeasurement extends Measurement {

        private final CountingInputStream mIs;
        
        InputMeasurement(@NonNull InputStream is) {
            super();
            this.mIs = new CountingInputStream(is);
        }

        @Override
        int getCount() {
            return mIs.getCount();
        }

        @Override
        String getReport() {

            return timeStamp +
                    " - Received: " +
                    getCount() + " B. " +
                    super.getReport();
        }

        InputStream getInputStream() {
            return mIs;
        }

        void setInputAction(@NonNull String bluetoothAction) {
            mAction = bluetoothAction;
        }
    }

    static class OutputMeasurement extends Measurement {

        private final CountingOutputStream mOs;

        OutputMeasurement(@NonNull OutputStream os, @NonNull String bluetoothAction) {
            super();
            this.mOs = new CountingOutputStream(os);
            this.mAction = bluetoothAction;
        }

        @Override
        int getCount() {
            return mOs.getCount();
        }

        @Override
        String getReport() {

            return timeStamp +
                    " - Transferred: " +
                    getCount() + " B. " +
                    super.getReport();
        }

        OutputStream getOutputStream() {
            return mOs;
        }
        
    }

    BTDataMetric(String path) throws IOException {

        Calendar time = Calendar.getInstance();

        String now = time.get(Calendar.DATE) + "." +
                time.get(Calendar.MONTH) + "." +
                time.get(Calendar.YEAR) + "_" +
                time.get(Calendar.HOUR) + "." +
                time.get(Calendar.MINUTE) + "." +
                time.get(Calendar.SECOND);
        this.mLogger = new BufferedWriter(new FileWriter(path + "/" + FILE_NAME + now + ".log"));
        this.mOverallDataTransmitted = 0;
    }

    public synchronized void log(Measurement m) {
        mOverallDataTransmitted += m.getCount();
        try {
            Log.d(TAG, "Report: " + m.getReport());
            mLogger.write(m.getReport());
            mLogger.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getOverallDataTransmitted() {
        return mOverallDataTransmitted;
    }

    @Override
    public void close() throws IOException {
        Log.d(TAG, "Closing the BTDataMetric");
        mLogger.newLine();
        mLogger.write("*****");
        mLogger.newLine();
        mLogger.write("Total data transmitted: " + mOverallDataTransmitted + " B " +
         "(" + (float)mOverallDataTransmitted/1024 + " KB)");
        mLogger.newLine();
        mLogger.write("*****");
        mLogger.newLine();
        mLogger.flush();
        mLogger.close();
    }
}
