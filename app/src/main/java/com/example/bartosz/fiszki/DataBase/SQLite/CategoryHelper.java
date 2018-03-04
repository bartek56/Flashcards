package com.example.bartosz.fiszki.DataBase.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.bartosz.fiszki.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by bartosz on 12.03.17.
 */

public class CategoryHelper extends Database {

    public CategoryHelper(Context context,String DatabaseFileName) {
        super(context,DatabaseFileName);
    }

    public void SetKnownWord(int id,boolean known)
    {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("known",known);
        String actualCategory = MainActivity.GetActualCategory();

        db.update(actualCategory,values,"idFlashcard="+id,null);
    }

    public int CountFlashcard(String category, boolean showAllWord){

            Cursor cursor;
            String tableName = category.replace(" ","_");
            SQLiteDatabase db = getReadableDatabase();
            String[] columns = {"idFlashcard", "known"};
            String args[] = {0 + ""};

            if (showAllWord)
                cursor = db.query(tableName, columns, null, null, null, null, null, null);
            else
                cursor = db.query(tableName, columns, " known=?", args, null, null, null, null);

        return cursor.getCount();
    }

    public List<Integer> IdFlashcard(String category, boolean allWords)
    {


        List<Integer> idKnownWords =null;
        String tableName = category.replace(" ","_");
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"idFlashcard", "known"};
        String args[] = {0 + ""};
        Cursor cursor;


        if(allWords)
            cursor = db.query(tableName, columns,null, null, null, null, null, null);
        else
            cursor = db.query(tableName, columns, " known=?", args, null, null, null, null);

        if (cursor.getCount()>0)
        {
            idKnownWords = new ArrayList<>();


            while(cursor.moveToNext())
            {
                idKnownWords.add(cursor.getInt(0));
            }

            return idKnownWords;
        }


        return null;

    }

    public void CreateCategory(String category) {

        SQLiteDatabase db = getWritableDatabase();
        String tableName = category.replace(" ","_");

        db.execSQL(
                "create table if not exists "+tableName+"(" +
                        "id integer primary key autoincrement," +
                        "idFlashcard integer,"+
                        "known boolean);"+
                        "");

    }

    public List<String> GetCategoriesList() {
        List<String> categoryList = new ArrayList<String>();

        String[] columns = {"idFlashcard"};
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        while(cursor.moveToNext())
        {
            if(!cursor.getString(0).equals("android_metadata") && !cursor.getString(0).equals("sqlite_sequence") && !cursor.getString(0).equals("flashcard") ) {
                String category = cursor.getString(0).replace("_", " ");
                categoryList.add(category);
            }

        }

        return categoryList;
    }

    public List<String> GetCategoriesListWithoutOther() {
        List<String> categoryList = new ArrayList<String>();

        String[] columns = {"idFlashcard"};
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        while(cursor.moveToNext())
        {
            if(!cursor.getString(0).equals("android_metadata") && !cursor.getString(0).equals("sqlite_sequence")
                    && !cursor.getString(0).equals("flashcard") && !cursor.getString(0).equals("inne")){
            String category = cursor.getString(0).replace("_", " ");
            categoryList.add(category);
            }

        }

        return categoryList;
    }

    public void DeleteCategory(String category) {
        String tableName = category.replace(" ", "_");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+tableName+"");
    }

    public void AddFlashcardToCategory(String category, int id) {
        String tableName = category.replace(" ","_");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idFlashcard",id);
        values.put("known",0);
        db.insertOrThrow(tableName,null,values);
    }

    public void DeleteFlashcardFromCategory(String category, int id)
    {
        String tableName = category.replace(" ","_");
        String args[] = {id + ""};
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName,"idFlashcard=?",args);
    }

    public void AddFlashcardsToCategoryFromQueue(Queue<Integer> idFlashcardQueue,Queue<Integer> flashcardIsKnownQueue, String category) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        String sql = "INSERT INTO "+category+" (idFlashcard, known) VALUES (?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);

        while (!idFlashcardQueue.isEmpty())
        {
            stmt.bindLong(1, idFlashcardQueue.remove());
            stmt.bindLong(2, flashcardIsKnownQueue.remove());
            stmt.execute();
            stmt.clearBindings();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
