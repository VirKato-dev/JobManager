package my.virkato.task.manager.entity;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import my.virkato.task.manager.AppUtil;
import my.virkato.task.manager.adapter.NetWork;


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
    @Override
    public String toString() {
        return String.format(Locale.US,
                "{\"id\":\"%s\", \"task_id\":\"%s\", \"description\":\"%s\", \"images\":%s, \"date\":%d}",
                id, task_id, description, new Gson().toJson(images), date);
    }

    /***
     * отправить информацию в базу данных
     * @param context для правильной работы заставки загрузки
     * @param ref сслыка на базу и хранилище
     */
    public void send(Context context, NetWork ref) {
        AppUtil.showSystemWait(context, true);
        this.context = context;
        nw = ref;
        db = ref.getDB();
        store = ref.getStore();
        if (images.size() > 0) {
            sendImages();
        } else {
            // без картинок
            save();
        }
    }

    // на время выолнения сохранения
    private Context context;
    private NetWork nw;
    private DatabaseReference db;
    private StorageReference store;

    private void save() {
        db.child(id).updateChildren(this.asMap(), (error, ref) -> AppUtil.showSystemWait(context, false));
    }

    /***
     * отправить все неотправленные и изменённые картинки отчёта
     */
    private void sendImages() {
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
        if (pc >= 0) {
            if (images.get(pc).url.equals("")) {
                // отправить файл в хранилище
                NetWork.OnSavedImageListener callBack = url -> {
                    images.get(pc).url = url; // ссылка на файл
                    save();
                    sendNextImage();
                };
                Log.e("SEND START", images.get(pc).original);
                nw.saveImageToStorage(task_id, id+"_"+pc, images.get(pc), nw, callBack);
            } else {
                Log.e("SEND SKIPED", images.get(pc).original);
                sendNextImage();
            }
        } else {
            Log.e("SEND FINISHED", "");
            AppUtil.showSystemWait(context, false);
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
