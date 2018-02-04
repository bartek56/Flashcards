package com.example.bartosz.fiszki.DataBase.SQLite.Tables;

/**
 * Created by bartosz on 26.02.17.
 */

public class Flashcard {
    private int id;
    private String engWord;
    private String plWord;
    private String engSentence;
    private String plSentence;


    public Flashcard(int id, String engWord, String plWord, String engSentence, String plSentence) {
        this.id = id;
        this.engWord = engWord;
        this.plWord = plWord;
        this.engSentence = engSentence;
        this.plSentence = plSentence;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEngWord() {
        return engWord;
    }

    public void setEngWord(String engWord) {
        this.engWord = engWord;
    }

    public String getPlWord() {
        return plWord;
    }

    public void setPlWord(String plWord) {
        this.plWord = plWord;
    }

    public String getEngSentence() {
        return engSentence;
    }

    public void setEngSentence(String engSentence) {
        this.engSentence = engSentence;
    }

    public String getPlSentence() {
        return plSentence;
    }

    public void setPlSentence(String plSentence) {
        this.plSentence = plSentence;
    }

}
