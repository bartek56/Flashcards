package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.Activity;
import android.app.Notification;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.fiszki.DataBase.SQLite.CategoryHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;
import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by bbrzozowski on 2017-08-25.
 */

public class GoogleDriveHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "Google_Drive_Helper";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final  int REQUEST_CODE_OPENER = 2;
    private GoogleApiClient mGoogleApiClient;
    private DriveId driveId;
    public DriveFile driveFile;
    private int write=0;
    Activity activity;
    private String fileName=null;
    private Handler handler;

    public GoogleDriveHelper(Activity activity){

            this.activity = activity;

            if (mGoogleApiClient == null) {

            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    //.addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(activity, "Connected", Toast.LENGTH_LONG).show();
        Log.v(TAG, "Connected with GoogleDrive");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());

        if (!connectionResult.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, connectionResult.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {

            connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException ex) {

            Log.e(TAG, "Exception while starting resolution activity", ex);
        }
    }

    public void onResume()
    {
        if (mGoogleApiClient == null) {

            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    //.addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        mGoogleApiClient.connect();
    }

    public void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

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

    public void CheckDataBaseFileInGoogle(String file)
    {
        this.fileName = file;
        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile())
                .setResultCallback(resultCallCreateFile);
    }



    ResultCallback<DriveApi.MetadataBufferResult> resultCallCreateFile =  new ResultCallback<DriveApi.MetadataBufferResult>(){
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Query error");
                return;
            }

            boolean fileExist=false;
            for (Metadata m : result.getMetadataBuffer()) {

                String title = m.getTitle();

                if (title.equals(fileName)) {
                    Log.v(TAG, "file exists");

                    fileExist=true;
                    break;
                }
            }

            if(!fileExist) {
                Log.v(TAG, "file not exists");
                CreateFileOnGoogleDrive();
            }
        }
    };

    private void DeleteFile(DriveFile file) {

        file.delete(mGoogleApiClient).setResultCallback(deleteCallback);
    }

    final private ResultCallback<Status> deleteCallback = new ResultCallback<Status>() {


        @Override
        public void onResult(@NonNull Status status) {
            if(status.isSuccess())
            {
                Log.v(TAG,"Removed file");
            }
        }
    };

    public void CreateFileOnGoogleDrive(){

        //final DriveContents driveContents = result.getDriveContents();


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
                        Log.v(TAG, "file created: "+ result.getDriveFile().getDriveId());

                SaveDateOnGoogleDrive(fileName);
            }

            return;

        }
    };

    public void ReadDataFromGoogleDrive(String file)
    {
        this.fileName = file;
        write=0;
        System.out.println("file name: "+fileName);
        OpenFileInGoogleDrive();
    }


    public void SaveDateOnGoogleDrive(String file)
    {
        this.fileName = file;
        write=1;
        OpenFileInGoogleDrive();
    }



    public void OpenFileInGoogleDrive()
    {
        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile())
                .setResultCallback(resultCal);
    }


    private ResultCallback<DriveApi.MetadataBufferResult> resultCal =  new ResultCallback<DriveApi.MetadataBufferResult>(){
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                handler.sendEmptyMessage(0);
                Log.e(TAG, "error with opening file");
                Toast.makeText(activity, "error with opening file", Toast.LENGTH_LONG).show();
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

                    if(write==1)
                            driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                                    .setResultCallback(saveInGoogleDriveResultCallBack);
                    else

                            driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                    .setResultCallback(readFromGoogleDriveResultCallBack);


                    break;
                }



            }
            if(!fileExist)
            {
                FlashcardHelper flashcardHelper= new FlashcardHelper(MainActivity.activity, fileName);
                flashcardHelper.GetFlashcard(1);
                CreateFileOnGoogleDrive();
            }
        }
    };

    private ResultCallback<DriveApi.DriveContentsResult> saveInGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                handler.sendEmptyMessage(0);
                Log.e(TAG, "Error with Save in Google Drive");
                Toast.makeText(activity, "error with save in Google Drive", Toast.LENGTH_LONG).show();
                return;
            }
            try {



                //Writer writer = new OutputStreamWriter(fileOutputStream);


                File sqllitefile = activity.getDatabasePath(fileName);

                OutputStream oos = result.getDriveContents().getOutputStream();
                InputStream is = new FileInputStream(sqllitefile);

                byte[] buf = new byte[4096];
                int c;
                while ((c = is.read(buf)) > 0) {
                    oos.write(buf, 0, c);

                }
                oos.flush();
                oos.close();

                result.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status result) {
                        if (result.isSuccess()) {
                            handler.sendEmptyMessage(0);
                            Log.v(TAG, "Successfull saved in Google Drive");
                            Toast.makeText(activity, "Successfull saved in Google Drive", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

/*
    private ResultCallback<DriveApi.DriveContentsResult> saveInGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error with Save in Google Drive");
                Toast.makeText(activity, "error with save in Google Drive", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                ParcelFileDescriptor parcelFileDescriptor = result.getDriveContents().getParcelFileDescriptor();
                FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor
                        .getFileDescriptor());
                // Read to the end of the file.
                fileInputStream.read(new byte[fileInputStream.available()]);
                //System.out.println("file: "+fileInputStream.read());
                // Append to the file.
                FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                        .getFileDescriptor());
                Writer writer = new OutputStreamWriter(fileOutputStream);


                File sqllitefile = activity.getDatabasePath(fileName);

                OutputStream oos = result.getDriveContents().getOutputStream();
                InputStream is = new FileInputStream(sqllitefile);
                byte[] buf = new byte[4096];
                int c;
                while ((c = is.read(buf, 0, buf.length)) > 0) {
                    oos.write(buf, 0, c);
                    oos.flush();
                }
                //oos.flush();
                writer.close();

                result.getDriveContents().commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status result) {
                        if (result.isSuccess()) {
                            Log.v(TAG, "Successfull saved in Google Drive");
                            Toast.makeText(activity, "Successfull saved in Google Drive", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
*/
/*
    private ResultCallback<DriveApi.DriveContentsResult> readFromGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {


        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error with Read file from Google Drive");
                Toast.makeText(activity, "Error with Read file from Google Drive", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG,"Reading data from Google Drive");

            MainActivity.dbFlashcard.DeleteAllFlashcards();
            File file = activity.getDatabasePath(FILENAME);


                try {
                OutputStream oos = new FileOutputStream(file);
                Writer writer = new OutputStreamWriter(oos);
                InputStream is = result.getDriveContents().getInputStream();

                byte[] buf = new byte[4096];
                int c;
                while ((c = is.read(buf, 0, buf.length)) > 0) {
                    oos.write(buf, 0, c);
                    oos.flush();
                }
                writer.close();


                SQLiteDatabase.openOrCreateDatabase(file, null);
                    Toast.makeText(activity, "Wczytano dane z Google Drive", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Wczytano dane z Google Drive");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
*/
private ResultCallback<DriveApi.DriveContentsResult> readFromGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {


    @Override
    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
        if (!result.getStatus().isSuccess()) {
            Log.e(TAG, "Error with Read file from Google Drive");
            handler.sendEmptyMessage(0);
            Toast.makeText(activity, "Error with Read file from Google Drive", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG,"Reading data from Google Drive");

        MainActivity.dbFlashcard.DeleteAllFlashcards();
        File file = activity.getDatabasePath(fileName);


        try {
            OutputStream oos = new FileOutputStream(file);
            //Writer writer = new OutputStreamWriter(oos);
            InputStream is = result.getDriveContents().getInputStream();


            byte[] buf = new byte[1024];
            int c;
            while ((c = is.read(buf)) > 0) {

                oos.write(buf);

            }
            oos.flush();
            oos.close();
            is.close();


            SQLiteDatabase.openOrCreateDatabase(file, null);
            Toast.makeText(activity, "Wczytano dane z Google Drive", Toast.LENGTH_LONG).show();
            Log.d(TAG,"Wczytano dane z Google Drive");

            handler.sendEmptyMessage(1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
};

    private ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error");
                        return;
                    }

                    DriveContents contents = result.getDriveContents();


                    BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    try
                    {

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }catch (IOException ex)
                    {
                        System.out.println("exception "+ex);
                    }


                    String contentsAsString = builder.toString();
                    System.out.println("zawartosc: "+contentsAsString);

                    Toast.makeText(activity, "zawartość: " +contentsAsString, Toast.LENGTH_LONG).show();
                }
            };


            public void setHandler(Handler handler)
            {
                this.handler = handler;
            }

}