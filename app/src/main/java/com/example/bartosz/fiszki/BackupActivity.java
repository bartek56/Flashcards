package com.example.bartosz.fiszki;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveRead;
import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveWrite;
import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.actualLanguageDataBase;
import static com.example.bartosz.fiszki.MainActivity.dbFlashcard;
import static com.example.bartosz.fiszki.MainActivity.getActualCsvFile;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

public class BackupActivity extends AppCompatActivity {


    private TextView synchronizationDate;
    private android.os.Handler handler;
    private GoogleDriveRead googleDriveRead;
    private GoogleDriveWrite googleDriveWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        synchronizationDate = (TextView) findViewById(R.id.tvLastSynchronizationDate);
        String dateModification = sharedPreferences.getString(MainActivity.dateModificationPreference,"21-02-2019");

        synchronizationDate.setText(dateModification);


        //String languageMode = sharedPreferences.getString(MainActivity.languageModePreference,MainActivity.languageModeEngPl);


    }

    public void ReadDataButtonOnClick(View view) {
        class handler2 extends Handler {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==1)
                {
                    Toast.makeText(activity, "Wczytano dane z Google Drive", Toast.LENGTH_LONG).show();
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Czy chcesz wczytać dane z Google Drive?");

        builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                dbFlashcard.DeleteAllFlashcards();
                dbFlashcard = new FlashcardHelper(activity,actualLanguageDataBase);
               // googleDriveRead = new GoogleDriveRead(activity,getActualCsvFile());
                handler = new handler2();
                //googleDriveRead.setHandler(handler);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NIE", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void SaveDataButtonOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Czy chcesz zapisać dane z Google Drive?");
        builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                //googleDriveWrite = new GoogleDriveWrite(activity,getActualCsvFile());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NIE", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
