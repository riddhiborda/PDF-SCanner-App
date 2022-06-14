package com.pdfscanner.pdf.scanpdf.listener;

public class HomeRenameUpdate {
    String oldFile;
    String renameFile;


    public HomeRenameUpdate(String oldFile, String renameFile) {
        this.oldFile = oldFile;
        this.renameFile = renameFile;
    }

    public String getOldFile() {
        return oldFile;
    }

    public String getRenameFile() {
        return renameFile;
    }
}
