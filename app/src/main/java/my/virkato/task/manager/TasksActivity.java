package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Iterator;

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
    private Button b_people;
    private Button b_my_profile;
    private FloatingActionButton fab_add_task;

    private Lv_tasksAdapter tasksAdapter;

    private final ArrayList<Task> lm_progress = new ArrayList<>();
    private final ArrayList<Task> lm_finished = new ArrayList<>();

    private String UID;
    private boolean finished = false;

    private NetWork dbUsers = NetWork.getInstance(NetWork.Info.USERS); //new NetWork(NetWork.Info.USERS);
    private NetWork dbTasks = NetWork.getInstance(NetWork.Info.TASKS); //new NetWork(NetWork.Info.TASKS);
    private NetWork dbAdmins = NetWork.getInstance(NetWork.Info.ADMINS); //new NetWork(NetWork.Info.ADMINS);

    private People people = dbUsers.getPeople();
    private Tasks tasks = dbTasks.getTasks();
    private People admins = dbAdmins.getPeople();

    private People.OnPeopleUpdatedListener peopleListener = (list, man) -> {
        setMainList();
        showWaitBanner(false);
        separateTasks(tasks.getList(), false, null);
    };

    private People.OnAdminsUpdatedListener adminsListener = () -> {
        showWaitBanner(false);
        separateTasks(tasks.getList(), false, null);
    };

    private Tasks.OnTasksUpdatedListener tasksListener = (tasks, removed, task) -> {
        separateTasks(tasks, removed, task);
    };


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_tasks);

        dbAdmins.receiveNewData();
        dbUsers.receiveNewData();
        dbTasks.receiveNewData();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (NetWork.user() == null) startActivity(new Intent(getApplicationContext(), AuthActivity.class));
        showWaitBanner(true);

        initVariables();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbUsers.stopReceiving();
    }

    /***
     * Первоначальные настройки переменных
     */
    private void initVariables() {
        lv_tasks = findViewById(R.id.lv_tasks);
        b_work = findViewById(R.id.button1);
        b_finished = findViewById(R.id.button2);
        b_my_profile = findViewById(R.id.b_my_profile);
        b_people = findViewById(R.id.b_people);
        fab_add_task = findViewById(R.id.fab_add_task);
        fab_add_task.setVisibility(View.GONE);

        UID = getIntent().getStringExtra("uid"); // для какого пользователя
        if (UID == null) UID = NetWork.user().getUid();

        tasksAdapter = new Lv_tasksAdapter(lm_progress);
        lv_tasks.setAdapter(tasksAdapter);
        tasksAdapter.notifyDataSetChanged();

        dbAdmins.stopReceiving();
        dbUsers.stopReceiving();
        dbTasks.stopReceiving();
        admins.setAdminsListener(adminsListener);
        people.setOnPeopleUpdatedListener(peopleListener);
        tasks.setOnTasksUpdatedListener(tasksListener);
        dbAdmins.startReceiving();
        dbUsers.startReceiving();
        dbTasks.startReceiving();

        lv_tasks.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(parent.getContext(), TaskActivity.class)
                    .putExtra("task", parent.getAdapter().getItem(position).toString())
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

        b_my_profile.setOnClickListener( view -> {
            Intent profile = new Intent(view.getContext(), ProfileActivity.class);
            profile.putExtra("man", dbUsers.getPeople().findManById(NetWork.user().getUid()).toString());
            startActivity(profile);
        });

        b_people.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), PeopleActivity.class));
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
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (!t.master_uid.equals(UID)) {
                it.remove();
            }
        }
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
//        b_people.setVisibility(NetWork.isAdmin()?View.VISIBLE:View.GONE);
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
