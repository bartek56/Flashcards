package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.example.bartosz.fiszki.DataBase.GoogleDrive.BaseDemoActivity.REQUEST_CODE_RESOLUTION;
import static com.example.bartosz.fiszki.MainActivity.activity;

/**
 * Created by Bartek on 2018-03-04.
 */

public class GoogleDriveWrite implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String fileName;
    private DriveId driveId;
    public DriveFile driveFile;
    private ProgressDialog dialog;
    private Context context;

    public GoogleDriveWrite(Context context, String fileName) {
        this.fileName = fileName;
        this.context = context;
        dialog = new ProgressDialog(context);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("connected");
        WriteDataToGoogleDrive();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                if (!connectionResult.hasResolution()) {
                    //GoogleApiAvailability.getInstance().getErrorDialog(context, connectionResult.getErrorCode(), 0).show();
                    System.out.println("connection failed "+ connectionResult.getErrorMessage() + " " +connectionResult.getErrorCode());
                    return;
                }

                try {
                    connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
                } catch (IntentSender.SendIntentException ex) {

                    Log.e("INFO", "Exception while starting resolution activity", ex);
                }
            }
        });



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

                        driveFile = m.getDriveId().asDriveFile();


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


            new AsyncTask<DriveApi.DriveContentsResult, Void, String>(){

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

                        result.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(finishedRead);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute(result);

        }
    };


    private ResultCallback<com.google.android.gms.common.api.Status> finishedRead = new ResultCallback<com.google.android.gms.common.api.Status>() {


        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                // handler.sendEmptyMessage(0);
                // Log.v(TAG, "Successfull saved in Google Drive");
                Toast.makeText(activity, "Zapisano na Google Drive", Toast.LENGTH_LONG).show();
                mGoogleApiClient.disconnect();
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
                //Log.v(TAG, "file created: "+ result.getDriveFile().getDriveId());

                WriteDataToGoogleDrive();
            }
            return;
        }
    };


}
