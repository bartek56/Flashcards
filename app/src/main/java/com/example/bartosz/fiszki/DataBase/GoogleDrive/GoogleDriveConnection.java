package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

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

    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private String databaseFileId;
    public static Drive driveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();


    public void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
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
                                    activity, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Flashcards")
                                    .build();

                    driveService = googleDriveService;
                    checkIfFileExist(fileName);

                }).addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }



    private void checkIfFileExist(String fileName) {

            Log.d(TAG, "Querying for files.");

            queryFiles()
                    .addOnSuccessListener(fileList -> {
                        for (File file : fileList.getFiles()) {
                            System.out.println(file.getName());
                            if(file.getName().equals(fileName))
                            {
                                databaseFileId = file.getId();
                                Log.d(TAG, "file exist "+databaseFileId);
                                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                                preferencesEditor.putString(MainActivity.datebaseFileIdPreference,databaseFileId);
                                preferencesEditor.commit();
                                break;
                            }
                        }

                        if(databaseFileId==null)
                        {
                            Log.d(TAG, "file doesn't exist");
                            createFileHelper(fileName);
                        }
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));

    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                driveService.files().list().setSpaces("drive").execute());
    }

    public void createFileHelper(String fileName) {

            Log.d(TAG, "Creating a file.");

            createFile(fileName)
                    .addOnSuccessListener(fileId -> Log.d(TAG, fileId))
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't create file.", exception));

    }


    public Task<String> createFile(String fileName) {
        return Tasks.call(mExecutor,() -> {

            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName(fileName);

            File googleFile=null;
            try {
                String content = "a";
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
                googleFile = driveService.files().create(metadata, contentStream).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

}
