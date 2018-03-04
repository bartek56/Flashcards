package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Bartek on 2018-03-04.
 */

public class GoogleDriveWrite extends AsyncTask<String, Void, String> {

    private GoogleApiClient mGoogleApiClient;
    private String fileName;
    private DriveId driveId;
    public DriveFile driveFile;


    public GoogleDriveWrite(Context context, String fileName) {
        this.fileName = fileName;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                //.addConnectionCallbacks(context)
                //.addOnConnectionFailedListener(context)
                .build();

    }

    @Override
    protected String doInBackground(String... strings) {

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(@Nullable Bundle bundle) {
                WriteDataToGoogleDrive();
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });

        mGoogleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }
        });

        mGoogleApiClient.connect();



        return null;
    }


    private Query QuerySearchFile()
    {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                //.addFilter(Filters.eq(CustomPropertyKey.PRIVATE,"flashcards"))
                //.addFilter(Filters.

                .build();

        return query;
    }

    public void WriteDataToGoogleDrive()
    {

        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile()).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {

                    //Log.e(TAG, "error with opening file");
                    //Toast.makeText(activity, "error with opening file", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean fileExist = false;

                for (Metadata m : result.getMetadataBuffer()) {

                    String title = m.getTitle();
                    if (title.equals(fileName)) {

                        fileExist=true;
                        driveId = m.getDriveId();

                        //driveId = "0B_7aHpqEzhaieUdlVzRDbzFOZWs";
                        driveFile = m.getDriveId().asDriveFile();


                        driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                                .setResultCallback(saveInGoogleDriveResultCallBack);


                        break;
                    }

                }
                /*
                if(!fileExist)
                {
                    //FlashcardHelper flashcardHelper= new FlashcardHelper(MainActivity.activity, fileName);
                    //flashcardHelper.GetFlashcard(1);
                    //CreateFileOnGoogleDrive();
                }
*/

            }
        });
    }

    private ResultCallback<DriveApi.DriveContentsResult> saveInGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
               // handler.sendEmptyMessage(0);
               // Log.e(TAG, "Error with Save in Google Drive");
               // Toast.makeText(activity, "error with save in Google Drive", Toast.LENGTH_LONG).show();
                return;
            }
            try {

                List<String> list = MainActivity.dbFlashcard.GetAllFlashcards();

                OutputStream oos = result.getDriveContents().getOutputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                for (String line : list) {
                    baos.write(line.getBytes("UTF-8"));
                    //baos.write('\n');
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


                result.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                    @Override
                    public void onResult(com.google.android.gms.common.api.Status result) {
                        if (result.isSuccess()) {
                           // handler.sendEmptyMessage(0);
                           // Log.v(TAG, "Successfull saved in Google Drive");
                           // Toast.makeText(activity, "Successfull saved in Google Drive", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
