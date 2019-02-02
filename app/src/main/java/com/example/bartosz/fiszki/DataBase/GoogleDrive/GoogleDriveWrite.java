package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.fiszki.MainActivity;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

/**
 * Created by Bartek on 2018-03-04.
 */

public class GoogleDriveWrite extends GoogleDriveConnection {



    public GoogleDriveWrite(Context context, String fileName) {
        super(context,fileName);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        WriteDataToGoogleDrive();
    }


    public void UpdateModificationDate()
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
                        modificationDate = m.getModifiedDate();
                        System.out.println("write: "+modificationDate.toString());
                        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                        preferencesEditor.putString(MainActivity.dateModificationPreference,modificationDate.toString());
                        preferencesEditor.commit();
                        break;
                    }
                }

                mGoogleApiClient.disconnect();

                result.release();
            }
        });
    }


    public void WriteDataToGoogleDrive()
    {
        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile()).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    //Log.e(TAG, "error with opening file");
                    Toast.makeText(activity, "Błąd przy połączeniu z GoogleDrive", Toast.LENGTH_LONG).show();
                    return;
                }
                boolean fileExist = false;

                for (Metadata m : result.getMetadataBuffer()) {
                    String title = m.getTitle();
                    if (title.equals(fileName)) {
                        fileExist=true;
                        driveId = m.getDriveId();
                        driveFile = m.getDriveId().asDriveFile();

                        //modificationDate = m.getModifiedDate();

                        // jeśli data modyfikacji jest inna, słówka zostały edytowane za pomocą programu desktopowego,
                        // zatem powinno wyświetlić się powiadomienie, czy na pewno chcesz zapisać


                        // check, if modification data is other

                        driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                                .setResultCallback(saveInGoogleDriveResultCallBack);

                        break;
                    }
                }
                if(!fileExist)
                {
                    System.out.println("File doesn't exist");
                    CreateFileOnGoogleDrive();
                }
                result.release();
            }
        });
    }

    private ResultCallback<DriveApi.DriveContentsResult> saveInGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess())
            {
               Log.e("GoogleDriveWrite", "Error with Save in Google Drive");
               Toast.makeText(activity, "error with save in Google Drive", Toast.LENGTH_LONG).show();
               return;
            }


            class MyTask extends AsyncTask<DriveApi.DriveContentsResult, Void, String>{

                private ProgressDialog dialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog = new ProgressDialog(context);
                    dialog.setTitle("Zapisywanie danych...");
                    dialog.setMessage("Czekaj...");
                    dialog.show();
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    dialog.dismiss();
                }

                @Override
                protected String doInBackground(DriveApi.DriveContentsResult... driveContentsResults) {

                    DriveApi.DriveContentsResult result = driveContentsResults[0];

                    try {
                        List<String> list = MainActivity.dbFlashcard.GetAllFlashcards();
                        OutputStream oos = result.getDriveContents().getOutputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        for (String line : list) {
                            baos.write(line.getBytes("UTF-8"));
                        }

                        byte[] bytes = baos.toByteArray();
                        InputStream is = new ByteArrayInputStream(bytes);

                        byte[] buf = new byte[512];
                        int c;
                        while ((c = is.read(buf)) > 0) {
                            oos.write(buf, 0, c);

                        }
                        oos.flush();
                        oos.close();

                        result.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(finishedRead);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }
            new MyTask().execute(result);
        }
    };


    private ResultCallback<com.google.android.gms.common.api.Status> finishedRead = new ResultCallback<com.google.android.gms.common.api.Status>() {


        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Toast.makeText(activity, "Zapisano na Google Drive", Toast.LENGTH_LONG).show();
                UpdateModificationDate();
            }
        }
    };

    public void CreateFileOnGoogleDrive(){

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .setMimeType("text/plain")

                .build();

        // create a file in root folder
        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                .createFile(mGoogleApiClient, changeSet, null)
                .setResultCallback(fileCallback);
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (result.getStatus().isSuccess()) {
                driveFile =  result.getDriveFile();

                Log.v("GoogleDriveWrite", "file created: "+ result.getDriveFile().getDriveId());
                UpdateModificationDate();
            }
            return;
        }
    };


}
