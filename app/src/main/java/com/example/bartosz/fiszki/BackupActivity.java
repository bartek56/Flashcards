package com.example.bartosz.fiszki;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveConnection;
import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.actualLanguageDataBase;
import static com.example.bartosz.fiszki.MainActivity.dbFlashcard;
import static com.example.bartosz.fiszki.MainActivity.getActualCsvFile;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

public class BackupActivity extends AppCompatActivity {


    private TextView synchronizationDate;
    private GoogleDriveHelper googleDriveHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        synchronizationDate = (TextView) findViewById(R.id.tvLastSynchronizationDate);
        String dateModification = sharedPreferences.getString(MainActivity.dateModificationPreference,"21-02-2019");

        synchronizationDate.setText(dateModification);
        googleDriveHelper = new GoogleDriveHelper(GoogleDriveConnection.driveService);

        //String languageMode = sharedPreferences.getString(MainActivity.languageModePreference,MainActivity.languageModeEngPl);

    }

    public void ReadDataButtonOnClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Czy chcesz wczytać dane z Google Drive?");

        builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                dbFlashcard.DeleteAllFlashcards();
                dbFlashcard = new FlashcardHelper(activity,actualLanguageDataBase);
                googleDriveHelper.ReadFlashcardsHelper(getActualCsvFile());
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
                googleDriveHelper.SaveFlashcardsHelper(getActualCsvFile());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NIE", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
