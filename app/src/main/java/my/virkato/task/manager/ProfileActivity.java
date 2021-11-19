package my.virkato.task.manager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.Task;
import my.virkato.task.manager.entity.Tasks;

/***
 * Страница профиля пользователя
 */
public class ProfileActivity extends AppCompatActivity {


    private Man you = new Man();

    private TextView t_phone;
    private EditText e_fio;
    private EditText e_spec;
    private Button b_save;

    private SharedPreferences sp;

    private final NetWork dbUsers = new NetWork(NetWork.Info.USERS);
    private final NetWork dbAdmins = new NetWork(NetWork.Info.ADMINS);
    private final NetWork dbTasks = new NetWork(NetWork.Info.TASKS);


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.profile);

        AppUtil.showSystemWait(this, true);
        initialize(_savedInstanceState);
        initializeLogic();

        dbAdmins.getPeople().setAdminsListener(this::initializeLogic);

        dbUsers.getPeople().setOnPeopleUpdatedListener((list, man) -> {
            if (NetWork.user() != null) {
                Man tmp = dbUsers.getPeople().findManById(you.id);
                if (tmp != null) {
                    you = tmp;
                    showUserData();
                }
            }
        });

        dbTasks.getTasks().setOnTasksUpdatedListener((tasks, removed, task) -> {
            updateStatistic(tasks);
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbUsers.stopReceiving();
    }


    private void initialize(Bundle _savedInstanceState) {
        t_phone = findViewById(R.id.t_phone);
        e_fio = findViewById(R.id.e_fio);
        e_spec = findViewById(R.id.e_spec);
        b_save = findViewById(R.id.b_save);
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        b_save.setOnClickListener(view -> {
            you.fio = e_fio.getText().toString();
            you.spec = e_spec.getText().toString();
            if (you.id.equals(NetWork.user().getUid())) {
                if (!you.phone.equals("")) {
                    you.phone = sp.getString("phone", "");
                }
            }
            if (you.spec.trim().equals("") || you.fio.trim().equals("")) {
                AppUtil.showMessage(view.getContext(), getString(R.string.fill_please));
            } else {
                dbUsers.getDB().child(you.id).updateChildren(you.asMap());
            }
        });
    }


    private void initializeLogic() {
        if ((NetWork.user() != null)) {
            if ("".equals(getIntent().getStringExtra("man"))) {
                you.id = NetWork.user().getUid();
            } else {
                you = new Man(new Gson().fromJson(getIntent().getStringExtra("man"),
                        new TypeToken<HashMap<String, Object>>() {
                        }.getType()));
            }
            showUserData();
        } else {
            finish();
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


    private void showUserData() {
        e_fio.setEnabled(false);
        e_spec.setEnabled(false);
        b_save.setVisibility(View.GONE);
        if ((NetWork.user() != null)) {
            if (NetWork.user().getUid().equals(you.id) || NetWork.isAdmin()) {
                e_fio.setEnabled(true);
                e_spec.setEnabled(true);
                b_save.setVisibility(View.VISIBLE);
            }
        }

        e_fio.setText(you.fio);
        e_spec.setText(you.spec);
        t_phone.setText(you.phone);
        AppUtil.showSystemWait(this, false);
    }

    private void updateStatistic(ArrayList<Task> tasks) {
        int tasks_total = 0;
        int tasks_done = 0;
        double pay_total = 0d;
        double pay_done = 0d;

        for (Task task : tasks) {
            if (you != null && you.id.equals(task.master_uid)) {
                tasks_total++;
                tasks_done += task.rewarded ? 1 : 0;
                pay_total += task.reward;
                pay_done += task.reward_got ? task.reward : 0d;

            }
        }

        ((TextView) findViewById(R.id.t_total_tasks)).setText(String.valueOf(tasks_total));
        ((TextView) findViewById(R.id.t_finished_tasks)).setText(String.valueOf(tasks_done));
        ((TextView) findViewById(R.id.t_total_pay)).setText(String.format(Locale.ENGLISH, "%.2f", pay_total));
        ((TextView) findViewById(R.id.t_finished_pay)).setText(String.format(Locale.ENGLISH, "%.2f", pay_done));
    }
}
