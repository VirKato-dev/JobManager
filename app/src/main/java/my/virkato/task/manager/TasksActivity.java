package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import my.virkato.task.manager.adapter.Lv_tasksAdapter;
import my.virkato.task.manager.adapter.NetWork;
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

    private NetWork dbUsers;
    private People people;
    private NetWork dbTasks;
    private Tasks tasks;
    private Intent detail = new Intent();

    private Lv_tasksAdapter adapter;

    private final ArrayList<Task> lm_progress = new ArrayList<>();
    private final ArrayList<Task> lm_finished = new ArrayList<>();
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

        showWaitBanner(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //
                    }
                });
            }
        };
        new Timer().schedule(timerTask, 1000);
    }


    private void showWaitBanner(boolean f) {
        AppUtil.showSystemWait(this, f);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (NetWork.isAdmin()) {
            showFAB();
            separateTasks(dbTasks.getTasks().getList(), false, new Task());
        }
        dbUsers.restartListening(dbUsers.getFolder());
        dbTasks.restartListening(dbTasks.getFolder());
    }


    private void showFAB() {
        fab_add_task.setVisibility(View.VISIBLE);
        fab_add_task.setOnClickListener(v -> {
            // запустить просмотр пустого задания (создание нового)
            startActivity(new Intent(v.getContext(), TaskActivity.class).putExtra("task", ""));
        });
    }


    People.OnAdminsUpdatedListener adminsListener = () -> {
        if (NetWork.user() == null) startActivity(new Intent(getApplicationContext(), AuthActivity.class));
        AppUtil.showSystemWait(getApplicationContext(),false);
    };


    /***
     * Первоначальные настройки переменных
     */
    private void initVariables() {
        // new NetWork() заодно обновит состояние авторизации при каждом создании объекта
        dbUsers = new NetWork(NetWork.Info.USERS);
        people = dbUsers.getPeople();
        people.setAdminsListener(adminsListener);

        dbTasks = new NetWork(NetWork.Info.TASKS);
        tasks = dbTasks.getTasks();
        adapter = new Lv_tasksAdapter(lm_progress);
        UID = getIntent().getStringExtra("uid"); // для какого пользователя
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

        lv_tasks.setAdapter(adapter);

        lv_tasks.setOnItemClickListener((_param1, _param2, _position, _param4) -> {
            detail.setClass(getApplicationContext(), TaskActivity.class);
            if (finished) {
                detail.putExtra("task", lm_finished.get(_position).asJson());
            } else {
                detail.putExtra("task", lm_progress.get(_position).asJson());
            }
            startActivity(detail);
        });

        b_work.setOnClickListener(_view -> {
            finished = false;
            setMainList();
        });

        b_finished.setOnClickListener(_view -> {
            finished = true;
            setMainList();
        });
    }


    private void setMainList() {
        if (finished) {
            adapter.setNewList(lm_finished);
        } else {
            adapter.setNewList(lm_progress);
        }
    }

    /***
     * Слушаем(получаем актуальный) список заданий
     */
    private void receiveTasks() {
        tasks.setOnTasksUpdatedListener(this::separateTasks);
        dbTasks.restartListening(dbTasks.getFolder());
    }

    /***
     * Когда изменился список заданий
     * @param tasks весь список заданий
     * @param removed если задание удалено
     * @param task текущее еффективное задание
     */
    public void separateTasks(ArrayList<Task> tasks, boolean removed, Task task) {
        lm_progress.clear();
        lm_finished.clear();
        for (Task t : tasks) {
            if (NetWork.isAdmin() || t.master_uid.equals(UID)) {
                if (t.finished) {
                    lm_finished.add(t);
                } else {
                    lm_progress.add(t);
                }
            }
        }
        setMainList();
    }

    private void receiveUsers() {
        dbUsers.restartListening(dbUsers.getFolder());
        people.setPeopleListener(
                (list, man) -> {
                    if (NetWork.user() != null) UID = NetWork.user().getUid();
                    setMainList();
                    showWaitBanner(false);
                }
        );
    }

}
