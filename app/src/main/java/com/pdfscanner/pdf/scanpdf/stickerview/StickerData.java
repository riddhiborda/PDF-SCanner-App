package com.pdfscanner.pdf.scanpdf.stickerview;

import java.util.Arrays;

public class StickerData {

    public String tabName;
    public int[] data;

    @Override
    public String toString() {
        return "StickerData{" +
                "tabName='" + tabName + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public String getTabName() {

        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }
}