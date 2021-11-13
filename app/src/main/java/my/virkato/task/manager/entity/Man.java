package my.virkato.task.manager.entity;

import java.util.HashMap;


public class Man {

    public String id = "";
    public String fio = "";
    public String spec = "";
    public String phone = "";
    public String avatar = "";


    public Man() {}


    public Man(HashMap<String, Object> map) {
        if (map.containsKey("fio")) fio = map.get("fio").toString();
        if (map.containsKey("spec")) spec = map.get("spec").toString();
        if (map.containsKey("phone")) phone = map.get("phone").toString();
        if (map.containsKey("uid")) id = map.get("uid").toString();
        if (map.containsKey("avatar")) avatar = map.get("avatar").toString();
    }


    public HashMap<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fio", fio);
        map.put("spec", spec);
        map.put("phone", phone);
        map.put("uid", id);
        map.put("avatar", avatar);
        return map;
    }


    @Override
    public String toString() {
        return String.format(
                "{\"uid\":\"%s\", \"fio\":\"%s\", \"spec\":\"%s\", \"phone\":\"%s\", \"avatar\":\"%s\"}",
                id, fio, spec, phone, avatar);
    }
}