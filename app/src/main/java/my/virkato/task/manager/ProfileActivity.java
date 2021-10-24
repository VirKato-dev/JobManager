package my.virkato.task.manager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import my.virkato.task.manager.adapter.NetWork;

/***
 * Страница профиля пользователя
 */
public class ProfileActivity extends AppCompatActivity {


    private HashMap<String, Object> manMap = new HashMap<>();

    private TextView t_phone;
    private EditText e_fio;
    private EditText e_spec;
    private Button b_save;

    private SharedPreferences sp;

    private final NetWork netWork = new NetWork("users");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.profile);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }


    private void initialize(Bundle _savedInstanceState) {

        t_phone = findViewById(R.id.t_phone);
        e_fio = findViewById(R.id.e_fio);
        e_spec = findViewById(R.id.e_spec);
        b_save = findViewById(R.id.b_save);
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        b_save.setOnClickListener(_view -> {
            manMap.put("fio", e_fio.getText().toString());
            manMap.put("spec", e_spec.getText().toString());
            if (manMap.get("uid").toString().equals(auth.getCurrentUser().getUid())) {
                if (!manMap.containsKey("phone")) {
                    manMap.put("phone", sp.getString("phone", ""));
                }
            }
            netWork.getDB().child(manMap.get("uid").toString()).updateChildren(manMap);
        });

        netWork.getPeople().setListener((list, man) -> {
            if (manMap != null) {
                if (man.uid.equals(manMap.get("uid").toString())) {
                    e_fio.setText(man.fio);
                    e_spec.setText(man.spec);
                    t_phone.setText(man.phone);
                }
            }
        });

    }


    private void initializeLogic() {
        if ((auth.getCurrentUser() == null)) {
            finish();
        } else {
            if ("".equals(getIntent().getStringExtra("man"))) {
                manMap.put("uid", auth.getCurrentUser().getUid());
            } else {
                manMap = new Gson().fromJson(getIntent().getStringExtra("man"), new TypeToken<HashMap<String, Object>>() {
                }.getType());
                e_fio.setText(manMap.get("fio").toString());
                e_spec.setText(manMap.get("spec").toString());
                t_phone.setText(manMap.get("phone").toString());
            }
            _initDesign();
        }
    }


    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
        super.onActivityResult(_requestCode, _resultCode, _data);
        switch (_requestCode) {
            default:
                break;
        }
    }


    public void _initDesign() {
        e_fio.setEnabled(false);
        e_spec.setEnabled(false);
        b_save.setVisibility(View.GONE);
        if ((auth.getCurrentUser() != null)) {
            if (auth.getCurrentUser().getUid().equals(manMap.get("uid").toString())) {
                e_fio.setEnabled(true);
                e_spec.setEnabled(true);
                b_save.setVisibility(View.VISIBLE);
            }
        }
    }

}
