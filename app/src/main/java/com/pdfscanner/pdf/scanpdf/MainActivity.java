package com.pdfscanner.pdf.scanpdf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ad.AdmobAdManager;
import com.pdfscanner.pdf.scanpdf.adapter.RecentAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityMainBinding;
import com.pdfscanner.pdf.scanpdf.listener.HomeDeleteUpdate;
import com.pdfscanner.pdf.scanpdf.listener.HomeRenameUpdate;
import com.pdfscanner.pdf.scanpdf.listener.HomeUpdate;
import com.pdfscanner.pdf.scanpdf.model.PdfModel;
import com.pdfscanner.pdf.scanpdf.rating.BaseRatingBar;
import com.pdfscanner.pdf.scanpdf.rating.RotationRatingBar;
import com.pdfscanner.pdf.scanpdf.ui.DocumentActivity;
import com.pdfscanner.pdf.scanpdf.ui.EditScanActivity;
import com.pdfscanner.pdf.scanpdf.ui.ImageShowActivity;
import com.pdfscanner.pdf.scanpdf.ui.ImageToPdfActivity;
import com.pdfscanner.pdf.scanpdf.ui.QrScanActivity;
import com.pdfscanner.pdf.scanpdf.ui.SettingActivity;
import com.pdfscanner.pdf.scanpdf.ui.SplashActivity;
import com.pdfscanner.pdf.scanpdf.ui.SubscriptionActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static String mCurrentPhotoPath;
    RecentAdapter adapter;
    ArrayList<PdfModel> recentList = new ArrayList<>();

    public static String type;
    public static int imageCount = 1;
    AdmobAdManager admobAdManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        admobAdManager = AdmobAdManager.getInstance(this);

        intView();

        recentUpdate();
        recentDeleteUpdate();
        recentRenameUpdate();
        Constant.showOpenAd();
    }

    @Override
    public void onBackPressed() {
        if (!PreferencesManager.getRate(MainActivity.this)) {
            showRateUsDialog();
        } else {
            closeDialog();
        }
    }

    float rating_count = 0;

    public void showRateUsDialog() {
        rating_count = 0;
        final Dialog rateUsDialog = new Dialog(this, R.style.WideDialog);
        rateUsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        rateUsDialog.setContentView(R.layout.dialog_rate_us);
        rateUsDialog.setCancelable(true);
        rateUsDialog.setCanceledOnTouchOutside(true);

        TextView btn_rate = rateUsDialog.findViewById(R.id.btn_rate);
        ImageView btn_later = rateUsDialog.findViewById(R.id.btn_later);

        RotationRatingBar rotationRatingBar = rateUsDialog.findViewById(R.id.rotationratingbar_main);
        rotationRatingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                rating_count = rating;
            }
        });

        btn_later.setOnClickListener(v -> {
            rateUsDialog.dismiss();
            Constant.hideOpenAd();
            finish();
        });

        btn_rate.setOnClickListener(v -> {
            if (rating_count >= 4) {
                PreferencesManager.setRateUs(this, true);
                // playStore
                String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                rateUsDialog.dismiss();
                Constant.hideOpenAd();
                finish();
            } else if (rating_count <= 3 && rating_count > 0) {
                PreferencesManager.setRateUs(this, true);
                // feed back
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + Uri.encode(getResources().getString(R.string.feedback_email))));

                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email via..."));
                    rateUsDialog.dismiss();
                    finish();
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this,
                            "There are no email clients installed.", Toast.LENGTH_SHORT)
                            .show();
                    rateUsDialog.dismiss();
                }

            } else {
                Toast.makeText(this, "Please click on 5 Star to give us rating on playstore.", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        rateUsDialog.show();
    }

    private void closeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure to Exit app??");
        builder.setCancelable(false);
        builder.setPositiveButton(Html.fromHtml("<font color='#218dff'>Yes</font>") , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                dialogInterface.dismiss();
                Constant.hideOpenAd();
                finish();
            }
        });

        builder.setNegativeButton(Html.fromHtml("<font color='#218dff'>No</font>") , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.show();
    }

    public void intView() {

        /*if (!PreferencesManager.getSubscription(MainActivity.this)) {
            startActivity(new Intent(MainActivity.this, SubscriptionActivity.class).putExtra("ShowAd", true));
        }*/

        binding.ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });

        binding.btnDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = Constant.document;
                int counter = PreferencesManager.getInteger(MainActivity.this,Constant.MAIN_ACTIVITY);
                PreferencesManager.saveInteger(MainActivity.this,Constant.MAIN_ACTIVITY,counter + 1);

                if (!PreferencesManager.getString(MainActivity.this,Constant.INTERSTITIAL_ID).isEmpty()){
                    if (counter == PreferencesManager.getInteger(MainActivity.this,Constant.ADS_COUNTER)){
                        admobAdManager.loadInterstitialAd(MainActivity.this, PreferencesManager.getString(MainActivity.this,Constant.INTERSTITIAL_ID), 3, new AdmobAdManager.OnAdClosedListener() {
                            @Override
                            public void onAdClosed(Boolean isShowADs) {
                                PreferencesManager.saveInteger(MainActivity.this,Constant.MAIN_ACTIVITY,0);
                                getImgFromCamera();
                            }
                        });
                    }else {
                        getImgFromCamera();
                    }
                }else {
                    Utils.getAdsIds(MainActivity.this);
                    getImgFromCamera();
                }
            }
        });

        binding.btnBusinessCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = Constant.business;
                imageCount = 1;
                getImgFromCamera();
            }
        });


        binding.btnIdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = Constant.idCard;
                imageCount = 1;
                getImgFromCamera();
            }
        });

        binding.btnQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QrScanActivity.class));
            }
        });

        binding.btnOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = Constant.ocr;
                getImgFromCamera();
            }
        });

        binding.txtSeeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DocumentActivity.class));
            }
        });

        binding.btnJpgToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!PreferencesManager.getSubscription(MainActivity.this))
//                    startActivityForResult(new Intent(MainActivity.this, SubscriptionActivity.class),55);
//                else
//                    startActivity(new Intent(MainActivity.this, ImageToPdfActivity.class));
                startActivity(new Intent(MainActivity.this, ImageToPdfActivity.class));
            }
        });

        /*binding.ivPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
            }
        });*/

//        if (PreferencesManager.getSubscription(MainActivity.this)) {
//            binding.ivPremium.setVisibility(View.GONE);
//        } else {
//            binding.ivPremium.setVisibility(View.VISIBLE);
//        }

        intAdapter();


        getRecentData();

    }

    public void intAdapter() {
        adapter = new RecentAdapter(MainActivity.this, recentList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new RecentAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                pos = position;
                startActivityForResult(new Intent(MainActivity.this, ImageShowActivity.class)
                        .putExtra("filePath", recentList.get(position).getFilePath()), 101);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (PreferencesManager.getSubscription(MainActivity.this)) {
//            binding.ivPremium.setVisibility(View.GONE);
//        } else {
//            binding.ivPremium.setVisibility(View.VISIBLE);
//        }
    }

    public void getRecentData() {
        Observable.fromCallable(() -> {
            getRecentList();
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> {
                    runOnUiThread(() -> {
//                        binding.progressBar.setVisibility(View.GONE);
                        setAdapter();

                    });
                })
                .subscribe((result) -> {
                    runOnUiThread(() -> {
//                        binding.progressBar.setVisibility(View.GONE);
                        setAdapter();
                    });
                });

    }

    int pos = -1;

    public void setAdapter() {
        if (recentList != null && recentList.size() != 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.loutRecent.setVisibility(View.VISIBLE);

        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.loutRecent.setVisibility(View.GONE);
        }

        if (adapter == null) {
            intAdapter();
        }

    }

//    @Override
//    protected void onSaveInstanceState(@NonNull @org.jetbrains.annotations.NotNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBundle("type",type);
//        outState.putBundle("path",mCurrentPhotoPath);
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

                try {


                    if (mCurrentPhotoPath != null && !mCurrentPhotoPath.equalsIgnoreCase("")) {

                        File file = new File(mCurrentPhotoPath);
                        String path = file.getAbsolutePath();
                        if (file.exists()) {

                            MediaScannerConnection.scanFile(MainActivity.this, new String[]{file.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                                }
                            });
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            boolean isDefault = false;
                            Bitmap rotatedBitmap = null;
                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    isDefault = true;
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    isDefault = true;
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    isDefault = true;
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    isDefault = false;
                                    rotatedBitmap = bitmap;
                            }

                            if (isDefault) {

                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String imageFileName = "JPEG_" + timeStamp + "_";


                                File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                                if (!storageDir.exists()) {
                                    storageDir.mkdirs();
                                }
                                File image = File.createTempFile(
                                        imageFileName,  /* prefix */
                                        ".jpg",         /* suffix */
                                        storageDir      /* directory */
                                );

                                // Save a file: path for use with ACTION_VIEW intents
                                //   mCurrentPhotoPath = image.getPath();

                                FileOutputStream out = null;
                                String filepath = image.getPath();

                                File newFile = new File(filepath);
                                try {
                                    out = new FileOutputStream(filepath);

                                    //write the compressed bitmap at the destination specified by filename.
                                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

                                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri contentUri = Uri.fromFile(newFile);
                                    mediaScanIntent.setData(contentUri);
                                    sendBroadcast(mediaScanIntent);

                                    MediaScannerConnection.scanFile(this, new String[]{newFile.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri) {
                                            // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                                        }
                                    });

                                    mCurrentPhotoPath = newFile.getPath();
                                    Utils.deleteFiles(file, MainActivity.this);

                                    MediaScannerConnection.scanFile(this, new String[]{file.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri) {
                                            // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }

                    if (type.equalsIgnoreCase(Constant.document) || type.equalsIgnoreCase(Constant.ocr)) {

                        startActivity(new Intent(MainActivity.this, EditScanActivity.class).putExtra("file", mCurrentPhotoPath).putExtra("type", type));


                    } else if (type.equalsIgnoreCase(Constant.business) || type.equalsIgnoreCase(Constant.idCard)) {

                        startActivityForResult(new Intent(MainActivity.this, EditScanActivity.class)
                                .putExtra("imageCount", imageCount).putExtra("file", mCurrentPhotoPath).putExtra("type", type), 102);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Constant.showOpenAd();
                    }
                }, 1000);
            }
        }

        if (requestCode == 102) {
            if (resultCode == RESULT_OK ) {

                imageCount++;
                getImgFromCamera();

            } else {
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Constant.showOpenAd();
                    }
                }, 1000);
            }
        }
        if (resultCode == RESULT_OK && requestCode == 101) {

            recentList.remove(pos);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            if (recentList != null && recentList.size() != 0) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.loutRecent.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.loutRecent.setVisibility(View.GONE);
            }

        }

        if (resultCode == RESULT_OK && requestCode == 55) {
            startActivity(new Intent(MainActivity.this, ImageToPdfActivity.class));
        }
    }

    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    private void getImgFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = null;
        try {
            Constant.hideOpenAd();
            file = createImageFile();
            Uri mUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

            startActivityForResult(cameraIntent, 2);

        } catch (IOException e) {
            e.printStackTrace();
            Constant.showOpenAd();
        }

    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    String[] types = new String[]{".pdf"};

    public ArrayList<PdfModel> getRecentList() {

        ArrayList<PdfModel> list = new ArrayList<>();

        Cursor mCursor = null;
        String sortOrder = "LOWER(" + MediaStore.Files.FileColumns.DATE_MODIFIED + ") DESC"; // unordered

        final String[] projection = {MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE, MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME};

//        String[] projection = {MediaStore.Images.Media.DATA
//                , MediaStore.MediaColumns.DATE_MODIFIED
//                , MediaStore.MediaColumns.DISPLAY_NAME
//                , MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//                , MediaStore.MediaColumns.SIZE
//        };

        Uri uri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        mCursor = getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection, // Projection
                null,
                null,
                sortOrder);

        if (mCursor != null) {

            mCursor.moveToFirst();

            String strFolderName = getString(R.string.app_name);

            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                long size = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                if (size != 0) {

                    String bucketName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME));

                    if (bucketName != null && bucketName.equalsIgnoreCase(getString(R.string.app_name))) {

                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                        if (path != null && contains(types, path)) {
                            if (!Utils.isPDFEncrypted(path)) {

                                String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));

                                PdfModel model = new PdfModel();
                                model.setFilePath(path);
                                model.setFileName(title);
                                model.setSize(size);

                                recentList.add(model);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (adapter != null) {
                                            adapter.notifyItemInserted(recentList.size());

                                            if (recentList != null && recentList.size() != 0) {
                                                binding.recyclerView.setVisibility(View.VISIBLE);
                                                binding.loutRecent.setVisibility(View.VISIBLE);

                                            } else {
                                                binding.recyclerView.setVisibility(View.GONE);
                                                binding.loutRecent.setVisibility(View.GONE);
                                            }
                                        }

                                    }
                                });

                            }
                        }

                    }

                }

            }

            mCursor.close();
        }

        return list;
    }

    boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) {
                return true;
            }
        }
        return false;
    }


    public void recentRenameUpdate() {

        Subscription subscription = RxBus.getInstance().toObservable(HomeRenameUpdate.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<HomeRenameUpdate>() {
                    @Override
                    public void call(HomeRenameUpdate event) {
                        if (event.getOldFile() != null && !event.getOldFile().equals("") && event.getRenameFile() != null && !event.getRenameFile().equals("")) {

                            if (recentList != null && recentList.size() != 0) {

                                for (int k = 0; k < recentList.size(); k++) {

                                    if (recentList.get(k).getFilePath().equalsIgnoreCase(event.getOldFile())) {
                                        File file = new File(event.getRenameFile());

                                        recentList.get(k).setFilePath(file.getPath());
                                        recentList.get(k).setFileName(file.getName());

                                        if (adapter != null)
                                            adapter.notifyItemChanged(k);

                                        break;
                                    }


                                }
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public void recentDeleteUpdate() {

        Subscription subscription = RxBus.getInstance().toObservable(HomeDeleteUpdate.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<HomeDeleteUpdate>() {
                    @Override
                    public void call(HomeDeleteUpdate event) {
                        if (event.getFilePath() != null && !event.getFilePath().equals("")) {

                            if (recentList != null && recentList.size() != 0) {

                                for (int k = 0; k < recentList.size(); k++) {

                                    if (recentList.get(k).getFilePath().equalsIgnoreCase(event.getFilePath())) {
                                        recentList.remove(k);
                                        if (adapter != null)
                                            adapter.notifyItemRemoved(k);
//                                            adapter.notifyDataSetChanged();
//                                            adapter.notifyItemRemoved(k);

                                        break;
                                    }


                                }
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public void recentUpdate() {

        Subscription subscription = RxBus.getInstance().toObservable(HomeUpdate.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<HomeUpdate>() {
                    @Override
                    public void call(HomeUpdate event) {
                        if (event.getFilePath() != null && !event.equals("")) {

                            File file = new File(event.getFilePath());
                            if (file.exists()) {

                                if (contains(types, file.getPath())) {

                                    PdfModel model = new PdfModel();
                                    model.setFilePath(file.getPath());
                                    model.setFileName(file.getName());
                                    model.setSize(file.length());

                                    recentList.add(0, model);

                                    if (recentList != null && recentList.size() != 0) {
                                        binding.recyclerView.setVisibility(View.VISIBLE);
                                        binding.loutRecent.setVisibility(View.VISIBLE);

                                    } else {
                                        binding.recyclerView.setVisibility(View.GONE);
                                        binding.loutRecent.setVisibility(View.GONE);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (adapter != null) {
                                                adapter.notifyDataSetChanged();
//                                                adapter.setData(recentList);
//
//                                        adapter.notifyItemInserted(0);
                                            } else {
                                                setAdapter();
                                            }


                                        }
                                    });

                                }

                            }

                        } else {

                            if (recentList != null && recentList.size() != 0) {
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                binding.loutRecent.setVisibility(View.VISIBLE);

                            } else {
                                binding.recyclerView.setVisibility(View.GONE);
                                binding.loutRecent.setVisibility(View.GONE);
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            } else {
                                setAdapter();
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

}