package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Constant;
import com.pdfscanner.pdf.scanpdf.ads.AdmobAdsManager;
import com.pdfscanner.pdf.scanpdf.adapter.FilterAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityEditorTwoBinding;
import com.pdfscanner.pdf.scanpdf.stickerview.StickerView;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

public class EditorTwoActivity extends AppCompatActivity {

    ActivityEditorTwoBinding binding;

    public Bitmap bitmap;
    public Bitmap bitmap2;
    public Bitmap backupBitmap;
    public Bitmap backupBitmap2;
    private ArrayList<View> mViews;
    private StickerView mCurrentView;

    FilterAdapter filterAdapter;
    List<ThumbnailItem> thumbs;

    int width, height;
    int pos = -1;
    String type;

    int select = 1;
    AdmobAdsManager admobAdsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor_two);
        admobAdsManager = AdmobAdsManager.getInstance(this);
        intView();
    }

    public void intView() {
        System.loadLibrary("NativeImageProcessor");

        Intent intent = getIntent();

        if (intent != null) {
            type = intent.getStringExtra("type");
        }

        bitmap = Constant.cropBitmap1;
        bitmap2 = Constant.cropBitmap2;
        mViews = new ArrayList<>();
        backupBitmap = bitmap;
        backupBitmap2 = bitmap2;

        binding.imageView.setImageBitmap(bitmap);
        binding.imageView2.setImageBitmap(bitmap2);

        getFilterList();
        setFilterAdapter();

        width = binding.imageView.getDrawable().getIntrinsicWidth();
        height = binding.imageView.getDrawable().getIntrinsicHeight();

        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentEditFalse();
//                binding.stickerView.setLocked(true);
                binding.loutImageTwo.setBackgroundColor(getResources().getColor(R.color.transparent));
                binding.loutImageOne.setBackgroundColor(getResources().getColor(R.color.transparent));
                binding.loutMain.setDrawingCacheEnabled(true);
                Constant.EditBitmap = null;
                Constant.EditBitmap = binding.loutMain.getDrawingCache().copy(Bitmap.Config.ARGB_8888, false);
                binding.loutMain.destroyDrawingCache();
                Constant.signatureBitmap = null;
                startActivity(new Intent(EditorTwoActivity.this, PreviewActivity.class).putExtra("type", type));

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
                if (select == 1) {
                    backupBitmap = rotateBitmap(backupBitmap, 90);
                    if (pos == -1) {
                        binding.imageView.setImageBitmap(backupBitmap);
                    } else {
//                        Bitmap bitmap = thumbs.get(pos).filter.processFilter(Bitmap.createScaledBitmap(backupBitmap, width, height, false));
//                        binding.imageView.setImageBitmap(bitmap);
                        Bitmap bitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();
                        binding.imageView.setImageBitmap(rotateBitmap(bitmap, 90));
                    }


                } else {
                    backupBitmap2 = rotateBitmap(backupBitmap2, 90);
                    if (pos == -1) {
                        binding.imageView2.setImageBitmap(backupBitmap2);
                    } else {
//                        Bitmap bitmap = thumbs.get(pos).filter.processFilter(Bitmap.createScaledBitmap(backupBitmap2, width, height, false));
                        Bitmap bitmap = ((BitmapDrawable) binding.imageView2.getDrawable()).getBitmap();
                        binding.imageView2.setImageBitmap(rotateBitmap(bitmap, 90));
                    }


                }

            }
        });


        binding.btnSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(EditorTwoActivity.this, SignatureActivity.class), 50);
            }
        });

        binding.loutImageOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setOneSelectLayout();

            }
        });

        binding.loutImageTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTwoSelectLayout();
            }
        });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOneSelectLayout();
            }
        });

        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTwoSelectLayout();
            }
        });


    }

    public void setOneSelectLayout() {
        if (select != 1) {
            select = 1;
//            binding.loutImageTwo.setBackgroundColor(getResources().getColor(R.color.white));
            binding.loutImageTwo.setBackground(getResources().getDrawable(R.drawable.edit_bg));
            binding.loutImageOne.setBackground(getResources().getDrawable(R.drawable.edit_select));
        }
    }


    public void setTwoSelectLayout() {
        if (select != 2) {
            select = 2;
            binding.loutImageOne.setBackground(getResources().getDrawable(R.drawable.edit_bg));
//            binding.loutImageOne.setBackgroundColor(getResources().getColor(R.color.white));
            binding.loutImageTwo.setBackground(getResources().getDrawable(R.drawable.edit_select));
        }
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
                if (select == 1) {
                    Bitmap bitmap = thumbs.get(position).filter.processFilter(Bitmap.createScaledBitmap(backupBitmap, width, height, false));
                    binding.imageView.setImageBitmap(bitmap);
                } else {
                    Bitmap bitmap = thumbs.get(position).filter.processFilter(Bitmap.createScaledBitmap(backupBitmap2, width, height, false));
                    binding.imageView2.setImageBitmap(bitmap);
                }
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