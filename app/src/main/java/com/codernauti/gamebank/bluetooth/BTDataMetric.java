package com.codernauti.gamebank.bluetooth;

import android.support.annotation.NonNull;

import java.util.Calendar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.input.CountingInputStream;

public class BTDataMetric {

    private final static String FILE_NAME = "BTMetric";

    private final BufferedWriter mLogger;

    private long mOverallDataTransmitted;

    abstract static class Measurement {

        final String timeStamp;

        Measurement() {
            this.timeStamp = String.valueOf((int)Calendar.getInstance().getTimeInMillis()/1000L);
        }

        abstract int getCount();
        abstract String getReport();
    }

    static class InputMeasurement extends Measurement {

        private final CountingInputStream mIs;
        
        InputMeasurement(@NonNull ObjectInputStream is) {
            super();
            this.mIs = new CountingInputStream(is);
        }

        @Override
        int getCount() {
            return mIs.getCount();
        }

        @Override
        String getReport() {

            return timeStamp + " - Received: " + getCount() + " bytes";
        }
    }

    static class OutputMeasurement extends Measurement {

        private final CountingOutputStream mOs;

        OutputMeasurement(@NonNull ObjectOutputStream os) {
            super();
            this.mOs = new CountingOutputStream(os);
        }

        @Override
        int getCount() {
            return mOs.getCount();
        }

        @Override
        String getReport() {

            return timeStamp + " - Transferred: " + getCount() + " bytes";
        }
        
    }

    public BTDataMetric(String path) throws IOException {

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
    protected void finalize() {
        try {            
            mLogger.flush();
            mLogger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
