package com.example.bartosz.fiszki.DataBase.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;

/**
 * Created by bartosz on 12.03.17.
 */

public class FlashcardHelper extends Database {

    private Context context;
    public static String FILENAME=null;

    public FlashcardHelper(Context context, String DatabaseFileName) {
        super(context,DatabaseFileName);
        FILENAME = DatabaseFileName;
        this.context = context;
    }

    public void AddFlashcard(Flashcard flashcard){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("engWord",flashcard.getEngWord());
        values.put("plWord",flashcard.getPlWord());
        values.put("engSentence",flashcard.getEngSentence());
        values.put("plSentence",flashcard.getPlSentence());
        db.insertOrThrow("flashcard",null,values);
    }

    public Flashcard GetFlashcard(int id)
    {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, engWord, plWord, engSentence, plSentence FROM flashcard WHERE id='"+id+"'", null);
        Flashcard flashcard=null;
        if (cursor.getCount()>0)
        {
            cursor.moveToFirst();
            int id2 = cursor.getInt(0);
            String engWord = cursor.getString(1);
            String plWord = cursor.getString(2);
            String engSentence = cursor.getString(3);
            String plSentence = cursor.getString(4);

            flashcard = new Flashcard(id2,engWord,plWord,engSentence,plSentence);

        }
        return flashcard;

    }

    public boolean GetKnow(String category, int idFlashcard) {

        String tableName = category.replace(" ","_");

        boolean value = false;
        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {"idFlashcard", "known"};
        String args[] = {idFlashcard + ""};
        Cursor cursor = db.query(tableName, columns, " idFlashcard=?", args, null, null, null, null);

        if (cursor.getCount()>0)
        {
            cursor.moveToFirst();
            value = cursor.getInt(1) >0;
            return value;
        }


        return value;

    }

    public Flashcard EditFlashcard(int id, Flashcard flashcard) {

        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"id", "engWord", "plWord", "engSentence", "plSentence"};
        String args[] = {id + ""};
        Cursor cursor = db.query("flashcard", columns, " id=?", args, null, null, null, null);

        if (cursor != null)
        {
            cursor.moveToFirst();
            flashcard.setId(cursor.getInt(0));
            flashcard.setEngWord(cursor.getString(1));
            flashcard.setPlWord(cursor.getString(2));
            flashcard.setEngSentence(cursor.getString(3));
            flashcard.setPlSentence(cursor.getString(4));
        }

        return flashcard;

    }


    public void SetFlashcard(Flashcard flashcard)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("engWord",flashcard.getEngWord());
        values.put("plWord",flashcard.getPlWord());
        values.put("engSentence",flashcard.getEngSentence());
        values.put("plSentence",flashcard.getPlSentence());
        db.update("flashcard",values,"id="+flashcard.getId(),null);
    }

    public Flashcard GetFlashcardFromCategory(String category, int id) {
        int idWord=1;
        Cursor cursor;
        String tableName = category.replace(" ","_");
        SQLiteDatabase db = getWritableDatabase();

        String[] columns = {"idFlashcard"};
        String args[] = {id + ""};
        cursor = db.query(tableName, columns, " id=?", args, null, null, null, null);
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            idWord = cursor.getInt(0);

        }

        Flashcard flashcard = GetFlashcard(idWord);

        return flashcard;
    }

    public int GetIdEnglishWord(String enWord) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id FROM flashcard WHERE engWord='"+enWord+"'", null);

        if (cursor.getCount()>0)
        {
            cursor.moveToFirst();

            return (int)cursor.getLong(0);

        }

        return 0;
    }

    public void DeleteFlashcard(int id)
    {
        String args[] = {id + ""};
        SQLiteDatabase db = getWritableDatabase();
        db.delete("flashcard","id=?",args);
    }

    public void DeleteAllFlashcards()
    {
        // brak pętli która zwraca nazwy tabel
        /*
        SQLiteDatabase db = getWritableDatabase();
        db.delete("flashcard",null,null);
        SQLiteDatabase db2 = getWritableDatabase();
        db2.delete("inne",null,null);
        */
        context.deleteDatabase(FILENAME);

    }

}