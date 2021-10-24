package my.virkato.task.manager.bean;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * Список пользователей
 */
public class People {

    private static ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
    private static ArrayList<Man> people = new ArrayList<>();
    private static ArrayList<Man> admins = new ArrayList<>();

    private OnPeopleUpdatedListener peopleListener;
    private OnAdminsUpdatedListener adminsListener;

    private boolean after = false;


    /***
     * Следим за изменениями списка всех пользователей
     */
    public interface OnPeopleUpdatedListener {
        void onPeopleUpdated(ArrayList<HashMap<String, Object>> list, Man man);
    }

    /***
     * Следим за изменением списка админов
     */
    public interface OnAdminsUpdatedListener {
        void onAdminsUpdated();
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

    public People() {
        // для получения имеющегося списка
    }


    public void setPeopleListener(OnPeopleUpdatedListener listener) {
        peopleListener = listener;
    }

    public void setAdminsListener(OnAdminsUpdatedListener listener) {
        adminsListener = listener;
    }

    public OnAdminsUpdatedListener getAdminsListener() {
        return adminsListener;
    }


    /***
     * удалить пользователя из списка
     * @param man пользователь
     */
    public void remove(Man man) {
        for (int i = people.size()-1; i >= 0; i--) {
            if (people.get(i).uid.equals(man.uid)) {
                people.remove(i);
            }
        }
        if (!after) note(man);
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
        note(man);
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


    private void note(Man man) {
        if (peopleListener != null) peopleListener.onPeopleUpdated(listMap, man);
    }


    public Man findManById(String uid) {
        for (Man man : people) {
            if (man.uid.equals(uid)) {
                return man;
            }
        }
        return null;
    }


    public ArrayList<Man> getList() {
        return people;
    }

    public ArrayList<Man> getAdmins() {
        return admins;
    }

}