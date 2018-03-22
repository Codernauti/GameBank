package com.codernauti.gamebank.bluetooth;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

class BTDataMetric implements Closeable {

    private final static String FILE_NAME = "BTMetric";
    private final static String FOLDER_NAME = "btmetrics";
    private static final String TAG = "BTDataMetric";
    private static final String SEPARATOR = ",";

    private final BufferedWriter mLogger;

    private long mOverallDataTransmitted;

    abstract static class Measurement {

        final String timeStamp;
        String mAction;

        Measurement() {
            this.timeStamp = String.valueOf((int)Calendar.getInstance().getTimeInMillis()/1000L);
        }

        abstract int getCount();
        abstract String getTransmissionType();

        String getReport() {
            return timeStamp + SEPARATOR +
                    mAction + SEPARATOR +
                    getTransmissionType() + SEPARATOR +
                    getCount();
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
        String getTransmissionType() {
            return "input";
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
        String getTransmissionType() {
            return "output";
        }

        OutputStream getOutputStream() {
            return mOs;
        }
        
    }

    BTDataMetric(String path) throws IOException {

        File metricFolder = new File(path + "/" + FOLDER_NAME);

        if (!metricFolder.exists()) {
            boolean res = metricFolder.mkdir();

            Log.d(TAG, "Database folder created successfully? " + res);
            if (!res) {
                throw new IOException("Impossible to create database folder in " + metricFolder);
            }
        }

        Calendar time = Calendar.getInstance();

        String now = time.get(Calendar.DATE) + "." +
                time.get(Calendar.MONTH) + "." +
                time.get(Calendar.YEAR) + "_" +
                time.get(Calendar.HOUR) + "." +
                time.get(Calendar.MINUTE) + "." +
                time.get(Calendar.SECOND);
        this.mLogger = new BufferedWriter(new FileWriter(metricFolder.getAbsolutePath() + "/" + FILE_NAME + now + ".csv"));
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

    @Override
    public void close() throws IOException {
        Log.d(TAG, "Closing the BTDataMetric");
        Log.d(TAG, "Total data transmitted: " + mOverallDataTransmitted + " B " +
                "(" + (float)mOverallDataTransmitted/1024 + " KB)");

        mLogger.flush();
        mLogger.close();
    }
}
