package my.virkato.task.manager.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.R;

/***
 * Список отчетов для конкретного задания
 */
public class Lv_reportsAdapter extends BaseAdapter {
    ArrayList<HashMap<String, Object>> _data;
    Context context;


    public Lv_reportsAdapter(Context context, ArrayList<HashMap<String, Object>> _arr) {
        _data = _arr;
        this.context = context;
    }


    @Override
    public int getCount() {
        return _data.size();
    }


    @Override
    public HashMap<String, Object> getItem(int _index) {
        return _data.get(_index);
    }


    @Override
    public long getItemId(int _index) {
        return _index;
    }


    @Override
    public View getView(final int _position, View _v, ViewGroup _container) {
        LayoutInflater _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _view = _v;
        if (_view == null) {
            _view = _inflater.inflate(R.layout.a_report, null);
        }

        final LinearLayout linear1 = _view.findViewById(R.id.linear1);
        final ImageView i_images = _view.findViewById(R.id.i_images);
        final LinearLayout linear2 = _view.findViewById(R.id.linear2);
        final TextView t_desc = _view.findViewById(R.id.t_desc);

        t_desc.setEllipsize(TextUtils.TruncateAt.END);
        i_images.setColorFilter(0xFF2196F3, PorterDuff.Mode.MULTIPLY);

        return _view;
    }
}