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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.Calendar;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.entity.Task;

public class AlertService extends Service {

    private static NetWork dbTasks;

    private static final ArrayList<Task> notifiedTask = new ArrayList<>();

    private static int num = 0; // для каждого мастера только 1 очередь


    public AlertService() {
        Log.e("SERVICE", "constructor");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        Log.e("SERVICE", "onCreate");
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SERVICE", "onStartCommand");
        Log.e("TASK LIST SIZE", "" + notifiedTask.size());

        FirebaseApp.initializeApp(this);
        if (dbTasks == null) dbTasks = new NetWork(NetWork.Info.TASKS);

        dbTasks.getTasks().setOnTasksUpdatedListener((tasks, removed, task) -> {
            for (Task t : tasks) {
                if (t.master_uid.equals(NetWork.user().getUid())) {
                    long period = 2 * 24 * 60 * 60 * 1000;
                    long date = Calendar.getInstance().getTimeInMillis();
                    if (t.date_finish - date < period) {
                        if (!notifiedTask.contains(t)) {
                            notifiedTask.add(t);
                            showNotification(t);
                        }
                    }
                }
            }
        });
        if (dbTasks.getTasks().getList().size() == 0) {
            dbTasks.receiveNewData();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.e("SERVICE", "onDestroy");
        super.onDestroy();
    }


    private void showNotification(Task task) {
        Log.e("showNotification", "new");
        Log.e("TASK LIST SIZE", "" + notifiedTask.size());

        String channelId = "finish date";
        String title = "Скоро сдача работы";
        Intent intent = new Intent(this, AlertService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(task.description)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_ALARM)
                .setTicker(title)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        notificationManager.notify(num, builder.build());
//        num++;
    }
}