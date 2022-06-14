package com.pdfscanner.pdf.scanpdf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfscanner.pdf.scanpdf.R;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.ArrayList;
import java.util.List;


public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    Context context;
    ArrayList<String> filterList = new ArrayList<>();
    private static ClickListener listener;
    List<ThumbnailItem> thumbs;
    Bitmap bitmap;


    public FilterAdapter(Context context, List<ThumbnailItem> thumbs, Bitmap bitmap) {
        this.context = context;
        this.filterList = filterList;
        this.thumbs = thumbs;
        this.bitmap = bitmap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int width = context.getResources().getDimensionPixelSize(R.dimen._55sdp);
        int height = context.getResources().getDimensionPixelSize(R.dimen._55sdp);

//        if (position == 0) {
//            holder.ivFilter.setImageBitmap(bitmap);
//            holder.txt_filter_name.setText("None");
//        } else {
            //  holder.ivFilter.setImageDrawable(context.getResources().getDrawable(R.drawable.filter_bg));
            holder.txt_filter_name.setText(thumbs.get(position).filterName);
            try {
                holder.ivFilter.setImageBitmap(thumbs.get(position).filter.processFilter(Bitmap.createScaledBitmap(bitmap, width, height, false)));
            } catch (Throwable throwable) {

            }
//        }


    }


    @Override
    public int getItemCount() {
        return thumbs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatImageView ivFilter;
        LinearLayout row;
        TextView txt_filter_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            ivFilter = itemView.findViewById(R.id.iv_filter);
            txt_filter_name = itemView.findViewById(R.id.txt_filter_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(getAdapterPosition(), view);
        }
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }

}
