package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.bartosz.fiszki.MainActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;

import static com.example.bartosz.fiszki.MainActivity.activity;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

/**
 * Created by programmer on 2/1/19.
 */

public class GoogleDriveUpdate extends GoogleDriveConnection{

    public boolean modified = false;
    public GoogleDriveUpdate(Context context, String fileName) {
        super(context, fileName);
        isLastModification();
    }

    public void isLastModification()
    {
        Drive.DriveApi.query(mGoogleApiClient, QuerySearchFile()).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    //Log.e(TAG, "error with opening file");
                    Toast.makeText(activity, "Błąd przy połączeniu z GoogleDrive", Toast.LENGTH_LONG).show();
                    return;
                }

                for (Metadata m : result.getMetadataBuffer()) {
                    String title = m.getTitle();
                    if (title.equals(fileName)) {
                        String modificationDate = m.getModifiedDate().toString();
                        String lastModificationDate = sharedPreferences.getString(MainActivity.dateModificationPreference,"");
                        modified = !modificationDate.equals(lastModificationDate);
                        break;
                    }
                }
                mGoogleApiClient.disconnect();
                result.release();
            }
        });
    }

}
