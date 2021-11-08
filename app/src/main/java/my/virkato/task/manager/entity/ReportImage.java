package my.virkato.task.manager.entity;

import androidx.annotation.NonNull;

import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;

/***
 * картинка отчёта
 */
public class ReportImage {

    /***
     * путь к картинке на устройстве пользователя
     */
    public String original = "";

    /***
     * путь к картинке в хранилище Firebase
     */
    public String url = "";

    /***
     * путь к картинке на устройстве админа
     */
    public String received = "";

    public ReportImage() {}

    public ReportImage(LinkedTreeMap<String,String> ltm) {
        original = ltm.get("original");
        url = ltm.get("url");
        received = ltm.get("received");
    }

    public ReportImage fromMap(HashMap<String, String> map) {
        original = map.get("original");
        url = map.get("url");
        received = map.get("received");
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\"original\":\"" + original + "\", \"url\":\"" + url + "\", \"received\":\"" + received + "\"}";
    }

}
