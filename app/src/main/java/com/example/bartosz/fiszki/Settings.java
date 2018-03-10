package com.example.bartosz.fiszki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

public class Settings extends AppCompatActivity {

    private Spinner sCategory;
    //private Spinner sLanguage;
    private CheckBox cbRandom;
    private CheckBox cbShowKnownWords;
    private RadioGroup rbgLanguageGroup;
    private RadioButton rbChoosen;
    private RadioButton rbEngPl;
    private RadioButton rbPlEng;
    private static String actualCategory = "inne";
    private static boolean showKnownWords;
    private static boolean random = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbRandom = (CheckBox)  findViewById(R.id.cbRandom);
        cbShowKnownWords = (CheckBox)  findViewById(R.id.cbShowKnownWord);
        rbgLanguageGroup = (RadioGroup)  findViewById(R.id.rbLanguageGroup);
        rbEngPl = (RadioButton)findViewById(R.id.rbEngPl);
        rbPlEng = (RadioButton)findViewById(R.id.rbPlEng);
        cbRandom.setChecked(sharedPreferences.getBoolean(MainActivity.randomWordPreference,false));
        cbShowKnownWords.setChecked(sharedPreferences.getBoolean(MainActivity.showKnownWordsPreference,true));
        sCategory = (Spinner) findViewById(R.id.sCategory);
        ArrayAdapter<String> categoryDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        //categoryDataAdapter.add("Wszystkie");
        categoryDataAdapter.addAll(MainActivity.dbFlashcard.GetCategoriesList());
        categoryDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(categoryDataAdapter);

        int categoryPosition = categoryDataAdapter.getPosition(MainActivity.GetActualCategory());
        sCategory.setSelection(categoryPosition);

        String languageMode = sharedPreferences.getString(MainActivity.languageModePreference,MainActivity.languageModeEngPl);


        if(languageMode.equals(MainActivity.languageModeEngPl))
            rbEngPl.setChecked(true);
        else
            rbPlEng.setChecked(true);
    }


    public void EditWordButtonOnClick(View view) {
        actualCategory = sCategory.getSelectedItem().toString();


        random = cbRandom.isChecked();
        showKnownWords=cbShowKnownWords.isChecked();

        int selectedId=rbgLanguageGroup.getCheckedRadioButtonId();
        rbChoosen = (RadioButton) findViewById(selectedId);

        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putBoolean(MainActivity.showKnownWordsPreference,showKnownWords);
        preferencesEditor.putBoolean(MainActivity.randomWordPreference,random);

        preferencesEditor.putString(MainActivity.languageModePreference,rbChoosen.getText().toString());

        switch (sharedPreferences.getString(MainActivity.languagePreference, MainActivity.engLanguageDatabase))
        {
            case MainActivity.engLanguageDatabase: preferencesEditor.putString(MainActivity.actualCategoryEngPreference,actualCategory); break;
            case MainActivity.frLanguageDatabase: preferencesEditor.putString(MainActivity.actualCategoryFrPreference,actualCategory); break;
            case MainActivity.deLanguageDatabase: preferencesEditor.putString(MainActivity.actualCategoryDePreference,actualCategory); break;
        }


        preferencesEditor.commit();
        Toast.makeText(getApplicationContext(), "Ustawienia zostały zmienione", Toast.LENGTH_SHORT).show();
        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}
