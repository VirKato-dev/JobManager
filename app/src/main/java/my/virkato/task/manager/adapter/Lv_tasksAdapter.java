package my.virkato.task.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import my.virkato.task.manager.R;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Task;

/***
 * Список всех заданий для конкретного мастера
 */
public class Lv_tasksAdapter extends BaseAdapter {
    ArrayList<Task> _data = new ArrayList<>();

    public Lv_tasksAdapter(ArrayList<Task> _arr) {
        setNewList(_arr);
    }

    public void setNewList(ArrayList<Task> list) {
        _data.clear();
        _data.addAll(list);
        notifyDataSetChanged();
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
        LayoutInflater _inflater = LayoutInflater.from(_container.getContext());
        View _view = _v;
        if (_view == null) {
            _view = _inflater.inflate(R.layout.a_task, null);
        }

        TextView t_description = _view.findViewById(R.id.t_description);
        TextView t_fio = _view.findViewById(R.id.t_fio);
        TextView t_task_date = _view.findViewById(R.id.t_task_date);

        Task task = getItem(_position);
        String fio = "БЕЗ ИМЕНИ";
        Man man = new People().findManById(task.master_uid);
        if (man != null) fio = man.fio;

        t_fio.setText(fio);
        t_description.setText(task.description);
        t_task_date.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(task.date_start));

        return _view;
    }
}