package my.virkato.task.manager.adapter;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.internal.LinkedTreeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import my.virkato.task.manager.AppUtil;
import my.virkato.task.manager.FileUtil;
import my.virkato.task.manager.entity.People;
import my.virkato.task.manager.entity.Report;
import my.virkato.task.manager.entity.ReportImage;
import my.virkato.task.manager.entity.Reports;
import my.virkato.task.manager.entity.Tasks;

/***
 * Работа с базой данных и хранилищем
 */
public class NetWork {

    /***
     * для настройки источника данных из базы
     */
    public enum Info {
        USERS("users"), TASKS("tasks"), ADMINS("admins"), REPORTS("reports");

        public String path;

        Info(String path) {
            this.path = path;
        }
    }

    /***
     * когда картинка отправлена в хранилище
     */
    public interface OnSavedImageListener {
        void onSaved(String url);
    }

    /***
     * сервер авторизации
     */
    private static FirebaseAuth auth;

    /***
     * сервер базы данных
     */
    private FirebaseDatabase fb_db;

    /***
     * источник данных из базы
     */
    private DatabaseReference db;

    /***
     * текущая папка источника данных/файлов
     */
    private Info folder;

    /***
     * слушатель данных из источника
     */
    private ChildEventListener db_child_listener;

    /***
     * сервер хранилища файлов
     */
    private FirebaseStorage fb_storage = FirebaseStorage.getInstance();

    /***
     * источник файлов из хранилища
     */
    private StorageReference store;

    /***
     * слушатель отправки файлов в хранилище
     */
    private OnSuccessListener<Uri> store_upload_success_listener;

    /***
     * слушатель процесса отправки файлов в хранилище
     */
    private OnProgressListener store_upload_progress_listener;

    /***
     * слушатель скачивания файла из хранилища
     */
    private OnSuccessListener<FileDownloadTask.TaskSnapshot> store_download_success_listener;

    /***
     * слушатель удаления файла из хранилища
     */
    private OnSuccessListener store_delete_success_listener;

    /***
     * слушатель процесса скачивания файла из хранилища
     */
    private OnProgressListener store_download_progress_listener;

    /***
     * слушатель сбоя в работе с хранилищем
     */
    private OnFailureListener store_failure_listener;

    /***
     * ссылка на список людей (один для всего приложения)
     */
    private static People people;

    /***
     * ссылка на список заданий (один для всего приложения)
     */
    private static Tasks tasks;

    /***
     * ссылка на список отчётов (один для всего приложения)
     */
    private static Reports reports;

    /***
     * настроить адаптер для получения данных из указанного источника
     * @param folder источник данных
     */
    public NetWork(Info folder) {
        this.folder = folder;

        switch (folder) {
            case USERS:
                if (people == null) people = new People();
                receiveFromFolder();
                break;
            case ADMINS:
                if (people == null) people = new People();
                receiveAdmins();
                break;
            case TASKS:
                if (tasks == null) tasks = new Tasks();
                receiveFromFolder();
                break;
            case REPORTS:
                if (reports == null) reports = new Reports();
                receiveReports();
        }

        store_upload_progress_listener = (OnProgressListener<UploadTask.TaskSnapshot>) _param1 -> {
            double _progressValue = (100.0 * _param1.getBytesTransferred()) / _param1.getTotalByteCount();
        };

        store_upload_success_listener = _param1 -> {
            final String _downloadUrl = _param1.toString();
            if (onSavedImageListener != null) onSavedImageListener.onSaved(_downloadUrl);
        };

        store_download_progress_listener = (OnProgressListener<FileDownloadTask.TaskSnapshot>) _param1 -> {
            double _progressValue = (100.0 * _param1.getBytesTransferred()) / _param1.getTotalByteCount();
        };

        store_download_success_listener = _param1 -> {
            final long _totalByteCount = _param1.getTotalByteCount();

        };

        store_delete_success_listener = _param1 -> {
        };

        store_failure_listener = _param1 -> {
            final String _message = _param1.getMessage();
        };
    }

    /***
     * проверить авторизацию
     * @return текущий пользователь
     */
    public static FirebaseUser user() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /***
     * получить ссылку на пользователей
     * @return все пользователи приложения
     */
    public People getPeople() {
        return people;
    }

    /***
     * получить ссылку на задания
     * @return все задания приложения
     */
    public Tasks getTasks() {
        return tasks;
    }

    /***
     * получить ссылку на отчёты
     * @return все отчёты приложения
     */
    public Reports getReports() {
        return reports;
    }

    /***
     * получить ссылку на текущий источник данных
     * @return указатель текущего источника из базы
     */
    public DatabaseReference getDB() {
        return db;
    }

    /***
     * узнать название текущего источника
     * @return папка источника
     */
    public Info getFolder() {
        return folder;
    }

    /***
     * получить ссылку на источник файлов из хранилища
     * @return текущий источник
     */
    public StorageReference getStore() {
        return store;
    }

    /***
     * проверить статус пользователя
     * @return админ ли
     */
    public static boolean isAdmin() {
        if (user() == null) return false;
        return people.getAdmins().contains(user().getUid());
    }

    /***
     * слушатель списка админов из базы
     */
    private final ValueEventListener dba_listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            people.getAdmins().clear();
            GenericTypeIndicator<HashMap<String, Object>> ind = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                AppUtil.getAllKeysFromMap(dataSnapshot.getValue(ind), people.getAdmins());
            }
            if (people.getAdminsListener() != null) {
                people.getAdminsListener().onUpdated();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    /***
     * Получить список UID Админов
     */
    private void receiveAdmins() {
        fb_db = FirebaseDatabase.getInstance();
        db = fb_db.getReference(folder.path);
        db.addListenerForSingleValueEvent(dba_listener);
    }

    /***
     * слушатель списка отчётов из базы
     */
    private final ValueEventListener dbr_listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            reports.getList().clear();
            GenericTypeIndicator<HashMap<String, Object>> ind = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Report r = new Report(dataSnapshot.getValue(ind));
                reports.getList().add(r);
            }
            if (isAdmin()) {
                getImagesFromStorage();
            }
            if (reports.getReportsListener() != null) {
                reports.getReportsListener().onUpdated();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    /***
     * Получить список отчётов
     */
    private void receiveReports() {
        fb_db = FirebaseDatabase.getInstance();
        db = fb_db.getReference(folder.path);
        db.addListenerForSingleValueEvent(dbr_listener);

        fb_storage = FirebaseStorage.getInstance();
        store = fb_storage.getReference(folder.path);
    }

    /***
     * Получать данные из выбранной папки (пользователи, задания, отчёты)
     */
    private void receiveFromFolder() {
        fb_db = FirebaseDatabase.getInstance();
        db = fb_db.getReference(folder.path);
        db_child_listener = db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                _childValue.put("id", _childKey);
                if ((user() != null)) {
                    switch (folder) {
                        case USERS:
                            people.update(_childValue);
                            break;
                        case TASKS:
                            tasks.update(_childValue, true);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                _childValue.put("id", _childKey);
                if ((user() != null)) {
                    switch (folder) {
                        case USERS:
                            people.update(_childValue);
                            break;
                        case TASKS:
                            tasks.update(_childValue, true);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                _childValue.put("id", _childKey);
                if ((user() != null)) {
                    switch (folder) {
                        case USERS:
                            people.remove(_childValue);
                            break;
                        case TASKS:
                            tasks.remove(_childValue, true);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot _param1, String _param2) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError _param1) {
                final int _errorCode = _param1.getCode();
                final String _errorMessage = _param1.getMessage();
                Log.e("ОШИБКА", _errorMessage);
            }
        });
    }

    /***
     * прекратить получение данных из источника
     */
    public void stopListening() {
        if (db_child_listener != null) db.removeEventListener(db_child_listener);
    }

    /***
     * начать получение данных из базы
     */
    public void startListening() {
        if (db_child_listener != null) db.addChildEventListener(db_child_listener);
    }

    /***
     * получить картинки из хранилища на устройство админа
     */
    private void getImagesFromStorage() {
        for (Report rep : reports.getList()) {
            for (ReportImage ri : rep.images) {
                if (ri.received.equals("") || !new File(ri.received).exists()) {
                    // для неполученных картинок
                    if (!ri.url.equals("")) {
                        // которые имеются в хранилище
                        File toFile = new File(localFolder + "/reports/" + rep.id +
                                ri.original.substring(ri.original.lastIndexOf("/") + 1));
                        fb_storage.getReferenceFromUrl(ri.url)
                                .getFile(toFile)
                                .addOnSuccessListener(store_download_success_listener);

                        ri.received = toFile.getAbsolutePath();
                        rep.updateReceivedImagePath(db);
                    }
                }
            }
        }
    }

    private String localFolder = Environment.getDownloadCacheDirectory().getAbsolutePath();


    public void removeImageFromStorage(String url) {
        fb_storage.getReferenceFromUrl(url).delete();
    }

    /***
     * отправить картинку в хранилище и сохранить её URL в базу
     */
    public void saveImageToStorage(String task_id, String rep_id,
                                   ReportImage repImg, NetWork ref,
                                   OnSavedImageListener callBack) {
        onSavedImageListener = callBack;
        String ext = repImg.original.substring(repImg.original.lastIndexOf("."));
        ref.store.child(task_id)
                .child(rep_id + ext)
                .putFile(Uri.fromFile(new File(repImg.original)))
                .continueWithTask(task -> ref.store
                        .child(task_id)
                        .child(rep_id + ext)
                        .getDownloadUrl())
                .addOnSuccessListener(store_upload_success_listener);
    }

    private OnSavedImageListener onSavedImageListener;


}
