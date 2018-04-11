package com.example.abhishek.workoutdiary;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Abhishek on 05/12/17.
 */

public class cardioExercise {
    private String name;//name of the cardio exercise
    private List<Dates>dates;//dates for the cardio exercise along with the respective values

    class Dates{
        private String date;//date text
        private List<Values>values;// workout values
        private double total;//workout value for graph
        private float total_cals;//total calories expended for that day
        class Values{
            private String distance,time,speed,calories,incline;//all the different attributes for a cardio exercise
            private long id;//database entry id
            Values(String distance,String time,String speed,String calories,String incline,long id){
                this.distance=distance;
                this.time=time;
                this.speed=speed;
                this.calories=calories;
                this.incline=incline;
                this.id=id;
            }
            //getter functions
            long getId(){return this.id;}
            String getDistance(){return this.distance;}
            String getTime(){return this.time;}
            String getSpeed(){return this.speed;}
            String getCalories(){return this.calories;}
            String getIncline(){return this.incline;}
            //setter functions
            void set(String distance,String time,String speed,String calories,String incline)
            {
                this.distance=distance;
                this.time=time;
                this.speed=speed;
                this.calories=calories;
                this.incline=incline;
            }
        }
        Dates(String date){
            this.date=date;
            this.values=new ArrayList<>();
            this.total=0;
            this.total_cals=0;
        }
        //adds a new cardio exercise value
        void add(String distance,String time,String speed,String calories,String incline,long id){
            Values v=new Values(distance,time,speed,calories,incline,id);
            this.values.add(v);
            total=Double.parseDouble(distance)+total;
            this.total_cals+=(Float.parseFloat(calories));
        }
        //sets the value for updating rather than adding a new value
        void set(String distance,String time,String speed,String calories,String incline,long id){
            Values v=new Values(distance,time,speed,calories,incline,id);
            for(Values values:this.values){
                if(values.getId()==v.getId()){
                    values.set(v.distance,v.time,v.speed,v.calories,v.incline);
                    break;
                }
            }
        }
        //getter methods
        double getTotal(){return this.total;}
        float getTotal_cals(){return this.total_cals;}
        String getDate(){return this.date;}
        List<Values> getValues(){return this.values;}

        @Override
        public boolean equals(Object obj) {
            if(obj!=null&&obj.getClass().equals(this.getClass())){
                Dates d=(Dates)obj;
                if(this.getDate().equalsIgnoreCase(d.getDate()))
                    return true;
            }
            return false;
        }
    }
    cardioExercise(String name){
        this.name=name;
        this.dates=new ArrayList<>();
    }
    //getter methods
    List<Dates> getDates(){return this.dates;}
    String getName(){return this.name;}
    //get index of a particular date if present
    int indexOf(String date){
        for(int i=0;i<dates.size();i++){
            if(dates.get(i).getDate().equalsIgnoreCase(date))
                return i;
        }
        return -1;
    }
    //setter methods
    void setDate(String date,String distance,String time,String speed,String calories,String incline,long id){
        Dates d=new Dates(date);
        if(dates.contains(d)){
            int pos=dates.indexOf(d);
            dates.get(pos).set(distance,time,speed,calories,incline,id);
        }
    }
    //adds a new cardio exercise value
    void addDate(String date,String distance,String time,String speed,String calories,String incline,long id){
        Dates d=new Dates(date);
        if(dates.contains(d)){
            int pos=indexOf(date);
            dates.get(pos).add(distance,time,speed,calories,incline,id);
        }
        else{
            d.add(distance,time,speed,calories,incline,id);
            dates.add(d);
            sort();
        }
    }
    //adds a date text to the list of dates
    int addDate(String date){
        Dates temp=new Dates(date);
        if(!dates.contains(temp)){
            dates.add(temp);
            sort();
            return 1;
        }
        return -1;
    }
    String getDate(int position){return dates.get(position).getDate();}
    //sorts the dates in reverse chronological order
    private void sort(){
        Collections.sort(dates, new Comparator<Dates>() {
            @Override
            public int compare(Dates o1, Dates o2) {
                String d1=o1.getDate(),d2=o2.getDate();
                DateFormat df=DateFormat.getDateInstance(DateFormat.LONG);
                try{
                    Date date1=df.parse(d1),date2=df.parse(d2);
                    if(date1.after(date2))return -1;
                    else return 1;
                }catch (ParseException P){
                    return 0;
                }
            }
        });
    }

}
