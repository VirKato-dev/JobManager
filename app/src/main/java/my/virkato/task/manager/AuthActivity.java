package my.virkato.task.manager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Random;
import java.util.concurrent.TimeUnit;


public class AuthActivity extends AppCompatActivity {


    private String verificationId = "";

    private LinearLayout linear1;
    private LinearLayout l_reg;
    private LinearLayout l_auth;
    private LinearLayout l_phone;
    private LinearLayout l_otp;
    private TextView textview1;
    private EditText e_num;
    private Button b_verify_phone;
    private TextView textview2;
    private EditText e_otp;
    private Button b_send_otp;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks auth_phone;
    private PhoneAuthProvider.ForceResendingToken auth_phone_resendToken;
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
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.auth);
        initialize(_savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {

        linear1 = (LinearLayout) findViewById(R.id.linear1);
        l_reg = (LinearLayout) findViewById(R.id.l_reg);
        l_auth = (LinearLayout) findViewById(R.id.l_auth);
        l_phone = (LinearLayout) findViewById(R.id.l_phone);
        l_otp = (LinearLayout) findViewById(R.id.l_otp);
        textview1 = (TextView) findViewById(R.id.textview1);
        e_num = (EditText) findViewById(R.id.e_num);
        b_verify_phone = (Button) findViewById(R.id.b_verify_phone);
        textview2 = (TextView) findViewById(R.id.textview2);
        e_otp = (EditText) findViewById(R.id.e_otp);
        b_send_otp = (Button) findViewById(R.id.b_send_otp);
        auth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        b_verify_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                String str = "+" + e_num.getText().toString().replaceAll("[+.*#()/,\\- ]", "");
                e_num.setText(str);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(e_num.getText().toString(), 60, TimeUnit.SECONDS, AuthActivity.this, auth_phone);
                l_phone.setVisibility(View.GONE);
            }
        });

        b_send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                FirebaseAuth.getInstance().signInWithCredential(PhoneAuthProvider.getCredential(verificationId, e_otp.getText().toString())).addOnCompleteListener(auth_phoneAuthListener);
            }
        });

        auth_phone = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential _credential) {
                FirebaseAuth.getInstance().signInWithCredential(_credential).addOnCompleteListener(auth_phoneAuthListener);
                AppUtil.showMessage(getApplicationContext(), "Verified");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                final String _exception = e.getMessage();
                AppUtil.showMessage(getApplicationContext(), _exception);
            }

            @Override
            public void onCodeSent(@NonNull String _verificationId, @NonNull PhoneAuthProvider.ForceResendingToken _token) {
                verificationId = _verificationId;
                auth_phone_resendToken = _token;
                l_otp.setVisibility(View.VISIBLE);
                e_otp.requestFocus();
            }
        };

        auth_updateEmailListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        auth_updatePasswordListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        auth_emailVerificationSentListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        auth_deleteUserListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        auth_phoneAuthListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                final boolean _success = task.isSuccessful();
                final String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
                if (_success) {
                    sp.edit().putString("phone", e_num.getText().toString()).commit();
                    finish();
                } else {
                    l_reg.setVisibility(View.VISIBLE);
                    l_phone.setVisibility(View.VISIBLE);
                    l_otp.setVisibility(View.GONE);
                    AppUtil.showMessage(getApplicationContext(), _errorMessage);
                }
            }
        };

        auth_updateProfileListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        auth_googleSignInListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                final boolean _success = task.isSuccessful();
                final String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
            }
        };

        _auth_create_user_listener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        _auth_sign_in_listener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> _param1) {
                final boolean _success = _param1.isSuccessful();
                final String _errorMessage = _param1.getException() != null ? _param1.getException().getMessage() : "";
            }
        };

        _auth_reset_password_listener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> _param1) {
                final boolean _success = _param1.isSuccessful();
            }
        };
    }

    private void initializeLogic() {
        l_auth.setVisibility(View.GONE);
        l_otp.setVisibility(View.GONE);
    }

    @Deprecated
    public void showMessage(String _s) {
        Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
    }

    @Deprecated
    public int getRandom(int _min, int _max) {
        Random random = new Random();
        return random.nextInt(_max - _min + 1) + _min;
    }

    @Deprecated
    public float getDip(int _input) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
    }

}
