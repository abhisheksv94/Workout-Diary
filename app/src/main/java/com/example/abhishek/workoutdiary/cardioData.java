package com.example.abhishek.workoutdiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 05/12/17.
 */

public class cardioData extends SQLiteOpenHelper {
    private static String Database_name="Cardio Exercises db";
    private static String table_name="Exercises";
    private static String col1="_id";
    private static String col2="Exercise";
    private static String col3="Date";
    private static String col4="Distance";
    private static String col5="Time";
    private static String col6="Speed";
    private static String col7="Calories";
    private static String col8="Incline";
    cardioData(Context context) {
        super(context, Database_name, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists "+table_name+" ( "
                +col1+" integer primary key autoincrement, "
                +col2+" text not null, "
                +col3+" text not null, "
                +col4+" real , "
                +col5+" text, "
                +col6+" real, "
                +col7+" real, "
                +col8+" integer ) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+table_name);
        onCreate(db);
    }
    /*
    insert the given cardio exercise into the database
    input: exercise name, date, distance, time taken, speed, calories burnt, incline
    output: database row id
     */
    long insert(String exercise,String date,String distance,String time,String speed,String calories,String incline){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(col2,exercise);cv.put(col3,date);cv.put(col4,distance);cv.put(col5,time);cv.put(col6,speed);
        cv.put(col7,calories);cv.put(col8,incline);
        long id=db.insert(table_name,null,cv);
        db.close();
        return id;
    }
    //delete the database entry
    //input: id of the row to be deleted
    //output: if 1 row has been deleted or not
    boolean delete(long id){
        SQLiteDatabase db=this.getWritableDatabase();
        int c=db.delete(table_name,col1+" = "+id,null);
        db.close();
        return c==1;
    }
    /*
    updates the database entry
    input: new distance, new time taken, new speed, new calories , new incline value, id of the row to be updated in the
    database
    output:whether only 1 row has been updated or not
     */
    boolean update(String distance,String time,String speed,String calories,String incline,long id){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put(col4,distance);cv.put(col5,time);cv.put(col6,speed);
        cv.put(col7,calories);cv.put(col8,incline);
        int c=db.update(table_name,cv,col1+" = "+id,null);
        db.close();
        return c==1;
    }
    /*
    get exercise names
    output: list of exercise name
     */
    List<String> getExerciseNames(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+table_name,null);
        List<String>exerciseNames=new ArrayList<>();
        while(cr.moveToNext()){
            String e=cr.getString(1);
            if(!exerciseNames.contains(e))
                exerciseNames.add(e);
        }
        cr.close();
        db.close();
        return exerciseNames;
    }
    //gets the cardio exercise object with all its values from the database
    cardioExercise getExercise(String name){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+table_name+" where "+col2+" = '"+name+"'",null);
        cardioExercise c=new cardioExercise(name);
        while(cr.moveToNext()){
            long id=cr.getLong(0);
            String date=cr.getString(2);
            String distance=cr.getString(3);
            String time=cr.getString(4);
            String speed=cr.getString(5);
            String calories=cr.getString(6);
            String incline=cr.getString(7);
            c.addDate(date,distance,time,speed,calories,incline,id);
        }
        cr.close();
        db.close();
        return c;
    }

}
