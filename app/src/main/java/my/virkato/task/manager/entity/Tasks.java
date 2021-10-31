package my.virkato.task.manager.entity;

import java.util.ArrayList;
import java.util.HashMap;


public class Tasks {

    private static final ArrayList<Task> tasks = new ArrayList<>();
    private OnTasksUpdatedListener onTasksUpdatedListener;

    /***
     * Извещатель об изменении списка заданий
     */
    public interface OnTasksUpdatedListener {
        void onUpdated(ArrayList<Task> tasks, boolean removed, Task task);
    }

    /***
     * Подключить слушатель изменений списка
     * @param listener слушатель
     */
    public void setOnTasksUpdatedListener(OnTasksUpdatedListener listener) {
        onTasksUpdatedListener = listener;
        if (tasks.size()>0) listener.onUpdated(tasks, false, tasks.get(0));
    }

    public Tasks() {
        // для получения имеющегося списка
    }

    /***
     * Создать список заданий из ListMap
     * @param arrayList список полученный от Firebase
     */
    public Tasks(ArrayList<HashMap<String, Object>> arrayList) {
        tasks.clear();
        for (HashMap<String, Object> map : arrayList) tasks.add(new Task(map));
    }

    /***
     * Обновить/добавить задание в список (удобно для Firebase)
     * @param map добавляемое задание
     * @param finalize известить об изменении списка
     */
    public void update(HashMap<String, Object> map, boolean finalize) {
        update(new Task(map), finalize);
    }

    /***
     * Обновить/добавить задание в список (удобно для обработки)
     * @param task добавляемое задание
     * @param finalize известить об изменении списка
     */
    public void update(Task task, boolean finalize) {
        remove(task, false);
        tasks.add(task);
        if (finalize && onTasksUpdatedListener != null) {
            onTasksUpdatedListener.onUpdated(tasks, false, task);
        }
    }

    /***
     * Удалить задание из списка (удобно для событий Firebase)
     * @param map удаляемое задание
     * @param finalize известить об изменении списка
     */
    public void remove(HashMap<String, Object> map, boolean finalize) {
        remove(new Task(map), finalize);
    }

    /***
     * Удалить задание из списка (удобно для обработки)
     * @param task удаляемое задание
     * @param finalize известить об изменении списка
     */
    public void remove(Task task, boolean finalize) {
        Task found = findTaskById(task.id);
        if (found != null) {
            tasks.remove(found);
            if (finalize && onTasksUpdatedListener != null)
                onTasksUpdatedListener.onUpdated(tasks, true, found);
        }
    }

    /***
     * Получить список заданий.
     * @return список удобный для обработки
     */
    public ArrayList<Task> getList() {
        return tasks;
    }

    /***
     * Получить список заданий.
     * @return список удобный для ListView
     */
    public ArrayList<HashMap<String, Object>> getListMap() {
        ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
        for (Task t : tasks) listMap.add(t.asMap());
        return listMap;
    }

    /***
     * Получить задание по его ID
     * @param id искомое задание
     * @return задание удобное для обработки
     */
    public Task findTaskById(String id) {
        if (id != null) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                if (id.equals(tasks.get(i).id)) {
                    return tasks.get(i);
                }
            }
        }
        return null;
    }

}
