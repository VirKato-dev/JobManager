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

import java.io.File;
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
        String pict = item.url;
        if (pict.equals("")) pict = item.original;
        if (NetWork.isAdmin()) {
            if (!item.received.equals("")) {
                if (new File(pict).exists()) {
                    // пока картинка не скачалась на устройство админа
                    pict = item.received;
                }
            }
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


    public ReportImage getItem(int position) {
        return listItem.get(position);
    }

    /***
     * Виджет элемента списка
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView pic;

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
