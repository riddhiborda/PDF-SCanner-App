package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ads.AdmobAdsManager;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityEditDocumentBinding;
import com.pdfscanner.pdf.scanpdf.model.home.Update;
import com.pdfscanner.pdf.scanpdf.pdf.ImageToPDFOptions;
import com.pdfscanner.pdf.scanpdf.stickerview.StickerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class EditDocumentActivity extends AppCompatActivity {

    ActivityEditDocumentBinding binding;
    int imageQuality = 30;
    ImageToPDFOptions mPdfOptions;
    private int mPageColor;
    private String mPageNumStyle;
    ProgressDialog loadingDialog;
    Bitmap bitmap;
    private ArrayList<View> mViews;
    private StickerView mCurrentView;
    String filePath;
    AdmobAdsManager admobAdsManager;
    boolean isAlredyExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_document);
        admobAdsManager = AdmobAdsManager.getInstance(this);
        intView();
    }

    public void intView() {
        Intent intent = getIntent();
        mViews = new ArrayList<>();
        if (intent != null) {
            filePath = intent.getStringExtra("filePath");

            binding.imgDisplay.getController().getSettings()
                    .setMaxZoom(6f)
                    .setMinZoom(0)
                    .setDoubleTapZoom(3f);

            Glide.with(EditDocumentActivity.this)
                    .load(Utils.getImageFromPdf(EditDocumentActivity.this, filePath))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.imgDisplay);

            binding.btnSignature.setOnClickListener(v -> startActivityForResult(new Intent(EditDocumentActivity.this, SignatureActivity.class), 50));

            binding.icBack.setOnClickListener(v -> onBackPressed());

            binding.btnSave.setOnClickListener(v -> showSaveDilaog(filePath));

            loadingDialog = new ProgressDialog(this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setCancelable(false);
            loadingDialog.setMessage("Saving...");
//            loadingDialog.setMessage("Create pdf...");
            loadingDialog.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50 && resultCode == RESULT_OK) {
            addStickerView(Constant.signatureBitmap);
        }
    }

    private void setCurrentEdit(StickerView stickerView) {
        if (mCurrentView != null) {
            mCurrentView.setInEdit(false);
        }
        mCurrentView = stickerView;
        stickerView.setInEdit(true);
    }

    private void setCurrentEditFalse() {
        if (mCurrentView != null) {
            mCurrentView.setInEdit(false);
        }
    }

    public void addStickerView(Bitmap img) {
        if (img != null) {
            final StickerView stickerView = new StickerView(this);
            stickerView.setBitmap(img);
            // stickerView.setImageResource(img);
            stickerView.setOperationListener(new StickerView.OperationListener() {
                @Override
                public void onDeleteClick() {
                    mViews.remove(stickerView);
                    binding.loutMain.removeView(stickerView);
                }

                @Override
                public void onEdit(StickerView stickerView) {
                    mCurrentView.setInEdit(false);
                    mCurrentView = stickerView;
                    mCurrentView.setInEdit(true);
                    Log.e("onEdit Stricker", " getX: " + stickerView.getX() + " getY: " + stickerView.getY());
                }

                @Override
                public void onTop(StickerView stickerView) {
                    int position = mViews.indexOf(stickerView);
                    if (position == mViews.size() - 1) {
                        return;
                    }
                    StickerView stickerTemp = (StickerView) mViews.remove(position);
                    mViews.add(mViews.size(), stickerTemp);
                }

            });
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(binding.loutMain.getWidth(), binding.loutMain.getHeight());
            binding.loutMain.addView(stickerView, lp);
            mViews.add(stickerView);
            setCurrentEdit(stickerView);
        }
    }

    public void setPdfIntview() {
        mPdfOptions = new ImageToPDFOptions();
        mPdfOptions.setBorderWidth(0);

        if (PreferencesManager.getSubscription(EditDocumentActivity.this)) {
            imageQuality = 100;
        } else {
            imageQuality = 30;
        }

        mPageColor = Color.WHITE;
        mPdfOptions.setQualityString(Integer.toString(imageQuality));
        mPdfOptions.setPageSize("A4");
        mPdfOptions.setPasswordProtected(false);
        mPdfOptions.setImageScaleType("maintain_aspect_ratio");
        mPdfOptions.setMasterPwd("PDF Converter");
        mPdfOptions.setPageColor(mPageColor);
        mPdfOptions.setMargins(0, 0, 0, 0);
    }

    public void showSaveDilaog(String filePath) {
        File file = new File(filePath);
        Dialog dialog = new Dialog(this, R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.edit_save_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        TextView save, newAdd;

        save = dialog.findViewById(R.id.txt_1);
        newAdd = dialog.findViewById(R.id.txt_2);

        save.setOnClickListener(v -> {
            dialog.dismiss();
            bitmap = getImage();
            if (bitmap != null) {
                isAlredyExit = true;
                saveImage(bitmap);
            }
        });

        newAdd.setOnClickListener(v -> {
            dialog.dismiss();
            bitmap = getImage();
            if (bitmap != null) {
                saveImage(bitmap);

            }
        });

        dialog.show();
    }

    public Bitmap getImage() {
        Bitmap bitmap;
        setCurrentEditFalse();
        binding.loutMain.setDrawingCacheEnabled(true);
        bitmap = binding.loutMain.getDrawingCache().copy(Bitmap.Config.ARGB_8888, false);
        binding.loutMain.destroyDrawingCache();
        return bitmap;
    }

    public void saveImage(Bitmap bitmap) {
        if (loadingDialog != null) {
            loadingDialog.setMessage("Saving...");
            loadingDialog.show();
        }
        setPdfIntview();
        new Thread(EditDocumentActivity.this::cratePdfFile).start();
    }

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
        String mMasterPwd;
        int mPageColor;

        mPassword = mPdfOptions.getPassword();
        mQualityString = mPdfOptions.getQualityString();
        mPageSize = mPdfOptions.getPageSize();
        mPasswordProtected = mPdfOptions.isPasswordProtected();
        mBorderWidth = mPdfOptions.getBorderWidth();
        mMarginTop = mPdfOptions.getMarginTop();
        mMarginBottom = mPdfOptions.getMarginBottom();
        mMarginRight = mPdfOptions.getMarginRight();
        mMarginLeft = mPdfOptions.getMarginLeft();
        mMasterPwd = mPdfOptions.getMasterPwd();
        mPageColor = mPdfOptions.getPageColor();

        File sd = getCacheDir();
        File rootfolder = new File(sd, "/" + getString(R.string.app_name));
        if (!rootfolder.isDirectory()) {
            rootfolder.mkdir();
        }

        File subfolder = new File(sd, "/" + getString(R.string.app_name) + "/" + Constant.hiddenImagePath);
        if (!subfolder.isDirectory()){
            subfolder.mkdir();
        }

        if (isAlredyExit) {
            Utils.deleteFiles(new File(filePath), EditDocumentActivity.this);
            MediaScannerConnection.scanFile(EditDocumentActivity.this, new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                }
            });
        }

        String pdfName = "Document_" + System.currentTimeMillis() + ".pdf";
        String imageName = "Document_" + System.currentTimeMillis() + ".png";

        if (isAlredyExit) {
            mPath = filePath;
        } else {
            mPath = rootfolder.getAbsolutePath() + "/" + pdfName;
        }

        String pdfpath = mPath;
        mSuccess = true;

        File pictureFile = new File(subfolder.getAbsoluteFile() + "/" + imageName);
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

                MediaScannerConnection.scanFile(EditDocumentActivity.this, new String[]{pictureFile.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });
                mSuccess = true;

            } catch (Exception e) {
                e.printStackTrace();
                mSuccess = false;
                Log.e("error", "Message: " + e.getMessage());
            }
        } else {
            mSuccess = false;
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

                Image image = Image.getInstance(pictureFile.getPath());
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                double qualityMod = quality * 0.09;
                Log.e("save", "qualityMod: " + qualityMod);
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(mBorderWidth);

                float pageWidth = document.getPageSize().getWidth() - (mMarginLeft + mMarginRight);
                float pageHeight = document.getPageSize().getHeight() - (mMarginBottom + mMarginTop);

                image.scaleToFit(pageWidth, pageHeight);

                image.setAbsolutePosition((documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2);
                document.add(image);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
                mSuccess = false;
            }
        }
        if (subfolder.exists()) {
            Utils.deleteFiles(subfolder, EditDocumentActivity.this);
        }

        boolean finalMSuccess = mSuccess;
        runOnUiThread(() -> {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }

            if (finalMSuccess) {
                MediaScannerConnection.scanFile(EditDocumentActivity.this, new String[]{mPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });

                if (isAlredyExit) {
                    RxBus.getInstance().post(new Update());
                } else {
                    RxBus.getInstance().post(new Update(mPath));
                }

                int counter = PreferencesManager.getInteger(EditDocumentActivity.this,Constant.EDIT_DOCUMENT);
                PreferencesManager.saveInteger(EditDocumentActivity.this,Constant.EDIT_DOCUMENT,counter + 1);

                if (!PreferencesManager.getString(EditDocumentActivity.this,Constant.INTERSTITIAL_ID).isEmpty()){
                    if (counter == PreferencesManager.getInteger(EditDocumentActivity.this,Constant.ADS_COUNTER)){
                        admobAdsManager.loadInterstitialAd(EditDocumentActivity.this, PreferencesManager.getString(EditDocumentActivity.this,Constant.INTERSTITIAL_ID), 1, new AdmobAdsManager.OnAdClosedListener() {
                            @Override
                            public void onAdClosed(Boolean isShowADs) {
                                Toast.makeText(EditDocumentActivity.this, "Document save successfully", Toast.LENGTH_SHORT).show();
                                PreferencesManager.saveInteger(EditDocumentActivity.this,Constant.EDIT_DOCUMENT,0);
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    }else {
                        Toast.makeText(EditDocumentActivity.this, "Document save successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }else {
                    Utils.getAdsIds(EditDocumentActivity.this);
                    Toast.makeText(EditDocumentActivity.this, "Document save successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

            } else {
                Toast.makeText(EditDocumentActivity.this, "error", Toast.LENGTH_SHORT).show();
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