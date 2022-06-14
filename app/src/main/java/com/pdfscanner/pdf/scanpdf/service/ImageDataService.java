package com.pdfscanner.pdf.scanpdf.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.pdfscanner.pdf.scanpdf.model.PhotoData;
import com.pdfscanner.pdf.scanpdf.Util.NotificationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class ImageDataService extends Service {

    public static boolean isComplete = false;

    public static ArrayList<String> folderList = new ArrayList<>();

    public static LinkedHashMap<String, ArrayList<PhotoData>> bucketimagesDataPhotoHashMap;

   public static ArrayList<PhotoData> photoDataArrayList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification notification = new NotificationCompat.Builder(this, NotificationUtils.ANDROID_CHANNEL_ID)
                        .setContentTitle("")
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentText("").build();

                startForeground(1, notification);
                stopForeground(STOP_FOREGROUND_REMOVE);
            }
        } catch (RuntimeException e) {

        }


        folderList = new ArrayList<>();
        photoDataArrayList = new ArrayList<>();
        bucketimagesDataPhotoHashMap = new LinkedHashMap<>();
        isComplete = false;

        Observable.fromCallable(() -> {
            Log.e("ImageGet", "service photo getting start....");

            folderList.clear();
            photoDataArrayList.clear();
            bucketimagesDataPhotoHashMap.clear();
            folderList = getImagesList();
            return true;
        }).subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    isComplete = true;
                    Intent intent1 = new Intent("LoardDataComplete");
                    intent1.putExtra("completed", true);
                    sendBroadcast(intent1);
                })
                .subscribe((result) -> {
                    isComplete = true;
                    Intent intent1 = new Intent("LoardDataComplete");
                    intent1.putExtra("completed", true);
                    Log.e("ImageGet", "service photo set list....");
                    sendBroadcast(intent1);
                });

        return super.onStartCommand(intent, flags, startId);
    }

    public ArrayList<String> getImagesList() {
        List<Object> photoList = new ArrayList<>();


        Cursor mCursor = null;

        String BUCKET_DISPLAY_NAME;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BUCKET_DISPLAY_NAME = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
        } else
            BUCKET_DISPLAY_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;


        String[] projection = {MediaStore.Images.Media.DATA/*, MediaStore.Images.Media.TITLE*/
//                , BUCKET_DISPLAY_NAME
                , MediaStore.MediaColumns.DATE_MODIFIED
                , MediaStore.MediaColumns.DISPLAY_NAME
                , MediaStore.MediaColumns.SIZE
                ,MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                //*MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                // , MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media._ID
        };

        Uri uri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        mCursor = getContentResolver().query(
                uri, // Uri
                projection, // Projection
                null,
                null,
                "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");

        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
//        SimpleDateFormat format = new SimpleDateFormat("MMM yyyy", Locale.US);
        SimpleDateFormat format2 = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

        if (mCursor != null) {

            mCursor.moveToFirst();


            Date date = null;

            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) { //2sec
                long size = mCursor.getLong(mCursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
                if (size != 0) {

                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                    String bucketName = mCursor.getString(mCursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME));


                    long d = mCursor.getLong(mCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
                    d = d * 1000;
                    String strDate = format.format(d);

                    PhotoData imagesData = new PhotoData();
                    imagesData.setFilePath(path);
                    imagesData.setFileName(title);
                    imagesData.setSize(size);
                    imagesData.setDateValue(d);
                    imagesData.setFolder(bucketName);

                    photoDataArrayList.add(imagesData);

                    if (bucketimagesDataPhotoHashMap.containsKey(bucketName)) {
                        ArrayList<PhotoData> imagesData1 = bucketimagesDataPhotoHashMap.get(bucketName);
                        if (imagesData1 == null)
                            imagesData1 = new ArrayList<>();

                        imagesData1.add(imagesData);
                        bucketimagesDataPhotoHashMap.put(bucketName, imagesData1);

                    } else {
                        ArrayList<PhotoData> imagesData1 = new ArrayList<>();
                        imagesData1.add(imagesData);
                        bucketimagesDataPhotoHashMap.put(bucketName, imagesData1);
                    }

                }
            }

            mCursor.close();
        }


        if (photoDataArrayList != null && photoDataArrayList.size() != 0) {

            LinkedHashMap<String, ArrayList<PhotoData>> hashMap = new LinkedHashMap<>();
            hashMap.putAll(bucketimagesDataPhotoHashMap);
            bucketimagesDataPhotoHashMap.clear();
            bucketimagesDataPhotoHashMap = new LinkedHashMap<>();

            bucketimagesDataPhotoHashMap.put("All Images", photoDataArrayList);
            bucketimagesDataPhotoHashMap.putAll(hashMap);


        }

        Set<String> keys = bucketimagesDataPhotoHashMap.keySet();
        ArrayList<String> listkeys = new ArrayList<>();
        listkeys.addAll(keys);
        return listkeys;
    }


}
