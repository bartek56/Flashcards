package com.example.bartosz.fiszki;




import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.bartosz.fiszki.DataBase.GoogleDrive.GoogleDriveHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.CategoryHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.FlashcardHelper;
import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;
import com.example.bartosz.fiszki.DataBase.SQLite.RandomNumber;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity
{
    public static CategoryHelper dbCategory;
    public static FlashcardHelper dbFlashcard;



    public static SharedPreferences sharedPreferences;
    public static final String TAG = "Main_Activity";
    public static String actualCategory;
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
    public static final String engLanguageCsvFile = "flashcardsEng.csv";
    public static final String deLanguageCsvFile = "flashcardsDe.csv";
    public static final String frLanguageCsvFile = "flashcardsFr.csv";
    public static final String languageModeEngPl = "Eng-Pl";
    public static final String languageModePlEng = "Pl-Eng";
    public static int randomNumbers[];
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public static MenuItem itemSetting;
    private static int countFlashcards1;
    public static GoogleDriveHelper googleDriveHelper;
    public static Activity activity;
    public static String actualLanguageDataBase;
    public static List<Integer> idKnownWords = new ArrayList<>();
    private ProgressDialog progressDialog;
    private android.os.Handler handler;


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

        actualLanguageDataBase = sharedPreferences.getString(languagePreference, engLanguageDatabase);

        dbCategory = new CategoryHelper(this, actualLanguageDataBase);
        dbFlashcard = new FlashcardHelper(this, actualLanguageDataBase);

        actualCategory = GetActualCategory();

        switch (actualLanguageDataBase)
        {
            case engLanguageDatabase: setTitle("Fiszki Angielski "+actualCategory); break;
            case frLanguageDatabase: setTitle("Fiszki Francuski "+actualCategory); break;
            case deLanguageDatabase: setTitle("Fiszki Niemiecki "+actualCategory); break;
        }

        countFlashcards1 = dbCategory.CountFlashcard(actualCategory,sharedPreferences.getBoolean(showKnownWordsPreference,true));
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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        idKnownWords = dbCategory.IdFlashcard(actualCategory,sharedPreferences.getBoolean(showKnownWordsPreference, false));
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        activity = this;

        googleDriveHelper = new GoogleDriveHelper(activity);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1)
                    Update();
                progressDialog.dismiss();
            }
        };

        googleDriveHelper.setHandler(handler);

    }

    public static String getActualCsvFile()
    {
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

        dbCategory = new CategoryHelper(this, actualLanguageDataBase);
        dbFlashcard = new FlashcardHelper(this, actualLanguageDataBase);

        countFlashcards1 = dbCategory.CountFlashcard(GetActualCategory(),sharedPreferences.getBoolean(showKnownWordsPreference,true));
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

        idKnownWords = dbCategory.IdFlashcard(GetActualCategory(),sharedPreferences.getBoolean(showKnownWordsPreference, false));
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        googleDriveHelper.onResume();
        Log.e(TAG,"Resume app");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        googleDriveHelper.onStop();
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
                MainActivity.dbCategory = new CategoryHelper(this,deLanguageDatabase);
                MainActivity.dbFlashcard = new FlashcardHelper(this,deLanguageDatabase);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(languagePreference,deLanguageDatabase);
                preferencesEditor.commit();
                String category=sharedPreferences.getString(actualCategoryDePreference, "inne");

                //idKnownWords = dbCategory.IdFlashcard(category,sharedPreferences.getBoolean(showKnownWordsPreference, false));
                setTitle("Fiszki Niemiecki "+category);
                Update();

                break;
            }

            case R.id.action_englanguage: {
                MainActivity.dbCategory = new CategoryHelper(this,engLanguageDatabase);
                MainActivity.dbFlashcard = new FlashcardHelper(this,engLanguageDatabase);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(languagePreference,engLanguageDatabase);
                preferencesEditor.commit();

                String category=sharedPreferences.getString(actualCategoryEngPreference, "inne");
                //idKnownWords = dbCategory.IdFlashcard(category,sharedPreferences.getBoolean(showKnownWordsPreference, false));
                setTitle("Fiszki Angielski "+category);
                Update();
                break;
            }

            case R.id.action_frlanguage: {
                MainActivity.dbCategory = new CategoryHelper(this,frLanguageDatabase);
                MainActivity.dbFlashcard = new FlashcardHelper(this,frLanguageDatabase);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putString(languagePreference,frLanguageDatabase);
                preferencesEditor.commit();
                String category=sharedPreferences.getString(actualCategoryFrPreference, "inne");
                //idKnownWords = dbCategory.IdFlashcard(category,sharedPreferences.getBoolean(showKnownWordsPreference, false));
                setTitle("Fiszki Francuski "+category);
                Update();

                break;
            }

            case R.id.action_readDataFromGoogle: {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Czy chcesz wczytać dane z Google Drive?");

                builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog = ProgressDialog.show(MainActivity.this, "Odczyt z Google Drive", "Wczytywanie");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                googleDriveHelper.ReadDataFromGoogleDrive(getActualCsvFile());
                            }
                        }).start();

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NIE", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
            }

            case R.id.action_writeDataToGoogle: {

                /*
                List <String> allFlashcard = dbFlashcard.GetAllFlashcards();

                for (String s: allFlashcard)
                {
                    System.out.println(s);
                }
*/
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Czy chcesz zapisać dane z Google Drive?");
                builder.setPositiveButton("TAK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        progressDialog = ProgressDialog.show(MainActivity.this, "Zapis na Google Drive", "Zapisywanie");
                        new Thread(){
                            @Override
                            public void run()
                            {
                                googleDriveHelper.SaveDateOnGoogleDrive(getActualCsvFile());
                            }
                        }.start();

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NIE", null);

                AlertDialog dialog = builder.create();
                dialog.show();





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

            boolean showKnownWords = false;

            int countFlashcards;


            String languageMode;

            showKnownWords = sharedPreferences.getBoolean(showKnownWordsPreference, false);
            languageMode = sharedPreferences.getString(languageModePreference, languageModeEngPl);


            tSentence1 = (TextView) rootView.findViewById(R.id.tSentence1);
            tSentence2 = (TextView) rootView.findViewById(R.id.tSentence2);
            tWord1 = (TextView) rootView.findViewById(R.id.tWord1);
            tWord2 = (TextView) rootView.findViewById(R.id.tWord2);
            tId = (TextView) rootView.findViewById(R.id.tId);
            bShow = (Button) rootView.findViewById(R.id.bShow);
            cbKnown = (CheckBox) rootView.findViewById(R.id.cbKnown);
            String actCategory = GetActualCategory();
            countFlashcards = dbCategory.CountFlashcard(actCategory,sharedPreferences.getBoolean(showKnownWordsPreference,true));

            if (countFlashcards > 0)
            {
                //countFlashcards1=countFlashcards;
                //mSectionsPagerAdapter.notifyDataSetChanged();
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
                Toast.makeText(getActivity(), "Brak słów, dodaj nowe", Toast.LENGTH_SHORT).show();
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

                    //if(sharedPreferences.getBoolean(showKnownWordsPreference, false))
                      //  dbCategory.SetKnownWord(actualFlashcardNumber, cbKnown.isChecked());
                    //else
                    dbCategory.SetKnownWord(actualFlashcardNumber, cbKnown.isChecked());

                    /*
                    if(sharedPreferences.getBoolean(showKnownWordsPreference, false) && cbKnown.isChecked()==true)
                    {

                        idKnownWords.remove(actualFlashcardNumber);
                    }
                     else if(sharedPreferences.getBoolean(showKnownWordsPreference, false) && cbKnown.isChecked()==false)
                    {
                        idKnownWords.add(actualFlashcardNumber);
                    }
*/

                    //countFlashcards1 = dbCategory.CountFlashcard(sharedPreferences.getString(actualCategoryEngPreference, "inne"),sharedPreferences.getBoolean(showKnownWordsPreference,true));
                    ///mSectionsPagerAdapter.notifyDataSetChanged();

                }
            });

            return rootView;
        }

    }

}
