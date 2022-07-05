package com.pdfscanner.pdf.scanpdf.ui;

import static com.pdfscanner.pdf.scanpdf.Util.Utils.changeStatusBarColor;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.adapter.imageToPdf.PhotosAdapter;
import com.pdfscanner.pdf.scanpdf.adapter.imageToPdf.PhotoSelectAdapter;
import com.pdfscanner.pdf.scanpdf.adapter.imageToPdf.SpinnerAdapter;
import com.pdfscanner.pdf.scanpdf.databinding.ActivityImageBinding;
import com.pdfscanner.pdf.scanpdf.model.PhotoData;
import com.pdfscanner.pdf.scanpdf.listener.OnSelectImage;
import com.pdfscanner.pdf.scanpdf.service.ImageDataService;

import java.util.ArrayList;

import io.reactivex.Observable;

public class ImageActivity extends AppCompatActivity implements OnSelectImage {

    ActivityImageBinding binding;
    public ArrayList<String> folderList = new ArrayList<>();
    ArrayList<PhotoData> photoList = new ArrayList<>();
    public static ArrayList<PhotoData> selectPhotoList = new ArrayList<>();
    ArrayList<PhotoData> folderPhotoList = new ArrayList<>();
    PhotosAdapter adapter;
    PhotoSelectAdapter selecAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        changeStatusBarColor(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image);

        intView();
    }

    @Override
    public void OnSelected() {
        if (selecAdapter != null) {
            selecAdapter.notifyDataSetChanged();
        } else {
            setSelectAdapter();
        }

        if (selectPhotoList != null && selectPhotoList.size() != 0) {
            binding.loutImageSelect.setVisibility(View.VISIBLE);
            binding.btnNext.setVisibility(View.VISIBLE);

        } else {
            binding.loutImageSelect.setVisibility(View.GONE);
            binding.btnNext.setVisibility(View.GONE);
        }
    }

    public void getData() {
        Observable.fromCallable(() -> {
            getImageData();
            return true;
        }).subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnError(throwable -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        folderList = new ArrayList<>();
                        photoList = new ArrayList<>();

                        folderList.addAll(ImageDataService.folderList);
                        photoList.addAll(ImageDataService.photoDataArrayList);
                        folderPhotoList.addAll(ImageDataService.photoDataArrayList);

                        initAdapter(photoList);
                        initSpinner();

                    });
                })
                .subscribe((result) -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.GONE);
                        folderList = new ArrayList<>();
                        photoList = new ArrayList<>();

                        folderList.addAll(ImageDataService.folderList);
                        photoList.addAll(ImageDataService.photoDataArrayList);
                        folderPhotoList.addAll(ImageDataService.photoDataArrayList);

                        initAdapter(photoList);
                        initSpinner();
                    });
                });
    }
    public void intView() {


       /* Intent intent = getIntent();
        if (intent != null) {

            openType = intent.getStringExtra("type");

        }*/

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loutImageSelect.setVisibility(View.GONE);
        binding.btnNext.setVisibility(View.GONE);
        selectPhotoList = new ArrayList<>();
        getData();
//        new Thread(ImageActivity.this::getImageData).start();

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectPhotoList != null && selectPhotoList.size() != 0) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ImageActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        setSelectAdapter();
    }

    public void setSelectAdapter() {

        selecAdapter = new PhotoSelectAdapter(ImageActivity.this, selectPhotoList);
        binding.selectRecyclerView.setLayoutManager(new LinearLayoutManager(ImageActivity.this, LinearLayoutManager.HORIZONTAL, false));
//        binding.selectRecyclerView.hasFixedSize();
        binding.selectRecyclerView.setAdapter(selecAdapter);

        selecAdapter.setOnItemClickListener(new PhotoSelectAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                selectPhotoList.remove(position);
                selecAdapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();


                if (selectPhotoList != null && selectPhotoList.size() != 0) {
                    binding.loutImageSelect.setVisibility(View.VISIBLE);
                    binding.btnNext.setVisibility(View.VISIBLE);

                } else {
                    binding.loutImageSelect.setVisibility(View.GONE);
                    binding.btnNext.setVisibility(View.GONE);
                }
            }
        });

    }

    public void initSpinner() {
        if (photoList.size() > 0) {
            binding.navigationSpinner.setVisibility(View.VISIBLE);
            SpinnerAdapter spinner_adapter = new SpinnerAdapter(ImageActivity.this, folderList);
            spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.navigationSpinner.setAdapter(spinner_adapter);
            initListener();
        } else {
            binding.navigationSpinner.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        binding.navigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(ContextCompat.getColor(ImageActivity.this,R.color.white));
                if (position != 0) {
                    folderPhotoList.clear();
                    for (int i = 0; i < photoList.size(); i++) {
                        if (photoList.get(i).getFolder() != null) {
                            if (photoList.get(i).getFolder().equals(folderList.get(position))) {
                                folderPhotoList.add(photoList.get(i));
                            }
                        }
                    }
                    initAdapter(folderPhotoList);
                } else {
                    initAdapter(photoList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        photo_hide.setOnClickListener(this);

    }

    public void initAdapter(ArrayList<PhotoData> photoDataList) {
        adapter = new PhotosAdapter(ImageActivity.this, photoDataList, ImageActivity.this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(ImageActivity.this, 3, RecyclerView.VERTICAL, false));
        binding.recyclerView.hasFixedSize();
        binding.recyclerView.setAdapter(adapter);

        if (photoDataList != null && photoDataList.size() != 0){
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.llEmpty.setVisibility(View.GONE);
        } else {
            binding.llEmpty.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        }

    }

    public void getImageData() {
        while (true) {
            if (ImageDataService.isComplete) {
                break;
            }
        }

        runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            folderList = new ArrayList<>();
            photoList = new ArrayList<>();

            folderList.addAll(ImageDataService.folderList);
            photoList.addAll(ImageDataService.photoDataArrayList);
            folderPhotoList.addAll(ImageDataService.photoDataArrayList);

            initAdapter(photoList);
            initSpinner();
        });

    }


}