package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

/***
 * оформить отчёт о ходе работ
 */
public class ReportActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.report);

        initialize(_savedInstanceState);
        initializeLogic();
    }


    private void initialize(Bundle _savedInstanceState) {

    }


    private void initializeLogic() {
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
