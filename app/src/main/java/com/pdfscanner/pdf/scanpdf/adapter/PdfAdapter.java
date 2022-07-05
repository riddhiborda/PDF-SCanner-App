package com.pdfscanner.pdf.scanpdf.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;


import com.pdfscanner.pdf.scanpdf.R;
import com.pdfscanner.pdf.scanpdf.model.PDFModel;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class PdfAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private static ClickListener listener;
    private static LongClickListener longClickListener;
    boolean isGrid = false;
    ArrayList<PDFModel> pdfList = new ArrayList<>();

    public static final int TYPE_AD = 2;
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_ITEM_GRID = 1;


    public PdfAdapter(Context context, ArrayList<PDFModel> pdfList) {
        this.context = context;
        this.pdfList.addAll(pdfList);

       /* this.isGrid = isGrid;
        this.documentSelect = documentSelect;*/
    }

    public void setFilterlist(ArrayList<PDFModel> pdfList) {
        this.pdfList.clear();
        this.pdfList.addAll(pdfList);
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }

    public interface LongClickListener {
        void onItemLongClick(int position, View v);
    }

    public void setOnLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_ITEM: {
                View v = inflater.inflate(R.layout.item_pdf, parent, false);
                viewHolder = new ViewHolder(v);
                break;
            }
            default:
                break;

        }
        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case TYPE_ITEM:
                    try {

                        ViewHolder viewholder = (ViewHolder) holder;
                        PDFModel model = pdfList.get(position);

//                        viewholder.txtFolderName.setText(model.getName());

                        File file = new File(model.getFilePath());
                        model.setFilePath(file.getPath());
                        model.setFileName(file.getName());
                        viewholder.txt_folder_name.setText(model.getFileName());

                        String mimeType = getFilenameExtension(model.getFileName());

                        String fileSize = Formatter.formatShortFileSize(context, file.length());


                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        String strDate = sdf.format(file.lastModified());

                        SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                        String strTime = sdf2.format(file.lastModified());

                        viewholder.txt_size.setText(strDate + " | " + strTime + " | " + fileSize);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getFilenameExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }


    public String size(int size) {
        String hrSize = "";
        double m = size / 1024.0;
        DecimalFormat dec = new DecimalFormat("0.00");

        if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(size).concat(" KB");
        }
        return hrSize;
    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_image;
        AppCompatTextView txt_folder_name, txt_size;
        LinearLayout btn_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
            txt_folder_name = itemView.findViewById(R.id.txt_folder_name);
            txt_size = itemView.findViewById(R.id.txt_size);
            btn_delete = itemView.findViewById(R.id.btn_delete);

//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);

        }


    }

}
