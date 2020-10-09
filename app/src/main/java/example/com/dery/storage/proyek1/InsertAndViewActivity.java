package example.com.dery.storage.proyek1;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class InsertAndViewActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_STORAGE = 100;
    private int eventID = 0;
    private EditText etFileName;
    private EditText etContent;
    //boolean isEditable = false;
    private String tempCatatan = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_and_view);

        //untuk menggunakan toolbar, pastikan pastikan parent pada style bernilai "Theme.AppCompat.Light.NoActionBar"
        //pada styles.xml di folder res/values
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        etFileName = findViewById(R.id.et_filename);
        etContent = findViewById(R.id.et_content);
        Button btSimpan = findViewById(R.id.btSimpan);


        //check bundle dari intent apakah tidak null dan tidak kosong
        if (getIntent().getExtras() != null && !getIntent().getExtras().isEmpty()) {

            //ambil nilai bundle untuk key "filename" berikan isinya ke view etFilename
            etFileName.setText(getIntent().getExtras().getString("filename"));

            //ubah title pada toolbar
            getSupportActionBar().setTitle("Ubah Catatan");
        } else {
            //ubah title pada toolbar
            getSupportActionBar().setTitle("Tambah Catatan");
        }


        eventID = 1;
        if (Build.VERSION.SDK_INT >= 23) {
            if (periksaIzinPenyimpanan()) {
                bacaFile();
            }
        } else {
            bacaFile();
        }

        //event handler
        btSimpan.setOnClickListener(this::btSimpanOnClick);
    }

    private void btSimpanOnClick(View view) {
        eventID = 2;
        if (!tempCatatan.equals(etContent.getText().toString())) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (periksaIzinPenyimpanan()) {
                    tampilSaveDialog();
                }
            } else {
                tampilSaveDialog();
            }
        }
    }

    public boolean periksaIzinPenyimpanan() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE);

                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if (eventID == 1)
                bacaFile();
            else
                tampilSaveDialog();
    }


    void bacaFile() {
        String path = Environment.getExternalStorageDirectory().toString() + "/kominfo.proyek1";
        File file = new File(path, etFileName.getText().toString());
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                StringBuffer temp = new StringBuffer();
                int c;
                while ((c = fis.read()) != -1) {
                    temp.append((char) c);
                }

                etContent.setText(temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    void buatDanUbah() {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            Toast.makeText(this, "ES unavailable or not in read/write mode", Toast.LENGTH_SHORT).show();
            return;
        }

        String path = Environment.getExternalStorageDirectory().toString() + "/kominfo.proyek1";
        File parent = new File(path);

        if (parent.exists()) {
            File file = new File(path, etFileName.getText().toString());

            try (FileOutputStream fos = new FileOutputStream(file); OutputStreamWriter osw = new OutputStreamWriter(fos)) {
                osw.append(etContent.getText());
                osw.flush();
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            if (parent.mkdir()) return;

            File file = new File(path, etFileName.getText().toString());
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(etContent.getText().toString().getBytes());
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        onBackPressed();
    }

    void tampilSaveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Simpan Catatan")
                .setMessage("Apakah Anda yakin ingin menyimpan Catatan ini?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("YES", (dialog, whichButton) -> buatDanUbah())
                .setNegativeButton("NO", null).show();
    }

    @Override
    public void onBackPressed() {
        if (!tempCatatan.equals(etContent.getText().toString())) {
            tampilSaveDialog();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
