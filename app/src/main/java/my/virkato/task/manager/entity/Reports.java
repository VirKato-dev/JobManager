package my.virkato.task.manager.entity;

import java.util.ArrayList;
import java.util.HashMap;


public class Reports {

    /***
     * все отчёты
     */
    private static ArrayList<Report> reports = new ArrayList<>();

    /***
     * слушатель изменений списка отчётов
     */
    private OnReportsUpdatedListener onReportsUpdatedListener;

    /***
     * Извещатель об изменении списка отчётов
     */
    public interface OnReportsUpdatedListener {
        void onUpdated();
    }

    /***
     * Подключить слушатель изменений списка
     * @param listener слушатель
     */
    public void setOnReportsUpdatedListener(OnReportsUpdatedListener listener) {
        onReportsUpdatedListener = listener;
    }

    /***
     *
     * @return
     */
    public OnReportsUpdatedListener getReportsListener() {
        return onReportsUpdatedListener;
    }


    public Reports() {
        // для получения имеющегося списка
    }

    /***
     * Создать список отчётов из ListMap
     * @param arrayList список полученный от Firebase
     */
    public Reports(ArrayList<HashMap<String, Object>> arrayList) {
        reports.clear();
        for (HashMap<String, Object> map : arrayList) reports.add(new Report(map));
    }

    /***
     * Обновить/добавить отчёт в список (удобно для Firebase)
     * @param map добавляемый отчёт
     * @param finalize известить об изменении списка
     */
    public void update(HashMap<String, Object> map, boolean finalize) {
        update(new Report(map), finalize);
    }

    /***
     * Обновить/добавить отчёт в список (удобно для обработки)
     * @param report добавляемый отчёт
     * @param finalize известить об изменении списка
     */
    public void update(Report report, boolean finalize) {
        remove(report, false);
        reports.add(report);
        if (finalize && onReportsUpdatedListener != null)
            onReportsUpdatedListener.onUpdated();
    }

    /***
     * Удалить отчёт из списка (удобно для событий Firebase)
     * @param map удаляемый отчёт
     * @param finalize известить об изменении списка
     */
    public void remove(HashMap<String, Object> map, boolean finalize) {
        remove(new Report(map), finalize);
    }

    /***
     * Удалить отчёт из списка (удобно для обработки)
     * @param report удаляемый отчёт
     * @param finalize известить об изменении списка
     */
    public void remove(Report report, boolean finalize) {
        Report found = findReportById(report.id);
        if (found != null) {
            reports.remove(found);
            if (finalize && onReportsUpdatedListener != null)
                onReportsUpdatedListener.onUpdated();
        }
    }

    /***
     * Получить список отчётов.
     * @return список удобный для обработки
     */
    public ArrayList<Report> getList() {
        return reports;
    }

    /***
     * Получить список отчётов.
     * @return список удобный для ListView
     */
    public ArrayList<HashMap<String, Object>> getListMap() {
        ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
        for (Report r : reports) listMap.add(r.asMap());
        return listMap;
    }

    /***
     * Получить отчёт по его ID
     * @param id искомый отчёт
     * @return отчёт удобный для обработки
     */
    public Report findReportById(String id) {
        if (id != null) {
            for (int i = reports.size() - 1; i >= 0; i--) {
                if (id.equals(reports.get(i).id)) {
                    return reports.get(i);
                }
            }
        }
        return null;
    }

}
