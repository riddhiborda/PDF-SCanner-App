package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityOcrBinding;
import com.pdfscanner.pdf.scanpdf.listener.HomeUpdate;
import com.pdfscanner.pdf.scanpdf.pdf.ImageToPDFOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import io.reactivex.Observable;

public class OcrActivity extends AppCompatActivity {

    ActivityOcrBinding binding;

    Bitmap bitmap;

    TextRecognizer textRecognizer;
    SparseArray<TextBlock> textBlocks;
    String copyText;

    int imageQuality = 30;
    ImageToPDFOptions mPdfOptions;
    private int mPageColor;
    private String mPageNumStyle;

    ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ocr);

        intView();
    }

    public void intView() {
        bitmap = Constant.cropBitmap1;

        binding.imageView.setImageBitmap(bitmap);

        textRecognizer = new TextRecognizer.Builder(OcrActivity.this).build();

        getOcrData();


        binding.btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (copyText != null) {

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", copyText);
                    if (clipboard == null || clip == null) return;
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(OcrActivity.this, "Text copied!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(OcrActivity.this, "Text not recognized", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadingDialog = new ProgressDialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Saving...");
//            loadingDialog.setMessage("Create pdf...");
        loadingDialog.setCanceledOnTouchOutside(false);

    }


    public void seTexttData() {

        if (textBlocks != null && textBlocks.size() != 0) {
            copyText = "";
            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                String imageText = textBlock.getValue();

                //   imageText = imageText.replaceAll("[\r\n]+", " ");


                String[] separated = imageText.split("[\r\n]+");

                if (separated != null && separated.length != 0) {

                    for (int j = 0; j < separated.length; j++) {

                        String textImage = separated[j];
                        if (textImage != null && !textImage.equalsIgnoreCase("")) {

                            TextView textView = new TextView(this);
                            textView.setText(textImage);
                            textView.setTextSize(15);
                            textView.setTextColor(getResources().getColor(R.color.black));

                            textView.setPadding(0, 28, 0, 28);

                            LinearLayout ll = new LinearLayout(this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen._1sdp));
                            params.setMarginStart(getResources().getDimensionPixelSize(R.dimen._6sdp));

                            ll.setLayoutParams(params);
                            ll.setOrientation(LinearLayout.VERTICAL);
                            ll.setBackgroundColor(getResources().getColor(R.color.gray_divider));

                            binding.llList.addView(textView);
                            binding.llList.addView(ll);

                            if (copyText.equals("")) {
                                copyText = textImage;
                            } else {
                                copyText = copyText + "\n" + textImage;
                            }
                        }
                    }
                } else {

                    TextView textView = new TextView(this);
                    textView.setText(imageText);
                    textView.setTextSize(15);
                    textView.setTextColor(getResources().getColor(R.color.black));

                    textView.setPadding(0, 28, 0, 28);

                    LinearLayout ll = new LinearLayout(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen._1sdp));
                    params.setMarginStart(getResources().getDimensionPixelSize(R.dimen._6sdp));

                    ll.setLayoutParams(params);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.setBackgroundColor(getResources().getColor(R.color.gray_divider));

                    binding.llList.addView(textView);
                    binding.llList.addView(ll);

                    if (copyText.equals("")) {
                        copyText = imageText;
                    } else {
                        copyText = copyText + "\n" + imageText;
                    }
                }
            }

        }
    }

    public void getOcrData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();

        Observable.fromCallable(() -> {
            textBlocks = textRecognizer.detect(imageFrame);
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        seTexttData();

                    });
                })
                .subscribe((result) -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        seTexttData();
                    });
                });
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
            loadingDialog.setMessage("Saving...");
            loadingDialog.show();
        }
        setPdfIntview();

        new Thread(OcrActivity.this::cratePdfFile).start();
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


        File folderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name));
        if (!folderFile.exists())
            folderFile.mkdir();

        File dirFile = new File(folderFile.getPath() + "/" + Constant.hiddenImagePath);
        if (!dirFile.exists())
            dirFile.mkdir();

//        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String pdfName = Constant.ocr + "_" + System.currentTimeMillis() + ".pdf";
        String imageName = Constant.ocr + "_" + System.currentTimeMillis() + ".png";
        String textFileName = Constant.ocr + "_" + System.currentTimeMillis() + ".txt";

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

                MediaScannerConnection.scanFile(OcrActivity.this, new String[]{pictureFile.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
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
            Utils.deleteFiles(dirFile, OcrActivity.this);
        }

        // save ocr text file
        if (mSuccess && copyText != null){
            try {
                File textFile = new File(folderFile,textFileName);
                FileWriter writer = new FileWriter(textFile);
                writer.append(copyText);
                writer.flush();
                writer.close();

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        boolean finalMSuccess = mSuccess;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }

                if (finalMSuccess) {

                    MediaScannerConnection.scanFile(OcrActivity.this, new String[]{mPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                        }
                    });

                    RxBus.getInstance().post(new HomeUpdate(mPath));

                    Toast.makeText(OcrActivity.this, "OCR save successfully", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(OcrActivity.this, "Cretae pdf successfully", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    Toast.makeText(OcrActivity.this, "error", Toast.LENGTH_SHORT).show();
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