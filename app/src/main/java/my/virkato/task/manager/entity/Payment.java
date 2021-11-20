package my.virkato.task.manager.entity;

import java.util.HashMap;
import java.util.Locale;

public class Payment {

    /***
     * описание платежа
     */
    public String description = "";

    /***
     * время производства платежа
     */
    public long date = 0L;

    /***
     * размер платежа
     */
    public double cost = 0d;

    /***
     * факт получения платежа
     */
    public boolean received = false;

    public Payment(HashMap<String, Object> map) {
        if (map.containsKey("date")) date = (long) Double.parseDouble(map.get("date").toString());
        if (map.containsKey("cost")) cost = Double.parseDouble(map.get("cost").toString());
        if (map.containsKey("description")) description = map.get("description").toString().replaceAll("\"", "'");
        if (map.containsKey("received")) received = Boolean.parseBoolean(map.get("received").toString());
    }


    public Payment() {}


    public HashMap<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("cost", cost);
        map.put("description", description.replaceAll("\"", "'"));
        map.put("received", received);
        return map;
    }


    @Override
    public String toString() {
        return String.format(Locale.US,
                "{\"date\":\"%d\", \"cost\":\"%.2f\", \"description\":\"%s\","+
                        "\"received\": %b}",
                date, cost, description.replaceAll("\"", "'"), received);
    }

}
