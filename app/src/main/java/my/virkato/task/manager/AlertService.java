package my.virkato.task.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;

import java.net.NetPermission;
import java.util.ArrayList;
import java.util.Calendar;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Task;
import my.virkato.task.manager.entity.Tasks;

public class AlertService extends Service {

//    private static final String TAG_BOOT_EXECUTE_SERVICE = "BOOT_BROADCAST_SERVICE";

    private NetWork dbTasks = new NetWork(NetWork.Info.TASKS);

    private ArrayList<Task> notifiedTask = new ArrayList<>();

    private static int num = 0;


    public AlertService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

//        Log.e(TAG_BOOT_EXECUTE_SERVICE, "AlertService onCreate() method.");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = "AlertService onStartCommand() method.";
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//        Log.e(TAG_BOOT_EXECUTE_SERVICE, "AlertService onStartCommand() method.");

        dbTasks.getTasks().setOnTasksUpdatedListener((tasks, removed, task) -> {

            for (Task t : tasks) {
                if (t.master_uid.equals(NetWork.user().getUid())) {
                    long period = 24 * 60 * 60 * 1000;
                    long date = Calendar.getInstance().getTimeInMillis();
                    if (t.date_finish - date < period) {
                        if (!notifiedTask.contains(t)) {
                            showNotification(t);
                            notifiedTask.add(t);
                        }
                    }
                }
            }
        });
        dbTasks.receiveNewData();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        new BootDeviceReceiver().startServiceByAlarm(this);
    }


    private void showNotification(Task task) {
//        Log.e(TAG_BOOT_EXECUTE_SERVICE, "уведомить");
        String channelId = "finish date";
        Intent intent = new Intent(this, AlertService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Приближение даты окончания работы")
                .setContentText(task.description)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(num, builder.build());
        num++;
    }
}