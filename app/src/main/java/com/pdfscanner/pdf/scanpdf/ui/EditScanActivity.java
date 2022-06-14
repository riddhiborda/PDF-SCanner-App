package com.pdfscanner.pdf.scanpdf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityEditScanBinding;

import vn.nano.photocropper.CropListener;

public class EditScanActivity extends AppCompatActivity {

    ActivityEditScanBinding binding;

    String type;
    int imageCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_scan);

        intView();
    }

    public void intView() {
//        file
        Intent intent = getIntent();

        if (intent != null) {
            String filePath = intent.getStringExtra("file");
            type = intent.getStringExtra("type");
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
            binding.cropImageView.setImageBitmap(bitmap);

            if (type.equalsIgnoreCase(Constant.business) || type.equalsIgnoreCase(Constant.idCard)) {
                imageCount = intent.getIntExtra("imageCount", 1);

                if (imageCount == 2){
                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Constant.showOpenAd();
                        }
                    }, 1000);
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


        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cropLoader.getVisibility() == View.GONE)
                    onBackPressed();
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cropLoader.getVisibility() == View.GONE) {
                    binding.cropLoader.setVisibility(View.VISIBLE);

                    binding.cropImageView.crop(new CropListener() {
                        @Override
                        public void onFinish(Bitmap bitmap) {

                            if (bitmap != null) {
                                if (type.equalsIgnoreCase(Constant.document)) {
                                    Constant.cropBitmap1 = bitmap;
                                    binding.cropLoader.setVisibility(View.GONE);
                                    startActivity(new Intent(EditScanActivity.this, EditorActivity.class).putExtra("type", type));
                                    finish();
                                } else  if (type.equalsIgnoreCase(Constant.ocr)) {
                                    Constant.cropBitmap1 = bitmap;
                                    binding.cropLoader.setVisibility(View.GONE);
                                    startActivity(new Intent(EditScanActivity.this, OcrActivity.class));
                                    finish();
                                }
                                else if (type.equalsIgnoreCase(Constant.business) || type.equalsIgnoreCase(Constant.idCard)) {
                                    binding.cropLoader.setVisibility(View.GONE);

                                    if (imageCount == 1){
                                        Constant.cropBitmap1 = bitmap;
                                        setResult(RESULT_OK);
                                        finish();
                                    } else {
                                        Constant.cropBitmap2 = bitmap;
                                        startActivity(new Intent(EditScanActivity.this, EditorTwoActivity.class).putExtra("type", type));
                                        finish();
                                    }

                                }
                            }
                        }
                    }, true);
                }
            }
        });

    }
}