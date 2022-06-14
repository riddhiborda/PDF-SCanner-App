package com.pdfscanner.pdf.scanpdf.model;

public class PdfModel {
    String fileName;
    String filePath;
    long size;
    long dateValue;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDateValue() {
        return dateValue;
    }

    public void setDateValue(long dateValue) {
        this.dateValue = dateValue;
    }
}
