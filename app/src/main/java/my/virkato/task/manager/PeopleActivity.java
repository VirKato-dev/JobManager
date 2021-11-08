package my.virkato.task.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import my.virkato.task.manager.adapter.Lv_peopleAdapter;
import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;
import my.virkato.task.manager.entity.People;

/***
 * Страница со списком пользователей
 */
public class PeopleActivity extends AppCompatActivity {


    private HashMap<String, Object> manMap = new HashMap<>();

    private ArrayList<Man> lm_people = new ArrayList<>();

    private ListView lv_people;

    private final Intent tasks = new Intent();
    private final Intent profile = new Intent();
    private final Intent authentication = new Intent();
    private NetWork netWork = new NetWork(NetWork.Info.USERS);
    ;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.people);

        initialize(_savedInstanceState);
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
            tasks.setClass(getApplicationContext(), TasksActivity.class);
            tasks.putExtra("uid", lm_people.get(_position).id);
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
    }


    TimerTask delay;

    @Override
    public void onResume() {
        super.onResume();
        AppUtil.showSystemWait(this, true);
        delay = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // отправляемся на заполнение профиля
                    AppUtil.showSystemWait(lv_people.getContext(), false);
                    profile.setClass(getApplicationContext(), ProfileActivity.class);
                    profile.putExtra("man", new Gson().toJson(manMap));
                    startActivity(profile);
                });
            }
        };
        new Timer().schedule(delay, 5000);

        lv_people.setVisibility(View.GONE);

        if ((NetWork.user() == null)) {
            // требуется авторизация
            authentication.setClass(getApplicationContext(), AuthActivity.class);
            startActivity(authentication);
        } else {
            // при первом входе в аккаунт создаётся бланк профиля
            manMap = new HashMap<>();
            manMap.put("uid", NetWork.user().getUid());
            manMap.put("phone", sp.getString("phone", ""));
            manMap.put("fio", "");
            manMap.put("spec", "");

            // перезапускаем проверку пользователей
            netWork = new NetWork(NetWork.Info.USERS);
            // сначала получаем список Админов
            netWork.getPeople().setAdminsListener(adminsUpdatedListener);
        }

    }

    People.OnAdminsUpdatedListener adminsUpdatedListener = new People.OnAdminsUpdatedListener() {
        @Override
        public void onUpdated() {
            lm_people = netWork.getPeople().getList();
            lv_people.setAdapter(new Lv_peopleAdapter(lv_people.getContext(), lm_people));
            ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
            netWork.getPeople().setOnPeopleUpdatedListener((list, man) -> {
                lm_people = list;
                if (man.id.equals(NetWork.user().getUid())) {
                    sp.edit().putString("account", man.toString()).commit();
                    AppUtil.showMessage(getApplicationContext(), "Ваши данные получены");

                    delay.cancel();
                    if (!netWork.isAdmin()) {
                        // обычные пользователи идут на экран своих заданий
                        tasks.setClass(getApplicationContext(), TasksActivity.class);
                        tasks.putExtra("uid", NetWork.user().getUid());
                        startActivity(tasks);
                        finish();
                    } else {
                        // только Админ может остаться на экране списка пользователей
                        lv_people.setVisibility(View.VISIBLE);
                        AppUtil.showMessage(lv_people.getContext(), "Вы Админ");
                    }
                }
                ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
            });
        }
    };

}
