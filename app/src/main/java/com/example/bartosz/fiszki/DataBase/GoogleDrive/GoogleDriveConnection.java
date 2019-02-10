package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.content.Intent;
import android.util.Log;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.bartosz.fiszki.MainActivity.activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

/**
 * Created by Bartek on 2018-03-10.
 */

public class GoogleDriveConnection {

    private static final String TAG = "GoogleDriveConnection";

    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private String mOpenFileId;
    private GoogleDriveHelper googleDriveHelper;
    private Drive driveService;
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

    public void handleSignInResult(Intent result) {
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

                    googleDriveHelper = new GoogleDriveHelper(googleDriveService);

                    createFile();


                }).addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    public void createFile() {
        if (googleDriveHelper != null) {
            Log.d(TAG, "Creating a file.");

            googleDriveHelper.createFile()
                    .addOnSuccessListener(fileId -> System.out.println(fileId))
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't create file.", exception));
        }
    }


    private void query() {
        if (googleDriveHelper != null) {
            Log.d(TAG, "Querying for files.");

            googleDriveHelper.queryFiles()
                    .addOnSuccessListener(fileList -> {
                        StringBuilder builder = new StringBuilder();
                        for (File file : fileList.getFiles()) {
                            builder.append(file.getName()).append("\n");
                            System.out.println(file.getName());
                        }
                        String fileNames = builder.toString();

                        System.out.println(fileNames);
                    })
                    .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));
        }
    }

}
