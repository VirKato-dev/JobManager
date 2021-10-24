package my.virkato.task.manager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.R;

/***
 * Список всех заданий для конкретного мастера
 */
public class Lv_tasksAdapter extends BaseAdapter {
    ArrayList<HashMap<String, Object>> _data;
    Context context;

    public Lv_tasksAdapter(Context context, ArrayList<HashMap<String, Object>> _arr) {
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
            _view = _inflater.inflate(R.layout.a_task, null);
        }

        final TextView textview1 = _view.findViewById(R.id.textview1);

        textview1.setText(_data.get(_position).get("text").toString());

        return _view;
    }
}