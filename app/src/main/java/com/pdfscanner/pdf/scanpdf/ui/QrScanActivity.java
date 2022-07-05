package com.pdfscanner.pdf.scanpdf.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.google.zxing.Result;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityQrScanBinding;

public class QrScanActivity extends AppCompatActivity {

    ActivityQrScanBinding binding;
    CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr_scan);

        intView();
    }

    public void intView() {
        mCodeScanner = new CodeScanner(this, binding.scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(() -> {
                    if (result != null)
                        showResult(result.getText());
                    mCodeScanner.startPreview();
                });
            }
        });

        mCodeScanner.setErrorCallback(error -> Log.e("TAG", "intView: " + error.getMessage()));

        binding.icBack.setOnClickListener(v -> onBackPressed());


        binding.scannerView.setOnClickListener(v -> mCodeScanner.startPreview());

    }

    public void showResult(String result) {
        binding.txtResult.setText(result);
        boolean isUrl = false;

        boolean b1 = result.startsWith("https://");
        boolean b2 = result.startsWith("http://");

        if (b1 || b2) {
            isUrl = true;
            binding.btnOpenCpoy.setText("Open URL");
        } else {
            isUrl = false;
            binding.btnOpenCpoy.setText("Copy");
        }

        if (binding.loutResult.getVisibility() == View.GONE) {
            binding.loutResult.setVisibility(View.VISIBLE);
        }

        boolean finalIsUrl = isUrl;
        binding.btnOpenCpoy.setOnClickListener(v -> {
            if (finalIsUrl) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
            } else {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", result);
                if (clipboard == null || clip == null) return;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(QrScanActivity.this, "Text copied!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnClose.setOnClickListener(v -> binding.loutResult.setVisibility(View.GONE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}