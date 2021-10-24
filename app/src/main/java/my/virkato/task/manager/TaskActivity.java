package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.bean.People;

/***
 * Информация о задании
 */
public class TaskActivity extends AppCompatActivity {


    private HashMap<String, Object> task = new HashMap<>();
//    private NetWork netWork = new NetWork("users");
    private People people = new People();

    private LinearLayout linear1;
    private TextView t_fio;
    private TextView t_task_id;
    private TextView t_task_text;
    private Button b_approve;
    private ListView lv_reports;
    private Button b_add_report;

    private FirebaseAuth auth;
    private OnCompleteListener<Void> auth_updateEmailListener;
    private OnCompleteListener<Void> auth_updatePasswordListener;
    private OnCompleteListener<Void> auth_emailVerificationSentListener;
    private OnCompleteListener<Void> auth_deleteUserListener;
    private OnCompleteListener<Void> auth_updateProfileListener;
    private OnCompleteListener<AuthResult> auth_phoneAuthListener;
    private OnCompleteListener<AuthResult> auth_googleSignInListener;
    private OnCompleteListener<AuthResult> _auth_create_user_listener;
    private OnCompleteListener<AuthResult> _auth_sign_in_listener;
    private OnCompleteListener<Void> _auth_reset_password_listener;


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.task);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }


    private void initialize(Bundle _savedInstanceState) {

        linear1 = findViewById(R.id.linear1);
        t_fio = findViewById(R.id.textview1);
        t_task_id = findViewById(R.id.textview2);
        t_task_text = findViewById(R.id.textview3);
        b_approve = findViewById(R.id.b_approve);
        lv_reports = findViewById(R.id.lv_reports);
        b_add_report = findViewById(R.id.b_add_report);
        auth = FirebaseAuth.getInstance();

        b_approve.setOnClickListener(_view -> {
        });

        b_add_report.setOnClickListener(_view -> {
        });

        auth_updateEmailListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_updatePasswordListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_emailVerificationSentListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_deleteUserListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_phoneAuthListener = task -> {
            final boolean _success = task.isSuccessful();
            final String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
        };

        auth_updateProfileListener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        auth_googleSignInListener = task -> {
            final boolean _success = task.isSuccessful();
            final String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
        };

        _auth_create_user_listener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        _auth_sign_in_listener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
            final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
        };

        _auth_reset_password_listener = _param1 -> {
            final boolean _success = _param1.isSuccessful();
        };
    }


    private void initializeLogic() {
        if ("".equals(getIntent().getStringExtra("task"))) {

        } else {
            task = new Gson().fromJson(getIntent().getStringExtra("task"), new TypeToken<HashMap<String, Object>>() {
            }.getType());
            t_fio.setText(people.findManById(task.get("uid").toString()).fio);
            t_task_id.setText(task.get("id").toString());
            t_task_text.setText(task.get("text").toString());
        }
        b_approve.setVisibility(View.GONE);
        if ((auth.getCurrentUser() != null)) {
            b_approve.setVisibility(View.VISIBLE);
            b_add_report.setVisibility(View.GONE);
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

}
