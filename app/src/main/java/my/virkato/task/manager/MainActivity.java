package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends  AppCompatActivity  { 

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		com.google.firebase.FirebaseApp.initializeApp(this);
		Intent intent = new Intent().setClass(getApplicationContext(), PeopleActivity.class);
		startActivity(intent);
		finish();
	}

}
