package com.example.bartosz.fiszki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;

import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

public class EditWord extends AppCompatActivity {

    private EditText etEnglishWord;
    private EditText etPolishWord;
    private EditText etEnglishSentence;
    private EditText etPolishSentence;
    private CheckBox cbDeleteWord;
    private Spinner sCategory;
    private int actualId;
    private String newCategory;
    private String actualCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_word);

        actualId = sharedPreferences.getInt(MainActivity.actualNumberPreference, 1);

        Flashcard flashcard = MainActivity.dbFlashcard.GetFlashcard(actualId);


        etEnglishWord = (EditText) findViewById(R.id.etEnglishWord);
        etEnglishWord.setText(flashcard.getEngWord());

        etEnglishSentence = (EditText) findViewById(R.id.etEnglishSentence);
        etEnglishSentence.setText(flashcard.getEngSentence());

        etPolishWord = (EditText) findViewById(R.id.etPolishWord);
        etPolishWord.setText(flashcard.getPlWord());

        etPolishSentence = (EditText) findViewById(R.id.etPolishSentence);
        etPolishSentence.setText(flashcard.getPlSentence());


        cbDeleteWord = (CheckBox) findViewById(R.id.cbDeleteWord);

        sCategory = (Spinner) findViewById(R.id.sCategory);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MainActivity.dbCategory.GetCategoriesList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(dataAdapter);
        actualCategory=MainActivity.GetActualCategory();
        int position = dataAdapter.getPosition(actualCategory);
        sCategory.setSelection(position);

    }

    public void EditWordButtonOnClick(View view) {
        if(cbDeleteWord.isChecked())
        {
            MainActivity.dbCategory.DeleteFlashcardFromCategory(actualCategory, actualId);
            MainActivity.dbFlashcard.DeleteFlashcard(actualId);
            SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
            preferencesEditor.putString(MainActivity.languageModePreference,"inne");
            preferencesEditor.commit();
            Toast.makeText(getApplicationContext(), "Usunięto fiszkę", Toast.LENGTH_SHORT).show();

        }
        else {
            String enWord = etEnglishWord.getText().toString();
            String plWord = etPolishWord.getText().toString();
            String enSentence = etEnglishSentence.getText().toString();
            String plSentence = etPolishSentence.getText().toString();

            Flashcard flashcard = new Flashcard(actualId, enWord, plWord, enSentence, plSentence);
            MainActivity.dbFlashcard.SetFlashcard(flashcard);
            MainActivity.dbCategory.DeleteFlashcardFromCategory(actualCategory, actualId);

            newCategory = sCategory.getSelectedItem().toString();
            MainActivity.dbCategory.AddFlashcardToCategory(newCategory,actualId);
            Toast.makeText(getApplicationContext(), "Edytowano fiszkę", Toast.LENGTH_SHORT).show();

        }
        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
