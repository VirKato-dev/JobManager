package my.virkato.task.manager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import my.virkato.task.manager.R;
import my.virkato.task.manager.entity.Payment;


public class Rv_paymentsAdapter extends RecyclerView.Adapter<Rv_paymentsAdapter.ViewHolder> {

    public interface OnChanged {
        void onChanged(int position);
    }

    private ArrayList<Payment> data;
    private View.OnClickListener click;
    private View.OnLongClickListener longClick;
    private OnChanged changed;
    private String UID;


    public Rv_paymentsAdapter(ArrayList<Payment> payments) {
        data = payments;
    }


    public void setList(ArrayList<Payment> payments) {
        data = payments;
    }

    public void setUID(String uid) {
        UID = uid;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.a_payment, parent, false);
        v.setOnClickListener(click);
        v.setOnLongClickListener(longClick);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment item = getItem(position);
        holder.getT_date().setText(new SimpleDateFormat("dd.MM.y\nHH:mm", Locale.getDefault()).format(item.date));
        holder.getT_cost().setText(String.format(Locale.ENGLISH, "%.2f", item.cost));
        holder.getT_description().setText(item.description);
        holder.getI_received().setImageResource(item.received ? R.drawable.ic_ok : R.drawable.ic_not);
        holder.getI_received().setOnClickListener(v -> {
            if (NetWork.user().getUid().equals(UID)) {
                item.received = !item.received;
                if (changed != null) changed.onChanged(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    public Payment getItem(int position) {
        return data.get(position);
    }

    /***
     * Виджет элемента списка
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView t_description;
        private final TextView t_cost;
        private final TextView t_date;
        private final ImageView i_received;

        private ViewHolder(View itemView) {
            super(itemView);
            t_description = itemView.findViewById(R.id.t_payment_description);
            t_cost = itemView.findViewById(R.id.t_payment_cost);
            t_date = itemView.findViewById(R.id.t_payment_date);
            i_received = itemView.findViewById(R.id.i_received);
        }

        private ImageView getI_received() {
            return i_received;
        }

        private TextView getT_description() {
            return t_description;
        }

        private TextView getT_cost() {
            return t_cost;
        }

        private TextView getT_date() {
            return t_date;
        }
    }


    public void setOnClickListener(View.OnClickListener listener) {
        click = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        longClick = listener;
    }

    public void setOnChangeListener(OnChanged listener) {
        changed = listener;
    }

}
