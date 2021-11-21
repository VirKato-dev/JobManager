package my.virkato.task.manager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/***
 * Страница авторизации/регистрации
 */
public class AuthActivity extends AppCompatActivity {

    private String verificationId = "";

    private LinearLayout linear1;
    private LinearLayout l_reg;
    private LinearLayout l_phone;
    private LinearLayout l_otp;
    private TextView textview1;
    private EditText e_num;
    private Button b_verify_phone;
    private EditText e_otp;
    private Button b_send_otp;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks auth_phone;
    private PhoneAuthProvider.ForceResendingToken auth_phone_resendToken;
    private FirebaseAuth auth;
    private OnCompleteListener<AuthResult> auth_phoneAuthListener;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_auth);

        initialize(_savedInstanceState);
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {
        linear1 = findViewById(R.id.linear1);
        l_reg = findViewById(R.id.l_reg);
        l_phone = findViewById(R.id.l_phone);
        l_otp = findViewById(R.id.l_otp);
        textview1 = findViewById(R.id.t_description);
        e_num = findViewById(R.id.e_num);
        b_verify_phone = findViewById(R.id.b_verify_phone);
        e_otp = findViewById(R.id.e_otp);
        b_send_otp = findViewById(R.id.b_send_otp);
        auth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("data", Activity.MODE_PRIVATE);

        b_verify_phone.setOnClickListener(_view -> {
            String str = "+" + e_num.getText().toString().replaceAll("[+.*#()/,\\- ]", "");
            e_num.setText(str);
            PhoneAuthOptions paob = PhoneAuthOptions.newBuilder()
                    .setActivity(AuthActivity.this)
                    .setPhoneNumber(e_num.getText().toString())
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setCallbacks(auth_phone)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(paob);
            l_phone.setVisibility(View.GONE);
        });

        auth_phone = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential _credential) {
                FirebaseAuth.getInstance()
                        .signInWithCredential(_credential)
                        .addOnCompleteListener(auth_phoneAuthListener);
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

        b_send_otp.setOnClickListener(_view -> FirebaseAuth.getInstance()
                .signInWithCredential(PhoneAuthProvider.getCredential(verificationId, e_otp.getText().toString()))
                .addOnCompleteListener(auth_phoneAuthListener));


        auth_phoneAuthListener = task -> {
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
        };

    }

    private void initializeLogic() {
        l_otp.setVisibility(View.GONE);
    }

}
