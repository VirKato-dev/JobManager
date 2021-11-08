package my.virkato.task.manager;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import my.virkato.task.manager.adapter.Lv_reportsAdapter;
import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;
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
    private Task task;

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

    private TextView e_description; // текст задание

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
    private LinearLayout l_dates;
    private LinearLayout l_date_start, l_date_end;
    private TextView t_date_start, t_date_finish;
    private ImageView i_date_start, i_date_finish;

    /***
     * первичное заполнение данных для существующего задания
     */
    boolean init = true;

    private OnCompleteListener<Void> auth_updateEmailListener;
    private OnCompleteListener<Void> auth_updatePasswordListener;
    private OnCompleteListener<Void> auth_emailVerificationSentListener;
    private OnCompleteListener<Void> auth_deleteUserListener;
    private OnCompleteListener<Void> auth_updateProfileListener;
    private OnCompleteListener<AuthResult> auth_phoneAuthListener;
    private OnCompleteListener<AuthResult> auth_googleSignInListener;
    private OnCompleteListener<AuthResult> _auth_create_user_listener;
    private OnCompleteListener<AuthResult> _auth_sign_in_listener;
    private OnCompleteListener<Void> _auth_reset_password_listener;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.task);

        initialize(_savedInstanceState);
        initializeLogic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiveAllReports();
    }

    private void initialize(Bundle _savedInstanceState) {
        e_description = findViewById(R.id.e_task_description);
        spin_master = findViewById(R.id.spin_master);
        spin_spec = findViewById(R.id.spin_spec);
        b_approve = findViewById(R.id.b_approve);
        b_create = findViewById(R.id.b_create);
        lv_reports = findViewById(R.id.lv_reports);
        b_add_report = findViewById(R.id.b_add_report);
        l_dates = findViewById(R.id.l_dates);
        l_date_start = findViewById(R.id.l_date_start);
        l_date_end = findViewById(R.id.l_date_end);
        t_date_start = findViewById(R.id.t_date_start);
        t_date_finish = findViewById(R.id.t_date_finish);
        i_date_start = findViewById(R.id.i_date_start);
        i_date_finish = findViewById(R.id.i_date_finish);

        i_date_start.setColorFilter(R.color.colorAccent, PorterDuff.Mode.MULTIPLY);
        i_date_finish.setColorFilter(R.color.colorAccent, PorterDuff.Mode.MULTIPLY);

        spin_master.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, masters));
        ((ArrayAdapter)spin_master.getAdapter()).notifyDataSetChanged();

        spin_spec.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spec));
        ((ArrayAdapter)spin_spec.getAdapter()).notifyDataSetChanged();

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
                    .putExtra("report", ((Report)parent.getAdapter().getItem(position)).toString())
            );
        });

        b_approve.setOnClickListener(_view -> {
            task.finished = !task.finished;
            b_create.performClick();
            showViewsForUser(NetWork.isAdmin());
        });

        b_create.setOnClickListener(_view -> {
            task.master_uid = masters_uid.get(spin_master.getSelectedItemPosition());
            task.description = e_description.getText().toString();
            task.send(_view.getContext(), dbTasks.getDB());
        });

        b_add_report.setOnClickListener(_view -> {
            startActivity(new Intent(this, ReportActivity.class)
            .putExtra("report", "{\"task_id\":\""+task.id+"\"}"));
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

        auth_updateEmailListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_updatePasswordListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_emailVerificationSentListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_deleteUserListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_phoneAuthListener = task -> {
            final boolean _success = task.isSuccessful();
            final String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
        };

        auth_updateProfileListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_googleSignInListener = task -> {
            final boolean _success = task.isSuccessful();
            final String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
        };

        _auth_create_user_listener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        _auth_sign_in_listener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        _auth_reset_password_listener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
        };
    }

    /***
     * для пользователей и админов экран немного отличается
     * @param admin админ?
     */
    private void showViewsForUser(boolean admin) {
        spin_master.setEnabled(admin); // пользователь не может изменить задание
        spin_spec.setEnabled(admin);
        e_description.setEnabled(admin);
        l_date_start.setEnabled(admin);
        l_date_end.setEnabled(admin);

        b_create.setVisibility(admin ? View.VISIBLE : View.GONE);
        b_add_report.setVisibility(admin ? View.GONE : View.VISIBLE);
        b_approve.setVisibility((admin && !task.master_uid.equals("")) ? View.VISIBLE : View.GONE);
        if (!task.master_uid.equals("")) {
            e_description.setText(task.description);
            t_date_start.setText(new SimpleDateFormat("dd.MM.y", Locale.getDefault()).format(task.date_start));
            t_date_finish.setText(new SimpleDateFormat("dd.MM.y", Locale.getDefault()).format(task.date_finish));
            b_create.setText("Изменить это задание");
            b_approve.setText(task.finished ? "Считать невыполненным" : "Считать выполненным");
        }
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
            task = new Task(new Gson().fromJson(getIntent().getStringExtra("task"), new TypeToken<HashMap<String, Object>>() {
            }.getType()));
        }
        receiveAllMasters();
    }

    /***
     * получаем список всех отчётов и выбираем из него только отчёты к текущему заданию
     */
    private void receiveAllReports() {
        dbReports = new NetWork(NetWork.Info.REPORTS); // заново

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
                String cvalif = m.spec;
                if (!cvalif.equals("admin") && !spec.contains(cvalif)) spec.add(cvalif);
            }
            ((ArrayAdapter)spin_spec.getAdapter()).notifyDataSetChanged();

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
            ((ArrayAdapter)spin_master.getAdapter()).notifyDataSetChanged();

            if (init) {
                spin_master.setSelection(masters_uid.indexOf(task.master_uid));
                AppUtil.showSystemWait(this, false);
                init = false;
            }

            showViewsForUser(NetWork.isAdmin());
        }
    }

}
