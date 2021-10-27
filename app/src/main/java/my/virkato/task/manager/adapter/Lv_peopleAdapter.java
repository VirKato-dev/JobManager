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
 * Список всех пользователей
 */
public class Lv_peopleAdapter extends BaseAdapter {
    ArrayList<HashMap<String, Object>> _data;
    Context context;

    public Lv_peopleAdapter(Context context, ArrayList<HashMap<String, Object>> _arr) {
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
            _view = _inflater.inflate(R.layout.a_man, null);
        }

        final TextView textview1 = _view.findViewById(R.id.t_description);
        final TextView textview2 = _view.findViewById(R.id.e_task_description);

        textview1.setText(_data.get(_position).get("fio").toString());
        textview2.setText(_data.get(_position).get("spec").toString());

        return _view;
    }
}
