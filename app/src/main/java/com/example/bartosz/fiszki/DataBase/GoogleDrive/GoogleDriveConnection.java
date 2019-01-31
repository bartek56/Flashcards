package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.util.Date;

import static com.example.bartosz.fiszki.MainActivity.activity;

/**
 * Created by Bartek on 2018-03-10.
 */

public class GoogleDriveConnection implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final int NEXT_AVAILABLE_REQUEST_CODE = 2;

    protected GoogleApiClient mGoogleApiClient;
    protected String fileName;
    protected Date modificationDate;
    protected DriveId driveId;
    protected DriveFile driveFile;
    protected Context context;


    public GoogleDriveConnection(Context context, String fileName) {
        this.fileName = fileName;
        this.context = context;

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
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("connection suspend");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("connection failed: " + connectionResult.getErrorMessage() + " " +connectionResult.getErrorCode());

        if (!connectionResult.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, connectionResult.getErrorCode(), 0).show();
            System.err.println("connection suspend "+ connectionResult.getErrorMessage() + " " +connectionResult.getErrorCode());
            return;
        }
        try {
            connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            Toast.makeText(activity, "Wybierz konto i spróbuj ponownie", Toast.LENGTH_LONG).show();
        } catch (IntentSender.SendIntentException ex) {

            Log.e("INFO", "Exception while starting resolution activity", ex);
        }
    }


    protected Query QuerySearchFile()
    {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .build();

        return query;
    }

}
