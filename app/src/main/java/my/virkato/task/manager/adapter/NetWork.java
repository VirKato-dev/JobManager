package my.virkato.task.manager.adapter;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.bean.People;

/***
 * Работа с базой данных и хранилищем
 */
public class NetWork {

    private FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private FirebaseStorage _firebase_storage = FirebaseStorage.getInstance();
    private FirebaseAuth auth;
    private DatabaseReference db;
    private ChildEventListener _db_child_listener;
    private StorageReference store;
    private OnCompleteListener<Uri> _store_upload_success_listener;
    private OnSuccessListener<FileDownloadTask.TaskSnapshot> _store_download_success_listener;
    private OnSuccessListener _store_delete_success_listener;
    private OnProgressListener _store_upload_progress_listener;
    private OnProgressListener _store_download_progress_listener;
    private OnFailureListener _store_failure_listener;

    private People people;
    private String folder;


    public NetWork(String folder) {

        this.folder = folder;

        db = _firebase.getReference(folder);
        store = _firebase_storage.getReference(folder);
        auth = FirebaseAuth.getInstance();
        people = new People();

        // получаем данные о пользователях
        _db_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                _childValue.put("uid", _childKey);
                if ((auth.getCurrentUser() != null)) {
                    if (folder.equals("users")) people.update(_childValue);
                    if (folder.equals("reports")) {} //reports.update(_childValue);
                    if (folder.equals("tasks")) {} //tasks.update(_childValue);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                _childValue.put("uid", _childKey);
                if ((auth.getCurrentUser() != null)) {
                    if (folder.equals("users")) people.update(_childValue);
                    if (folder.equals("reports")) {} //reports.update(_childValue);
                    if (folder.equals("tasks")) {} //tasks.update(_childValue);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                _childValue.put("uid", _childKey);
                if ((auth.getCurrentUser() != null)) {
                    if (folder.equals("users")) people.remove(_childValue);
                    if (folder.equals("reports")) {} //reports.remove(_childValue);
                    if (folder.equals("tasks")) {} //tasks.remove(_childValue);
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
        db.addChildEventListener(_db_child_listener);

        _store_upload_progress_listener = (OnProgressListener<UploadTask.TaskSnapshot>) _param1 -> {
            double _progressValue = (100.0 * _param1.getBytesTransferred()) / _param1.getTotalByteCount();
        };

        _store_download_progress_listener = (OnProgressListener<FileDownloadTask.TaskSnapshot>) _param1 -> {
            double _progressValue = (100.0 * _param1.getBytesTransferred()) / _param1.getTotalByteCount();
        };

        _store_upload_success_listener = _param1 -> {
            final String _downloadUrl = _param1.getResult().toString();
        };

        _store_download_success_listener = _param1 -> {
            final long _totalByteCount = _param1.getTotalByteCount();
        };

        _store_delete_success_listener = _param1 -> {
        };

        _store_failure_listener = _param1 -> {
            final String _message = _param1.getMessage();
        };

    }


    public People getPeople() {
        return people;
    }


    public DatabaseReference getDB() {
        return db;
    }


    public StorageReference getStore() {
        return store;
    }
}
