package com.example.bartosz.fiszki.DataBase.SQLite;

import java.util.Random;

/**
 * Created by bartosz on 02.03.17.
 */

public class RandomNumber {

    public static int[] RandomNoRepeat(int size)
    {
        boolean[] repeating = new boolean[size];
        int randomNumber;

        int [] randomNumbersTable = new int[size];

        Random random = new Random();

        for(int i=0; i<size;i++)
        {
            randomNumber = (random.nextInt(size));
            while (repeating[randomNumber]) {
                randomNumber = random.nextInt(size);
            }
            randomNumbersTable[i]=randomNumber;
            repeating[randomNumber]=true;
        }

        return randomNumbersTable;
    }
}
