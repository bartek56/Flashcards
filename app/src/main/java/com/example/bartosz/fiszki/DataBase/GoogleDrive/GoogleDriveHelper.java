package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.datebaseFileIdPreference;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

/**
 * Created by Bartek on 2019-02-03.
 */

public class GoogleDriveHelper {

    private Drive driveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final String TAG = "GoogleDriveHelper";
    public GoogleDriveHelper(Drive driveService)
    {
        this.driveService=driveService;
    }


    public void ReadFlashcardsHelper(String fileName) {

        Log.d(TAG, "Reading file");

        ReadFlashcards()
                .addOnSuccessListener(fileId ->
                {
                    Log.d(TAG, fileId);
                    Toast.makeText(activity, "Wczytano z Google Drive", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(exception ->
                {
                    Log.e(TAG, "Couldn't read file.", exception);
                    Toast.makeText(activity, "Błąd przy wczytywaniu", Toast.LENGTH_LONG).show();
                });

    }


    public void SaveFlashcardsHelper(String fileName) {

        Log.d(TAG, "Saving file");

        SaveFlashcards(fileName)
                .addOnSuccessListener(fileId ->
                {
                    Log.d(TAG, fileId);
                    Toast.makeText(activity, "Zapisano na Google Drive", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(exception ->
                {
                    Log.e(TAG, "Couldn't save file.", exception);
                    Toast.makeText(activity, "Błąd w zapisywaniu", Toast.LENGTH_LONG).show();
                });
    }



    public Task<String> SaveFlashcards(String fileName)
    {
        return Tasks.call(mExecutor,() -> {

            String fileId = sharedPreferences.getString(datebaseFileIdPreference, "454");
            File metadata = new File()
                    .setName(fileName);

            File googleFile=null;
            try {
                List<String> list = MainActivity.dbFlashcard.GetAllFlashcards();
                String wholeString="";
                for (String line : list) {
                    wholeString+=line;
                }
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain",wholeString);
                googleFile = driveService.files().update(fileId,metadata,contentStream).execute();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });

    }


    public Task<String> ReadFlashcards()
    {
        return Tasks.call(mExecutor,() -> {

            String fileId = sharedPreferences.getString(datebaseFileIdPreference, "454");

            InputStream inpout = driveService.files().get(fileId).executeMediaAsInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inpout));

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
                System.out.println(line);
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
            inpout.close();
            MainActivity.dbFlashcard.AddFlashcardsFromQueue(engWord, plWord, engSentence, plSentence);
            MainActivity.dbFlashcard.AddFlashcardsToCategoryFromQueue(idFlashcardQueue, flashcardIsKnownQueue, category);


            return "dfd";
        });

    }


}
