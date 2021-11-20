package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import my.virkato.task.manager.adapter.Lv_reportsAdapter;
import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.Payment;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Report;
import my.virkato.task.manager.entity.Reports;
import my.virkato.task.manager.entity.Task;

/***
 * Информация о задании
 */
public class TaskActivity extends AppCompatActivity {

    /***
     * адаптер для получения списка отчётов
     */
    private NetWork dbReports = new NetWork(NetWork.Info.REPORTS);

    /***
     * ссылка на отчёты
     */
    private Reports reports = dbReports.getReports();

    /***
     * адаптер списка отчётов
     */
    private Lv_reportsAdapter reportsAdapter;

    /***
     * отчёты
     */
    private ArrayList<Report> lm_reports = new ArrayList<>();

    /***
     * адаптер для получения списка заданий
     */
    private NetWork dbTasks = new NetWork(NetWork.Info.TASKS);

    /***
     * текущее задание
     */
    private Task task = new Task();

    /***
     * адаптер для получения списка людей
     */
    private NetWork dbPeople = new NetWork(NetWork.Info.USERS);

    /***
     * имена мастеров текущей специализации
     */
    private ArrayList<String> masters = new ArrayList<>();

    /***
     * UID мастеров текущей специализации
     */
    private ArrayList<String> masters_uid = new ArrayList<>();

    /***
     * все специализации найденные в списке людей
     */
    private ArrayList<String> spec = new ArrayList<>();

    /***
     * поле ввода текста задания
     */
    private TextView e_description;

    /***
     * список имён мастеров
     */
    private Spinner spin_master;

    /***
     * список специализаций мастеров
     */
    private Spinner spin_spec;

    /***
     * меняем статус задания
     */
    private Button b_approve;

    /***
     * сохраняем задание
     */
    private Button b_create;

    /***
     * сумма вознаграждения
     */
    private EditText e_reward;

    /***
     * маркер получения оплаты
     */
    private ImageView i_reward_got;

    /***
     * вывод списка отчётов по заданию
     */
    private ListView lv_reports;

    /***
     * перейти к странице создания отчёта о ходе работ
     */
    private Button b_add_report;

    /***
     * поля ввода дат выполнения задания с помощью диалога
     */
    private LinearLayout l_task_dates;
    private LinearLayout l_date_start, l_date_end;
    private TextView t_date_start, t_date_finish;
    private ImageView i_date_start, i_date_finish;

    /***
     * поля ввода очередного перевода/платежа
     */
    private LinearLayout l_payment;
    private EditText e_payment;
    private Button b_payment;
    private ImageView i_payment_list;

    /***
     * первичное заполнение данных для существующего задания
     */
    boolean init = true;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_task);
        initialize(_savedInstanceState);
        initializeLogic();

//        dbPeople.receiveNewData();
        dbTasks.receiveNewData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        dbTasks.getTasks().setOnTasksUpdatedListener((tasks, removed, t) -> {
            Task tmp = dbTasks.getTasks().findTaskById(task.id);
            if (tmp != null) {
                task = tmp;
                showViewsForUser();
            }
        });

        if (NetWork.user() != null) {
            showViewsForUser();
            receiveAllReports();
        } else finish();
    }

    private void initialize(Bundle _savedInstanceState) {
        e_description = findViewById(R.id.e_task_description);
        spin_master = findViewById(R.id.spin_master);
        spin_spec = findViewById(R.id.spin_spec);
        b_approve = findViewById(R.id.b_approve);
        b_create = findViewById(R.id.b_create);
        e_reward = findViewById(R.id.e_reward);
        i_reward_got = findViewById(R.id.i_reward_got);
        lv_reports = findViewById(R.id.lv_reports);
        b_add_report = findViewById(R.id.b_add_report);
        l_task_dates = findViewById(R.id.l_task_dates);
        l_date_start = findViewById(R.id.l_date_start);
        l_date_end = findViewById(R.id.l_date_end);
        t_date_start = findViewById(R.id.t_date_start);
        t_date_finish = findViewById(R.id.t_date_finish);
        i_date_start = findViewById(R.id.i_date_start);
        i_date_finish = findViewById(R.id.i_date_finish);

        l_payment = findViewById(R.id.l_payment);
        e_payment = findViewById(R.id.e_payment);
        b_payment = findViewById(R.id.b_payment);
        i_payment_list = findViewById(R.id.i_payment_list);

//        i_date_start.setColorFilter(R.color.colorAccent, PorterDuff.Mode.MULTIPLY);
//        i_date_finish.setColorFilter(R.color.colorAccent, PorterDuff.Mode.MULTIPLY);

        spin_master.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, masters));
        ((ArrayAdapter) spin_master.getAdapter()).notifyDataSetChanged();

        spin_spec.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spec));
        ((ArrayAdapter) spin_spec.getAdapter()).notifyDataSetChanged();

        spin_spec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMastersBySpec();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        reportsAdapter = new Lv_reportsAdapter(lm_reports);
        lv_reports.setAdapter(reportsAdapter);
        reportsAdapter.notifyDataSetChanged();

        lv_reports.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(parent.getContext(), ReportActivity.class)
                    .putExtra("report", parent.getAdapter().getItem(position).toString())
            );
        });

        b_approve.setOnClickListener(view -> {
            task.finished = !task.finished;
            save();
        });

        b_create.setOnClickListener(view -> {
            save();
        });

        b_add_report.setOnClickListener(view -> {
            startActivity(new Intent(this, ReportActivity.class)
                    .putExtra("report", "{\"task_id\":\"" + task.id + "\"}"));
        });

        l_date_start.setOnClickListener(v -> {
            AppUtil.showSelectDateDialog(t_date_start, time -> {
                task.date_start = time;
            });
        });

        l_date_end.setOnClickListener(v -> {
            AppUtil.showSelectDateDialog(t_date_finish, time -> {
                task.date_finish = time;
            });
        });

        b_payment.setOnClickListener(v -> {
            Payment curPay = new Payment();
            double cost = 0d;
            try {
                cost = Double.parseDouble(e_payment.getText().toString().trim());
            } catch (Exception ignore) {}
            curPay.cost = cost;
            task.payments.add(curPay);
            if (cost != 0) task.send(this, dbTasks.getDB());
        });

        i_payment_list.setOnClickListener(v -> {
            startActivity(new Intent(this, PaymentsActivity.class)
                    .putExtra("task_id", task.id));
        });
    }

    /***
     * для пользователей и админов экран немного отличается
     */
    private void showViewsForUser() {
        boolean admin = NetWork.isAdmin();
        spin_master.setEnabled(admin); // пользователь не может изменить задание
        spin_spec.setEnabled(admin);
        e_description.setEnabled(admin);
        e_reward.setEnabled(admin && !task.finished && task.reward == 0);
        l_date_start.setEnabled(admin);
        l_date_end.setEnabled(admin);
        b_create.setVisibility(admin ? View.VISIBLE : View.GONE);
        b_add_report.setVisibility(admin ? View.GONE : View.VISIBLE);
        b_approve.setVisibility((admin && !task.master_uid.equals("")) ? View.VISIBLE : View.GONE);

        if (!task.master_uid.equals("")) {
            String currentUser = NetWork.user().getUid();
            boolean owner = currentUser.equals(task.master_uid);
            b_add_report.setVisibility((owner && !task.finished) ? View.VISIBLE : View.GONE);
            e_payment.setEnabled(!owner);
            b_payment.setVisibility(!owner ? View.VISIBLE : View.GONE);

            e_description.setText(task.description);
            double total_pay = 0d;
            double total_pay_not = 0d;
            boolean got = true;
            if (task.payments.size() > 0) {
                for (Payment pay : task.payments) {
                    if (pay.received) total_pay += pay.cost;
                    else {
                        total_pay_not += pay.cost;
                        got = false;
                    }
                }
            } else {
                got = false;
            }
            if (owner) {
                e_payment.setText(String.format(Locale.ENGLISH, "%.2f", total_pay_not));
            }

            i_reward_got.setImageResource(got ? R.drawable.ic_ok : R.drawable.ic_not);
            e_reward.setText(String.format(Locale.ENGLISH, "%.2f (%.2f)", task.reward, total_pay));
            t_date_start.setText(new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault()).format(task.date_start));
            t_date_finish.setText(new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault()).format(task.date_finish));
            b_create.setText(R.string.change_this_task);
            b_approve.setText(task.finished ? R.string.task_not_finished : R.string.task_finished);
            l_payment.setVisibility(View.VISIBLE);
        } else {
            l_payment.setVisibility(View.GONE);
        }

    }

    /***
     * сохранить задание в базу
     * и обновить виджеты
     */
    private void save() {
        task.master_uid = masters_uid.get(spin_master.getSelectedItemPosition());
        task.description = e_description.getText().toString();
        String num = e_reward.getText().toString().trim();
        int pos = num.indexOf('(');
        if (pos >= 0) num = num.substring(0, pos-1);
        double reward = 0d;
        try {
            reward = Double.parseDouble(num);
        } catch (Exception ignore) {}
        task.reward = reward;
        task.send(this, dbTasks.getDB());
        showViewsForUser();
    }


    private void initializeLogic() {
        if ("".equals(getIntent().getStringExtra("task"))) {
            // создать новое задание
            b_approve.setVisibility(View.GONE);
            lv_reports.setVisibility(View.GONE);
            b_add_report.setVisibility(View.GONE);

            task = new Task();
            task.id = dbTasks.getDB().push().getKey();
        } else {
            // изменить/просмотреть задание
            task = new Task(getIntent().getStringExtra("task"));
        }
        receiveAllMasters();
    }

    /***
     * получаем список всех отчётов и выбираем из него только отчёты к текущему заданию
     */
    private void receiveAllReports() {
        dbReports.setContext(getApplicationContext()); // для сохранения картинок на устройстве админа
        dbReports.receiveNewData(); // заново

        Reports.OnReportsUpdatedListener onReportsUpdatedListener = () -> {
            lm_reports.clear();
            lm_reports.addAll(reports.getList());

            Iterator<Report> ir = lm_reports.iterator();
            while (ir.hasNext()) {
                if (!ir.next().task_id.equals(task.id)) {
                    ir.remove();
                }
            }
            reportsAdapter.notifyDataSetChanged();
        };
        dbReports.getReports().setOnReportsUpdatedListener(onReportsUpdatedListener);
    }

    /***
     * получаем список всех мастеров и выделяем из него имена, специализации, идентификаторы
     */
    private void receiveAllMasters() {
        People.OnPeopleUpdatedListener onPeopleUpdatedListener = (list, man) -> {
            spec.clear();
            for (Man m : list) {
                String qualif = m.spec;
                if (!qualif.equals("admin") && !spec.contains(qualif)) spec.add(qualif);
            }
            ((ArrayAdapter) spin_spec.getAdapter()).notifyDataSetChanged();

            if (init) {
                if (!task.master_uid.equals("")) {
                    spin_spec.setSelection(spec.indexOf(dbPeople.getPeople().findManById(task.master_uid).spec));
                }
            }
        };
        dbPeople.getPeople().setOnPeopleUpdatedListener(onPeopleUpdatedListener);
        AppUtil.showSystemWait(this, true);
    }

    /***
     * обновляем список мастеров в соответствие с выбранной специализацией
     */
    private void updateMastersBySpec() {
        if (spec.size() > 0 && spin_spec.getSelectedItemPosition() >= 0) {
            ArrayList<String> selected = new ArrayList<>();
            masters_uid = new ArrayList<>();
            for (Man m : dbPeople.getPeople().getList()) {
                if (m.spec.equals(spec.get(spin_spec.getSelectedItemPosition()))) {
                    selected.add(m.fio);
                    masters_uid.add(m.id);
                }
            }
            masters.clear();
            masters.addAll(selected);
            ((ArrayAdapter) spin_master.getAdapter()).notifyDataSetChanged();

            if (init) {
                spin_master.setSelection(masters_uid.indexOf(task.master_uid));
                AppUtil.showSystemWait(this, false);
                init = false;
            }

            showViewsForUser();
        }
    }

}
