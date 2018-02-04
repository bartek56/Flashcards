package com.example.bartosz.fiszki;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;

import static android.app.PendingIntent.getActivity;
import static com.example.bartosz.fiszki.MainActivity.sharedPreferences;

public class AddWord extends AppCompatActivity {

    private EditText etEnglishWord;
    private EditText etPolishWord;
    private EditText etEnglishSentence;
    private EditText etPolishSentence;
    private Spinner sCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sCategory = (Spinner) findViewById(R.id.sCategory);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MainActivity.dbCategory.GetCategoriesList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(dataAdapter);

        int position = dataAdapter.getPosition(MainActivity.GetActualCategory());
        sCategory.setSelection(position);

        etEnglishWord = (EditText) findViewById(R.id.etEnglishWord);
        etEnglishSentence = (EditText) findViewById(R.id.etEnglishSentence);
        etPolishWord = (EditText) findViewById(R.id.etPolishWord);
        etPolishSentence = (EditText) findViewById(R.id.etPolishSentence);
    }

    public void AddWordButtonOnClick(View view) throws InterruptedException {

        Flashcard flashcard = new Flashcard(3,etEnglishWord.getText().toString(),etPolishWord.getText().toString(),etEnglishSentence.getText().toString(),etPolishSentence.getText().toString());
        MainActivity.dbFlashcard.AddFlashcard(flashcard);

        int id = MainActivity.dbFlashcard.GetIdEnglishWord(flashcard.getEngWord());

        MainActivity.dbCategory.AddFlashcardToCategory(sCategory.getSelectedItem().toString(),id);
        Toast.makeText(getApplicationContext(), "Dodano fiszkę", Toast.LENGTH_SHORT).show();

        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


}
