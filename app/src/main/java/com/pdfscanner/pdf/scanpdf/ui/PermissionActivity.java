package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


import com.pdfscanner.pdf.scanpdf.MainActivity;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.service.ImageDataService;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity {

    Toolbar toolbar;
    AppCompatTextView txtDec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        setContentView(R.layout.activity_permission);

        intView();
    }

    private void intView() {
        toolbar = findViewById(R.id.toolbar);
        txtDec = findViewById(R.id.txt_dec);

        findViewById(R.id.btn_allow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    showHomeScreen();
                }
            }
        });

        if (isPermissionGranted()) {
            showHomeScreen();
        }
        txtDec.setText(getResources().getString(R.string.permission_txt1) + " " + getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.permission_txt2));
    }

    private void showHomeScreen() {
        startService(new Intent(PermissionActivity.this, ImageDataService.class));
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                GetStorage_Permission();
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private void GetStorage_Permission() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();

        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read Storage");

        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read Storage");

        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add("Camrea");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                for (int i = 0; i < 1; i++)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
                    }

                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
            }
            return;
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (permissions.length >= 1)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showHomeScreen();
                }
        }
        return;
    }
}
