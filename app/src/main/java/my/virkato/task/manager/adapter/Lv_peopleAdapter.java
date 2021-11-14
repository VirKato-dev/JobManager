package my.virkato.task.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import my.virkato.task.manager.R;
import my.virkato.task.manager.entity.Man;

/***
 * Список всех пользователей
 */
public class Lv_peopleAdapter extends BaseAdapter {
    ArrayList<Man> data;

    public Lv_peopleAdapter(ArrayList<Man> people) {
        data = people;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Man getItem(int index) {
        return data.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(final int position, View v, ViewGroup container) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = v;
        if (view == null) {
            view = inflater.inflate(R.layout.a_man, null);
        }

        final TextView textview1 = view.findViewById(R.id.t_description);
        final TextView textview2 = view.findViewById(R.id.e_task_description);

        textview1.setText(getItem(position).fio);
        textview2.setText(getItem(position).spec);

        return view;
    }
}
