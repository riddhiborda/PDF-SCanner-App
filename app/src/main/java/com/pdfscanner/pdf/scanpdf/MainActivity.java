package com.pdfscanner.pdf.scanpdf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ads.AdmobAdsManager;
import com.pdfscanner.pdf.scanpdf.adapter.RecentAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityMainBinding;
import com.pdfscanner.pdf.scanpdf.model.home.UpdateDelete;
import com.pdfscanner.pdf.scanpdf.model.home.RenameUpdate;
import com.pdfscanner.pdf.scanpdf.model.home.Update;
import com.pdfscanner.pdf.scanpdf.model.PDFModel;
import com.pdfscanner.pdf.scanpdf.rating.BaseRatingBar;
import com.pdfscanner.pdf.scanpdf.rating.RotationRatingBar;
import com.pdfscanner.pdf.scanpdf.ui.DocumentActivity;
import com.pdfscanner.pdf.scanpdf.ui.EditScanActivity;
import com.pdfscanner.pdf.scanpdf.ui.ImageShowActivity;
import com.pdfscanner.pdf.scanpdf.ui.ImageToPdfActivity;
import com.pdfscanner.pdf.scanpdf.ui.QrScanActivity;
import com.pdfscanner.pdf.scanpdf.ui.SettingActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    ArrayList<PDFModel> recentList = new ArrayList<>();
    public static String type;
    public static int imageCount = 1;
    AdmobAdsManager admobAdsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        admobAdsManager = AdmobAdsManager.getInstance(this);

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
        rateUsDialog.setContentView(R.layout.rate_us_dialog);
        rateUsDialog.setCancelable(true);
        rateUsDialog.setCanceledOnTouchOutside(true);

        TextView btn_rate = rateUsDialog.findViewById(R.id.btn_rate);
        ImageView btn_later = rateUsDialog.findViewById(R.id.btn_later);

        RotationRatingBar rotationRatingBar = rateUsDialog.findViewById(R.id.rotationratingbar_main);
        rotationRatingBar.setOnRatingChangeListener((ratingBar, rating, fromUser) -> rating_count = rating);

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
        builder.setPositiveButton(Html.fromHtml("<font color='#218dff'>Yes</font>") , (dialogInterface, which) -> {
            dialogInterface.dismiss();
            Constant.hideOpenAd();
            finish();
        });

        builder.setNegativeButton(Html.fromHtml("<font color='#218dff'>No</font>") , (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public void intView() {
        binding.ivSetting.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));

        binding.btnDocument.setOnClickListener(v -> {
            type = Constant.document;
            int counter = PreferencesManager.getInteger(MainActivity.this,Constant.MAIN_ACTIVITY);
            PreferencesManager.saveInteger(MainActivity.this,Constant.MAIN_ACTIVITY,counter + 1);

            if (!PreferencesManager.getString(MainActivity.this,Constant.INTERSTITIAL_ID).isEmpty()){
                if (counter == PreferencesManager.getInteger(MainActivity.this,Constant.ADS_COUNTER)){
                    admobAdsManager.loadInterstitialAd(MainActivity.this, PreferencesManager.getString(MainActivity.this,Constant.INTERSTITIAL_ID), 3, new AdmobAdsManager.OnAdClosedListener() {
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
        });

        binding.btnBusinessCard.setOnClickListener(v -> {
            type = Constant.business;
            imageCount = 1;
            getImgFromCamera();
        });

        binding.btnIdCard.setOnClickListener(v -> {
            type = Constant.idCard;
            imageCount = 1;
            getImgFromCamera();
        });

        binding.btnQrCode.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, QrScanActivity.class)));

        binding.btnOcr.setOnClickListener(v -> {
            type = Constant.ocr;
            getImgFromCamera();
        });

        binding.txtSeeMore.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DocumentActivity.class)));

        binding.btnJpgToPdf.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ImageToPdfActivity.class)));
        intAdapter();
        getRecentData();
    }

    public void intAdapter() {
        adapter = new RecentAdapter(MainActivity.this, recentList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((position, v) -> {
            pos = position;
            startActivityForResult(new Intent(MainActivity.this, ImageShowActivity.class)
                    .putExtra("filePath", recentList.get(position).getFilePath()), 101);
        });
    }

    @SuppressLint("CheckResult")
    public void getRecentData() {
        Observable.fromCallable(() -> {
            getRecentList();
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> runOnUiThread(this::setAdapter))
                .subscribe((result) -> runOnUiThread(this::setAdapter));
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

    public ArrayList<PDFModel> getRecentList() {
        ArrayList<PDFModel> list = new ArrayList<>();

        File sd = getCacheDir();
        File subfolder = new File(sd, "/" + getString(R.string.app_name));
        if (!subfolder.isDirectory()) {
            subfolder.mkdir();
        }

        getAllData(subfolder);

        return list;
    }

    private ArrayList<PDFModel> getAllData(File dir){
        ArrayList<PDFModel> list = new ArrayList<>();

        File[] files = dir.listFiles();
        for (File file : files) {
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
        }

        Collections.sort(list, (p1, p2) -> {
            long date1 = new File(p1.getFilePath()).lastModified();
            long date2 = new File(p2.getFilePath()).lastModified();
            return Utils.getData(date1).after(Utils.getData(date2)) ? -1 : 1;
        });

        recentList.addAll(list);

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
        Subscription subscription = RxBus.getInstance().toObservable(RenameUpdate.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(event -> {
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
                }, throwable -> {
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public void recentDeleteUpdate() {
        Subscription subscription = RxBus.getInstance().toObservable(UpdateDelete.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<UpdateDelete>() {
                    @Override
                    public void call(UpdateDelete event) {
                        if (event.getFilePath() != null && !event.getFilePath().equals("")) {
                            if (recentList != null && recentList.size() != 0) {
                                for (int k = 0; k < recentList.size(); k++) {
                                    if (recentList.get(k).getFilePath().equalsIgnoreCase(event.getFilePath())) {
                                        recentList.remove(k);
                                        if (adapter != null)
                                            adapter.notifyItemRemoved(k);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }, throwable -> {
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public void recentUpdate() {
        Subscription subscription = RxBus.getInstance().toObservable(Update.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(event -> {
                    if (event.getFilePath() != null && !event.equals("")) {
                        File file = new File(event.getFilePath());
                        if (file.exists()) {

                            if (contains(types, file.getPath())) {

                                PDFModel model = new PDFModel();
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

                                runOnUiThread(() -> {
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        setAdapter();
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
                }, throwable -> {
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }
}