package my.virkato.task.manager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.adapter.Rv_paymentsAdapter;
import my.virkato.task.manager.entity.Payment;
import my.virkato.task.manager.entity.Task;

public class PaymentsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Rv_paymentsAdapter adapter;
    private final NetWork dbTasks = new NetWork(NetWork.Info.TASKS);
    private Task task;
    private String task_id;

    private LinearLayout l_current_payment;
    private EditText e_payment_cost;
    private EditText e_description;
    private Button b_payment_apply;

    private Payment curPay = new Payment();
    private int position = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        task_id = getIntent().getStringExtra("task_id");
        if (task_id.equals("")) exit();

        l_current_payment = findViewById(R.id.l_current_payment);
        e_payment_cost = findViewById(R.id.e_payment_cost);
        e_description = findViewById(R.id.e_payment_desc);

        b_payment_apply = findViewById(R.id.b_payment_apply);
        b_payment_apply.setOnClickListener(v -> {
            save();
        });

        rv = findViewById(R.id.rv_payments);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Rv_paymentsAdapter(new ArrayList<>());
        adapter.setUID(NetWork.user().getUid());
        rv.setAdapter(adapter);
        adapter.setOnChangeListener(position -> {
            task.send(this, dbTasks.getDB());
        });
        adapter.setOnClickListener(v -> {
            position = rv.getChildAdapterPosition(v);
            curPay = adapter.getItem(position);
            editPeyment();
        });

        dbTasks.getTasks().setOnTasksUpdatedListener((tasks, removed, t) -> {
            task = dbTasks.getTasks().findTaskById(task_id);
            showPaymentsList();
        });

        showPaymentsList();
        editPeyment();
    }


    private void showPaymentsList() {
        if (task != null) {
            adapter.setList(task.payments);
            adapter.notifyDataSetChanged();
        } else exit();
    }


    private void editPeyment() {
        if (curPay != null) {
            b_payment_apply.setText(R.string.button_payment_apply);
        } else {
            b_payment_apply.setText(R.string.button_payment_make);
        }
        e_payment_cost.setText(String.format(Locale.ENGLISH, "%.2f", curPay.cost));
        e_description.setText(curPay.description);
    }


    private void save() {
        double cost = 0d;
        try {
            cost = Double.parseDouble(e_payment_cost.getText().toString());
        } catch (Exception ignored) {
        }
        if (cost == 0) return;
        curPay.cost = cost;
        curPay.description = e_description.getText().toString().trim();
        curPay.date = Calendar.getInstance().getTimeInMillis();

        if (position < 0) position = task.payments.size();
        else task.payments.remove(position);

        task.payments.add(position, curPay);
        curPay = new Payment();
        position = -1;
        editPeyment();

        task.send(this, dbTasks.getDB());
    }


    private void exit() {
        finish();
    }
}