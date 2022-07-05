package com.pdfscanner.pdf.scanpdf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.Util.Utils;
import com.pdfscanner.pdf.scanpdf.model.PDFModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    Context context;
    private static ClickListener listener;
    ArrayList<PDFModel> list;


    public DocumentAdapter(Context context, ArrayList<PDFModel> list) {
        this.context = context;
        this.list = list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);

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


        File file = new File(list.get(holder.getAdapterPosition()).getFilePath());

        holder.txt_name.setText(file.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        String strDate = sdf.format(file.lastModified());

        holder.txt_date.setText(strDate);

        holder.ic_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.ic_more);
                popup.getMenuInflater().inflate(R.menu.document_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {

                            case R.id.menu_rename:
                                listener.onRenameClick(position, v);
                                break;

                            case R.id.menu_delete:
                                listener.onDeleteClick(position, v);
                                break;

                            case R.id.menu_share:
                                listener.onShareClick(position, v);
                                break;
                            case R.id.menu_ocr_text:
                                listener.onOcrClick(position, v);
                                break;

                        }
                        return false;
                    }
                });

                popup.show();
            }
        });

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

        ImageView image, ic_more;
        LinearLayout row;
        TextView txt_name, txt_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            ic_more = itemView.findViewById(R.id.ic_more);
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

        void onRenameClick(int position, View v);

        void onDeleteClick(int position, View v);

        void onShareClick(int position, View v);

        void onOcrClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }

}
