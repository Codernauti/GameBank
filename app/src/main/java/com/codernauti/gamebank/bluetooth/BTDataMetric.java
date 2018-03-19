package com.codernauti.gamebank.bluetooth;

import android.support.annotation.NonNull;
import android.util.Log;
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

    private final BufferedWriter logger;

    abstract static class Measurement {

        protected final String timeStamp;

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

        String now = time.get(Calendar.DATE) + "." + time.get(Calendar.MONTH) + "." + time.get(Calendar.YEAR) + "_" + time.get(Calendar.HOUR) + "." + time.get(Calendar.MINUTE) + "." + time.get(Calendar.SECOND);
        this.logger = new BufferedWriter(new FileWriter(path + "/" + FILE_NAME + now + ".log"));
    }

    public synchronized void log(Measurement m) {
        try {
            logger.write(m.getReport());
            logger.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() {
        try {            
            logger.flush();
            logger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
