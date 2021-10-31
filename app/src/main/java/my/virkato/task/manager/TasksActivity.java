package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private Lv_tasksAdapter tasksAdapter;

    private final ArrayList<Task> lm_progress = new ArrayList<>();
    private final ArrayList<Task> lm_finished = new ArrayList<>();

    private String UID;
    private boolean finished = false;

    private NetWork dbUsers = new NetWork(NetWork.Info.USERS);
    private NetWork dbTasks = new NetWork(NetWork.Info.TASKS);
    private NetWork dbAdmins = new NetWork(NetWork.Info.ADMINS);

    private People people = dbUsers.getPeople();
    private Tasks tasks = dbTasks.getTasks();
    private People admins = dbAdmins.getPeople();

    private People.OnPeopleUpdatedListener peopleListener = (list, man) -> {
        if (NetWork.user() != null) UID = NetWork.user().getUid();
        else startActivity(new Intent(getApplicationContext(), AuthActivity.class));
        setMainList();
        showWaitBanner(false);
        separateTasks(tasks.getList(), false, null);
    };

    private People.OnAdminsUpdatedListener adminsListener = () -> {
        if (NetWork.user() == null) startActivity(new Intent(getApplicationContext(), AuthActivity.class));
        showWaitBanner(false);
        separateTasks(tasks.getList(), false, null);
    };

    private Tasks.OnTasksUpdatedListener tasksListener = (tasks, removed, task) -> {
        separateTasks(tasks, removed, task);
    };


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.tasks);
    }


    @Override
    protected void onResume() {
        super.onResume();

        showWaitBanner(true);

        initVariables();
    }

    /***
     * Первоначальные настройки переменных
     */
    private void initVariables() {
        lv_tasks = findViewById(R.id.lv_tasks);
        b_work = findViewById(R.id.button1);
        b_finished = findViewById(R.id.button2);
        fab_add_task = findViewById(R.id.fab_add_task);
        fab_add_task.setVisibility(View.GONE);

        UID = getIntent().getStringExtra("uid"); // для какого пользователя

        dbAdmins.stopListening();
        dbUsers.stopListening();
        dbTasks.stopListening();
        admins.setAdminsListener(adminsListener);
        people.setOnPeopleUpdatedListener(peopleListener);
        tasks.setOnTasksUpdatedListener(tasksListener);
        dbAdmins.startListening();
        dbUsers.startListening();
        dbTasks.startListening();

        tasksAdapter = new Lv_tasksAdapter(lm_progress);
        lv_tasks.setAdapter(tasksAdapter);
        tasksAdapter.notifyDataSetChanged();

        lv_tasks.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(parent.getContext(), TaskActivity.class)
                    .putExtra("task", ((Task) parent.getAdapter().getItem(position)).asJson())
            );
        });

        b_work.setOnClickListener(view -> {
            finished = false;
            setMainList();
        });

        b_finished.setOnClickListener(view -> {
            finished = true;
            setMainList();
        });
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


    private void setMainList() {
        if (finished) {
            tasksAdapter.setNewList(lm_finished);
        } else {
            tasksAdapter.setNewList(lm_progress);
        }
        showFAB(NetWork.isAdmin());
    }


    private void showWaitBanner(boolean f) {
        AppUtil.showSystemWait(this, f);
    }


    private void showFAB(boolean f) {
        fab_add_task.setVisibility(f ? View.VISIBLE : View.GONE);
        fab_add_task.setOnClickListener(v -> {
            // запустить просмотр пустого задания (создание нового)
            startActivity(new Intent(v.getContext(), TaskActivity.class).putExtra("task", ""));
        });
    }

}
