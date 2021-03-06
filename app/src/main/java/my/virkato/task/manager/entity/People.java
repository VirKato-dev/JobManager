package my.virkato.task.manager.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/***
 * Список пользователей
 */
public class People {

    private static ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
    private static ArrayList<Man> people = new ArrayList<>();
    private static ArrayList<String> admins = new ArrayList<>();

    private OnPeopleUpdatedListener peopleListener;
    private OnAdminsUpdatedListener adminsListener;

    private boolean after = false;


    /***
     * Следим за изменениями списка всех пользователей
     */
    public interface OnPeopleUpdatedListener {
        void onUpdated(ArrayList<Man> list, Man man);
    }

    /***
     * Следим за изменением списка админов
     */
    public interface OnAdminsUpdatedListener {
        void onUpdated();
    }


    /***
     * Новый список пользователей
     * @param list список ListMap
     */
    public People(@NonNull ArrayList<HashMap<String, Object>> list) {
        this();
        listMap.clear();
        listMap.addAll(list);
        people.clear();
        for (HashMap<String, Object> map : list) {
            people.add(new Man(map));
        }
    }

    public People() {
    }


    public void setOnPeopleUpdatedListener(OnPeopleUpdatedListener listener) {
        peopleListener = listener;
        if (people.size() > 0) note(people.get(0));
    }

    public void setAdminsListener(OnAdminsUpdatedListener listener) {
        adminsListener = listener;
        if (admins.size() > 0) listener.onUpdated();
    }

    public OnAdminsUpdatedListener getAdminsListener() {
        return adminsListener;
    }


    /***
     * удалить пользователя из списка
     * @param man пользователь
     */
    public void remove(Man man) {
        Iterator<Man> im = people.iterator();
        while (im.hasNext()) {
            if (im.next().id.equals(man.id)) {
                im.remove();
            }
        }
        if (!after) note(man);
    }


    public void remove(HashMap<String, Object> map) {
        remove(new Man(map));
        asListMap();
    }


    /***
     * обновить/добавить пользователя в списке
     * @param man пользователь
     */
    public void update(Man man) {
        after = true; // callback after addidng
        remove(man);
        people.add(man);
        asListMap();
        note(man);
    }


    public void update(HashMap<String, Object> map) {
        update(new Man(map));
    }


    /***
     * конвертировать список в ListMap
     * @return ListMap
     */
    public ArrayList<HashMap<String, Object>> asListMap() {
        listMap.clear();
        for (Man m : people) {
            listMap.add(m.asMap());
        }
        return listMap;
    }


    private void note(Man man) {
        if (peopleListener != null) peopleListener.onUpdated(people, man);
    }


    public Man findManById(String id) {
        for (Man man : people) {
            if (man.id.equals(id)) {
                return man;
            }
        }
        return null;
    }


    public ArrayList<Man> getList() {
        return people;
    }

    public ArrayList<String> getAdmins() {
        return admins;
    }

}