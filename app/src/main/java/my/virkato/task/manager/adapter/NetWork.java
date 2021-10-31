package my.virkato.task.manager.adapter;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import my.virkato.task.manager.AppUtil;
import my.virkato.task.manager.entity.People;
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
     * слушатель данных из источника
     */
    private static ChildEventListener db_child_listener;

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
    private OnCompleteListener<Uri> store_upload_success_listener;

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
     * текущая папка источника данных/файлов
     */
    private Info folder;

    /***
     * настроить адаптер для получения данных из указанного источника
     * @param folder источник данных
     */
    public NetWork(Info folder) {
        this.folder = folder;
        auth = FirebaseAuth.getInstance();
        restartListening(folder);

        switch (folder) {
            case USERS:
                if (people == null) people = new People();
                receiveAdmins();
                store = fb_storage.getReference(folder.path);
                receiveFromFolder();
                break;
            case TASKS:
                if (tasks == null) tasks = new Tasks();
                receiveFromFolder();
        }

        store_upload_progress_listener = (OnProgressListener<UploadTask.TaskSnapshot>) _param1 -> {
            double _progressValue = (100.0 * _param1.getBytesTransferred()) / _param1.getTotalByteCount();
        };

        store_download_progress_listener = (OnProgressListener<FileDownloadTask.TaskSnapshot>) _param1 -> {
            double _progressValue = (100.0 * _param1.getBytesTransferred()) / _param1.getTotalByteCount();
        };

        store_upload_success_listener = _param1 -> {
            final String _downloadUrl = _param1.getResult().toString();
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
     * настроить источник
     * @param folder папка источника
     */
    public void restartListening(Info folder) {
        fb_db = FirebaseDatabase.getInstance();
        db = fb_db.getReference(folder.path);
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
        FirebaseDatabase.getInstance().getReference(Info.ADMINS.path).addListenerForSingleValueEvent(dba_listener);
    }

    /***
     * Получать данные из выбранной папки (folder)
     */
    private void receiveFromFolder() {
        db_child_listener = new ChildEventListener() {
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
            }
        };
        db.removeEventListener(db_child_listener);
        db.addChildEventListener(db_child_listener);
    }

}
