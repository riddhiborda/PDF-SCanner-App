package com.pdfscanner.pdf.scanpdf.adapter.imageToPdf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.model.PhotoData;

import java.util.ArrayList;

public class SelectPhotoAdapter extends RecyclerView.Adapter<SelectPhotoAdapter.ViewHolder> {

    Context context;
    ArrayList<PhotoData> photoList = new ArrayList<>();
    private static ClickListener listener;
//    int i2, i3, h, w;
//    int deviceheight, devicewidth;
//    OnSelectImage onSelectImage;

    public SelectPhotoAdapter(Context context, ArrayList<PhotoData> photoData) {
        this.context = context;
        this.photoList = photoData;

//        this.onSelectImage = onSelectImage;

//        int margin = context.getResources().getDimensionPixelSize(R.dimen._4sdp);
//
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        deviceheight = (displaymetrics.widthPixels - margin) / 4;
//        devicewidth = (displaymetrics.widthPixels - margin) / 4;
//        i2 = displaymetrics.widthPixels;
//        i3 = displaymetrics.heightPixels;
//        h = (int) ((19.0f * ((float) i2)) / 100.0f);
//        w = (int) ((11.0f * ((float) i3)) / 100.0f);

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

//        holder.image_view.se

        PhotoData imageList = photoList.get(position);

        Glide.with(context).load(imageList.getFilePath())
                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.image_view);


        holder.iv_close.setVisibility(View.VISIBLE);


        holder.iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                addRemoveSelectionList(holder, position);
                listener.onItemClick(position, view);
            }
        });

    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }

    public void addRemoveSelectionList(ViewHolder holder, int position) {
//        try {
//
//            if (ImageActivity.selectPhotoList.contains(photoList.get(position))) {
//                ImageActivity.selectPhotoList.remove(photoList.get(position));
//                holder.iv_select_image.setVisibility(View.GONE);
//            } else {
//                ImageActivity.selectPhotoList.add(photoList.get(position));
//                holder.iv_select_image.setVisibility(View.VISIBLE);
//            }
//
//            onSelectImage.OnSelected();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
        ImageView iv_close;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_view = itemView.findViewById(R.id.image_view);
            iv_close = itemView.findViewById(R.id.iv_close);
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
