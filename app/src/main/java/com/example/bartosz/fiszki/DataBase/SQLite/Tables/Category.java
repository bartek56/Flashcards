package com.example.bartosz.fiszki.DataBase.SQLite.Tables;

/**
 * Created by bartosz on 26.02.17.
 */

public class Category {
    private long nr;
    private long idFlashcard;
    private int known;

    public Category(long nr, long idFlashcard,int known) {

        this.idFlashcard = idFlashcard;
        this.nr = nr;
        this.known = known;

    }

    public long getNr() {
        return nr;
    }

    public void setNr(long nr) {
        this.nr = nr;
    }

    public long getIdFlashcard() {
        return idFlashcard;
    }

    public void setIdFlashcard(long idFlashcard) {
        this.idFlashcard = idFlashcard;
    }

    public int getKnown() {
        return known;
    }

    public void setKnown(int known) {
        this.known = known;
    }
}
