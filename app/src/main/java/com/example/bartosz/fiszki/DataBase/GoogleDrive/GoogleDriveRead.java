package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;
import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Bartek on 2018-03-04.
 */

public class GoogleDriveRead extends AsyncTask<String, Void, String> {

    //private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
    private GoogleApiClient  mGoogleApiClient;
        private String fileName;
        private DriveId driveId;
        public DriveFile driveFile;
        private Context context;
        private ProgressDialog dialog = new ProgressDialog(context);


    public GoogleDriveRead(Context context, String fileName) {
            this.fileName = fileName;
            this.context = context;

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
                ReadDataFromGoogleDrive();
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Wait...");
        dialog.show();

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        dialog.dismiss();
        //MainActivity.Update();
        Toast.makeText(context, "Read file from Google Drive", Toast.LENGTH_LONG).show();

    }


    public void ReadDataFromGoogleDrive()
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


                        driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                    .setResultCallback(readFromGoogleDriveResultCallBack);


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


    private ResultCallback<DriveApi.DriveContentsResult> readFromGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {


        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                //Log.e(TAG, "Error with Read file from Google Drive");
                //handler.sendEmptyMessage(0);
                //Toast.makeText(activity, "Error with Read file from Google Drive", Toast.LENGTH_LONG).show();
                return;
            }
            //Log.d(TAG,"Reading data from Google Drive");

            //MainActivity.dbFlashcard.DeleteAllFlashcards();
            //File file = activity.getDatabasePath(getActualDatabse());


            try {
                //OutputStream oos = new FileOutputStream(file);
                //Writer writer = new OutputStreamWriter(oos);
                //InputStream is = result.getDriveContents().getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(result.getDriveContents().getInputStream()));
                String allText="";
                int i=0;

                Flashcard flashcard = new Flashcard(0,null,null,null,null);
                String category="";

                String line;


                while ((line = reader.readLine()) != null) {
                    //builder.append(line).append("\n");
                    for (char ch:line.toCharArray()) {
                        if(ch=='~')
                        {
                            i++;

                            switch (i)
                            {
                                case 1: category = allText; MainActivity.dbCategory.CreateCategory(category);  break;
                                case 2: flashcard.setEngWord(allText);  break;
                                case 3: flashcard.setPlWord(allText); break;
                                case 4: flashcard.setEngSentence(allText); break;
                                case 5: flashcard.setPlSentence(allText); break;
                                case 6:
                                {
                                    i=0;
                                    int id = MainActivity.dbFlashcard.AddFlashcardIfNotExist(flashcard);
                                    //System.out.println(flashcard.getEngSentence());
                                    if(id!=0)
                                    {
                                        MainActivity.dbCategory.AddFlashcardToCategory(category,id);
                                    }

                                    break;
                                }
                            }
                            allText="";
                        }
                        else
                        {
                            allText+=ch;
                        }
                    }
                }

                reader.close();
                mGoogleApiClient.disconnect();


                //Toast.makeText(activity, "Wczytano dane z Google Drive", Toast.LENGTH_LONG).show();
                //Log.d(TAG,"Wczytano dane z Google Drive");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private Query QuerySearchFile()
    {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                //.addFilter(Filters.eq(CustomPropertyKey.PRIVATE,"flashcards"))
                //.addFilter(Filters.

                .build();

        return query;
    }

}
