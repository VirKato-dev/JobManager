package my.virkato.task.manager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import my.virkato.task.manager.adapter.Lv_peopleAdapter;
import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Man;

/***
 * Страница со списком пользователей
 */
public class PeopleActivity extends AppCompatActivity {


    private Man you = new Man();

    private ListView lv_people;

    private final Intent tasks = new Intent();
    private final Intent profile = new Intent();
    private final Intent authentication = new Intent();
    private final NetWork dbAdmins = new NetWork(NetWork.Info.ADMINS);
    private final NetWork dbUsers = new NetWork(NetWork.Info.USERS);
    private final ArrayList<Man> lm_people = dbUsers.getPeople().getList();
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.people);

        initialize(_savedInstanceState);
    }


    private void initialize(Bundle _savedInstanceState) {
        lv_people = findViewById(R.id.lv_people);
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        lv_people.setAdapter(new Lv_peopleAdapter(lm_people));
        ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();


        lv_people.setOnItemClickListener((parent, view, position, id) -> {
            tasks.setClass(getApplicationContext(), TasksActivity.class);
            tasks.putExtra("uid", ((Lv_peopleAdapter) parent.getAdapter()).getItem(position).id);
            startActivity(tasks);
        });

        lv_people.setOnItemLongClickListener((parent, view, position, id) -> {
            profile.setClass(getApplicationContext(), ProfileActivity.class);
            profile.putExtra("man", ((Lv_peopleAdapter) parent.getAdapter()).getItem(position).toString());
            startActivity(profile);
            return true;
        });
    }


    TimerTask delay;

    @Override
    public void onResume() {
        super.onResume();
        AppUtil.showSystemWait(this, true);
        // если в течение 10сек не получены данные текущего аккаунта
        // то отправить на заполнение профиля
        delay = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // отправляемся на заполнение профиля
                    AppUtil.showSystemWait(lv_people.getContext(), false);
                    if (you.fio.equals("")) {
                        profile.setClass(getApplicationContext(), ProfileActivity.class);
                        profile.putExtra("man", you.toString());
                        startActivity(profile);
                    }
                });
            }
        };
        new Timer().schedule(delay, 10000);

        lv_people.setVisibility(View.GONE);

        if ((NetWork.user() == null)) {
            // требуется авторизация
            authentication.setClass(this, AuthActivity.class);
            startActivity(authentication);
        } else {
            if (you.id.equals("")) {
                you.id = NetWork.user().getUid();
                you.phone = sp.getString("phone", "");
            }
            // перезапускаем проверку пользователей
            dbUsers.getPeople().setOnPeopleUpdatedListener((list, man) -> {
                Man tmp = dbUsers.getPeople().findManById(you.id);
                if (tmp != null) {
                    you = tmp;
                    sp.edit().putString("account", you.toString()).commit();
                    if (false) {
                        AppUtil.showMessage(getApplicationContext(), "Ваши данные получены");
                    }
                    delay.cancel(); // найдены данные текущего пользователя

                    if (!NetWork.isAdmin()) {
                        // обычные пользователи идут на экран своих заданий
                        tasks.setClass(getApplicationContext(), TasksActivity.class);
                        tasks.putExtra("uid", you.id);
                        startActivity(tasks);
                        finish();
                    } else {
                        // только Админ может остаться на экране списка пользователей
                        lv_people.setVisibility(View.VISIBLE);
                        if (false) {
                            AppUtil.showMessage(lv_people.getContext(), "Вы Админ");
                        }

                    }
//                    saving = false;
                }
                ((BaseAdapter) lv_people.getAdapter()).notifyDataSetChanged();
                AppUtil.showSystemWait(lv_people.getContext(), false);
            });

            dbAdmins.getPeople().setAdminsListener(dbUsers::receiveNewData);
            dbAdmins.receiveNewData();
        }
    }

}
