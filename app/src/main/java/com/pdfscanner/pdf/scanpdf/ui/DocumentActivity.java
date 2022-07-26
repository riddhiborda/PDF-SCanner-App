package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
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

import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.Util.PreferencesManager;
import com.pdfscanner.pdf.scanpdf.Util.RxBus;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.ads.AdsEventListener;
import com.pdfscanner.pdf.scanpdf.ads.AdmobAdsManager;
import com.pdfscanner.pdf.scanpdf.adapter.DocumentAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityDocumentBinding;
import com.pdfscanner.pdf.scanpdf.model.home.UpdateDelete;
import com.pdfscanner.pdf.scanpdf.model.home.RenameUpdate;
import com.pdfscanner.pdf.scanpdf.model.home.Update;
import com.pdfscanner.pdf.scanpdf.model.PDFModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DocumentActivity extends AppCompatActivity {
    ActivityDocumentBinding binding;
    ArrayList<PDFModel> docList = new ArrayList<>();
    DocumentAdapter adapter;
    int pos = -1;
    String[] types = new String[]{".pdf"};
    AdmobAdsManager admobAdsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_document);
        admobAdsManager = AdmobAdsManager.getInstance(this);
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
                admobAdsManager.LoadAdaptiveBanner(this, binding.loutBanner, PreferencesManager.getString(DocumentActivity.this, Constant.BANNER_ID), new AdsEventListener() {
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
            RxBus.getInstance().post(new UpdateDelete(docList.get(pos).getFilePath()));
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

    @SuppressLint("CheckResult")
    public void getDocData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Observable.fromCallable(() -> {
            getList();
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    setAdapter();
                    loadbanner();
                }))
                .subscribe((result) -> runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    setAdapter();
                    loadbanner();
                }));
    }

    public ArrayList<PDFModel> getList() {
        ArrayList<PDFModel> list = new ArrayList<>();

        File sd = getCacheDir();
        File subfolder = new File(sd, "/" + getString(R.string.app_name));
        if (!subfolder.isDirectory()) {
            subfolder.mkdir();
        }

        getAllData(subfolder);

        return list;
    }

    private ArrayList<PDFModel> getAllData(File dir){
        ArrayList<PDFModel> list = new ArrayList<>();

        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()){
                getAllData(file);
            }else {
                if (file.isFile() && file.getName().endsWith(".pdf")) {
                    Log.e("TAG", "getList: " + file.getAbsolutePath());
                    PDFModel model = new PDFModel();
                    model.setFilePath(file.getAbsolutePath());
                    model.setFileName(file.getName());
                    model.setSize(file.length());
                    list.add(model);
                }
            }
        }

        Collections.sort(list, (p1, p2) -> {
            long date1 = new File(p1.getFilePath()).lastModified();
            long date2 = new File(p2.getFilePath()).lastModified();
            return Utils.getData(date1).after(Utils.getData(date2)) ? -1 : 1;
        });

        docList.addAll(list);

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
                    RxBus.getInstance().post(new UpdateDelete(docList.get(pos).getFilePath()));
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
        Subscription subscription = RxBus.getInstance().toObservable(Update.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<Update>() {
                    @Override
                    public void call(Update event) {
                        if (event.getFilePath() != null && !event.equals("")) {
                            File file = new File(event.getFilePath());
                            if (file.exists()) {
                                if (contains(types, file.getPath())) {
                                    PDFModel model = new PDFModel();
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
                }, throwable -> {
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public void renameDialog() {
        File file = new File(docList.get(pos).getFilePath());
        Dialog dialog = new Dialog(this, R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.rename_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        AppCompatEditText edtFileName;
        LinearLayout btn_cancel, btn_ok;
        edtFileName = dialog.findViewById(R.id.edt_file_name);
        btn_cancel = dialog.findViewById(R.id.btn_cancel);
        btn_ok = dialog.findViewById(R.id.btn_ok);

        String currentString1 = file.getName();
        String[] separated1 = currentString1.split("\\.");
        String type = separated1[separated1.length - 1];

        String strname = currentString1.replace("." + type, "");

        edtFileName.setText(strname);

        btn_cancel.setOnClickListener(view -> dialog.dismiss());

        btn_ok.setOnClickListener(view -> {
            String mimeType = Utils.getFilenameExtension(file.getName());
            if (!edtFileName.getText().toString().isEmpty()) {
                if (edtFileName.getText().toString().equalsIgnoreCase(file.getName())) {
                    dialog.show();
                } else if (!file.isDirectory()) {
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

                RxBus.getInstance().post(new RenameUpdate(oldFile, file2.getPath()));
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
        validationDialog.setContentView(R.layout.same_name_validation_dialog);
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