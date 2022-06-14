package com.pdfscanner.pdf.scanpdf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.model.PdfModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    Context context;
    private static ClickListener listener;
    ArrayList<PdfModel> list;


    public RecentAdapter(Context context, ArrayList<PdfModel> list) {
        this.context = context;
        this.list = list;

    }

    public void setData(ArrayList<PdfModel> arrayList) {
        this.list.clear();
        this.list.addAll(arrayList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context).load(Utils.getImageFromPdf(context, list.get(holder.getAdapterPosition()).getFilePath()))
//                .apply(new RequestOptions().centerCrop())
//                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.image);

        holder.txt_name.setText(list.get(holder.getAdapterPosition()).getFileName());

        File file = new File(list.get(holder.getAdapterPosition()).getFilePath());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        String strDate = sdf.format(file.lastModified());

        holder.txt_date.setText(strDate);

    }


    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

        Glide.with(context).clear((holder).image);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        LinearLayout row;
        TextView txt_name, txt_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_date = itemView.findViewById(R.id.txt_date);
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
