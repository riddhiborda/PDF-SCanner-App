package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfscanner.pdf.scanpdf.MainActivity;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ad.AdmobAdManager;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityPreviewBinding;
import com.pdfscanner.pdf.scanpdf.listener.HomeUpdate;
import com.pdfscanner.pdf.scanpdf.pdf.ImageToPDFOptions;

import java.io.File;
import java.io.FileOutputStream;

public class PreviewActivity extends AppCompatActivity {

    ActivityPreviewBinding binding;

    Bitmap bitmap;
    String type;
    int imageQuality = 30;
    ImageToPDFOptions mPdfOptions;
    private int mPageColor;
    private String mPageNumStyle;

    ProgressDialog loadingDialog;
    AdmobAdManager admobAdManager;

    boolean isOriginalClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview);

        admobAdManager = AdmobAdManager.getInstance(this);

        int counter = PreferencesManager.getInteger(PreviewActivity.this,Constant.PREVIEW);
        PreferencesManager.saveInteger(PreviewActivity.this,Constant.PREVIEW,counter + 1);

        if (!PreferencesManager.getString(PreviewActivity.this,Constant.INTERSTITIAL_ID).isEmpty()){
            if (counter == PreferencesManager.getInteger(PreviewActivity.this,Constant.ADS_COUNTER)){
                admobAdManager.loadInterstitialAd(PreviewActivity.this, PreferencesManager.getString(PreviewActivity.this,Constant.INTERSTITIAL_ID), 1, new AdmobAdManager.OnAdClosedListener() {
                    @Override
                    public void onAdClosed(Boolean isShowADs) {
                        PreferencesManager.saveInteger(PreviewActivity.this,Constant.PREVIEW,0);
                    }
                });
            }
        }else {
            Utils.getAdsIds(PreviewActivity.this);
        }

        intView();
    }

    public void intView() {

        Intent intent = getIntent();

        if (intent != null) {
            type = intent.getStringExtra("type");
        }

        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        bitmap = Constant.EditBitmap;
        binding.image.setImageBitmap(bitmap);

        binding.btnOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageQuality = 30;
                isOriginalClick = true;
//                String saveFile = saveImage();


                saveImage();

//                if (saveFile != null) {
//                    RxBus.getInstance().post(new HomeUpdate(saveFile));
//                    finish();
//                } else {
//                    Toast.makeText(PreviewActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        binding.btnPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PreferencesManager.getSubscription(PreviewActivity.this))
                    startActivity(new Intent(PreviewActivity.this, SubscriptionActivity.class));
                else {
                    imageQuality = 100;
                    isOriginalClick = false;

                    saveImage();
                }

            }
        });

        loadingDialog = new ProgressDialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
//        loadingDialog.setMessage("Create pdf...");
        loadingDialog.setMessage("Saving...");
        loadingDialog.setCanceledOnTouchOutside(false);

    }

    public void setPdfIntview() {

        mPdfOptions = new ImageToPDFOptions();

        mPdfOptions.setBorderWidth(0);

        mPageColor = Color.WHITE;
        mPdfOptions.setQualityString(Integer.toString(imageQuality));
        mPdfOptions.setPageSize("A4");
        mPdfOptions.setPasswordProtected(false);
        mPdfOptions.setImageScaleType("maintain_aspect_ratio");
        mPdfOptions.setMasterPwd("PDF Converter");
        mPdfOptions.setPageColor(mPageColor);
        mPdfOptions.setMargins(0, 0, 0, 0);

    }

    public void saveImage() {

        if (loadingDialog != null) {
//            loadingDialog.setMessage("Create pdf...");
            loadingDialog.setMessage("Saving...");
            loadingDialog.show();
        }
        setPdfIntview();

        new Thread(PreviewActivity.this::cratePdfFile).start();

//        Bitmap bm = bitmap;
////        Bitmap bm  = ((BitmapDrawable) binding.image.getDrawable()).getBitmap();
//
//
//        File folderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name));
//        if (!folderFile.exists())
//            folderFile.mkdir();
//
//        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//
//
//        String imageName = type + " " + timeStamp + ".png";
//        File pictureFile = new File(folderFile.getPath() + "/" + imageName);
//        if (bitmap != null) {
//            FileOutputStream out = null;
//            try {
//                out = new FileOutputStream(pictureFile.getPath());
//
//                //write the compressed bitmap at the destination specified by filename.
//                bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, out);
//
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(pictureFile);
//                mediaScanIntent.setData(contentUri);
//                sendBroadcast(mediaScanIntent);
//
//                MediaScannerConnection.scanFile(PreviewActivity.this, new String[]{pictureFile.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
//                    public void onScanCompleted(String path, Uri uri) {
//                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
//                    }
//                });
////                return pictureFile.getPath();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e("error", "Message: " + e.getMessage());
//            }
//        }


    }

    String pdfpath;

    public void cratePdfFile() {

        String mPassword;
        String mQualityString;
        int mBorderWidth;
        boolean mSuccess;
        String mPath;
        String mPageSize;
        boolean mPasswordProtected;
        int mMarginTop;
        int mMarginBottom;
        int mMarginRight;
        int mMarginLeft;
        String mImageScaleType;
        String mPageNumStyle;
        String mMasterPwd;
        int mPageColor;


        mPassword = mPdfOptions.getPassword();
        mQualityString = mPdfOptions.getQualityString();
//        mOnPDFCreatedInterface = onPDFCreated;
        mPageSize = mPdfOptions.getPageSize();
        mPasswordProtected = mPdfOptions.isPasswordProtected();
        mBorderWidth = mPdfOptions.getBorderWidth();
//        mWatermark = mPdfOptions.getWatermark();
        mMarginTop = mPdfOptions.getMarginTop();
        mMarginBottom = mPdfOptions.getMarginBottom();
        mMarginRight = mPdfOptions.getMarginRight();
        mMarginLeft = mPdfOptions.getMarginLeft();
        mImageScaleType = mPdfOptions.getImageScaleType();
        mPageNumStyle = mPdfOptions.getPageNumStyle();
        mMasterPwd = mPdfOptions.getMasterPwd();
        mPageColor = mPdfOptions.getPageColor();
        boolean isCreated = false;
        File folderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name));
        if (!folderFile.exists())
            folderFile.mkdirs();

        File dirFile = new File(folderFile.getPath() + "/" + Constant.hiddenImagePath);
        if (!dirFile.exists())
            isCreated = dirFile.mkdirs();

//        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String pdfName = type + "_" + System.currentTimeMillis() + ".pdf";
        String imageName = type + "_" + System.currentTimeMillis() + ".png";

        mPath = folderFile.getPath() + "/" + pdfName;
        pdfpath = mPath;
        mSuccess = true;

        Bitmap bm = bitmap;

        File pictureFile = new File(dirFile.getPath() + "/" + imageName);
        if (bitmap != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(pictureFile.getPath());

                //write the compressed bitmap at the destination specified by filename.
                bitmap.compress(Bitmap.CompressFormat.PNG, imageQuality, out);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(pictureFile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                MediaScannerConnection.scanFile(PreviewActivity.this, new String[]{pictureFile.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });
                mSuccess = true;
//                return pictureFile.getPath();

            } catch (Exception e) {
                e.printStackTrace();
                mSuccess = false;
                Log.e("error", "Message: " + e.getMessage());
            }
        }


        if (mSuccess) {
            Rectangle pageSize = new Rectangle(PageSize.getRectangle(mPageSize));
            pageSize.setBackgroundColor(getBaseColor(mPageColor));
            Document document = new Document(pageSize,
                    mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
            document.setMargins(mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
            Rectangle documentRect = document.getPageSize();

            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mPath));

                if (mPasswordProtected) {
                    writer.setEncryption(mPassword.getBytes(), mMasterPwd.getBytes(),
                            PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                            PdfWriter.ENCRYPTION_AES_128);

                }

                document.open();

                int quality;
                quality = 30;
                if (mQualityString != null && !mQualityString.toString().trim().equals("")) {
                    quality = Integer.parseInt(mQualityString);
                }


//                int size = bitmap.getRowBytes() * bitmap.getHeight();
//                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//                bitmap.copyPixelsToBuffer(byteBuffer);
//                byte[] byteArray = byteBuffer.array();

                Image image = Image.getInstance(pictureFile.getPath());
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                double qualityMod = quality * 0.09;
                Log.e("save", "qualityMod: " + qualityMod);
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(mBorderWidth);


                float pageWidth = document.getPageSize().getWidth() - (mMarginLeft + mMarginRight);
                float pageHeight = document.getPageSize().getHeight() - (mMarginBottom + mMarginTop);
//            if (mImageScaleType.equals(Constants.IMAGE_SCALE_TYPE_ASPECT_RATIO))
//                image.scaleToFit(pageWidth, pageHeight);
//            else
//                image.scaleAbsolute(pageWidth, pageHeight);

                image.scaleToFit(pageWidth, pageHeight);

                image.setAbsolutePosition((documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2);

                document.add(image);

//                document.newPage();

                document.close();

            } catch (Exception e) {
                e.printStackTrace();
                mSuccess = false;
            }
        }
        if (dirFile.exists()) {
            Utils.deleteFiles(dirFile, PreviewActivity.this);
        }

        boolean finalMSuccess = mSuccess;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (finalMSuccess) {

                    MediaScannerConnection.scanFile(PreviewActivity.this, new String[]{mPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                        }
                    });

                    RxBus.getInstance().post(new HomeUpdate(mPath));

                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }

//                    Toast.makeText(PreviewActivity.this, "Cretae pdf successfully", Toast.LENGTH_SHORT).show();
                    if (isOriginalClick) {
                        int counter = PreferencesManager.getInteger(PreviewActivity.this,Constant.PREVIEW_ORIGINAL);
                        PreferencesManager.saveInteger(PreviewActivity.this,Constant.PREVIEW_ORIGINAL,counter + 1);

                        if (!PreferencesManager.getString(PreviewActivity.this,Constant.INTERSTITIAL_ID).isEmpty()){
                            if (counter == PreferencesManager.getInteger(PreviewActivity.this,Constant.ADS_COUNTER)){
                                admobAdManager.loadInterstitialAd(PreviewActivity.this, PreferencesManager.getString(PreviewActivity.this,Constant.INTERSTITIAL_ID), 1, new AdmobAdManager.OnAdClosedListener() {
                                    @Override
                                    public void onAdClosed(Boolean isShowADs) {
                                        PreferencesManager.saveInteger(PreviewActivity.this,Constant.PREVIEW_ORIGINAL,0);
                                        Toast.makeText(PreviewActivity.this, type + " save successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }else {
                                Toast.makeText(PreviewActivity.this, type + " save successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }else {
                            Utils.getAdsIds(PreviewActivity.this);
                            Toast.makeText(PreviewActivity.this, type + " save successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }else {
                        Toast.makeText(PreviewActivity.this, type + " save successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else {
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    Toast.makeText(PreviewActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private BaseColor getBaseColor(int color) {
        return new BaseColor(
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }


}