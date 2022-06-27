package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pdfscanner.pdf.scanpdf.MainActivity;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ad.AdEventListener;
import com.pdfscanner.pdf.scanpdf.ad.AdmobAdManager;
import com.pdfscanner.pdf.scanpdf.adapter.DocumentAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityDocumentBinding;
import com.pdfscanner.pdf.scanpdf.listener.HomeDeleteUpdate;
import com.pdfscanner.pdf.scanpdf.listener.HomeRenameUpdate;
import com.pdfscanner.pdf.scanpdf.listener.HomeUpdate;
import com.pdfscanner.pdf.scanpdf.model.PdfModel;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DocumentActivity extends AppCompatActivity {
    ActivityDocumentBinding binding;
    ArrayList<PdfModel> docList = new ArrayList<>();
    DocumentAdapter adapter;
    int pos = -1;
    String[] types = new String[]{".pdf"};
    AdmobAdManager admobAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_document);
        admobAdManager = AdmobAdManager.getInstance(this);
        recentUpdate();
        intview();
    }

    public void intview() {
        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getDocData();
    }

    private void loadbanner() {
        if (docList != null && docList.size() != 0) {
            if (!PreferencesManager.getString(DocumentActivity.this, Constant.BANNER_ID).isEmpty()) {
                admobAdManager.LoadAdaptiveBanner(this, binding.loutBanner, PreferencesManager.getString(DocumentActivity.this, Constant.BANNER_ID), new AdEventListener() {
                    @Override
                    public void onAdLoaded(Object object) {
                        binding.loutBanner.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdClosed() {

                    }

                    @Override
                    public void onLoadError(String errorCode) {
                        binding.loutBanner.setVisibility(View.GONE);
                    }
                });
            }else {
                Utils.getAdsIds(DocumentActivity.this);
            }
        }
    }

    public void setAdapter() {
        if (docList != null && docList.size() != 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.loutNoData.setVisibility(View.GONE);

            adapter = new DocumentAdapter(DocumentActivity.this, docList);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(DocumentActivity.this));
            binding.recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new DocumentAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    pos = position;
                    startActivityForResult(new Intent(DocumentActivity.this, ImageShowActivity.class)
                            .putExtra("filePath", docList.get(position).getFilePath()), 101);
                }

                @Override
                public void onRenameClick(int position, View v) {
                    pos = position;
                    renameDialog();
                }

                @Override
                public void onDeleteClick(int position, View v) {
                    pos = position;
                    deleteDialog();
                }

                @Override
                public void onShareClick(int position, View v) {
                    pos = position;
                    File file = new File(docList.get(position).getFilePath());
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

                @Override
                public void onOcrClick(int position, View v) {
                    pos = position;
                }
            });

        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.loutNoData.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101) {

            RxBus.getInstance().post(new HomeDeleteUpdate(docList.get(pos).getFilePath()));
            docList.remove(pos);

            if (adapter != null) {
                adapter.notifyItemRemoved(pos);
            }

            if (docList != null && docList.size() != 0) {

                binding.loutNoData.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);

            } else {

                binding.recyclerView.setVisibility(View.GONE);
                binding.loutNoData.setVisibility(View.VISIBLE);

            }


        }
    }

    public void getDocData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Observable.fromCallable(() -> {
            docList = getList();
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        setAdapter();
                        loadbanner();
                    });
                })
                .subscribe((result) -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        setAdapter();
                        loadbanner();
                    });
                });

    }


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

                    if (bucketName != null && bucketName.equalsIgnoreCase(getString(R.string.app_name))) {

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


    public void deleteDialog() {

        File file = new File(docList.get(pos).getFilePath());

        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentActivity.this);
        builder.setMessage("Are you sure do you want to delete it?");
        builder.setCancelable(false);

        builder.setPositiveButton(Html.fromHtml("<font color='#218dff'>Yes</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                Utils.deleteFiles(file, DocumentActivity.this);
                MediaScannerConnection.scanFile(DocumentActivity.this, new String[]{file.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });
                Toast.makeText(DocumentActivity.this, "Delete file successfully", Toast.LENGTH_SHORT).show();

                if (adapter != null) {
                    RxBus.getInstance().post(new HomeDeleteUpdate(docList.get(pos).getFilePath()));
                    docList.remove(pos);
                    adapter.notifyItemRemoved(pos);
                }

                if (docList != null && docList.size() != 0) {

                    binding.loutNoData.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                } else {

                    binding.recyclerView.setVisibility(View.GONE);
                    binding.loutNoData.setVisibility(View.VISIBLE);

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

    }

    public void recentUpdate() {

        Subscription subscription = RxBus.getInstance().toObservable(HomeUpdate.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<HomeUpdate>() {
                    @Override
                    public void call(HomeUpdate event) {
                        if (event.getFilePath() != null && !event.equals("")) {

                            File file = new File(event.getFilePath());
                            if (file.exists()) {

                                if (contains(types, file.getPath())) {

                                    PdfModel model = new PdfModel();
                                    model.setFilePath(file.getPath());
                                    model.setFileName(file.getName());
                                    model.setSize(file.length());

                                    docList.add(0, model);


                                    if (docList != null && docList.size() != 0) {
                                        binding.recyclerView.setVisibility(View.VISIBLE);
                                        binding.loutNoData.setVisibility(View.GONE);
                                    } else {
                                        binding.recyclerView.setVisibility(View.GONE);
                                        binding.loutNoData.setVisibility(View.VISIBLE);
                                    }

                                    if (adapter != null) {
//                                        adapter.notifyDataSetChanged();
                                        adapter.notifyItemInserted(0);
                                    }
                                }

                            }

                        } else {

                            if (docList != null && docList.size() != 0) {
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                binding.loutNoData.setVisibility(View.GONE);
                            } else {
                                binding.recyclerView.setVisibility(View.GONE);
                                binding.loutNoData.setVisibility(View.VISIBLE);
                            }

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            } else {
                                setAdapter();
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public void renameDialog() {

        File file = new File(docList.get(pos).getFilePath());

        Dialog dialog = new Dialog(this, R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rename);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);


        AppCompatEditText edtFileName;
        LinearLayout btn_cancel, btn_ok;
        edtFileName = dialog.findViewById(R.id.edt_file_name);
        btn_cancel = dialog.findViewById(R.id.btn_cancel);
        btn_ok = dialog.findViewById(R.id.btn_ok);

        String currentString1 = file.getName().toString();
        String[] separated1 = currentString1.split("\\.");
        String type = separated1[separated1.length - 1];

        String strname = currentString1.replace("." + type, "");


        edtFileName.setText(strname);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mimeType = Utils.getFilenameExtension(file.getName());

                if (!edtFileName.getText().toString().isEmpty()) {
                    if (edtFileName.getText().toString().equalsIgnoreCase(file.getName())) {
                        dialog.show();
                    } else if (!file.isDirectory()) {
//                        String currentString = edtFileName.getText().toString();
//                        String[] separated = currentString.split("\\.");
//                        String type = separated[separated.length - 1];

                        // set rename
                        Log.e("", "rename");
                        dialog.dismiss();
                        reNameFile(file, edtFileName.getText().toString() + "." + type);


                    } else {
                        // set rename
                        dialog.dismiss();
                        reNameFile(file, edtFileName.getText().toString() + "." + type);
                    }

                } else {

                    Toast.makeText(DocumentActivity.this, "New name can't be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();

    }

    private void reNameFile(File file, String newName) {


        File file2 = new File(file.getParent() + "/" + newName);
        if (file2.exists()) {
            Log.e("rename", "File already exists!");
            showRenameValidationDialog();
        } else {

            String oldFile = file.getPath();


            boolean renamed = false;
            renamed = file.renameTo(file2);

            if (renamed) {
                Log.e("LOG", "File renamed...");
                MediaScannerConnection.scanFile(DocumentActivity.this, new String[]{file2.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });
                RxBus.getInstance().post(new HomeRenameUpdate(oldFile, file2.getPath()));

                docList.get(pos).setFilePath(file2.getPath());
                docList.get(pos).setFileName(file2.getName());
                adapter.notifyItemChanged(pos);

                Toast.makeText(this, "Rename file successfully", Toast.LENGTH_SHORT).show();
            } else {

            }
        }
    }

    private void showRenameValidationDialog() {
        Dialog validationDialog = new Dialog(this, R.style.WideDialog);
        validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        validationDialog.setCancelable(true);
        validationDialog.setContentView(R.layout.dialog_rename_same_name_validation);
        validationDialog.setCanceledOnTouchOutside(true);
        validationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        validationDialog.getWindow().setGravity(Gravity.CENTER);

        LinearLayout btn_ok;
        btn_ok = validationDialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validationDialog.dismiss();
            }
        });

        validationDialog.show();
    }

}