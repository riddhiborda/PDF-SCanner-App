package com.pdfscanner.pdf.scanpdf.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.pdf.PdfReader;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.model.PDFModel;
import com.pdfscanner.pdf.scanpdf.ui.SettingActivity;
import com.pdfscanner.pdf.scanpdf.ui.SplashActivity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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

    public static void getAdsIds(Context context){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Ads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Log.e("TAG", "onDataChange: "+postSnapshot.getKey() +"--"+postSnapshot.getValue(String.class));
                    if (postSnapshot.getKey().equals(Constant.APP_OPEN_ID)){
                        PreferencesManager.saveString(context, Constant.APP_OPEN_ID,postSnapshot.getValue(String.class));
                    }else if (postSnapshot.getKey().equals(Constant.APP_UNIT_ID)){
                        PreferencesManager.saveString(context, Constant.APP_UNIT_ID,postSnapshot.getValue(String.class));
                    }else if (postSnapshot.getKey().equals(Constant.BANNER_ID)){
                        PreferencesManager.saveString(context, Constant.BANNER_ID,postSnapshot.getValue(String.class));
                    }else if (postSnapshot.getKey().equals(Constant.INTERSTITIAL_ID)){
                        PreferencesManager.saveString(context, Constant.INTERSTITIAL_ID,postSnapshot.getValue(String.class));
                    }else if (postSnapshot.getKey().equals(Constant.NATIVE_APP_ID)){
                        PreferencesManager.saveString(context, Constant.NATIVE_APP_ID,postSnapshot.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: "+error.getMessage());
            }
        });
    }

    public static void getAdsCounter(Context context){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("AdsCounter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PreferencesManager.saveInteger(context,Constant.ADS_COUNTER,snapshot.getValue(Integer.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: "+error.getMessage());
            }
        });
    }

    public static Date getData(Long date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm aa");
        SimpleDateFormat dSdf = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");

        String strDate = sdf.format(date);
        try {
            return dSdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*public static ArrayList<PDFModel> getAllData(File dir){
        ArrayList<PDFModel> list = new ArrayList<>();

        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()){
                getAllData(files[i]);
            }else {
                if (files[i].isFile() && files[i].getName().endsWith(".pdf")) {
                    Log.e("TAG", "getList: " + files[i].getAbsolutePath());
                    PDFModel model = new PDFModel();
                    model.setFilePath(files[i].getAbsolutePath());
                    model.setFileName(files[i].getName());
                    model.setSize(files[i].length());
                    list.add(model);
                }
            }
        }

        *//*for (File file : files) {
            if (file.isDirectory()){
                getAllData(file);
            }else {
                if (file.isFile() && file.getName().endsWith(".pdf")) {
                    Log.e("TAG", "getList: " + file.getAbsolutePath());
                    PDFModel model = new PDFModel();
                    model.setFilePath(file.getAbsolutePath());
                    model.setFileName(file.getName());
                    model.setSize(file.length());
                    list.add(model);
                }
            }
        }*//*

        return list;
    }*/
}
