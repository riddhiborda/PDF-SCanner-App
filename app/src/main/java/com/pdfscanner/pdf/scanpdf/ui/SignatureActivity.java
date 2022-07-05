package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.databinding.ActivitySignatureBinding;

public class SignatureActivity extends AppCompatActivity {

    ActivitySignatureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signature);

        initClick();
    }

    private void initClick() {
        binding.icBack.setOnClickListener(v -> finish());

        binding.btnReset.setOnClickListener(v -> binding.signatureView.clearCanvas());


        binding.btnSave.setOnClickListener(v -> {
            if (!binding.signatureView.isBitmapEmpty()) {
                Bitmap bitmap = binding.signatureView.getSignatureBitmap();

                if (bitmap != null) {
                    Constant.signatureBitmap = null;
                    Constant.signatureBitmap = bitmap;
                    setResult(RESULT_OK);
                    finish();
                }

            } else {
                Toast.makeText(SignatureActivity.this, "Please cretae signature", Toast.LENGTH_SHORT).show();

            }
        });
    }
}