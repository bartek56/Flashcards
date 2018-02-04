package com.example.bartosz.fiszki;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EditCategories extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner spinner = (Spinner) findViewById(R.id.sCategory);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MainActivity.dbCategory.GetCategoriesListWithoutOther());

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
    }

    public void DeleteCategoryButtonOnClick(View view) {
        Spinner sCategory = (Spinner) findViewById(R.id.sCategory);

        MainActivity.dbCategory.DeleteCategory(sCategory.getSelectedItem().toString());
        Toast.makeText(getApplicationContext(), "Usunięto kategorie", Toast.LENGTH_SHORT).show();
        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void AddCategoryButtonOnClick(View view) {
        EditText newCategory = (EditText) findViewById(R.id.etCategory);
        String category = newCategory.getText().toString();
        MainActivity.dbCategory.CreateCategory(category);

        Toast.makeText(getApplicationContext(), "Dodano kategorie", Toast.LENGTH_SHORT).show();

        SharedPreferences.Editor preferencesEditor = MainActivity.sharedPreferences.edit();

        preferencesEditor.putString(MainActivity.actualCategoryEngPreference,category);

        preferencesEditor.commit();

        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }
}
