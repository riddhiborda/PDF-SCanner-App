package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.pdfscanner.pdf.scanpdf.MainActivity;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.ad.AdmobAdManager;
import com.pdfscanner.pdf.scanpdf.adapter.FilterAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityEditorBinding;
import com.pdfscanner.pdf.scanpdf.stickerview.StickerView;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    ActivityEditorBinding binding;

    public static Bitmap bitmap;
    public Bitmap backupBitmap;
    private ArrayList<View> mViews;
    private StickerView mCurrentView;

    FilterAdapter filterAdapter;
    List<ThumbnailItem> thumbs;

    int width, height;
    int pos = -1;
    String type;
    AdmobAdManager admobAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
        admobAdManager = AdmobAdManager.getInstance(this);
        intView();
    }

    public void intView() {
        System.loadLibrary("NativeImageProcessor");

        Intent intent = getIntent();

        if (intent != null) {
            type = intent.getStringExtra("type");
        }
        bitmap = Constant.cropBitmap1;
        mViews = new ArrayList<>();
        backupBitmap = bitmap;
        binding.imageView.setImageBitmap(bitmap);

        getFilterList();
        setFilterAdapter();

        width = binding.imageView.getDrawable().getIntrinsicWidth();
        height = binding.imageView.getDrawable().getIntrinsicHeight();

        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentEditFalse();
                binding.stickerView.setLocked(true);
                binding.loutMain.setDrawingCacheEnabled(true);
                Constant.EditBitmap = null;
                Constant.EditBitmap = binding.loutMain.getDrawingCache().copy(Bitmap.Config.ARGB_8888, false);
                binding.loutMain.destroyDrawingCache();
                Constant.signatureBitmap = null;

                startActivity(new Intent(EditorActivity.this, PreviewActivity.class).putExtra("type", type));
                finish();

            }
        });


        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        binding.btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                backupBitmap = rotateBitmap(backupBitmap, 90);
                if (pos == -1) {
                    binding.imageView.setImageBitmap(backupBitmap);
                } else {
                    Bitmap bitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();
                    binding.imageView.setImageBitmap(rotateBitmap(bitmap, 90));

//                    Bitmap bitmap = thumbs.get(pos).filter.processFilter(Bitmap.createScaledBitmap(backupBitmap, width, height, false));
//                    binding.imageView.setImageBitmap(bitmap);
                }

            }
        });

        binding.btnSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(EditorActivity.this, SignatureActivity.class), 50);
            }
        });

    }

    public void setFilterAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        filterAdapter = new FilterAdapter(this, thumbs, bitmap);
        binding.recyclerView.setAdapter(filterAdapter);

        filterAdapter.setOnItemClickListener(new FilterAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                pos = position;
                Bitmap bitmap = thumbs.get(position).filter.processFilter(Bitmap.createScaledBitmap(backupBitmap, width, height, false));
                binding.imageView.setImageBitmap(bitmap);
            }
        });


    }

    private void getFilterList() {

        List<Filter> filters = FilterPack.getFilterPack(this);

        for (Filter filter : filters) {
            ThumbnailItem item = new ThumbnailItem();
            item.image = bitmap;
            item.filter = filter;
            item.filterName = filter.getName();
            if (filter.getName().equalsIgnoreCase("Haan")) {

            } else if (filter.getName().equalsIgnoreCase("April")) {

            } else if (filter.getName().equalsIgnoreCase("Rise")) {

            } else if (filter.getName().equalsIgnoreCase("OldMan")) {

            } else {
                ThumbnailsManager.addThumb(item);
            }

        }

        thumbs = ThumbnailsManager.processThumbs(this);

//        thumbs.add(0, null);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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