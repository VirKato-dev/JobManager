package my.virkato.task.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class HomeActivity extends AppCompatActivity {

    private Timer _timer = new Timer();

    private double n = 0;
    private double size = 0;
    private HashMap<String, Object> task = new HashMap<>();
    private boolean finished = false;

    private ArrayList<HashMap<String, Object>> lm_tasks = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> lm_progress = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> lm_finished = new ArrayList<>();
    private ArrayList<String> fio = new ArrayList<>();

    private LinearLayout linear1;
    private LinearLayout linear2;
    private ListView lv_tasks;
    private Button button1;
    private Button button2;

    private TimerTask delay;
    private AlertDialog.Builder d_wait;
    private Intent detail = new Intent();

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.home);
        initialize(_savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {

        linear1 = (LinearLayout) findViewById(R.id.linear1);
        linear2 = (LinearLayout) findViewById(R.id.linear2);
        lv_tasks = (ListView) findViewById(R.id.lv_tasks);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        d_wait = new AlertDialog.Builder(this);

        lv_tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
                final int _position = _param3;
                detail.setClass(getApplicationContext(), TaskActivity.class);
                if (finished) {
                    detail.putExtra("task", new Gson().toJson(lm_finished.get((int) (_position))));
                } else {
                    detail.putExtra("task", new Gson().toJson(lm_progress.get((int) (_position))));
                }
                startActivity(detail);
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                finished = false;
                lv_tasks.setAdapter(new Lv_tasksAdapter(lm_progress));
                ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                finished = true;
                lv_tasks.setAdapter(new Lv_tasksAdapter(lm_finished));
                ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void initializeLogic() {
        _system_wait(true);
        _initList();
        _separateTasks();
        delay = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv_tasks.setAdapter(new Lv_tasksAdapter(lm_progress));
                        ((BaseAdapter) lv_tasks.getAdapter()).notifyDataSetChanged();
                        _system_wait(false);
                    }
                });
            }
        };
        _timer.schedule(delay, (int) (1000));
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
        fio.clear();
        fio.add("Иванов И.И.");
        fio.add("Петров П.П.");
        fio.add("Сидоров С.С.");
        lm_tasks.clear();
        for (int n = 0; n < (int) (100); n++) {
            task = new HashMap<>();
            task.put("text", "text".concat(String.valueOf((long) (n))));
            task.put("fio", fio.get((int) (n % fio.size())));
            task.put("finished", (n % 2) == 0);
            task.put("id", new DecimalFormat("00000000").format(AppUtil.getRandom((int) (0), (int) (99999999))));
            lm_tasks.add(task);
        }
    }


    public void _separateTasks() {
        lm_progress.clear();
        lm_finished.clear();
        n = 0;
        for (int _repeat12 = 0; _repeat12 < (int) (lm_tasks.size()); _repeat12++) {
            if (!"".equals(getIntent().getStringExtra("fio"))) {
                if (lm_tasks.get((int) n).get("fio").toString().equals(getIntent().getStringExtra("fio"))) {
                    if ((boolean) lm_tasks.get((int) (n)).get("finished")) {
                        lm_finished.add(lm_tasks.get((int) (n)));
                    } else {
                        lm_progress.add(lm_tasks.get((int) (n)));
                    }
                }
            }
            n++;
        }
    }


    public void _system_wait(final boolean _show) {
        if (_show) {
            LayoutInflater design = getLayoutInflater();

            View convertView = (View) design.inflate(R.layout.loader, null);
            d_wait.setView(convertView);
            d_wait.setCancelable(false);
            adv = d_wait.create();
            adv.show();
            size = AppUtil.getDip(getApplicationContext(), (int) (100));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(adv.getWindow().getAttributes());
            lp.width = (int) size;
            lp.height = (int) size;
            adv.getWindow().setAttributes(lp);

            adv.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            adv.hide();
        }
    }

    private AlertDialog adv;

    {
    }


    public class Lv_tasksAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> _data;

        public Lv_tasksAdapter(ArrayList<HashMap<String, Object>> _arr) {
            _data = _arr;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public HashMap<String, Object> getItem(int _index) {
            return _data.get(_index);
        }

        @Override
        public long getItemId(int _index) {
            return _index;
        }

        @Override
        public View getView(final int _position, View _v, ViewGroup _container) {
            LayoutInflater _inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View _view = _v;
            if (_view == null) {
                _view = _inflater.inflate(R.layout.a_task, null);
            }

            final TextView textview1 = (TextView) _view.findViewById(R.id.textview1);

            textview1.setText(_data.get((int) _position).get("text").toString());

            return _view;
        }
    }

    @Deprecated
    public void showMessage(String _s) {
        Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
    }

    @Deprecated
    public int getLocationX(View _v) {
        int _location[] = new int[2];
        _v.getLocationInWindow(_location);
        return _location[0];
    }

    @Deprecated
    public int getLocationY(View _v) {
        int _location[] = new int[2];
        _v.getLocationInWindow(_location);
        return _location[1];
    }

    @Deprecated
    public int getRandom(int _min, int _max) {
        Random random = new Random();
        return random.nextInt(_max - _min + 1) + _min;
    }

    @Deprecated
    public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
        ArrayList<Double> _result = new ArrayList<Double>();
        SparseBooleanArray _arr = _list.getCheckedItemPositions();
        for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
            if (_arr.valueAt(_iIdx))
                _result.add((double) _arr.keyAt(_iIdx));
        }
        return _result;
    }

    @Deprecated
    public float getDip(int _input) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
    }

    @Deprecated
    public int getDisplayWidthPixels() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Deprecated
    public int getDisplayHeightPixels() {
        return getResources().getDisplayMetrics().heightPixels;
    }

}
