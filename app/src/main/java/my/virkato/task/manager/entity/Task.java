package my.virkato.task.manager.entity;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Locale;

import my.virkato.task.manager.AppUtil;
import my.virkato.task.manager.adapter.NetWork;


public class Task {

    /***
     * идентификатор задания
     */
    public String id = "";

    /***
     * идентификатор мастера исполнителя
     */
    public String master_uid = "";

    /***
     * описание задания
     */
    public String description = "";

    /***
     * размер оплаты за задание
     */
    public double reward = 0d;

    /***
     * состояние оплаты задания
     */
    public boolean rewarded = false;

    /***
     * состояние получения оплаты
     */
    public boolean reward_got = false;

    /***
     * дата начала задания
     */
    public long date_start = 0;

    /***
     * дата завершения задания
     */
    public long date_finish = 0;

    /***
     * состояние прогресса задания
     */
    public boolean finished = false;


    public Task(HashMap<String, Object> map) {
        if (map.containsKey("id")) id = map.get("id").toString();
        if (map.containsKey("master_uid")) master_uid = map.get("master_uid").toString();
        if (map.containsKey("description")) description = map.get("description").toString().replaceAll("\"", "'");
        if (map.containsKey("reward")) reward = Double.parseDouble(map.get("reward").toString());
        if (map.containsKey("rewarded")) rewarded = Boolean.parseBoolean(map.get("rewarded").toString());
        if (map.containsKey("reward_got")) reward_got = Boolean.parseBoolean(map.get("reward_got").toString());
        if (map.containsKey("date_start")) date_start = (long) Double.parseDouble(map.get("date_start").toString());
        if (map.containsKey("date_finish")) date_finish = (long) Double.parseDouble(map.get("date_finish").toString());
        if (map.containsKey("finished")) finished = Boolean.parseBoolean(map.get("finished").toString());
    }


    public Task() {}


    public HashMap<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("master_uid", master_uid);
        map.put("description", description.replaceAll("\"", "'"));
        map.put("reward_got", reward_got);
        map.put("reward", reward);
        map.put("rewarded", rewarded);
        map.put("date_start", date_start);
        map.put("date_finish", date_finish);
        map.put("finished", finished);
        return map;
    }


    @Override
    public String toString() {
        return String.format(Locale.US,
                "{\"id\":\"%s\", \"master_uid\":\"%s\", \"description\":\"%s\","+
                "\"reward\": %.2f, \"rewarded\": %b, \"reward_got\": %b, \"date_start\": %d,"+
                "\"date_finish\": %d, \"finished\": %b}",
                id, master_uid, description.replaceAll("\"", "'"),
                reward, rewarded, reward_got, date_start, date_finish, finished);
    }

    public void send(Context context, DatabaseReference db) {
        AppUtil.showSystemWait(context, true);
        db.child(id).updateChildren(asMap(), (error, ref) -> {
            AppUtil.showSystemWait(context, false);
//            Log.e("ERROR", error != null ? error.toString() : "ok");
        });
    }
}
