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
import my.virkato.task.manager.entity.Report;
import my.virkato.task.manager.entity.Task;

/***
 * Список отчетов для конкретного задания
 */
public class Lv_reportsAdapter extends BaseAdapter {
    ArrayList<Report> _data;

    public Lv_reportsAdapter(ArrayList<Report> _arr) {
        _data = _arr;
    }

    public void setNewList(ArrayList<Report> list) {
        _data.clear();
        _data.addAll(list);
        notifyDataSetChanged();
    }

    public int getCount() {
        return _data.size();
    }

    public Report getItem(int _index) {
        return _data.get(_index);
    }

    public long getItemId(int _index) {
        return _index;
    }

    public View getView(final int _position, View _v, ViewGroup _container) {
        LayoutInflater _inflater = LayoutInflater.from(_container.getContext());
        View _view = _v;
        if (_view == null) {
            _view = _inflater.inflate(R.layout.a_report, null);
        }

        final ImageView i_images = _view.findViewById(R.id.i_images);
        final TextView t_desc = _view.findViewById(R.id.t_desc);

        t_desc.setEllipsize(TextUtils.TruncateAt.END);
        i_images.setColorFilter(0xFF2196F3, PorterDuff.Mode.MULTIPLY);

        t_desc.setText(getItem(_position).description);

        return _view;
    }
}