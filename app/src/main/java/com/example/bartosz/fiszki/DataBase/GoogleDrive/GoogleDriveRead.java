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
import java.util.ArrayDeque;
import java.util.Queue;

import static com.example.bartosz.fiszki.DataBase.GoogleDrive.BaseDemoActivity.REQUEST_CODE_RESOLUTION;
import static com.example.bartosz.fiszki.MainActivity.activity;

/**
 * Created by Bartek on 2018-03-04.
 */

public class GoogleDriveRead extends AsyncTask<String, Void, String> {


    private GoogleApiClient  mGoogleApiClient;
        private String fileName;
        private DriveId driveId;
        public DriveFile driveFile;
        private Context context;
        private ProgressDialog dialog;


    public GoogleDriveRead(Context context, String fileName) {
            this.fileName = fileName;
            this.context = context;
            dialog = new ProgressDialog(context);

            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    //.addConnectionCallbacks(this)
                    //.addOnConnectionFailedListener(context)
                    .build();
            /*
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mGoogleApiClient = builder.build();
        */

    }


    @Override
    protected String doInBackground(String... strings) {

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                ReadDataFromGoogleDrive();
                System.out.println("connected");
            }

            @Override
            public void onConnectionSuspended(int i) {
                System.out.println("connection suspend");
            }

        });


        mGoogleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                if (!connectionResult.hasResolution()) {
                    //GoogleApiAvailability.getInstance().getErrorDialog(context, connectionResult.getErrorCode(), 0).show();
                    System.out.println("connection failed "+ connectionResult.getErrorMessage() + " " +connectionResult.getErrorCode());

                }
                try {
                    dialog.dismiss();
                    connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
                    Toast.makeText(activity, "Wybierz konto i ponownie wczytaj dane", Toast.LENGTH_LONG).show();

                } catch (IntentSender.SendIntentException ex) {

                    Log.e("INFO", "Exception while starting resolution activity", ex);
                }
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
        //dialog.dismiss();
        //mGoogleApiClient.disconnect();
        //dialog.dismiss();
        //MainActivity.Update();
        //Toast.makeText(context, "Read file from Google Drive", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        //dialog.dismiss();

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

                if(!fileExist)
                {
                    System.out.println("Nie ma pliku");
                    Toast.makeText(activity, "Plik nie istnieje", Toast.LENGTH_LONG).show();
                }


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
                String category="inne";


                int idFlashcard =0;
                int columnNumber=0;

                String line;
                Queue<String> engWord= new ArrayDeque<String>();
                Queue<String> plWord=new ArrayDeque<String>();
                Queue<String> engSentence=new ArrayDeque<String>();
                Queue<String> plSentence=new ArrayDeque<String>();

                Queue<Integer> idFlashcardQueue =new ArrayDeque<Integer>();
                Queue<Integer> flashcardIsKnownQueue =new ArrayDeque<Integer>();

                while ((line = reader.readLine()) != null) {
                    //builder.append(line).append("\n");
                    for (char ch:line.toCharArray()) {
                        if(ch=='~')
                        {
                            columnNumber++;
                            switch (columnNumber)
                            {
                                case 1:
                                {
                                    if(!allText.equals(category))
                                    {
                                        MainActivity.dbCategory.CreateCategory(allText);
                                        MainActivity.dbFlashcard.AddFlashcardsFromQueue(engWord, plWord, engSentence, plSentence);
                                        MainActivity.dbCategory.AddFlashcardsToCategoryFromQueue(idFlashcardQueue,flashcardIsKnownQueue, category);
                                        category=allText;
                                    }
                                    break;
                                }
                                case 2: engWord.add(allText);  break;
                                case 3: plWord.add(allText); break;
                                case 4: engSentence.add(allText); break;
                                case 5: plSentence.add(allText); break;
                                case 6:
                                {

                                    columnNumber=0;
                                    idFlashcard++;
                                    idFlashcardQueue.add(idFlashcard);
                                    flashcardIsKnownQueue.add(Integer.parseInt(allText));

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
                MainActivity.dbFlashcard.AddFlashcardsFromQueue(engWord, plWord, engSentence, plSentence);
                MainActivity.dbCategory.AddFlashcardsToCategoryFromQueue(idFlashcardQueue,flashcardIsKnownQueue,category);

                mGoogleApiClient.disconnect();
                dialog.dismiss();

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
