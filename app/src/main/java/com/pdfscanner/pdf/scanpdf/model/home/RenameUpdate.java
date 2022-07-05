package com.pdfscanner.pdf.scanpdf.model.home;

public class RenameUpdate {
    String oldFile;
    String renameFile;

    public RenameUpdate(String oldFile, String renameFile) {
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
