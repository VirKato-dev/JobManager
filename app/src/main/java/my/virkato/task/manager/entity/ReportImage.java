package my.virkato.task.manager.entity;

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

}
