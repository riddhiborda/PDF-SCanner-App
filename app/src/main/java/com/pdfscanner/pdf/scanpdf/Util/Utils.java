package com.pdfscanner.pdf.scanpdf.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.core.content.ContextCompat;

import com.itextpdf.text.pdf.PdfReader;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.ui.SettingActivity;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static String getMimeTypeFromFilePath(String filePath) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFilenameExtension(filePath));
        return (mimeType == null) ? null : mimeType;
    }

    public static String getFilenameExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }


    public static final boolean deleteFiles(final File folder, Context context) {
        boolean totalSuccess = true;
        if (folder == null)
            return false;
        if (folder.isDirectory()) {
            for (File child : folder.listFiles()) {
                deleteFiles(child, context);
            }

            if (!folder.delete())
                totalSuccess = false;
        } else {

            if (!folder.delete())
                totalSuccess = false;
        }
        return totalSuccess;
    }

    public static Bitmap getImageFromPdf(Context context, String mPath) {
        ParcelFileDescriptor fileDescriptor = null;

        Uri mUri = Uri.fromFile(new File(mPath));
        try {

            fileDescriptor = context.getContentResolver().openFileDescriptor(mUri, "r");

            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);

                int i = 0;
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                        Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                page.close();
                renderer.close();

                return bitmap;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isPDFEncrypted(String path) {
        boolean isEncrypted;
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(path);
            isEncrypted = pdfReader.isEncrypted();
        } catch (IOException e) {
            isEncrypted = true;
        } finally {
            if (pdfReader != null) pdfReader.close();
        }
        return isEncrypted;
    }

    public static void changeStatusBarColor(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
            View decorView = activity.getWindow().getDecorView(); //set status background black
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); //set status text  light
        }
    }
}
