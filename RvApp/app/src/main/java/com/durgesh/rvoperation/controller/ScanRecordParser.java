package com.durgesh.rvoperation.controller;

import java.util.Arrays;

public class ScanRecordParser {

    private final static byte AD_TYPE_MANUFACTURER_SPECIFIC_DATA = (byte) 0xFF;

    private ScanRecordParser() {
    }

    public static class ScanRecordItem {
        private byte[] manufacturerSpecificData;


        public byte[] getManufacturerSpecificData() {
            return manufacturerSpecificData;
        }

    }


    public static ScanRecordParser getParser() {
        return new ScanRecordParser();
    }

    public ScanRecordItem parseString(byte[] scanRecord) {
        ScanRecordItem scanRecordItem = new ScanRecordItem();

        for (int i = 0; i < scanRecord.length; i++) {
            int length = (int) scanRecord[i];
            if (length > 0) {
                byte[] tmpData = Arrays.copyOfRange(scanRecord, i + 1, i + length + 1);
                if (tmpData[0] == AD_TYPE_MANUFACTURER_SPECIFIC_DATA) {
                    scanRecordItem.manufacturerSpecificData = Arrays.copyOfRange(tmpData, 1, length);
                }

                i = i + length;
            }
        }

        return scanRecordItem;
    }
}
