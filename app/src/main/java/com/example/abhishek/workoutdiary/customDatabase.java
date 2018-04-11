package com.example.abhishek.workoutdiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Abhishek on 04/12/17.
 */

public class customDatabase extends SQLiteOpenHelper {
    private static String Database_name="Exercisesdb";
    private static String Table_name="Exercises";
    private static String col1="_id";
    private static String col2="Exercise";
    private static String col3="Date";
    private static String col4="Weight";
    private static String col5="Reps";

    public customDatabase(Context context) {
        super(context, Database_name, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists "+Table_name+" ( "
        +col1+" integer primary key autoincrement, "
        +col2+" text not null, "
        +col3+" text not null, "
        +col4+" real not null, "
        +col5+" real not null )");
    }
    //method to delete the table
    void once(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("drop table "+Table_name);
        this.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+Table_name);
        onCreate(db);
    }
    //insert the exercise into the database
    //input: exercise name, date, weight lifted, repetitions of the exercise
    //output: the id of the database entry to be stored in the exercise object
    public long insert(String exercise,String date,String weight,String reps){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(col2,exercise);cv.put(col3,date);cv.put(col5,reps);
        cv.put(col4,weight);
        long id=db.insert(Table_name,null,cv);
        db.close();
        return id;
    }
    /*
    deletes the database entry
    input: id of the database entry
    output: if 1 or more rows have been deleted
     */
    public boolean delete(long id){
        SQLiteDatabase db=this.getWritableDatabase();
        int rows=db.delete(Table_name,col1+" = "+id,null);
        db.close();
        return (rows>0);
    }
    /*
    updates the values of a database entry
    input: id of the database entry, new weight value, new repetition value
     */
    boolean update(long id,String weight,String reps){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        double w=Double.parseDouble(weight);
        if((int)w==w){
            weight=(int)w+"";
        }
        cv.put(col4,weight);cv.put(col5,reps);
        int rows=db.update(Table_name,cv,col1+" = "+id,null);
        return rows==1;
    }
    /*
    method to get a list of exercise names
     */
    public List<String> getExercises(){
        List<String>exercises=new ArrayList<>();
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("Select * from "+Table_name,null);
        while(cr.moveToNext()){
            String exercise=cr.getString(1);
            if(!exercises.contains(exercise))
                exercises.add(exercise);
        }
        Collections.sort(exercises);
        cr.close();db.close();
        return exercises;
    }
    /*
    method to get an exercise object containing all the values in the database for that exercise
    input: the exercise name
    output: the exercise object with all the database values corresponding to the exercise name
     */
    Exercise getExerciseValues(String exercise){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+Table_name+" where "+col2+" = '"+exercise+"'",null);
        Exercise e=new Exercise(exercise);
        while(cr.moveToNext()){
            long id=cr.getLong(0);
            String date=cr.getString(2);
            String weight=cr.getString(3);
            String reps=cr.getString(4);
            e.addDate(date,weight,reps,id);
        }
        cr.close();db.close();
        return e;
    }

}
