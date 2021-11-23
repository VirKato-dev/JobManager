package my.virkato.task.manager.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.TextUtils;
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
import my.virkato.task.manager.entity.Report;

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

    public View getView(final int position, View v, ViewGroup container) {
        Context cont = container.getContext();
        LayoutInflater _inflater = LayoutInflater.from(cont);
        View _view = v;
        if (_view == null) {
            _view = _inflater.inflate(R.layout.a_report, null);
        }

        final ImageView i_images = _view.findViewById(R.id.i_images);
        final TextView t_desc = _view.findViewById(R.id.t_desc);
        final TextView t_date = _view.findViewById(R.id.t_date);
        final TextView t_repFIO = _view.findViewById(R.id.t_repFIO);

        if (getItem(position).images.size() > 0) i_images.setVisibility(View.VISIBLE);
        else i_images.setVisibility(View.INVISIBLE);
        i_images.setColorFilter(R.color.colorPrimary, PorterDuff.Mode.MULTIPLY);

        t_desc.setEllipsize(TextUtils.TruncateAt.END);
        t_desc.setText(getItem(position).description);

        if (getItem(position).date > 0)
            t_date.setText(new SimpleDateFormat(cont.getString(R.string.date_format), Locale.getDefault()).format(getItem(position).date));

        String m_id = getItem(position).master;
        String name = cont.getString(R.string.noname);
        if (m_id != null && !m_id.equals("")) {
            Man man = NetWork.getInstance(NetWork.Info.USERS).getPeople().findManById(m_id); //new NetWork(NetWork.Info.USERS).getPeople().findManById(m_id);
            if (man != null) {
                name = man.fio;
            }
        }
        t_repFIO.setText(name);
        return _view;
    }
}