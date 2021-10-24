package my.virkato.task.manager.bean;

import java.util.HashMap;


public class Man {

    public String uid = "";
    public String fio = "";
    public String spec = "";
    public String phone = "";
    public String avatar = "";


    public Man(HashMap<String, Object> map) {
        if (map.containsKey("fio")) fio = map.get("fio").toString();
        if (map.containsKey("spec")) spec = map.get("spec").toString();
        if (map.containsKey("phone")) phone = map.get("phone").toString();
        if (map.containsKey("uid")) uid = map.get("uid").toString();
        if (map.containsKey("avatar")) avatar = map.get("avatar").toString();
    }


    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fio", fio);
        map.put("spec", spec);
        map.put("phone", phone);
        map.put("uid", uid);
        map.put("avatar", avatar);
        return map;
    }


    public String toJson() {
        String json;
        json = String.format("{\"uid\":\"%s\", \"fio\":\"%s\", \"spec\":\"%s\", \"phone\":\"%s\", \"avatar\":\"%s\",",
                uid, fio, spec, phone, avatar);
        return json;
    }
}