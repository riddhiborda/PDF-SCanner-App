package com.pdfscanner.pdf.scanpdf.model.home;

public class UpdateDelete {
    String filePath;

    public UpdateDelete(String filePath) {
        this.filePath = filePath;
    }

    public UpdateDelete() {
    }

    public String getFilePath() {
        return filePath;
    }
}
