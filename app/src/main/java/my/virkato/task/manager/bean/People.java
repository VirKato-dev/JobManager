package my.virkato.task.manager.bean;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * Список пользователей
 */
public class People {

    private ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
    private ArrayList<Man> people = new ArrayList();
    private OnUpdatedListener listener;
    private boolean after = false;


    /***
     * Следим за изменениями списка
     */
    public interface OnUpdatedListener {
        void onUpdated(ArrayList<HashMap<String, Object>> list);
    }


    /***
     * Новый список пользователей
     * @param list список ListMap
     */
    public People(@NonNull ArrayList<HashMap<String, Object>> list) {
        listMap.addAll(list);
        for (HashMap<String, Object> map : list) {
            people.add(new Man(map));
        }
    }


    /***
     * Получить данные на одного пользователя
     * @param pos номер в списке
     * @return Man
     */
    public Man get(int pos) {
        return people.get(pos);
    }


    public void setListener(OnUpdatedListener listener) {
        this.listener = listener;
    }


    /***
     * удалить пользователя из списка
     * @param man пользователь
     */
    public void remove(Man man) {
        for (Man m : people) {
            if (m.uid.equals(man.uid)) people.remove(m);
        }
        if (!after) note();
    }


    public void remove(HashMap<String, Object> map) {
        remove(new Man(map));
        toListMap();
    }


    /***
     * обновить/добавить пользователя в списке
     * @param man пользователь
     */
    public void update(Man man) {
        after = true; // callback after addidng
        remove(man);
        people.add(man);
        toListMap();
        note();
    }


    public void update(HashMap<String, Object> map) {
        update(new Man(map));
    }


    /***
     * конвертировать список в ListMap
     * @return ListMap
     */
    public ArrayList<HashMap<String, Object>> toListMap() {
        listMap.clear();
        for (Man m : people) {
            listMap.add(m.toMap());
        }
        return listMap;
    }


    private void note() {
        if (listener != null) listener.onUpdated(listMap);
    }
}