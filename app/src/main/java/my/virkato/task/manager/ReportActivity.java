package my.virkato.task.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.adapter.Rv_picturesAdapter;
import my.virkato.task.manager.entity.Report;

/***
 * оформить отчёт о ходе работ
 */
public class ReportActivity extends AppCompatActivity {

    /***
     * описание отчёта
     */
    private EditText e_description;

    /***
     * сохранить отчёт
     */
    private Button b_report_save;

    /***
     * собавить фотографию отчёта
     */
    private Button b_add_picture;

    /***
     * адаптер для получения списка отчётов
     */
    private NetWork dbReports = new NetWork(NetWork.Info.REPORTS);

    /***
     * текущий отчёт
     */
    private Report report;

    /***
     * список фотографий отчёта
     */
    private RecyclerView rv;

    /***
     * картинки отчёта
     */
    private ArrayList<String> pictures;

    /***
     * приёмник выбранного файла
     */
    private ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String path = FileUtil.convertUriToFilePath(rv.getContext(), uri);
                    pictures.add(path);
                    rv.getAdapter().notifyDataSetChanged();
                }
            });


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.report);

        e_description = findViewById(R.id.e_description);
        b_report_save = findViewById(R.id.b_report_save);
        rv = findViewById(R.id.rv_pictures);
        b_add_picture = findViewById(R.id.b_add_picture);

        initializeLogic();
    }


    private void initializeLogic() {
        if ("".equals(getIntent().getStringExtra("report"))) {
            // нельзя создать отчёт к несуществующему заданию
            finish();
        }
        String json = getIntent().getStringExtra("report");
        Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
        HashMap<String, Object> map = new Gson().fromJson(json, type);
        report = new Report(map);
        if (report.id.equals("")) {
            // создать новый отчёт к указанному заданию
            report.id = dbReports.getDB().push().getKey();
        }

        b_report_save.setOnClickListener(v -> {
            report.description = e_description.getText().toString();
            report.date = System.currentTimeMillis();
            report.send(v.getContext(), dbReports.getDB());
        });

        pictures = report.images;

        rv.setAdapter(new Rv_picturesAdapter(this, pictures));
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.getAdapter().notifyDataSetChanged();

        showReport();

        b_add_picture.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });
    }

    /***
     * показать отчёт
     * редактировать отчёт разрешено в чечение 5 часов
     * редактировать отчёт может только исполнитель
     * добавлять фото может только исполнитель
     */
    private void showReport() {
        e_description.setText(report.description);
        b_report_save.setVisibility(NetWork.isAdmin() ? View.GONE : View.VISIBLE);
        b_add_picture.setVisibility(NetWork.isAdmin() ? View.GONE : View.VISIBLE);

        if (report.date > 0 && (System.currentTimeMillis() - report.date) > (5 * 60 * 60 * 1000)) {
            b_report_save.setVisibility(View.GONE);
            b_add_picture.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {

        super.onActivityResult(_requestCode, _resultCode, _data);

        switch (_requestCode) {

            default:
                break;
        }
    }

}
