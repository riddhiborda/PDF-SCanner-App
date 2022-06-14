package com.pdfscanner.pdf.scanpdf.adapter.imageToPdf;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.model.PhotoData;
import com.pdfscanner.pdf.scanpdf.oncliclk.OnSelectImage;
import com.pdfscanner.pdf.scanpdf.ui.ImageActivity;

import java.util.ArrayList;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    Context context;
    ArrayList<PhotoData> photoList = new ArrayList<>();
    int i2, i3, h, w;
    int deviceheight, devicewidth;
    OnSelectImage onSelectImage;

    public PhotosAdapter(Context context, ArrayList<PhotoData> photoData, OnSelectImage onSelectImage) {
        this.context = context;
        this.photoList = photoData;

        this.onSelectImage = onSelectImage;

        int margin = context.getResources().getDimensionPixelSize(R.dimen._4sdp);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        deviceheight = (displaymetrics.widthPixels - margin) / 4;
        devicewidth = (displaymetrics.widthPixels - margin) / 4;
        i2 = displaymetrics.widthPixels;
        i3 = displaymetrics.heightPixels;
        h = (int) ((19.0f * ((float) i2)) / 100.0f);
        w = (int) ((11.0f * ((float) i3)) / 100.0f);

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

//        holder.image_view.se

        PhotoData imageList = photoList.get(position);

        Glide.with(context).load(imageList.getFilePath())
//                .dontTransform().override(h, w)
                .apply(new RequestOptions().centerCrop())
                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.image_view);


        if (ImageActivity.selectPhotoList.contains(imageList)) {
            holder.iv_select_image.setVisibility(View.VISIBLE);

            holder.txt_select_count.setText("" + getSelectCount(position));

//            holder.ll_main.setBackgroundColor(context.getResources().getColor(R.color.selected_image));
//
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams( holder.image_view.getLayoutParams());
//            int margin = context.getResources().getDimensionPixelSize(R.dimen._8sdp);
//            layout.setMargins(margin, margin, margin, margin);
//            holder.image_view.setLayoutParams(layout);
        } else {
            holder.iv_select_image.setVisibility(View.GONE);

//            holder.ll_main.setBackgroundColor(context.getResources().getColor(R.color.white));
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams( holder.image_view.getLayoutParams());
//            int margin = 0;
//            layout.setMargins(margin, margin, margin, margin);
//            holder.image_view.setLayoutParams(layout);
        }


        holder.image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRemoveSelectionList(holder, position);
            }
        });

    }

    public int getSelectCount(int pos) {
        PhotoData imageList = photoList.get(pos);

        if (ImageActivity.selectPhotoList != null) {
            for (int i = 0; i < ImageActivity.selectPhotoList.size(); i++) {

                if (imageList.getFilePath().equals(ImageActivity.selectPhotoList.get(i).getFilePath())) {

                    return i + 1;
                }

            }
        }

        return 1;

    }


    public void addRemoveSelectionList(ViewHolder holder, int position) {
        try {

            if (ImageActivity.selectPhotoList.contains(photoList.get(position))) {
                ImageActivity.selectPhotoList.remove(photoList.get(position));
                holder.iv_select_image.setVisibility(View.GONE);

                notifyDataSetChanged();

//                holder.ll_main.setBackgroundColor(context.getResources().getColor(R.color.white));
//                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams( holder.image_view.getLayoutParams());
//                int margin = 0;
//                layout.setMargins(margin, margin, margin, margin);
//                holder.image_view.setLayoutParams(layout);

            } else {
                ImageActivity.selectPhotoList.add(photoList.get(position));
                holder.iv_select_image.setVisibility(View.VISIBLE);
                holder.txt_select_count.setText("" + ImageActivity.selectPhotoList.size());

//                holder.ll_main.setBackgroundColor(context.getResources().getColor(R.color.selected_image));
//
//                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams( holder.image_view.getLayoutParams());
//                int margin = context.getResources().getDimensionPixelSize(R.dimen._8sdp);
//                layout.setMargins(margin, margin, margin, margin);
//                holder.image_view.setLayoutParams(layout);

            }

            onSelectImage.OnSelected();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

        Glide.with(context).clear(holder.image_view);

    }

    public class ViewHolder extends RecyclerView.ViewHolder/* implements View.OnClickListener, View.OnLongClickListener*/ {

        ImageView image_view;
        RelativeLayout iv_select_image;
        RelativeLayout ll_main;
        TextView txt_select_count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_view = itemView.findViewById(R.id.image_view);
            iv_select_image = itemView.findViewById(R.id.iv_select_image);
            ll_main = itemView.findViewById(R.id.ll_main);
            txt_select_count = itemView.findViewById(R.id.txt_select_count);
//            iv_image = itemView.findViewById(R.id.iv_image);
//            txt_folder_name = itemView.findViewById(R.id.txt_folder_name);
//            txt_size = itemView.findViewById(R.id.txt_size);

//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);

        }

//        @Override
//        public void onClick(View view) {
//            listener.onItemClick(getAdapterPosition(), view);
//        }
//
//        @Override
//        public boolean onLongClick(View view) {
//            longClickListener.onItemLongClick(getAdapterPosition(), view);
//            return true;
//        }
    }


}
