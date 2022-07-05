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

public class PhotoSelectAdapter extends RecyclerView.Adapter<PhotoSelectAdapter.ViewHolder> {
    Context context;
    ArrayList<PhotoData> photoList;
    private static ClickListener listener;

    public PhotoSelectAdapter(Context context, ArrayList<PhotoData> photoData) {
        this.context = context;
        this.photoList = photoData;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_photo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhotoData imageList = photoList.get(position);

        Glide.with(context).load(imageList.getFilePath())
                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.image_view);

        holder.iv_close.setVisibility(View.VISIBLE);

        holder.iv_close.setOnClickListener(view -> listener.onItemClick(position, view));
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
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
        }
    }
}
