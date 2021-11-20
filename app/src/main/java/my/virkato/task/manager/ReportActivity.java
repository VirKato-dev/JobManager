package my.virkato.task.manager;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import my.virkato.task.manager.adapter.NetWork;
import my.virkato.task.manager.adapter.Rv_picturesAdapter;
import my.virkato.task.manager.entity.Report;
import my.virkato.task.manager.entity.ReportImage;
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
     * собавить фотографию отчёта
     */
    private Button b_add_picture;

    /***
     * адаптер для получения списка отчётов
     */
    private final NetWork dbReports = new NetWork(NetWork.Info.REPORTS);

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
    private ArrayList<ReportImage> pictures;

    /***
     * период к течение которого мастер может отредактировать свой отчёт
     */
    private final long PERIOD = 5 * 60 * 60 * 1000;

    /***
     * мастер текущего задания
     */
    private String master = "";

    /***
     * отчёт к этому заданию
     */
    private String taskId = "";

    /***
     * можно ли изменять/создавать отчёт
     */
    private boolean canChange = false;

    /***
     * приёмник выбранного файла
     */
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String path = FileUtil.convertUriToFilePath(rv.getContext(), uri);
                    ReportImage repImg = new ReportImage();
                    repImg.original = path;
                    pictures.add(repImg);
                    rv.getAdapter().notifyDataSetChanged();
                }
            });


    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_report);

        dbReports.setContext(getApplicationContext());
//        dbReports.receiveNewData();

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
        Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        HashMap<String, Object> map = new Gson().fromJson(json, type);
        taskId = map.get("task_id").toString();
        report = new Report(map);
        if (report.id.equals("")) {
            // создать новый отчёт к указанному заданию
            report.id = dbReports.getDB().push().getKey();
        }

        b_report_save.setOnClickListener(v -> {
            saveReport();
        });

        Task task = dbReports.getTasks().findTaskById(taskId);
        if (task != null) master = task.master_uid;
        canChange = master.equals(NetWork.user().getUid());

        pictures = report.images;

        rv.setAdapter(new Rv_picturesAdapter(this, pictures));
//        rv.setLayoutManager(new LinearLayoutManager(this));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        gridLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        rv.setLayoutManager(gridLayoutManager);
        rv.getAdapter().notifyDataSetChanged();

        ((Rv_picturesAdapter) rv.getAdapter()).setOnClickListener(v -> {
            int position = rv.getChildLayoutPosition(v);
            ReportImage item = ((Rv_picturesAdapter) rv.getAdapter()).getItem(position);
            showImageFullSize(item);
        });

        ((Rv_picturesAdapter) rv.getAdapter()).setOnLongClickListener(v -> {
            int position = rv.getChildLayoutPosition(v);
            if (position != RecyclerView.NO_POSITION && !NetWork.isAdmin()) {
                removePicture(position);
            }
            return true;
        });

        showReport();

        b_add_picture.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });
    }

    /***
     * сохранить отчёт
     */
    private void saveReport() {
        report.delete(dbReports);
        report.description = e_description.getText().toString();
        report.date = System.currentTimeMillis();
        report.master = NetWork.user().getUid();
        report.send(this, dbReports);
    }

    private void removePicture(int position) {
        ReportImage item = ((Rv_picturesAdapter) rv.getAdapter()).getItem(position);
        if (!item.url.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.ad_title_remove_pict);
            builder.setMessage(R.string.ad_message_remove_pict);
            builder.setPositiveButton(R.string.ad_positive_button, (dialog, which) -> {
                pictures.remove(position);
                dbReports.removeImageFromStorage(item);
                saveReport();
            });
            builder.setNegativeButton(R.string.ad_negative_button, (dialog, which) -> {
            });
            builder.create().show();
        }
    }

    /***
     * показать отчёт
     * редактировать отчёт разрешено в чечение 5 часов
     * редактировать отчёт может только исполнитель
     * добавлять фото может только исполнитель
     */
    private void showReport() {
        e_description.setText(report.description);
        e_description.setEnabled(canChange); // только мастер может редактировать
        b_report_save.setVisibility(canChange ? View.VISIBLE : View.GONE);
        b_add_picture.setVisibility(canChange ? View.VISIBLE : View.GONE);

        if (report.date > 0 && (System.currentTimeMillis() - report.date) > PERIOD) {
            b_report_save.setVisibility(View.GONE);
            b_add_picture.setVisibility(View.GONE);
        }
    }

    private void showImageFullSize(ReportImage repImg) {
        String pict = repImg.url;
        if (NetWork.isAdmin()) {
            if (!repImg.received.equals("")) {
                if (new File(repImg.received).exists()) {
                    pict = "file://" + repImg.received;
                }
            }
        } else {
            pict = "file://" + repImg.original;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View photo = inflater.inflate(R.layout.a_photo, null);
        PhotoView photoView = photo.findViewById(R.id.pv);
        photoView.setImageURI(Uri.parse(pict));
        photoView.setAllowParentInterceptOnEdge(true);
        alertDialog.setView(photoView);
        alertDialog.show();
//        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
