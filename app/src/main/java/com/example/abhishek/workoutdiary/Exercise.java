package com.example.abhishek.workoutdiary;



import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Abhishek on 04/12/17.
 */

public class Exercise {
    //class to store values of a particular exercise
    private String name;//exercise name
    private List<Dates>dates;//dates of the exercise
    class Dates{
        //class to store the dates of the exercise as well as the weights and the reps
        private String date;
        private List<Values>values;//list of values which contain the weights, reps and ids of the database entry
        private double total;//workout value for the progress graph
        class Values{
            private String weight,reps;
            private long id;
            Values(String weight,String reps,long id){
                this.weight=weight;
                this.reps=reps;
                this.id=id;
            }
            //getter methods
            long getId(){
                return this.id;
            }
            String getWeight(){
                return this.weight;
            }
            String getReps(){
                return this.reps;
            }
            //setter methods
            void set(String weight,String reps){
                this.weight=weight;
                this.reps=reps;
            }
        }
        Dates(String date){
            this.date=date;
            values=new ArrayList<>();
            total=0;
        }
        //add a new value to the values list
        //input: weight value, repetitions, database entry id
        public void add(String weight,String reps,long id){
            Values values=new Values(weight,reps,id);
            this.values.add(values);
            double w=Double.parseDouble(weight);double r=Double.parseDouble(reps);
            total+=(w*r);
        }
        //getter functions
        double getTotal(){return this.total;}
        public List<Values> getValues(){
            return this.values;
        }
        public long getID(int position){
            Values v=values.get(position);
            return v.getId();
        }
        public String getDate(){
            return date;
        }
        //setter function to set the value instead of adding a value
        void set(String weight,String reps,long id){
            for(Values v:values){
                if(v.getId()==id){
                    v.set(weight,reps);
                    break;
                }
            }
        }
        //equals function for overriding
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Dates){
                Dates d=(Dates)obj;
                if(this.getDate().equalsIgnoreCase(d.getDate()))
                    return true;
            }
            return false;
        }
    }
    Exercise(String name){
        dates=new ArrayList<>();
        this.name=name;
    }
    //method to obtain the index of a given date
    //input: date text
    //output: index of the date if present or -1 if not
    int indexOf(String date){
        for(int i=0;i<dates.size();i++){
            if(dates.get(i).getDate().equalsIgnoreCase(date))
                return i;
        }
        return -1;
    }
    //adds a new date value to the dates list
    public void addDate(String date,String weight,String reps,long id){
        Dates d=new Dates(date);
        if(dates.contains(d)){
            int position=indexOf(date);
            dates.get(position).add(weight,reps,id);
        }
        else{
            d.add(weight,reps,id);
            dates.add(d);
            sort();
        }
    }
    //just adds a date text and sorts it
    int addDate(String date){
        Dates d=new Dates(date);
        if(!this.dates.contains(d)){
            this.dates.add(d);
            sort();
            return 1;
        }
        return -1;
    }
    //getter function
    public List<Dates> getDates(){
        return this.dates;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Dates) {
            Dates q = (Dates) obj;
            for (Dates d : dates) {
                if (q.getDate().equalsIgnoreCase(d.getDate()))
                    return true;
            }
        }
        return false;
    }
    String getName(){
        return this.name;
    }
    //setter function
    void set(String date,String weight,String reps,long id){
        for(Dates d: dates){
            if(d.getDate().equalsIgnoreCase(date)){
                d.set(weight,reps,id);
                break;
            }
        }
    }
    long getID(String date,int index){
        for(Dates d:dates){
            if(d.getDate().equalsIgnoreCase(date)){
                return d.getID(index);
            }
        }
        return -1;
    }
    int getPosition(String date){
        for(int i=0;i<dates.size();i++){
            if(date.equalsIgnoreCase(dates.get(i).getDate()))
                return i;
        }
        return -1;
    }
    public String get(int position){
        return dates.get(position).getDate();
    }
    //sort comparator to sort dates in reverse chronological order
    private void sort(){
        Collections.sort(dates, new Comparator<Dates>() {
            @Override
            public int compare(Dates o1, Dates o2) {
                String d1=o1.getDate(),d2=o2.getDate();
                DateFormat df=DateFormat.getDateInstance(DateFormat.LONG);
                try{
                    Date date1=df.parse(d1),date2=df.parse(d2);
                    if(date1.after(date2))return -1;
                    else if(date1.before(date2))return 1;
                    else return 0;
                }catch (ParseException P){
                    return 0;
                }
            }
        });
    }
}
