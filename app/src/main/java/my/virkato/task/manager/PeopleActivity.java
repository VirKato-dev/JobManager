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

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.adapter.Lv_peopleAdapter;
import my.virkato.task.manager.adapter.NetWork;

/***
 * Страница со списком пользователей
 */
public class PeopleActivity extends AppCompatActivity {


    private HashMap<String, Object> man = new HashMap<>();

    private ArrayList<HashMap<String, Object>> lm_people = new ArrayList<>();

    private ListView lv_people;

    private final Intent tasks = new Intent();
    private final Intent profile = new Intent();
    private final Intent authentication = new Intent();
    private final NetWork netWork = new NetWork("users");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.people);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
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
            tasks.putExtra("uid", lm_people.get(_position).get("uid").toString());
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
        lm_people = netWork.getPeople().toListMap();
        lv_people.setAdapter(new Lv_peopleAdapter(this, lm_people));
        ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
        netWork.getPeople().setListener((list, man) -> {
            lm_people = list;
            if (man.uid.equals(auth.getCurrentUser().getUid())) {
                sp.edit().putString("account", man.toJson()).commit();
                AppUtil.showMessage(getApplicationContext(), "Ваши данные обновленны");
            }
            ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if ((auth.getCurrentUser() != null)) {
            if ("".equals(sp.getString("account", ""))) {
                man = new HashMap<>();
                man.put("uid", auth.getCurrentUser().getUid());
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

}
