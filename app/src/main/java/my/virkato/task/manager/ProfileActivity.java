package my.virkato.task.manager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.bean.Man;
import my.virkato.task.manager.bean.People;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseDatabase _firebase = FirebaseDatabase.getInstance();

    private HashMap<String, Object> man = new HashMap<>();

    private TextView t_phone;
    private EditText e_fio;
    private EditText e_spec;
    private Button b_save;

//    private FirebaseAuth auth;
//    private OnCompleteListener<Void> auth_updateEmailListener;
//    private OnCompleteListener<Void> auth_updatePasswordListener;
//    private OnCompleteListener<Void> auth_emailVerificationSentListener;
//    private OnCompleteListener<Void> auth_deleteUserListener;
//    private OnCompleteListener<Void> auth_updateProfileListener;
//    private OnCompleteListener<AuthResult> auth_phoneAuthListener;
//    private OnCompleteListener<AuthResult> auth_googleSignInListener;
//    private OnCompleteListener<AuthResult> _auth_create_user_listener;
//    private OnCompleteListener<AuthResult> _auth_sign_in_listener;
//    private OnCompleteListener<Void> _auth_reset_password_listener;
    private SharedPreferences sp;
//    private DatabaseReference dbu = _firebase.getReference("users");
    private ChildEventListener _dbu_child_listener;

    private NetWork netWork = new NetWork("users");
    private FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.profile);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {

        t_phone = (TextView) findViewById(R.id.t_phone);
        e_fio = (EditText) findViewById(R.id.e_fio);
        e_spec = (EditText) findViewById(R.id.e_spec);
        b_save = (Button) findViewById(R.id.b_save);
//        auth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                man.put("fio", e_fio.getText().toString());
                man.put("spec", e_spec.getText().toString());
                if (man.get("uid").toString().equals(auth.getCurrentUser().getUid())) {
                    if (!man.containsKey("phone")) {
                        man.put("phone", sp.getString("phone", ""));
                    }
                }
                netWork.getDB().child(man.get("uid").toString()).updateChildren(man);
            }
        });

        netWork.getPeople().setListener((list, man) -> {
            if (man.uid.equals(auth.getCurrentUser().getUid())) {
                e_fio.setText(man.fio);
                e_spec.setText(man.spec);
                t_phone.setText(man.phone);
            }
        });

    }

    private void initializeLogic() {
        if ((auth.getCurrentUser() != null)) {
            if ("".equals(getIntent().getStringExtra("man"))) {
                man.put("uid", auth.getCurrentUser().getUid());
            } else {
                man = new Gson().fromJson(getIntent().getStringExtra("man"), new TypeToken<HashMap<String, Object>>() {
                }.getType());
                e_fio.setText(man.get("fio").toString());
                e_spec.setText(man.get("spec").toString());
                t_phone.setText(man.get("phone").toString());
            }
            _initDesign();
        } else {
            finish();
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
        if ((auth.getCurrentUser() != null)) {
            if (auth.getCurrentUser().getUid().equals(man.get("uid").toString())) {
                e_fio.setEnabled(true);
                e_spec.setEnabled(true);
            }
        }
    }

}
