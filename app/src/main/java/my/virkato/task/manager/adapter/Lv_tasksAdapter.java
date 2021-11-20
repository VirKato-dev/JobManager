package my.virkato.task.manager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import my.virkato.task.manager.R;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.Payment;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Task;

/***
 * Список всех заданий для конкретного мастера
 */
public class Lv_tasksAdapter extends BaseAdapter {
    ArrayList<Task> data = new ArrayList<>();

    public Lv_tasksAdapter(ArrayList<Task> arr) {
        setNewList(arr);
    }

    public void setNewList(ArrayList<Task> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    public int getCount() {
        return data.size();
    }

    public Task getItem(int _index) {
        return data.get(_index);
    }

    public long getItemId(int _index) {
        return _index;
    }

    public View getView(final int position, View view, ViewGroup container) {
        Context cont = container.getContext();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        if (view == null) {
            view = inflater.inflate(R.layout.a_task, null);
        }

        TextView t_description = view.findViewById(R.id.t_description);
        TextView t_fio = view.findViewById(R.id.t_fio);
        TextView t_task_date = view.findViewById(R.id.t_task_date);
        ImageView i_pay_check = view.findViewById(R.id.i_pay_check);

        Task task = getItem(position);
        String fio = cont.getString(R.string.noname);
        Man man = new People().findManById(task.master_uid);
        if (man != null) fio = man.fio;

        t_fio.setText(fio);
        t_description.setText(task.description);
        t_task_date.setText(new SimpleDateFormat(cont.getString(R.string.date_format), Locale.getDefault()).format(task.date_start));

        boolean got = true;
        if (task.payments.size() > 0) {
            for (Payment pay : task.payments) {
                if (!pay.received) {
                    got = false;
                    break;
                }
            }
        } else {
            got = false;
        }

        i_pay_check.setImageResource(got ? R.drawable.ic_ok : R.drawable.ic_not);
//        i_pay_check.setVisibility(task.rewarded ? View.VISIBLE : View.INVISIBLE);

        return view;
    }
}