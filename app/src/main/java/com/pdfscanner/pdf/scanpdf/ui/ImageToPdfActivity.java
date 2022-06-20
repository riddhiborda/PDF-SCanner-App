package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
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
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RecyclerTouchListener;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.adapter.PdfAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityImageToPdfBinding;
import com.pdfscanner.pdf.scanpdf.model.PdfModel;
import com.pdfscanner.pdf.scanpdf.pdf.ImageToPDFOptions;
import com.rajat.pdfviewer.PdfViewerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import io.reactivex.Observable;

public class ImageToPdfActivity extends AppCompatActivity {

    ActivityImageToPdfBinding binding;

    ImageToPDFOptions mPdfOptions;
    private int mPageColor;

    ProgressDialog loadingDialog;
    File openFile = null;

    ArrayList<PdfModel> pdfList = new ArrayList<>();

    PdfAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_to_pdf);

        intView();

    }

    public void intView() {
        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.btnCreatePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectCount = mPdfOptions.getImagesUri().size();
                if (selectCount == 0) {
                    Toast.makeText(ImageToPdfActivity.this, "Please select images", Toast.LENGTH_SHORT).show();

                } else {

                    if (loadingDialog != null) {
                        loadingDialog.setMessage("Create pdf...");
                        loadingDialog.show();
                    }
//                    String timeStamp = new SimpleDateFormat("yyyyMMdd HH.mm.ss").format(new Date());
                    String fileName = "Images_to_PDF_" + System.currentTimeMillis() + Constant.pdfExtension;
                    mPdfOptions.setOutFileName(fileName);
                    mPdfOptions.setQualityString(Integer.toString(100));
                    new Thread(ImageToPdfActivity.this::cratePdfFile).start();
                }
            }
        });

        binding.btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ImageToPdfActivity.this, ImageActivity.class), 10);
            }
        });

        binding.btnOpenPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    openFile

                if (openFile != null) {
                    if (openFile.exists())
                        startActivity(PdfViewerActivity.Companion.launchPdfFromPath(ImageToPdfActivity.this, openFile.getPath(), openFile.getName(),
                                "", false, false));
                }

//                Uri uri = FileProvider.getUriForFile(ImageToPdfActivity.this, getPackageName() + ".provider", openFile);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(uri, Utils.getMimeTypeFromFilePath(openFile.getPath()));
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                startActivity(Intent.createChooser(intent, "Open with"));
            }
        });

        binding.btnOpenPdf.setVisibility(View.GONE);
        binding.btnCreatePdf.setVisibility(View.VISIBLE);

        binding.txtImageCount.setText("0 image(s) selected.");
        setPdfIntview();

        loadingDialog = new ProgressDialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Create pdf...");
        loadingDialog.setCanceledOnTouchOutside(false);

        getPdfata();
    }

    public void setSelectText() {
        int selectCount = mPdfOptions.getImagesUri().size();

        binding.txtImageCount.setText(selectCount + " image(s) selected.");

        binding.txtSelectImage.setText("Select Images (" + selectCount + " Images)");
    }


    public void setAdapter() {
        binding.progressBar.setVisibility(View.GONE);
        if (pdfList != null && pdfList.size() != 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(ImageToPdfActivity.this);
            binding.recyclerView.setLayoutManager(layoutManager);
            adapter = new PdfAdapter(ImageToPdfActivity.this, pdfList);
            binding.recyclerView.setAdapter(adapter);

            RecyclerTouchListener touchListener = new RecyclerTouchListener(this, binding.recyclerView);
            touchListener.setClickable(new RecyclerTouchListener.OnRowClickListener() {
                @Override
                public void onRowClicked(int position) {

                    File file = new File(pdfList.get(position).getFilePath());
                    startActivity(PdfViewerActivity.Companion.launchPdfFromPath(ImageToPdfActivity.this, file.getPath(), file.getName(),
                            "", false, false));


//                    Uri uri = FileProvider.getUriForFile(ImageToPdfActivity.this, getPackageName() + ".provider", file);
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_VIEW);
//                    intent.setDataAndType(uri, Utils.getMimeTypeFromFilePath(file.getPath()));
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                    startActivity(Intent.createChooser(intent, "Open with"));
                }

                @Override
                public void onIndependentViewClicked(int independentViewID, int position) {

                }
            })
                    .setSwipeOptionViews(R.id.btn_delete)
                    .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                        @Override
                        public void onSwipeOptionClicked(int viewID, int position) {
                            switch (viewID) {
                                case R.id.btn_delete:

                                    Log.e("Swipe", "position: " + position);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ImageToPdfActivity.this);
                                    builder.setMessage("Are you sure do you want to delete it?");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton(Html.fromHtml("<font color='#218dff'>Yes</font>"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            File file1 = new File(pdfList.get(position).getFilePath());

                                            boolean isDelete = Utils.deleteFiles(file1, ImageToPdfActivity.this);

                                            MediaScannerConnection.scanFile(ImageToPdfActivity.this, new String[]{file1.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                    // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                                                }
                                            });

                                            if (isDelete) {
                                                pdfList.remove(position);
                                                if (adapter != null)
                                                    adapter.setFilterlist(pdfList);

                                                if (pdfList != null && pdfList.size() != 0) {
                                                    binding.recyclerView.setVisibility(View.VISIBLE);
                                                } else {
                                                    binding.recyclerView.setVisibility(View.GONE);
                                                }

                                                if (binding.btnOpenPdf.getVisibility() == View.VISIBLE)
                                                    if (openFile != null) {
                                                        if (!openFile.exists()) {
                                                            binding.btnCreatePdf.setVisibility(View.VISIBLE);
                                                            binding.btnOpenPdf.setVisibility(View.GONE);
                                                            openFile = null;
                                                        }
                                                    }

                                            }
                                        }
                                    });

                                    builder.setNegativeButton(Html.fromHtml("<font color='#218dff'>No</font>"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    builder.show();

                                    break;
                            }
                        }
                    });

            binding.recyclerView.addOnItemTouchListener(touchListener);

        } else {
            binding.recyclerView.setVisibility(View.GONE);

        }

    }

    public void setPdfIntview() {

        mPdfOptions = new ImageToPDFOptions();

        mPdfOptions.setBorderWidth(Constant.DEFAULT_BORDER_WIDTH);

        mPdfOptions.setQualityString(Integer.toString(Constant.DEFAULT_QUALITY_VALUE));
        mPdfOptions.setPageSize(Constant.DEFAULT_PAGE_SIZE);
        mPdfOptions.setPasswordProtected(false);
        mPdfOptions.setMargins(0, 0, 0, 0);
//        mPageNumStyle =Constants.PREF_PAGE_STYLE;
        mPageColor = Color.WHITE;

        mPdfOptions.setPasswordProtected(false);
        mPdfOptions.setPassword(null);
        mPdfOptions.setPageSize("A4");
        mPdfOptions.setImageScaleType("maintain_aspect_ratio");
        mPdfOptions.setPageNumStyle(null);
        mPdfOptions.setMasterPwd("PDF Converter");
        mPdfOptions.setPageColor(mPageColor);
        mPdfOptions.setQualityString(Integer.toString(Constant.DEFAULT_QUALITY_VALUE));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> mImagesUri = new ArrayList<>();

                if (ImageActivity.selectPhotoList != null)
                    for (int i = 0; i < ImageActivity.selectPhotoList.size(); i++) {
                        mImagesUri.add(ImageActivity.selectPhotoList.get(i).getFilePath());

                    }

                mPdfOptions.getImagesUri().clear();
                mPdfOptions.setImagesUri(mImagesUri);
                setSelectText();

            }

            if (binding.btnCreatePdf.getVisibility() == View.GONE) {
                binding.btnCreatePdf.setVisibility(View.VISIBLE);
                binding.btnOpenPdf.setVisibility(View.GONE);
            }

        }
    }

    public void getPdfata() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Observable.fromCallable(() -> {
            pdfList = getList();
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        setAdapter();

                    });
                })
                .subscribe((result) -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        setAdapter();
                    });
                });

    }

    String[] types = new String[]{".pdf"};

    public ArrayList<PdfModel> getList() {

        ArrayList<PdfModel> list = new ArrayList<>();

        Cursor mCursor = null;

        String sortOrder = "LOWER(" + MediaStore.Files.FileColumns.DATE_MODIFIED + ") DESC"; // unordered

        final String[] projection = {MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME};

//        String[] projection = {MediaStore.Images.Media.DATA/*, MediaStore.Images.Media.TITLE*/
//                , MediaStore.MediaColumns.DATE_MODIFIED
//                , MediaStore.MediaColumns.DISPLAY_NAME
//                , MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//                , MediaStore.MediaColumns.SIZE
//                //*MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//                // , MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media._ID
//        };


        mCursor = getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection, // Projection
                null,
                null,
                sortOrder);

        if (mCursor != null) {

            mCursor.moveToFirst();

            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                long size = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                if (size != 0) {

                    String bucketName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME));

                    if (bucketName != null && bucketName.equalsIgnoreCase(Constant.ImageToPDFPath)) {

                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                        if (path != null && contains(types, path)) {
                            if (!Utils.isPDFEncrypted(path)) {

                                String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));

                                PdfModel model = new PdfModel();
                                model.setFilePath(path);
                                model.setFileName(title);
                                model.setSize(size);

                                list.add(model);

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

    public void cratePdfFile() {
        String mFileName;
        String mPassword;
        String mQualityString;
        ArrayList<String> mImagesUri;
        int mBorderWidth;
//        OnPDFCreatedInterface mOnPDFCreatedInterface;
        boolean mSuccess;
        String mPath;
        String mPageSize;
        boolean mPasswordProtected;
//        Boolean mWatermarkAdded;
//        Watermark mWatermark;
        int mMarginTop;
        int mMarginBottom;
        int mMarginRight;
        int mMarginLeft;
        String mImageScaleType;
        String mPageNumStyle;
        String mMasterPwd;
        int mPageColor;

        mImagesUri = mPdfOptions.getImagesUri();
        mFileName = mPdfOptions.getOutFileName();
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
//        mPath = mainPath;

        File folderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name));
        if (!folderFile.exists())
            folderFile.mkdir();


        File folder = new File(folderFile.getPath() + "/" + Constant.ImageToPDFPath);
        if (!folder.exists())
            folder.mkdir();

        mPath = folder.getPath() + "/" + mFileName /*+ Constants.pdfExtension*/;

        mSuccess = true;

        Rectangle pageSize = new Rectangle(PageSize.getRectangle(mPageSize));

        pageSize.setBackgroundColor(getBaseColor(mPageColor));
        Document document = new Document(pageSize,
                mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
        Log.v("stage 2", "Document Created");
        document.setMargins(mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
        Rectangle documentRect = document.getPageSize();


        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mPath));

            Log.v("Stage 3", "Pdf writer");

            if (mPasswordProtected) {
                writer.setEncryption(mPassword.getBytes(), mMasterPwd.getBytes(),
                        PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128);

                Log.v("Stage 3.1", "Set Encryption");
            }

//            if (mWatermarkAdded) {
//                WatermarkPageEvent watermarkPageEvent = new WatermarkPageEvent();
//                watermarkPageEvent.setWatermark(mWatermark);
//                writer.setPageEvent(watermarkPageEvent);
//            }

            document.open();

            Log.v("Stage 4", "Document opened");

            for (int i = 0; i < mImagesUri.size(); i++) {
                int quality;
                quality = 30;


                if (mQualityString != null && !mQualityString.toString().trim().equals("")) {
                    quality = Integer.parseInt(mQualityString);
                }
                Image image = Image.getInstance(mImagesUri.get(i));
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                double qualityMod = quality * 0.09;
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(mBorderWidth);

                Log.v("Stage 5", "Image compressed " + qualityMod);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mImagesUri.get(i), bmOptions);

                Log.v("Stage 6", "Image path adding");

                float pageWidth = document.getPageSize().getWidth() - (mMarginLeft + mMarginRight);
                float pageHeight = document.getPageSize().getHeight() - (mMarginBottom + mMarginTop);
                if (mImageScaleType.equals(Constant.IMAGE_SCALE_TYPE_ASPECT_RATIO))
                    image.scaleToFit(pageWidth, pageHeight);
                else
                    image.scaleAbsolute(pageWidth, pageHeight);

                image.setAbsolutePosition(
                        (documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2);

                document.add(image);


                document.newPage();
            }

            Log.v("Stage 8", "Image adding");

            document.close();

            Log.v("Stage 7", "Document Closed" + mPath);

            Log.v("Stage 8", "Record inserted in database");

        } catch (Exception e) {
            e.printStackTrace();
            mSuccess = false;
        }

        boolean finalMSuccess = mSuccess;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }

                if (finalMSuccess) {

                    MediaScannerConnection.scanFile(ImageToPdfActivity.this, new String[]{mPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

                    Toast.makeText(ImageToPdfActivity.this, "Create pdf successfully", Toast.LENGTH_SHORT).show();
                    openFile = new File(mPath);

                    binding.btnCreatePdf.setVisibility(View.GONE);
                    binding.btnOpenPdf.setVisibility(View.VISIBLE);
                    binding.txtSelectImage.setText("Select Images");
                    binding.txtImageCount.setText("No image(s) selected.");

                    mPdfOptions.getImagesUri().clear();

                    PdfModel model = new PdfModel();
                    model.setFilePath(mPath);
                    model.setFileName(openFile.getName());
                    model.setSize(openFile.length());

                    pdfList.add(0, model);


                    if (adapter != null)
                        adapter.setFilterlist(pdfList);
                    else
                        setAdapter();

                    if (pdfList != null && pdfList.size() != 0) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(ImageToPdfActivity.this, "error", Toast.LENGTH_SHORT).show();
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