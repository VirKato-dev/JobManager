package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
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
    private Tasks tasks = dbTasks.getTasks();
    private NetWork dbPeople = new NetWork(NetWork.Info.USERS);
    private People people = dbPeople.getPeople();
    private ArrayList<String> masters = new ArrayList<>();
    private ArrayList<String> spec = new ArrayList<>();

    private TextView e_description;
    private Spinner spin_master;
    private Spinner spin_spec;
    private Button b_approve;
    private Button b_create;
    private ListView lv_reports;
    private Button b_add_report;

    private FirebaseDatabase fb_db = FirebaseDatabase.getInstance();
    private DatabaseReference dbt = fb_db.getReference("tasks");
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
        FirebaseApp.initializeApp(this);
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
        });

        b_create.setOnClickListener(_view -> {
        });

        b_add_report.setOnClickListener(_view -> {
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


    private void showViewsForUser(boolean admin) {
        spin_master.setClickable(admin);
        spin_spec.setClickable(admin);
        e_description.setEnabled(admin);
        b_approve.setVisibility(admin?View.VISIBLE:View.GONE);
        b_add_report.setVisibility(admin?View.GONE:View.VISIBLE);
    }


    private void initializeLogic() {
        if ("".equals(getIntent().getStringExtra("task"))) {
            // создать новое задание
            b_approve.setVisibility(View.GONE);
            lv_reports.setVisibility(View.GONE);
            b_add_report.setVisibility(View.GONE);

            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", dbt.push().getKey());
            task = new Task(taskMap);
        } else {
            // изменить/просмотреть задание
            task = new Task(new Gson().fromJson(getIntent().getStringExtra("task"), new TypeToken<HashMap<String, Object>>() {
            }.getType()));
            e_description.setText(task.description);
            b_create.setVisibility(View.GONE);

            if ((NetWork.user() != null)) {
                showViewsForUser(dbPeople.isAdmin());
            }
        }

        People.OnPeopleUpdatedListener onPeopleUpdatedListener = (list, man) -> {
            spec.clear();
            for (HashMap<String, Object> map : list) {
                String cvalif = map.get("spec").toString();
                if (!cvalif.equals("admin") && !spec.contains(cvalif)) spec.add(cvalif);
            }
            ((ArrayAdapter) spin_spec.getAdapter()).notifyDataSetChanged();
            updateMastersBySpec();
        };
        dbPeople.getPeople().setPeopleListener(onPeopleUpdatedListener);
    }


    private void updateMastersBySpec() {
        if (spec.size()>0 && spin_spec.getSelectedItemPosition()>=0) {
            ArrayList<String> selected = new ArrayList<>();
            for (Man m : dbPeople.getPeople().getList()) {
                if (m.spec.equals(spec.get(spin_spec.getSelectedItemPosition()))) {
                    selected.add(m.fio);
                }
            }
            masters.clear();
            masters.addAll(selected);
            ((ArrayAdapter) spin_master.getAdapter()).notifyDataSetChanged();
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
