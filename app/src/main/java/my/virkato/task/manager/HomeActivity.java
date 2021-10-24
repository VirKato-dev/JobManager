package my.virkato.task.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import my.virkato.task.manager.adapter.Lv_tasksAdapter;
import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.bean.Man;
import my.virkato.task.manager.bean.People;

/***
 * Страница со списком заданий для конкретного мастера
 */
public class HomeActivity extends AppCompatActivity {

    private Timer _timer = new Timer();

    private HashMap<String, Object> task = new HashMap<>();
    private boolean finished = false;

    private NetWork netWork = new NetWork("users");
    private People people = netWork.getPeople();

    private ArrayList<HashMap<String, Object>> lm_tasks = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> lm_progress = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> lm_finished = new ArrayList<>();

    private LinearLayout linear1;
    private LinearLayout linear2;
    private ListView lv_tasks;
    private Button b_work;
    private Button b_finished;

    private TimerTask delay;
    private AlertDialog.Builder d_wait;
    private Intent detail = new Intent();


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.home);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {

        linear1 = findViewById(R.id.linear1);
        linear2 = findViewById(R.id.linear2);
        lv_tasks = findViewById(R.id.lv_tasks);
        b_work = findViewById(R.id.button1);
        b_finished = findViewById(R.id.button2);
        d_wait = new AlertDialog.Builder(this);

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

        b_finished.setOnClickListener(_view -> {
            finished = true;
            lv_tasks.setAdapter(new Lv_tasksAdapter(lv_tasks.getContext(), lm_finished));
            ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
        });
    }

    private void initializeLogic() {
        AppUtil.showSystemWait(this,true);
        _initList();
        _separateTasks();
        delay = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    lv_tasks.setAdapter(new Lv_tasksAdapter(lv_tasks.getContext(), lm_progress));
                    ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
                    AppUtil.showSystemWait(lv_tasks.getContext(),false);
                });
            }
        };
        _timer.schedule(delay, 1000);
    }

    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {

        super.onActivityResult(_requestCode, _resultCode, _data);

        switch (_requestCode) {

            default:
                break;
        }
    }

    public void _initList() {
        ArrayList<Man> fio = new ArrayList<>();
        for (Man man : people.getList()) {
            if (!man.spec.equals("admin")) {
                fio.add(man);
            }
        }

        lm_tasks.clear();
        for (int n = 0; n < 100; n++) {
            task = new HashMap<>();
            task.put("text", "text" + (long) (n));
            task.put("uid", fio.get(AppUtil.getRandom(0, fio.size()-1)).uid);
            task.put("finished", AppUtil.getRandom(0,1));
            task.put("id", new DecimalFormat("00000000").format(AppUtil.getRandom(0, 99999999)));
            lm_tasks.add(task);
        }
    }


    public void _separateTasks() {
        lm_progress.clear();
        lm_finished.clear();
        if (!"".equals(getIntent().getStringExtra("uid"))) {
            for (HashMap<String, Object> map : lm_tasks) {
                if (map.get("uid").toString().equals(getIntent().getStringExtra("uid"))) {
                    if (map.get("finished").toString().equals("1")) {
                        lm_finished.add(map);
                    } else {
                        lm_progress.add(map);
                    }
                }
            }
        }
    }

}
