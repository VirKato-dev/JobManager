package my.virkato.task.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class BootDeviceReceiver extends BroadcastReceiver {

    private static final String TAG_BOOT_BROADCAST_RECEIVER = "BOOT_BROADCAST_RECEIVER";
    private static boolean already = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String message = "BootDeviceReceiver onReceive, action is " + action;
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//        Log.e(TAG_BOOT_BROADCAST_RECEIVER, action);
        if (!already) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                already = true;
                startServiceDirectly(context);
//                startServiceByAlarm(context);
            }
        }
    }

    /* Start AlertService service directly and invoke the service every 10 seconds. */
    private void startServiceDirectly(Context context) {
        String message = "BootDeviceReceiver onReceive start service directly.";
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//        Log.e(TAG_BOOT_BROADCAST_RECEIVER, message);

        Intent startServiceIntent = new Intent(context, AlertService.class);
        context.startService(startServiceIntent);
    }

    /* Create an repeat Alarm that will invoke the background service for each execution time.
     * The interval time can be specified by your self.  */
    public void startServiceByAlarm(Context context) {
        // Get alarm manager.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Create intent to invoke the background service.
        Intent intent = new Intent(context, AlertService.class);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, flag);
        long startTime = System.currentTimeMillis();
        long intervalTime = 60 * 1000; // минимальный интервал для 5.1
        String message = "Start service use repeat alarm. ";
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//        Log.e(TAG_BOOT_BROADCAST_RECEIVER, message);
        // Create repeat alarm.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, intervalTime, pendingIntent);
    }
}