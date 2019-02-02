package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

/**
 * Created by programmer on 2/1/19.
 */

public class GoogleDriveUpdate extends GoogleDriveConnection{

    public boolean check = false;
    public GoogleDriveUpdate(Context context, String fileName) {
        super(context, fileName);
        isLastModification();
    }

    public void isLastModification()
    {
        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile()).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    //Log.e(TAG, "error with opening file");
                    Toast.makeText(activity, "Błąd przy połączeniu z GoogleDrive", Toast.LENGTH_LONG).show();
                    return;
                }

                for (Metadata m : result.getMetadataBuffer()) {
                    String title = m.getTitle();
                    if (title.equals(fileName)) {
                        String modificationDate = m.getModifiedDate().toString();
                        String lastModificationDate = sharedPreferences.getString(MainActivity.dateModificationPreference,"");
                        System.out.println("update last: "+lastModificationDate);
                        System.out.println("update new: "+modificationDate);
                        check = !modificationDate.equals(lastModificationDate);

                        if(check)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("Słówka uległy modifikacji przez inny program, czy chcesz je aktualizować?");

                            builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                    /*
                    dbFlashcard.DeleteAllFlashcards();
                    dbFlashcard = new FlashcardHelper(activity,actualLanguageDataBase);
                    googleDriveRead = new GoogleDriveRead(activity,getActualCsvFile());
                    handler = new handler2();
                    googleDriveRead.setHandler(handler);
                    dialog.dismiss();
                    */
                                }
                            });

                            builder.setNegativeButton("NIE", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                        break;
                    }
                }
                mGoogleApiClient.disconnect();
                result.release();
            }
        });
    }

}
