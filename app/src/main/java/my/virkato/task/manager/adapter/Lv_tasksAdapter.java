package my.virkato.task.manager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import my.virkato.task.manager.R;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Task;

/***
 * Список всех заданий для конкретного мастера
 */
public class Lv_tasksAdapter extends BaseAdapter {
    ArrayList<Task> _data;
    Context _context;

    public Lv_tasksAdapter(Context _context, ArrayList<Task> _arr) {
        _data = _arr;
        this._context = _context.getApplicationContext();
    }

    public int getCount() {
        return _data.size();
    }

    public Task getItem(int _index) {
        return _data.get(_index);
    }

    public long getItemId(int _index) {
        return _index;
    }

    public View getView(final int _position, View _v, ViewGroup _container) {
        LayoutInflater _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _view = _v;
        if (_view == null) {
            _view = _inflater.inflate(R.layout.a_task, null);
        }

        TextView t_description = _view.findViewById(R.id.t_description);
        TextView t_fio = _view.findViewById(R.id.t_fio);

        Task task = _data.get(_position);
        String fio = "БЕЗ ИМЕНИ";
        Man man = new People().findManById(task.master_uid);
        if (man != null) fio = man.fio;
        t_fio.setText(fio);
        t_description.setText(task.description);

        return _view;
    }
}