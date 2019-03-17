package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.actualLanguageDataBase;
import static com.example.bartosz.fiszki.MainActivity.datebaseFileIdPreference;
import static com.example.bartosz.fiszki.MainActivity.datebaseFolderIdPreference;
import static com.example.bartosz.fiszki.MainActivity.dbFlashcard;
import static com.example.bartosz.fiszki.MainActivity.getActualCsvFile;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;
import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * Created by Bartek on 2018-03-10.
 */

public class GoogleDriveConnection {

    private static final String TAG = "GoogleDriveConnection";
    private Handler handler;

    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private String databaseFileId;
    private String databaseFolderId;
    public static Drive driveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();


    public void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestScopes(new Scope(DriveScopes.DRIVE_METADATA))
                        //.requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(activity, signInOptions);

        activity.startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);

    }

    public void handleSignInResult(Intent result, String fileName) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());
                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    activity, Collections.singleton(DriveScopes.DRIVE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Flashcards")
                                    .build();

                    driveService = googleDriveService;
                    checkIfFolderExist(fileName);

                }).addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    private void checkIfFolderExist(String fileName) {

            Log.d(TAG, "Querying for folders.");
            queryFolders()
                    .addOnSuccessListener(fileList -> {
                        for (File file : fileList.getFiles()) {
                            Log.d(TAG, "folder " +file.getName());
                            if(file.getName().equals("flashcards"))
                            {
                                databaseFolderId = file.getId();
                                //DateTime date = file.getModifiedByMeTime();
//                                System.out.println( "date: " + date.toString());
                                Log.d(TAG, "folder exist " + databaseFolderId);
                                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                                preferencesEditor.putString(MainActivity.datebaseFolderIdPreference,databaseFolderId);
                                preferencesEditor.commit();
                                checkIfFileExist(fileName);
                                break;
                            }
                        }

                        if(databaseFolderId==null)
                        {
                            Log.d(TAG, "folder doesn't exist");
                            createFolderHelper(fileName);
                        }
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));

    }

    private void checkIfFileExist(String fileName) {

        Log.d(TAG, "Querying for files");

        queryFiles()
                .addOnSuccessListener(fileList -> {
                    for (File file : fileList.getFiles()) {
                        Log.d(TAG, "file " +file.getName());
                        if(file.getName().equals(fileName))
                        {
                            databaseFileId = file.getId();
                            //DateTime date = file.getModifiedByMeTime();
//                                System.out.println( "date: " + date.toString());
                            Log.d(TAG, "file exist " + databaseFileId);
                            SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                            preferencesEditor.putString(MainActivity.datebaseFileIdPreference,databaseFileId);
                            preferencesEditor.commit();
                            //System.out.println( "date: " + date);
                            checkIfDatabaseWasModified();

                            break;
                        }
                    }

                    if(databaseFileId==null)
                    {
                        Log.d(TAG, "file doesn't exist");

                        createFile(fileName)
                                .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                                .addOnFailureListener(exception ->
                                        Log.e(TAG, "Couldn't create file.", exception));
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));

    }

    public Task<FileList> queryFolders() {
        return Tasks.call(mExecutor, () ->
                driveService.files().list().setSpaces("drive").setQ("mimeType='application/vnd.google-apps.folder'").execute());
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                driveService.files().list().setSpaces("drive").execute());
    }

    public void updateId(String folderId)
    {
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(MainActivity.datebaseFileIdPreference,folderId);
        preferencesEditor.commit();
    }

    public void createFolderHelper(String fileName) {

            createFolder("flashcards")
                    .addOnSuccessListener(
                            folderId -> createFile(fileName)
                                .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                                .addOnFailureListener(exception ->
                                Log.e(TAG, "Couldn't create file.", exception)))
                    .addOnFailureListener(exception -> Log.e(TAG, "Couldn't create folder.", exception));
    }

    public Task<String> createFolder(String fileName) {

        Log.d(TAG, "Creating a folder.");
        return Tasks.call(mExecutor,() -> {

            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(fileName);

            File googleFile=null;
            try {
                //String content = "a";
                //ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

                googleFile = driveService.files().create(metadata).execute();

                Log.d(TAG, "Created folder id: " +googleFile.getId());

                databaseFolderId = googleFile.getId();
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(MainActivity.datebaseFolderIdPreference,googleFile.getId());
                preferencesEditor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<String> createFile(String fileName) {

        Log.d(TAG, "Creating a file.");

        return Tasks.call(mExecutor,() -> {

            File metadata = new File()
                    .setParents(Collections.singletonList(databaseFolderId))
                    .setMimeType("text/plain")
                    .setName(fileName);

            File googleFile=null;
            try {

                List<String> list = MainActivity.dbFlashcard.GetAllFlashcards();
                String wholeString="";
                for (String line : list) {
                    wholeString+=line;
                }
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain",wholeString);

                googleFile = driveService.files().create(metadata, contentStream).execute();
                Log.d(TAG, "Created file, id: " +googleFile.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<String> checkIfDatabaseWasModified()
    {
        return Tasks.call(mExecutor,() -> {

            String fileId = sharedPreferences.getString(datebaseFileIdPreference, "454");
            InputStream inpout = driveService.files().get(fileId).executeMediaAsInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inpout));

            String line;

            line = reader.readLine();
            if(line.contains("update"))
            {
                System.out.println("here");
                //Toast.makeText(activity, "here", Toast.LENGTH_LONG).show();

                GoogleDriveHelper googleDriveHelper = new GoogleDriveHelper(GoogleDriveConnection.driveService);

                handler.sendEmptyMessage(1);

            }

            reader.close();
            inpout.close();

            return "dfd";
        });

    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

}
