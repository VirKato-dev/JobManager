package my.virkato.task.manager.entity;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
     * идентификатор мастера отправившего текущий отчёт
     */
    public String master = "";

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
        if (map.containsKey("master")) master = map.get("master").toString();
        if (map.containsKey("description")) description = map.get("description").toString();
        if (map.containsKey("date")) date = (long) Double.parseDouble(map.get("date").toString());
        if (map.containsKey("images")) {
            ArrayList<LinkedTreeMap<String,String>> ltm = (ArrayList<LinkedTreeMap<String, String>>) map.get("images");
            Type type = new TypeToken<ArrayList<ReportImage>>(){}.getType();
            images = new Gson().fromJson(new Gson().toJson(ltm), type);
        }
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
        map.put("master", master);
        map.put("description", description);
        map.put("images", images);
        map.put("date", date);
        return map;
    }

    /***
     * в виде JSON
     * @return готово к отправке в другую Activity
     */
    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US,
                "{\"id\":\"%s\", \"task_id\":\"%s\", \"master\":\"%s\", \"description\":\"%s\", \"images\":%s, \"date\":%d}",
                id, task_id, master, description, new Gson().toJson(images), date);
    }

    public void delete(NetWork ref) {
        ref.getDB().child(id).removeValue();
    }

    /***
     * отправить информацию в базу данных
     * @param context для правильной работы заставки загрузки
     * @param ref сслыка на базу и хранилище
     */
    public void send(Context context, NetWork ref) {
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
        if (pc > 0) {
            AppUtil.showSystemWait(context, true);
            sendNextImage();
        }
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
                    sendNextImage();
                };
                Log.e("SEND START", images.get(pc).original);
                nw.saveImageToStorage(task_id, id+"_"+pc, images.get(pc), nw, callBack);
            } else {
                Log.e("SEND SKIPED", images.get(pc).original);
                sendNextImage();
            }
        } else {
            Log.e("SEND FINISHED", ".");
            AppUtil.showSystemWait(context, false);
            save();
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
