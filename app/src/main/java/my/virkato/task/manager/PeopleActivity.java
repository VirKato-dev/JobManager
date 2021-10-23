package my.virkato.task.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.adapter.Lv_peopleAdapter;
import my.virkato.task.manager.adapter.Net;
import my.virkato.task.manager.bean.People;


public class PeopleActivity extends AppCompatActivity {


    private HashMap<String, Object> man = new HashMap<>();

    private ArrayList<HashMap<String, Object>> lm_people = new ArrayList<>();

    private ListView lv_people;

    private Intent tasks = new Intent();
    private Intent profile = new Intent();
    private Intent authentication = new Intent();
    private SharedPreferences sp;
    private Net net = new Net();


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.people);
        initialize(_savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            initializeLogic();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initializeLogic();
        }
    }


    private void initialize(Bundle _savedInstanceState) {
        lv_people = findViewById(R.id.lv_people);
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        lv_people.setOnItemClickListener((_param1, _param2, _position, _param4) -> {
            tasks.setClass(getApplicationContext(), HomeActivity.class);
            tasks.putExtra("fio", lm_people.get(_position).get("fio").toString());
            startActivity(tasks);
        });

        lv_people.setOnItemLongClickListener((_param1, _param2, _position, _param4) -> {
            profile.setClass(getApplicationContext(), ProfileActivity.class);
            profile.putExtra("man", new Gson().toJson(lm_people.get(_position)));
            startActivity(profile);
            return true;
        });
    }


    private void initializeLogic() {
        lm_people = net.getPeople().toListMap();
        lv_people.setAdapter(new Lv_peopleAdapter(this, lm_people));
        ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
        net.getPeople().setListener(list -> {
            lm_people = list;
            ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if ((FirebaseAuth.getInstance().getCurrentUser() != null)) {
            if ("".equals(sp.getString("account", ""))) {
                man = new HashMap<>();
                man.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                man.put("phone", sp.getString("phone", ""));
                man.put("fio", "");
                man.put("spec", "");
                profile.setClass(getApplicationContext(), ProfileActivity.class);
                profile.putExtra("man", new Gson().toJson(man));
                startActivity(profile);
            }
        } else {
            authentication.setClass(getApplicationContext(), AuthActivity.class);
            startActivity(authentication);
        }
    }


    /***
     * при получении новых данных
     * @param _uid ключ в базе
     * @param _man данные
     */
    public void _addUser(final String _uid, final HashMap<String, Object> _man) {
        _man.put("uid", _uid);
        lm_people.add(_man);
        AppUtil.sortListMap(lm_people, "fio", false, true);
        if (_uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            sp.edit().putString("account", new Gson().toJson(_man)).commit();
            AppUtil.showMessage(getApplicationContext(), "Ваши данные обновленны");
        }
    }

}
