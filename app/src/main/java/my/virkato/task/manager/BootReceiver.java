package my.virkato.task.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("BROADCAST", "onReceive " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "my.virkato.task.manager.START_SERVICE".equals(action)) {

            Toast.makeText(context, "Stroy Sever запущен\n"+action, Toast.LENGTH_SHORT).show();
            startServiceByAlarm(context);
        }
    }


    public void startServiceByAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long startTime = System.currentTimeMillis() + 5000;
        long intervalTime = 60 * 1000; // минимальный интервал для 5.1
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, intervalTime, pendingIntent);
    }
}