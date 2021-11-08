package my.virkato.task.manager.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.FileUtil;
import my.virkato.task.manager.R;
import my.virkato.task.manager.entity.ReportImage;


public class Rv_picturesAdapter extends RecyclerView.Adapter<Rv_picturesAdapter.ViewHolder> {

    private ArrayList<ReportImage> listItem;
    private Context context;
    private ImageView img;
    private View.OnClickListener click;
    private View.OnLongClickListener longClick;

    public Rv_picturesAdapter(Context context, ArrayList<ReportImage> pictures) {
        this.context = context;
        this.listItem = pictures;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.a_picture, parent, false);
        v.setOnClickListener(click);
        v.setOnLongClickListener(longClick);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportImage item = getItem(position);
        String pict = "";
        if (NetWork.isAdmin()) {
            if (!item.received.equals("")) pict = item.received;
            else pict = item.url;
        } else {
            if (!item.original.equals("")) pict = item.original;
        }

        if (pict.startsWith("http")) {
            Glide.with(context).load(Uri.parse(pict)).into(holder.getImageView());
        } else {
            holder.getImageView().setImageBitmap(FileUtil.decodeSampleBitmapFromPath(pict, 1024, 1024));
        }
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    //TODO починить извлечение images из Report
    public ReportImage getItem(int position) {
        ReportImage repImg = new ReportImage();
        repImg.original = listItem.get(position).original;
        repImg.url = listItem.get(position).url;
        repImg.received = listItem.get(position).received;
        return repImg;
    }


    /***
     * Виджет элемента списка
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView pic;

        public ViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.i_picture);
        }

        public ImageView getImageView() {
            return pic;
        }
    }


    public void setOnClickListener(View.OnClickListener listener) {
        click = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        longClick = listener;
    }

}
