package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Collections;
import java.util.Date;

import static android.content.ContentValues.TAG;
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

import com.google.api.services.drive.DriveScopes;

/**
 * Created by Bartek on 2018-03-10.
 */

public class GoogleDriveConnection {

    private static final String TAG = "GoogleDriveConnection";

    public static final int REQUEST_CODE_SIGN_IN = 1;
    public static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private String mOpenFileId;

    public void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
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
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    //mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

                });
                //.addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

}
