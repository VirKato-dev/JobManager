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

import java.util.ArrayList;

import my.virkato.task.manager.FileUtil;
import my.virkato.task.manager.R;


public class Rv_picturesAdapter extends RecyclerView.Adapter<Rv_picturesAdapter.ViewHolder> {

    private ArrayList<String> listItem;
    private Context context;
    private ImageView img;

    public Rv_picturesAdapter(Context context, ArrayList pictures) {
        this.context = context;
        this.listItem = pictures;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.a_picture, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = getItem(position);
        if (item.startsWith("http")) {
            Glide.with(context).load(Uri.parse(item)).into(holder.getImageView());
        } else {
            holder.getImageView().setImageBitmap(FileUtil.decodeSampleBitmapFromPath(item, 1024, 1024));
        }
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public String getItem(int position) {
        return listItem.get(position);
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

}
