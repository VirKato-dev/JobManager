package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

/***
 * Здесь может быть стартовая заставка
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.main);
        FirebaseApp.initializeApp(this);

        Intent intent = new Intent().setClass(getApplicationContext(), TasksActivity.class);
        startActivity(intent);
        finish();
    }

}
