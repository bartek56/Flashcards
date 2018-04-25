package com.example.bartosz.fiszki.DataBase.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.bartosz.fiszki.DataBase.SQLite.Tables.Flashcard;
import com.example.bartosz.fiszki.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by bartosz on 12.03.17.
 */

public class FlashcardHelper extends Database {

    private Context context;
    public static String FILENAME=null;

/*
    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        Flashcard flashcard = null;
        switch (MainActivity.actualLanguageDataBase)
        {
            case MainActivity.engLanguageDatabase: flashcard = new Flashcard(1, "Hello", "Cześć", "", "");break;
            case MainActivity.deLanguageDatabase: flashcard = new Flashcard(1,"Hallo", "Cześć", "",""); break;
            case MainActivity.frLanguageDatabase: flashcard = new Flashcard(1,"Salut!", "Cześć", "",""); break;
        }
        AddFlashcard(flashcard);
        int id = GetIdEnglishWord(flashcard.getEngWord());
        AddFlashcardToCategory("inne",id);

    }
*/
    public FlashcardHelper(Context context, String DatabaseFileName) {
        super(context,DatabaseFileName);
        FILENAME = DatabaseFileName;
        this.context = context;
    }

    public void AddFlashcardsFromQueue(Queue<String> engWord, Queue<String> plWord, Queue<String> engSentence, Queue<String> plSentence) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String sql = "INSERT INTO flashcard (engWord, plWord, engSentence, plSentence) VALUES (?, ?, ?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);

        while (!engWord.isEmpty())
        {
            stmt.bindString(1, engWord.remove());
            stmt.bindString(2, plWord.remove());
            stmt.bindString(3, engSentence.remove());
            stmt.bindString(4, plSentence.remove());
            stmt.execute();
            stmt.clearBindings();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }


    public Flashcard GetFlashcard(int id){
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
        db.close();
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
            db.close();
            return value;
        }
        db.close();
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
        db.close();
        return flashcard;

    }

    public void SetFlashcard(Flashcard flashcard){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("engWord",flashcard.getEngWord());
        values.put("plWord",flashcard.getPlWord());
        values.put("engSentence",flashcard.getEngSentence());
        values.put("plSentence",flashcard.getPlSentence());
        db.update("flashcard",values,"id="+flashcard.getId(),null);
        db.close();
    }

    public int AddFlashcardIfNotExist(Flashcard flashcard){
        if(GetIdEnglishWord(flashcard.getEngWord())==0)
        {
            AddFlashcard(flashcard);

            int id =GetIdEnglishWord(flashcard.getEngWord());

            return id;
        }
        return 0;
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
        db.close();
        return flashcard;
    }


    public List<String> GetAllFlashcards(){
        List<String> list = new ArrayList<>();

        List<String> categoryList = GetCategoriesListWithoutSign();

        SQLiteDatabase db = getReadableDatabase();
        //db.beginTransaction();

        for (String category: categoryList)
        {
            //String tableName = category.replace(" ","_");
            db = getReadableDatabase();
            String[] columns = {"idFlashcard", "known"};
            Cursor cursor;

            cursor = db.query(category, columns,null, null, null, null, null, null);

            if (cursor.getCount()>0)
            {
                while(cursor.moveToNext())
                {
                    //idKnownWords.add(cursor.getInt(0));
                    Flashcard flashcard = GetFlashcard(cursor.getInt(0));
                    String flachcard = category + "~" +
                            flashcard.getEngWord() + "~"+
                            flashcard.getPlWord() + "~"+
                            flashcard.getEngSentence() + "~"+
                            flashcard.getPlSentence() + "~"+
                            cursor.getInt(1) + "~" +"\n";
                    list.add(flachcard);
                }
            }

        }
        db.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();


        return list;
    }

    public List<String> GetCategoriesListWithoutSign() {
        List<String> categoryList = new ArrayList<String>();

        String[] columns = {"idFlashcard"};
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        while(cursor.moveToNext())
        {
            if(!cursor.getString(0).equals("android_metadata") && !cursor.getString(0).equals("sqlite_sequence") && !cursor.getString(0).equals("flashcard") ) {
                categoryList.add(cursor.getString(0));
            }

        }
        db.close();

        return categoryList;
    }

    public void DeleteFlashcard(int id){
        String args[] = {id + ""};
        SQLiteDatabase db = getWritableDatabase();
        db.delete("flashcard","id=?",args);
        db.close();
    }

    public void DeleteAllFlashcards(){
        // brak pętli która zwraca nazwy tabel
        /*
        SQLiteDatabase db = getWritableDatabase();
        db.delete("flashcard",null,null);
        SQLiteDatabase db2 = getWritableDatabase();
        db2.delete("inne",null,null);
        */
        context.deleteDatabase(FILENAME);
        //SQLiteDatabase db = getWritableDatabase();
        //onCreate(db);


    }


    public void AddFlashcard(Flashcard flashcard){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("engWord",flashcard.getEngWord());
        values.put("plWord",flashcard.getPlWord());
        values.put("engSentence",flashcard.getEngSentence());
        values.put("plSentence",flashcard.getPlSentence());
        db.insertOrThrow("flashcard",null,values);
        db.close();
    }

    public int GetIdEnglishWord(String enWord) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id FROM flashcard WHERE engWord='"+enWord+"'", null);

        if (cursor.getCount()>0)
        {
            cursor.moveToFirst();
            db.close();
            return (int)cursor.getLong(0);
        }
        db.close();
        return 0;
    }

    public void SetKnownWord(int id,boolean known){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("known",known);
        String actualCategory = MainActivity.GetActualCategory().replace(" ","_");
        db.update(actualCategory,values,"idFlashcard="+id,null);
        db.close();

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

        int count = cursor.getCount();
        db.close();

        return count;
    }

    public List<Integer> IdFlashcard(String category, boolean allWords)    {
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
            db.close();
            return idKnownWords;
        }
        db.close();
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
        db.close();
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
        db.close();
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
        db.close();
        return categoryList;
    }

    public void DeleteCategory(String category) {
        String tableName = category.replace(" ", "_");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+tableName+"");
        db.close();
    }

    public void AddFlashcardToCategory(String category, int id) {
        String tableName = category.replace(" ","_");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idFlashcard",id);
        values.put("known",0);
        db.insertOrThrow(tableName,null,values);
        db.close();
    }

    public void DeleteFlashcardFromCategory(String category, int id)    {
        String tableName = category.replace(" ","_");
        String args[] = {id + ""};
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName,"idFlashcard=?",args);
        db.close();
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
        db.close();
    }


    /*
    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        Flashcard flashcard;
        switch (MainActivity.actualLanguageDataBase)
        {
            case MainActivity.engLanguageDatabase: flashcard = new Flashcard(1,"Hello", "Cześć", "",""); AddFlashcard(flashcard);break;
            case MainActivity.deLanguageDatabase: flashcard = new Flashcard(1,"Hallo", "Cześć", "",""); AddFlashcard(flashcard);break;
            case MainActivity.frLanguageDatabase: flashcard = new Flashcard(1,"Salut!", "Cześć", "",""); AddFlashcard(flashcard);break;
        }
    }
    */
}
