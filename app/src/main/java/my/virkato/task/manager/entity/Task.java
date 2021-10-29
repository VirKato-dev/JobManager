package my.virkato.task.manager.entity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Locale;

import my.virkato.task.manager.AppUtil;


public class Task {
    public String id = "";
    public String master_uid = "";
    public String description = "";
    public long date_start = 0;
    public long date_finish = 0;
    public boolean finished = false;


    public Task(HashMap<String, Object> map) {
        if (map.containsKey("id")) id = map.get("id").toString();
        if (map.containsKey("master_uid")) master_uid = map.get("master_uid").toString();
        if (map.containsKey("description")) description = map.get("description").toString();
        if (map.containsKey("date_start")) date_start = (long) map.get("date_start");
        if (map.containsKey("date_finish")) date_finish = (long) map.get("date_finish");
        if (map.containsKey("finished")) finished = map.get("finished").toString().equals("1")?true:false;
    }


    public Task() {}


    public HashMap<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("master_uid", master_uid);
        map.put("description", description);
        map.put("date_start", date_start);
        map.put("date_finish", date_finish);
        map.put("finished", finished?"1":"0");
        return map;
    }


    public String asJson() {
        return String.format(Locale.US,
                "{\"id\":\"%s\", \"master_uid\":\"%s\", \"description\":\"%s\", \"date_start\":%d, \"date_finish\":%d, \"finished\":%s}",
                id, master_uid, description, date_start, date_finish, finished?"1":"0");
    }

    public void send(Context context, DatabaseReference db) {
        AppUtil.showSystemWait(context, true);
        db.child(id).updateChildren(this.asMap(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                AppUtil.showSystemWait(context, false);
            }
        });
    }
}
