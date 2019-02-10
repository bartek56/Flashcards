package com.example.bartosz.fiszki.DataBase.GoogleDrive;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Bartek on 2019-02-03.
 */

public class GoogleDriveHelper {

    private Drive driveService;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    public GoogleDriveHelper(Drive driveService)
    {
        this.driveService=driveService;
    }

    public Task<String> createFile() {
        return Tasks.call(() -> {
            System.out.println("created 1");

            new Thread(new Runnable() {
                public void run() {
                    File metadata = new File()
                            //.setParents(Collections.singletonList("root"))
                            //.setFileExtension("txt")
                            //.setMimeType("text/plain")
                            .setTrashed(true);
                            //.setName("Untitledfile2");

                    System.out.println("created 2");
                    try {
                        //File googleFile = driveService.files().create(metadata).execute();
                        File googleFile = driveService.files().create(metadata).execute();

                        //driveService.permissions().
                        //File googleFile = driveService.permissions().files().create(metadata).execute();



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("created 3");
/*
                    if (googleFile == null) {
                        throw new IOException("Null result when requesting file creation.");
                    }
*/
                    //return googleFile.getId();
                }
            }).start();

        return "dfgfd";
        });
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                driveService.files().list().setSpaces("drive").execute());
    }

}
