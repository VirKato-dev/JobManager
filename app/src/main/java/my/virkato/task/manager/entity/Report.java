package my.virkato.task.manager.entity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import my.virkato.task.manager.AppUtil;


public class Report {

    /***
     * идентификатор этого отчёта в базе
     */
    public String id = "";

    /***
     * идентификатор задания, которому принадлежит отчёт
     */
    public String task_id = "";

    /***
     * описание отчёта
     */
    public String description = "";

    /***
     * пояснительные изображения
     */
    public ArrayList<String> images = new ArrayList<>();

    /***
     * дата отчёта
     */
    public long date = 0L;

    /***
     * новый отчёт создан из MAP
     * @param map первоначальные настройки
     */
    public Report(HashMap<String, Object> map) {
        if (map.containsKey("id")) id = map.get("id").toString();
        if (map.containsKey("task_id")) task_id = map.get("task_id").toString();
        if (map.containsKey("description")) description = map.get("description").toString();
        if (map.containsKey("images")) images = new Gson().fromJson(map.get("images").toString(), new TypeToken<ArrayList<String>>(){}.getType());
        if (map.containsKey("date")) date = (long) Double.parseDouble(map.get("date").toString());
    }


    public Report() {}

    /***
     * в виде MAP
     * @return готово для отправки в базу
     */
    public HashMap<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("task_id", task_id);
        map.put("description", description);
        map.put("images", new Gson().toJson(images));
        map.put("date", date);
        return map;
    }

    /***
     * в виде JSON
     * @return готово к отправке в другую Activity
     */
    public String asJson() {
        return String.format(Locale.US,
                "{\"id\":\"%s\", \"task_id\":\"%s\", \"description\":\"%s\", \"images\":\"%s\", \"date\":%d}",
                id, task_id, description, new Gson().toJson(images), date);
    }

    /***
     * отправить информацию в базу данных
     * @param context для правильной работы заставки загрузки
     * @param db место назначения в базе
     */
    public void send(Context context, DatabaseReference db) {
        AppUtil.showSystemWait(context, true);
        db.child(id).updateChildren(this.asMap(), (error, ref) -> AppUtil.showSystemWait(context, false));
    }
}
