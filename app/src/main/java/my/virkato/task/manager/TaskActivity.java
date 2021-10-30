package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Task;
import my.virkato.task.manager.entity.Tasks;

/***
 * Информация о задании
 */
public class TaskActivity extends AppCompatActivity {

    private Task task;
    private NetWork dbTasks = new NetWork(NetWork.Info.TASKS);
    private NetWork dbPeople = new NetWork(NetWork.Info.USERS);
    private ArrayList<String> masters = new ArrayList<>();
    private ArrayList<String> masters_uid = new ArrayList<>();
    private ArrayList<String> spec = new ArrayList<>();

    private TextView e_description; // текст задание
    private Spinner spin_master; // специалисты
    private Spinner spin_spec; // специальности
    private Button b_approve; // завершение работ
    private Button b_create; // новое задание
    private ListView lv_reports; // список отчётов по заданию
    private Button b_add_report; // отчитаться

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

    boolean init = true;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.task);
        initialize(_savedInstanceState);
        initializeLogic();
    }


    private void initialize(Bundle _savedInstanceState) {

        e_description = findViewById(R.id.e_task_description);
        spin_master = findViewById(R.id.spin_master);
        spin_spec = findViewById(R.id.spin_spec);
        b_approve = findViewById(R.id.b_approve);
        b_create = findViewById(R.id.b_create);
        lv_reports = findViewById(R.id.lv_reports);
        b_add_report = findViewById(R.id.b_add_report);

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

        b_approve.setOnClickListener(_view -> {
            // изменить статус задания
            task.finished = !task.finished;
            b_create.performClick();
            showTask();
        });

        b_create.setOnClickListener(_view -> {
            // отправить новое задание в базу
            task.master_uid = masters_uid.get(spin_master.getSelectedItemPosition());
            task.description = e_description.getText().toString();
            task.send(_view.getContext(), dbTasks.getDB());
        });

        b_add_report.setOnClickListener(_view -> {
            // перейти к странице создания отчёта о ходе работ
            startActivity(new Intent(this, ReportActivity.class));
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


    private void showTask() {
        showViewsForUser(NetWork.isAdmin());

        if (!task.master_uid.equals("")) {
            e_description.setText(task.description);
            b_create.setText("Изменить это задание");
            b_approve.setText(task.finished ? "Считать невыполненным" : "Считать выполненным");
        }
    }


    private void showViewsForUser(boolean admin) {
        spin_master.setClickable(admin);
        spin_spec.setClickable(admin);
        e_description.setEnabled(admin);
        b_create.setVisibility(admin ? View.VISIBLE : View.GONE);
        b_add_report.setVisibility(admin ? View.GONE : View.VISIBLE);
        b_approve.setVisibility((admin && !task.master_uid.equals("")) ? View.VISIBLE : View.GONE);
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
            Log.e("status", (task.finished?"В":"Нев")+"ыполнено");
            Log.e("состояние", getIntent().getStringExtra("task"));
        }

        People.OnPeopleUpdatedListener onPeopleUpdatedListener = (list, man) -> {
            spec.clear();
            for (Man m : list) {
                String cvalif = m.spec;
                if (!cvalif.equals("admin") && !spec.contains(cvalif)) spec.add(cvalif);
            }
            ((ArrayAdapter) spin_spec.getAdapter()).notifyDataSetChanged();

            if (init) {
                if (!task.master_uid.equals("")) {
                    spin_spec.setSelection(spec.indexOf(dbPeople.getPeople().findManById(task.master_uid).spec));
                }
            }
        };
        dbPeople.getPeople().setPeopleListener(onPeopleUpdatedListener);

        AppUtil.showSystemWait(this, true);
    }


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

            showTask();
        }
    }


    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {

        super.onActivityResult(_requestCode, _resultCode, _data);

        switch (_requestCode) {

            default:
                break;
        }
    }

}
