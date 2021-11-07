package my.virkato.task.manager.entity;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

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
     * отправленные картинки пользователем
     */
    public ArrayList<ReportImage> images = new ArrayList<>();

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
        if (map.containsKey("images")) images = (ArrayList<ReportImage>) map.get("images");
        if (map.containsKey("date")) date = (long) Double.parseDouble(map.get("date").toString());
    }


    public Report() {
    }

    /***
     * в виде MAP
     * @return готово для отправки в базу
     */
    public HashMap<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("task_id", task_id);
        map.put("description", description);
        map.put("images", images);
        map.put("date", date);
        return map;
    }

    /***
     * в виде JSON
     * @return готово к отправке в другую Activity
     */
    public String asJson() {
        return String.format(Locale.US,
                "{\"id\":\"%s\", \"task_id\":\"%s\", \"description\":\"%s\", \"images\":%s, \"date\":%d}",
                id, task_id, description, new Gson().toJson(images), date);
    }

    /***
     * отправить информацию в базу данных
     * @param context для правильной работы заставки загрузки
     * @param db место назначения в базе
     */
    public void send(Context context, DatabaseReference db) {
        AppUtil.showSystemWait(context, true);
        this.context = context;
        this.db = db;
        if (images.size() > 0) {
            sendImages();
        } else {
            // без картинок
            save();
        }
    }

    // на время выолнения сохранения
    private Context context;
    private DatabaseReference db;

    private void save() {
        db.child(id).updateChildren(this.asMap(), (error, ref) -> AppUtil.showSystemWait(context, false));
    }

    /***
     * отправить все неотправленные и изменённые картинки отчёта
     */
    public void sendImages() {
        AppUtil.showSystemWait(context, true);
        pc = images.size();
        if (pc > 0) sendNextImage();
    }

    // используется при отправке картинок в хранилище
    private int pc = 0;

    /***
     * отправить следующую неотправленную картинку
     */
    private void sendNextImage() {
        pc--;
        if (images.get(pc).url.equals("")) {
            // отправить файл в хранилище
            if (false) { // после завершения отправки
                images.get(pc).url = ""; // ссылка на файл
                save();
            }
        }
    }

    /***
     * обновить путь скачанной на устройство админа картинки
     * @param db папка для сохранения
     */
    public void updateReceivedImagePath(DatabaseReference db) {
        this.db = db;
        save();
    }
}
