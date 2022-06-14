package com.pdfscanner.pdf.scanpdf.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityImageShowBinding;

import java.io.File;

public class ImageShowActivity extends AppCompatActivity {

    ActivityImageShowBinding binding;
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_show);

        intView();
    }

    public void intView() {

        Intent intent = getIntent();
        if (intent != null) {

            filePath = intent.getStringExtra("filePath");

            File file = new File(filePath);
            binding.title.setText(file.getName());


            binding.imgDisplay.getController().getSettings()
                    .setMaxZoom(6f)
                    .setMinZoom(0)
                    .setDoubleTapZoom(3f);

            Glide.with(ImageShowActivity.this)
                    .load(Utils.getImageFromPdf(ImageShowActivity.this,filePath))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.imgDisplay);

            binding.icBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onBackPressed();
                }
            });

            binding.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDialog(file);
                }
            });

            binding.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String extra_text = "https://play.google.com/store/apps/details?id=" + getPackageName();
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction("android.intent.action.SEND");
                    shareIntent.setType(Utils.getMimeTypeFromFilePath(file.getPath()));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                    shareIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                    startActivity(Intent.createChooser(shareIntent, "Share with..."));

                }
            });


            binding.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivityForResult(new Intent(ImageShowActivity.this, EditDocumentActivity.class)
                            .putExtra("filePath", filePath), 10);

                }
            });

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 10) {


            binding.imgDisplay.getController().getSettings()
                    .setMaxZoom(6f)
                    .setMinZoom(0)
                    .setDoubleTapZoom(3f);


//            Glide.with(ImageShowActivity.this)
//                    .load(filePath)
//                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                    .into(binding.imgDisplay);

            Bitmap bitmap = Utils.getImageFromPdf(ImageShowActivity.this,filePath);
            if (bitmap != null) {
                Log.e("", "bitmap no null");
                Glide.with(ImageShowActivity.this)
                        .load(bitmap)
                        .into(binding.imgDisplay);
            }

        }

    }


    public void deleteDialog(File file) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ImageShowActivity.this);
        builder.setMessage("Are you sure do you want to delete it?");
        builder.setCancelable(false);

        builder.setPositiveButton(Html.fromHtml("<font color='#218dff'>Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                Utils.deleteFiles(file, ImageShowActivity.this);
                MediaScannerConnection.scanFile(ImageShowActivity.this, new String[]{file.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });
                Toast.makeText(ImageShowActivity.this, "Delete file successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();

            }
        });

        builder.setNegativeButton(Html.fromHtml("<font color='#218dff'>No</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.show();

    }
}