package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import my.virkato.task.manager.adapter.Lv_tasksAdapter;
import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Task;
import my.virkato.task.manager.entity.Tasks;

/***
 * Страница со списком заданий для конкретного мастера
 */
public class TasksActivity extends AppCompatActivity {

    private ListView lv_tasks;
    private Button b_work;
    private Button b_finished;
    private FloatingActionButton fab_add_task;

    private NetWork dbAdmins;
    private NetWork dbUsers;
    private NetWork dbTasks;
    private Tasks tasks;
    private Intent detail;

    private Timer _timer;
    private TimerTask delay;

    private ArrayList<HashMap<String, Object>> lm_progress;
    private ArrayList<HashMap<String, Object>> lm_finished;
    private String UID;
    private boolean finished = false;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.tasks);
        FirebaseApp.initializeApp(this);
        initVariables();
        initDesign();
        receiveTasks();
        receiveUsers();

        if (dbAdmins.getPeople().getAdminsListener() == null) {
            dbAdmins.getPeople().setAdminsListener(() -> {
                if (dbAdmins.isAdmin()) {
                    showFAB();
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (NetWork.user() != null) {
            if (dbAdmins.isAdmin()) showFAB();
        } else startActivity(new Intent(this, AuthActivity.class));
    }


    private void showFAB() {
        fab_add_task.setVisibility(View.VISIBLE);
        fab_add_task.setOnClickListener(v -> {
            // запустить просмотр пустого задания
            startActivity(new Intent(v.getContext(), TaskActivity.class).putExtra("task", ""));
        });
    }

    /***
     * Первоначальные настройки переменных
     */
    private void initVariables() {
        dbAdmins = new NetWork(NetWork.Info.ADMINS); // заодно обновит состояние авторизации
        dbUsers = new NetWork(NetWork.Info.USERS);
        dbTasks = new NetWork(NetWork.Info.TASKS);
        tasks = dbTasks.getTasks();
        detail = new Intent();
        lm_progress = new ArrayList<>();
        lm_finished = new ArrayList<>();
        _timer = new Timer();
        UID = getIntent().getStringExtra("uid"); // для какого пользователя
        if (UID == null) UID = "";
    }


    /***
     * Оформляем экран
     */
    private void initDesign() {
        lv_tasks = findViewById(R.id.lv_tasks);
        b_work = findViewById(R.id.button1);
        b_finished = findViewById(R.id.button2);
        fab_add_task = findViewById(R.id.fab_add_task);
        fab_add_task.setVisibility(View.GONE);

        lv_tasks.setOnItemClickListener((_param1, _param2, _position, _param4) -> {
            detail.setClass(getApplicationContext(), TaskActivity.class);
            if (finished) {
                detail.putExtra("task", new Gson().toJson(lm_finished.get(_position)));
            } else {
                detail.putExtra("task", new Gson().toJson(lm_progress.get(_position)));
            }
            startActivity(detail);
        });

        b_work.setOnClickListener(_view -> {
            finished = false;
            lv_tasks.setAdapter(new Lv_tasksAdapter(lv_tasks.getContext(), lm_progress));
            ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
        });
        b_work.performClick();

        b_finished.setOnClickListener(_view -> {
            finished = true;
            lv_tasks.setAdapter(new Lv_tasksAdapter(lv_tasks.getContext(), lm_finished));
            ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
        });
    }


    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
        super.onActivityResult(_requestCode, _resultCode, _data);
        switch (_requestCode) {
            default:
                break;
        }
    }

    /***
     * Слушаем(получаем актуальный) список заданий
     */
    private void receiveTasks() {
        tasks.setOnTasksUpdatedListener(this::separateTasks); // сигнатура метода соответствует интерфейсу
    }

    /***
     * Когда изменился список заданий
     * @param tasks весь список заданий
     * @param removed если задание удалено
     * @param task текущее еффективное задание
     */
    public void separateTasks(ArrayList<Task> tasks, boolean removed, Task task) {
        Log.e("ПОЛУЧЕНО ЗАДАНИЕ", task.master_uid + " : " + task.description);

        lm_progress.clear();
        lm_finished.clear();
        for (Task t : tasks) {
            if (dbAdmins.isAdmin() || t.master_uid.equals(UID)) {
                if (t.finished) {
                    lm_finished.add(t.asMap());
                } else {
                    lm_progress.add(t.asMap());
                }
            }
        }
        ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
//        lv_tasks.invalidate();
    }

    private void receiveUsers() {
        dbUsers.getPeople().setPeopleListener(
                (list, man) -> {
                    ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
//                    lv_tasks.invalidate();
                });
    }

}
