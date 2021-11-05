package my.virkato.task.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.adapter.Rv_picturesAdapter;
import my.virkato.task.manager.entity.Report;
import my.virkato.task.manager.entity.Task;

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


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.report);

        e_description = findViewById(R.id.e_description);
        b_report_save = findViewById(R.id.b_report_save);
        rv = findViewById(R.id.rv_pictures);

        initializeLogic();
    }


    private void initializeLogic() {
        if ("".equals(getIntent().getStringExtra("report"))) {
            // нельзя создать отчёт к несуществующему заданию
            finish();
        }
        report = new Report(new Gson().fromJson(getIntent().getStringExtra("report"), new TypeToken<HashMap<String, Object>>() {
        }.getType()));
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
        rv.getAdapter().notifyDataSetChanged();

        showReport();
   }

    /***
     * показать отчёт
     * редактировать отчёт разрешено в чечение 5 часов
     * редактировать отчёт может только исполнитель
     */
    private void showReport() {
        e_description.setText(report.description);
        b_report_save.setVisibility(NetWork.isAdmin()? View.GONE : View.VISIBLE);
        if ((System.currentTimeMillis() - report.date) > (5 * 60 * 60 * 1000)) {
            b_report_save.setVisibility(View.GONE);
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
