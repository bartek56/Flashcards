package com.example.bartosz.fiszki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveConnection;
import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;
import com.example.bartosz.fiszki.DataBase.SQLite.RandomNumber;

import java.util.ArrayList;
import java.util.List;

import static com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveConnection.REQUEST_CODE_OPEN_DOCUMENT;
import static com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveConnection.REQUEST_CODE_SIGN_IN;

public class MainActivity extends AppCompatActivity
{
    public static FlashcardHelper dbFlashcard;
    public static SharedPreferences sharedPreferences;
    private static final String TAG = "Main_Activity";
    private static String actualCategory;
    public static final String actualCategoryEngPreference = "actualCategoryEng";
    public static final String actualCategoryFrPreference = "actualCategoryFr";
    public static final String actualCategoryDePreference = "actualCategoryDe";
    public static final String randomWordPreference = "randomWord";
    public static final String showKnownWordsPreference = "showKnownWords";
    public static final String actualNumberPreference = "actualNumber";
    public static final String languageModePreference = "languageMode";
    public static final String languagePreference = "languagePreference";
    public static final String engLanguageDatabase = "flashcardsEng.db";
    public static final String deLanguageDatabase = "flashcardsDe.db";
    public static final String frLanguageDatabase = "flashcardsFr.db";
    public static final String engLanguageCsvFile = "flashcardsEng.txt";
    public static final String deLanguageCsvFile = "flashcardsDe.txt";
    public static final String frLanguageCsvFile = "flashcardsFr.txt";
    public static final String languageModeEngPl = "Eng-Pl";
    public static final String languageModePlEng = "Pl-Eng";
    public static final String dateModificationPreference = "dateModification";
    public static final String datebaseFileIdPreference = "datavaseFileId";
    public static final String datebaseFolderIdPreference = "databaseFolderId";
    public static Activity activity;
    public static String actualLanguageDataBase;

    private static int randomNumbers[];
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private static MenuItem itemSetting;
    private static int countFlashcards1;
    private static List<Integer> idKnownWords = new ArrayList<>();
    private android.os.Handler handler;
    private GoogleDriveConnection googleDriveConnection;
    private static int countChanges;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    googleDriveConnection.handleSignInResult(resultData,getActualCsvFile());
                }
                break;

            case REQUEST_CODE_OPEN_DOCUMENT:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        //openFileFromFilePicker(uri);
                        Log.d(TAG,"Open Document");
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(actualCategoryEngPreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(actualCategoryDePreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(actualCategoryFrPreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(randomWordPreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(showKnownWordsPreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(actualNumberPreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(languageModePreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(languagePreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(dateModificationPreference, Activity.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(datebaseFileIdPreference, Activity.MODE_PRIVATE);

        actualLanguageDataBase = sharedPreferences.getString(languagePreference, engLanguageDatabase);

        dbFlashcard = new FlashcardHelper(this, actualLanguageDataBase);

        actualCategory = GetActualCategory();

        switch (actualLanguageDataBase)
        {
            case engLanguageDatabase: setTitle("Flashcards English "+actualCategory); break;
            case frLanguageDatabase: setTitle("Flashcards French "+actualCategory); break;
            case deLanguageDatabase: setTitle("Flashcards German "+actualCategory); break;
        }

        countFlashcards1 = dbFlashcard.CountFlashcard(actualCategory,sharedPreferences.getBoolean(showKnownWordsPreference,true));
        if (countFlashcards1 == 0)
        {
            countFlashcards1 = 1;
            Flashcard flashcard = null;


            if(actualLanguageDataBase==engLanguageDatabase)
            {
                flashcard = new Flashcard(1,"Hello", "Cześć","Hello, this is the Flashcard application" ,"Cześć, to jest aplikacja Fiszki");
            }
            else if(actualLanguageDataBase==deLanguageDatabase)
            {
                flashcard = new Flashcard(1,"Hallo", "Cześć","Hallo, das ist die Fiszki-Anwendung", "Cześć, to jest aplikacja Fiszki");
            }
            else if(actualLanguageDataBase==frLanguageDatabase)
            {
                flashcard = new Flashcard(1,"Bonjour", "Cześć","Bonjour, voici l'application Fiszki" ,"Cześć, to jest aplikacja Fiszki");
            }


            MainActivity.dbFlashcard.AddFlashcard(flashcard);
            int id = MainActivity.dbFlashcard.GetIdEnglishWord(flashcard.getEngWord());
            MainActivity.dbFlashcard.AddFlashcardToCategory(actualCategory,id);
        }

        randomNumbers = new int[countFlashcards1];

        if(sharedPreferences.getBoolean(randomWordPreference,false))
            randomNumbers = RandomNumber.RandomNoRepeat(countFlashcards1);
        else
        {
            for(int i=0;i<countFlashcards1;i++)
            {
                randomNumbers[i]=i;
            }
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        idKnownWords = dbFlashcard.IdFlashcard(actualCategory,sharedPreferences.getBoolean(showKnownWordsPreference, false));
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        activity = this;


        class handler2 extends Handler{
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


                if(msg.what==1)
                {
                    GoogleDriveHelper googleDriveHelper = new GoogleDriveHelper(GoogleDriveConnection.driveService);
                    googleDriveHelper.setHandler(handler);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);
                    builder.setTitle("WARNING");
                    builder.setMessage("Słówka zostały edytowane przez inną aplikacje. Czy chcesz zaktualizować dane ?");
                    googleDriveHelper.RemoveFirstLineInFile(getActualCsvFile());
                    builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dbFlashcard.DeleteAllFlashcards();
                            dbFlashcard = new FlashcardHelper(activity,actualLanguageDataBase);

                            googleDriveHelper.ReadFlashcardsHelper(getActualCsvFile());
                        }
                    });

                    builder.setNegativeButton("NIE", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
                else if(msg.what==2)
                {
                    Update();
                }
            }
        }

        googleDriveConnection = new GoogleDriveConnection();
        handler = new handler2();
        googleDriveConnection.setHandler(handler);
        googleDriveConnection.requestSignIn();

    }

    public static String getActualCsvFile(){
        String actualCsvFile=null;
        switch (MainActivity.actualLanguageDataBase)
        {
            case engLanguageDatabase: actualCsvFile= engLanguageCsvFile; break;
            case deLanguageDatabase: actualCsvFile= deLanguageCsvFile;break;
            case frLanguageDatabase: actualCsvFile= frLanguageCsvFile;break;
        }
        return actualCsvFile;
    }

    public static String GetActualCategory() {
        String actualCategory=null;
        switch (sharedPreferences.getString(languagePreference, engLanguageDatabase))
        {
            case engLanguageDatabase: actualCategory=sharedPreferences.getString(actualCategoryEngPreference, "inne"); break;
            case frLanguageDatabase: actualCategory=sharedPreferences.getString(actualCategoryFrPreference, "inne"); break;
            case deLanguageDatabase: actualCategory=sharedPreferences.getString(actualCategoryDePreference, "inne"); break;
        }

        return actualCategory;
    }

    public static void SetActualCategory(String actualCategory) {

        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();

        switch (sharedPreferences.getString(languagePreference, engLanguageDatabase))
        {
            case engLanguageDatabase: preferencesEditor.putString(actualCategoryEngPreference,actualCategory); break;
            case frLanguageDatabase: preferencesEditor.putString(actualCategoryFrPreference,actualCategory); break;
            case deLanguageDatabase: preferencesEditor.putString(actualCategoryDePreference,actualCategory); break;
        }

        preferencesEditor.commit();

    }


    public void Update() {
        actualLanguageDataBase = sharedPreferences.getString(languagePreference, engLanguageDatabase);

        boolean showKnownWords = sharedPreferences.getBoolean(showKnownWordsPreference,true);

        dbFlashcard = new FlashcardHelper(this, actualLanguageDataBase);

        countFlashcards1 = dbFlashcard.CountFlashcard(GetActualCategory(),showKnownWords);
        if (countFlashcards1 == 0)
            countFlashcards1 = 1;
        randomNumbers = new int[countFlashcards1];

        if(sharedPreferences.getBoolean(randomWordPreference,false))
            randomNumbers = RandomNumber.RandomNoRepeat(countFlashcards1);
        else
        {
            for(int i=0;i<countFlashcards1;i++)
            {
                randomNumbers[i]=i;
            }
        }

        idKnownWords = dbFlashcard.IdFlashcard(GetActualCategory(),showKnownWords);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        googleDriveConnection = new GoogleDriveConnection();
        googleDriveConnection.requestSignIn();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e(TAG,"Resume app");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Log.e(TAG,"Stop app");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e(TAG,"Closed app");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        itemSetting = menu.findItem(R.id.action_editWord);
        itemSetting.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_addWord: {
                Intent intent = new Intent(this, AddWord.class);
                startActivity(intent);
                break;
            }
            case R.id.action_editWord: {
                Intent intent = new Intent(this, EditWord.class);
                startActivity(intent);
                break;
            }

            case R.id.action_settings: {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            }

            case R.id.action_addCategory: {
                Intent intent = new Intent(this, EditCategories.class);
                startActivity(intent);
                break;
            }

            case R.id.action_aboutMe: {
                Intent intent = new Intent(this, AboutMe.class);
                startActivity(intent);
                break;
            }

            case R.id.action_delanguage: {
                MainActivity.dbFlashcard = new FlashcardHelper(this,deLanguageDatabase);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(languagePreference,deLanguageDatabase);
                preferencesEditor.commit();
                String category=sharedPreferences.getString(actualCategoryDePreference, "inne");
                //idKnownWords = dbCategory.IdFlashcard(category,sharedPreferences.getBoolean(showKnownWordsPreference, false));
                setTitle("Flashcards German "+category);
                Update();
                break;
            }

            case R.id.action_englanguage: {
                MainActivity.dbFlashcard = new FlashcardHelper(this,engLanguageDatabase);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(languagePreference,engLanguageDatabase);
                preferencesEditor.commit();
                String category=sharedPreferences.getString(actualCategoryEngPreference, "inne");
                //idKnownWords = dbCategory.IdFlashcard(category,sharedPreferences.getBoolean(showKnownWordsPreference, false));
                setTitle("Flashcards English "+category);
                Update();
                break;
            }

            case R.id.action_frlanguage: {
                MainActivity.dbFlashcard = new FlashcardHelper(this,frLanguageDatabase);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(languagePreference,frLanguageDatabase);
                preferencesEditor.commit();
                String category=sharedPreferences.getString(actualCategoryFrPreference, "inne");
                setTitle("Flashcards French "+category);
                Update();
                break;
            }

            case R.id.action_backup: {
                Intent intent = new Intent(this, BackupActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return countFlashcards1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Word nr" + position;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);

            fragment.setArguments(args);

            return fragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final TextView tSentence1;
            final TextView tSentence2;
            final TextView tWord1;
            final TextView tWord2;
            final TextView tId;
            final Button bShow;
            final CheckBox cbKnown;

            final int actualFlashcardNumber;

            int countFlashcards;
            String languageMode = sharedPreferences.getString(languageModePreference, languageModeEngPl);

            tSentence1 = (TextView) rootView.findViewById(R.id.tSentence1);
            tSentence2 = (TextView) rootView.findViewById(R.id.tSentence2);
            tWord1 = (TextView) rootView.findViewById(R.id.tWord1);
            tWord2 = (TextView) rootView.findViewById(R.id.tWord2);
            tId = (TextView) rootView.findViewById(R.id.tId);
            bShow = (Button) rootView.findViewById(R.id.bShow);
            cbKnown = (CheckBox) rootView.findViewById(R.id.cbKnown);
            String actCategory = GetActualCategory();
            countFlashcards = dbFlashcard.CountFlashcard(actCategory,sharedPreferences.getBoolean(showKnownWordsPreference,true));

            if (countFlashcards > 0)
            {
                actualFlashcardNumber = idKnownWords.get(randomNumbers[getArguments().getInt(ARG_SECTION_NUMBER)-1]);
                Flashcard flashcard2 = dbFlashcard.GetFlashcard(actualFlashcardNumber);

                if(languageMode.equals(languageModeEngPl))
                {
                    tWord1.setText(flashcard2.getEngWord());
                    tWord2.setText(flashcard2.getPlWord());
                    tSentence1.setText(flashcard2.getEngSentence());
                    tSentence2.setText(flashcard2.getPlSentence());
                }
                else
                {
                    tWord1.setText(flashcard2.getPlWord());
                    tWord2.setText(flashcard2.getEngWord());
                    tSentence1.setText(flashcard2.getPlSentence());
                    tSentence2.setText(flashcard2.getEngSentence());
                }

                tId.setText(Integer.toString(flashcard2.getId()));


                if (dbFlashcard.GetKnow(actCategory, flashcard2.getId()))
                    cbKnown.setChecked(true);
                else
                    cbKnown.setChecked(false);

            } else {
                actualFlashcardNumber=1;
                idKnownWords=new ArrayList<>();
                idKnownWords.add(0);
                countFlashcards1 = 1;
                mSectionsPagerAdapter.notifyDataSetChanged();

                Flashcard flashcard = null;
                if(actualLanguageDataBase==engLanguageDatabase)
                {
                    flashcard = new Flashcard(1,"Hello", "Cześć","Hello, this is the Flashcard application" ,"Cześć, to jest aplikacja Fiszki");
                }
                if(actualLanguageDataBase==deLanguageDatabase)
                {
                    flashcard = new Flashcard(1,"Hallo", "Cześć","Hallo, das ist die Fiszki-Anwendung", "Cześć, to jest aplikacja Fiszki");
                }
                if(actualLanguageDataBase==frLanguageDatabase)
                {
                    flashcard = new Flashcard(1,"Bonjour", "Cześć","Bonjour, voici l'application Fiszki" ,"Cześć, to jest aplikacja Fiszki");
                }

                MainActivity.dbFlashcard.AddFlashcard(flashcard);
                int id = MainActivity.dbFlashcard.GetIdEnglishWord(flashcard.getEngWord());
                MainActivity.dbFlashcard.AddFlashcardToCategory(actualCategory,id);

                idKnownWords=new ArrayList<>();
                idKnownWords.add(0);
                countFlashcards1 = 1;
                mSectionsPagerAdapter.notifyDataSetChanged();

                Flashcard flashcard2 = dbFlashcard.GetFlashcard(actualFlashcardNumber);

                if(languageMode.equals(languageModeEngPl))
                {
                    tWord1.setText(flashcard2.getEngWord());
                    tWord2.setText(flashcard2.getPlWord());
                    tSentence1.setText(flashcard2.getEngSentence());
                    tSentence2.setText(flashcard2.getPlSentence());
                }
                else
                {
                    tWord1.setText(flashcard2.getPlWord());
                    tWord2.setText(flashcard2.getEngWord());
                    tSentence1.setText(flashcard2.getPlSentence());
                    tSentence2.setText(flashcard2.getEngSentence());
                }

                tId.setText(Integer.toString(flashcard2.getId()));


                if (dbFlashcard.GetKnow(actCategory, flashcard2.getId()))
                    cbKnown.setChecked(true);
                else
                    cbKnown.setChecked(false);

            }

            bShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (tWord2.getVisibility() == View.VISIBLE) {
                        tWord2.setVisibility(View.INVISIBLE);
                        tSentence2.setVisibility(View.INVISIBLE);
                        itemSetting.setVisible(false);
                    } else {
                        tWord2.setVisibility(View.VISIBLE);
                        tSentence2.setVisibility(View.VISIBLE);
                        itemSetting.setVisible(true);
                    }
                    SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                    preferencesEditor.putInt(actualNumberPreference,idKnownWords.get(randomNumbers[getArguments().getInt(ARG_SECTION_NUMBER)-1]));
                    preferencesEditor.commit();
                }
            });

            cbKnown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    countChanges++;
                    if(countChanges>5)
                    {
                        countChanges=0;
                        GoogleDriveHelper googleDriveHelper = new GoogleDriveHelper(GoogleDriveConnection.driveService);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);
                        builder.setTitle("GRATULACJE");
                        builder.setMessage("Gratuluje, tworzysz postępy. Czy chcesz zapisać zmiany na Google Drive ?");
                        googleDriveHelper.RemoveFirstLineInFile(getActualCsvFile());
                        builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                googleDriveHelper.SaveFlashcardsHelper(getActualCsvFile());
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("NIE", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    dbFlashcard.SetKnownWord(actualFlashcardNumber, cbKnown.isChecked());
                }
            });

            return rootView;
        }
    }
}
