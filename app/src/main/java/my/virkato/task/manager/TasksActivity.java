package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

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

    private Lv_tasksAdapter tasksAdapter;

    private final ArrayList<Task> lm_progress = new ArrayList<>();
    private final ArrayList<Task> lm_finished = new ArrayList<>();
    private String UID;
    private boolean finished = false;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.tasks);

        showWaitBanner(true);
    }


    private void showWaitBanner(boolean f) {
        AppUtil.showSystemWait(this, f);
    }


    @Override
    protected void onResume() {
        super.onResume();

        initVariables();
        initDesign();
    }


    private void showFAB(boolean f) {
        fab_add_task.setVisibility(f ? View.VISIBLE : View.GONE);
        fab_add_task.setOnClickListener(v -> {
            // запустить просмотр пустого задания (создание нового)
            startActivity(new Intent(v.getContext(), TaskActivity.class).putExtra("task", ""));
        });
    }


    People.OnAdminsUpdatedListener adminsListener = () -> {
        if (NetWork.user() == null)
            startActivity(new Intent(getApplicationContext(), AuthActivity.class));
        showWaitBanner(false);
        separateTasks(tasks.getList(), false, null);
    };


    /***
     * Первоначальные настройки переменных
     */
    private void initVariables() {
        // new NetWork() заодно обновит состояние авторизации при каждом создании объекта
        dbUsers = new NetWork(NetWork.Info.USERS);
        people = dbUsers.getPeople();
        people.setAdminsListener(adminsListener);
        receiveUsers();

        dbTasks = new NetWork(NetWork.Info.TASKS);
        tasks = dbTasks.getTasks();
        receiveTasks();

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

        tasksAdapter = new Lv_tasksAdapter(lm_progress);
        lv_tasks.setAdapter(tasksAdapter);
        tasksAdapter.notifyDataSetChanged();

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
            tasksAdapter.setNewList(lm_finished);
        } else {
            tasksAdapter.setNewList(lm_progress);
        }
        showFAB(NetWork.isAdmin());
    }

    /***
     * Слушаем(получаем актуальный) список заданий
     */
    private void receiveTasks() {
        tasks.setOnTasksUpdatedListener(this::separateTasks);
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
        people.setPeopleListener(
                (list, man) -> {
                    if (NetWork.user() != null) UID = NetWork.user().getUid();
                    setMainList();
                    showWaitBanner(false);
                }
        );
    }

}
