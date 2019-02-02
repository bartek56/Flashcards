package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

/**
 * Created by Bartek on 2018-03-04.
 */

public class GoogleDriveRead extends GoogleDriveConnection
{
    private Handler handler;

    public GoogleDriveRead(Context context, String fileName){
        super(context,fileName);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        ReadDataFromGoogleDrive();
    }

    public void ReadDataFromGoogleDrive(){
        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile()).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>()
        {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result)
            {
                if (!result.getStatus().isSuccess())
                {

                    Log.e("ukh", "error with opening file");
                    Toast.makeText(activity, "error with opening file", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean fileExist = false;

                for (Metadata m : result.getMetadataBuffer())
                {
                    String title = m.getTitle();
                    if (title.equals(fileName))
                    {
                        fileExist=true;
                        driveId = m.getDriveId();
                        driveFile = m.getDriveId().asDriveFile();

                        modificationDate = m.getModifiedDate();
                        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                        preferencesEditor.putString(MainActivity.dateModificationPreference,modificationDate.toString());
                        preferencesEditor.commit();

                        String modificationDate = m.getModifiedDate().toString();
                        String lastModificationDate = sharedPreferences.getString(MainActivity.dateModificationPreference,"");
                        System.out.println("read last: "+lastModificationDate);
                        System.out.println("read new: "+modificationDate);


                        driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                                    .setResultCallback(readFromGoogleDriveResultCallBack);

                        break;
                    }
                }
                if(!fileExist)
                {
                    System.out.println("file doesn't exist in GoogleDrive");
                    Toast.makeText(activity, "Nie ma pliku do odczytu z GoogleDrive", Toast.LENGTH_LONG).show();
                }
                result.release();
            }
        });
    }


    private ResultCallback<DriveApi.DriveContentsResult> readFromGoogleDriveResultCallBack = new ResultCallback<DriveApi.DriveContentsResult>() {

        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Toast.makeText(activity, "Błąd w odczytywaniu pliku", Toast.LENGTH_LONG).show();
                return;
            }

            class MyTask extends AsyncTask<DriveApi.DriveContentsResult, Void, String>
            {

                private ProgressDialog dialog;

                @Override
                protected void onPreExecute () {
                super.onPreExecute();
                dialog = new ProgressDialog(context);
                dialog.setTitle("Wczytywanie danych...");
                dialog.setMessage("Czekaj...");
                dialog.show();
                }

                @Override
                protected void onPostExecute (String s){
                super.onPostExecute(s);
                dialog.dismiss();
                mGoogleApiClient.disconnect();
                handler.sendEmptyMessage(1);
            }

                @Override
                protected String doInBackground (DriveApi.DriveContentsResult...results){

                DriveApi.DriveContentsResult result = results[0];

                    InputStream inputStream = result.getDriveContents().getInputStream();
                    try
                    {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String allText = "";
                    String category = "inne";
                    int idFlashcard = 0;
                    int columnNumber = 0;

                    String line;
                    Queue<String> engWord = new ArrayDeque<String>();
                    Queue<String> plWord = new ArrayDeque<String>();
                    Queue<String> engSentence = new ArrayDeque<String>();
                    Queue<String> plSentence = new ArrayDeque<String>();

                    Queue<Integer> idFlashcardQueue = new ArrayDeque<Integer>();
                    Queue<Integer> flashcardIsKnownQueue = new ArrayDeque<Integer>();

                    while ((line = reader.readLine()) != null) {
                        for (char ch : line.toCharArray()) {
                            if (ch == '~') {
                                columnNumber++;
                                switch (columnNumber) {
                                    case 1: {
                                        if (!allText.equals(category)) {
                                            MainActivity.dbFlashcard.CreateCategory(allText);
                                            MainActivity.dbFlashcard.AddFlashcardsFromQueue(engWord, plWord, engSentence, plSentence);
                                            MainActivity.dbFlashcard.AddFlashcardsToCategoryFromQueue(idFlashcardQueue, flashcardIsKnownQueue, category);
                                            category = allText;
                                        }
                                        break;
                                    }
                                    case 2:
                                        engWord.add(allText);
                                        break;
                                    case 3:
                                        plWord.add(allText);
                                        break;
                                    case 4:
                                        engSentence.add(allText);
                                        break;
                                    case 5:
                                        plSentence.add(allText);
                                        break;
                                    case 6: {

                                        columnNumber = 0;
                                        idFlashcard++;
                                        idFlashcardQueue.add(idFlashcard);
                                        flashcardIsKnownQueue.add(Integer.parseInt(allText));

                                        break;
                                    }
                                }
                                allText = "";
                            } else {
                                allText += ch;
                            }
                        }
                    }

                    reader.close();
                    inputStream.close();
                    MainActivity.dbFlashcard.AddFlashcardsFromQueue(engWord, plWord, engSentence, plSentence);
                    MainActivity.dbFlashcard.AddFlashcardsToCategoryFromQueue(idFlashcardQueue, flashcardIsKnownQueue, category);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
            }

            new MyTask().execute(result);

        }
    };

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
