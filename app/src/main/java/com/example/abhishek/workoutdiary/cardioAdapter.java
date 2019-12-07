package com.example.abhishek.workoutdiary;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.SystemClock;
import android.support.annotation.Px;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 12/05/17.
 */

public class cardioAdapter extends RecyclerView.Adapter<cardioAdapter.cardioViewHolder> implements View.OnClickListener,
    View.OnLongClickListener{

    class cardioViewHolder extends RecyclerView.ViewHolder {
        ImageButton insert,delete;//insert and delete a value
        Button dateName;//date text
        LinearLayout valueHolder;//contains all the values as entered by the user
        cardioViewHolder(View itemView) {
            super(itemView);
            insert=itemView.findViewById(R.id.insert);
            delete=itemView.findViewById(R.id.delete);
            dateName=itemView.findViewById(R.id.date);
            valueHolder=itemView.findViewById(R.id.values);
        }
    }
    private Context context;
    private RecyclerView recyclerView;
    private DisplayMetrics metrics;
    private cardioExercise exercise;
    cardioAdapter(Context context,cardioExercise exercise, RecyclerView view, DisplayMetrics metrics){
        this.context=context;
        this.exercise=exercise;
        this.recyclerView=view;
        this.metrics=metrics;
    }

    @Override
    public cardioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.adapter_viewholder_layout,parent,false);
        return new cardioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(cardioViewHolder holder, int position) {
        String date=exercise.getDate(position);
        holder.dateName.setText(date);
        holder.insert.setOnClickListener(this);
        holder.delete.setOnClickListener(this);
        final LinearLayout ll=holder.valueHolder;
        if(ll.getChildCount()>0)
            ll.removeAllViews();//in case the view has been recycled and the previous views are still present
        List<cardioExercise.Dates.Values> values=exercise.getDates().get(position).getValues();
        for(int i=0;i<values.size();i++){
            ll.addView(getView(values.get(i)));//adds the new values into the view
        }
        //if a new date has been added automatically insert a value
        if(ll.getChildCount()==0)
            holder.insert.performClick();
    }
    @Override
    public int getItemCount() {
        return exercise.getDates().size();
    }
    @Override
    public void onClick(final View v) {
        if(v.getId()==R.id.insert){
            //create dialog box for enterring the required values
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            LinearLayout ll=new LinearLayout(context);LinearLayout t=new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);t.setOrientation(LinearLayout.HORIZONTAL);
            final EditText e=new EditText(context);
            e.setSingleLine();
            final EditText e2=new EditText(context);
            e2.setSingleLine();
            final EditText e3=new EditText(context),e4=new EditText(context),e5=new EditText(context),e6=new EditText(context),e7=new EditText(context);
            t.addView(e2);t.addView(e3);t.addView(e4);
            //to make sure the input is controlled
            KeyListener key=DigitsKeyListener.getInstance(true,true);

            e.setHint("Enter Distance");e2.setHint("Enter Hours");e3.setHint("Enter Minutes");e4.setHint("Enter Seconds");
            e5.setHint("Enter Speed");e6.setHint("Enter Calories");e7.setHint("Enter Incline");

            e.setKeyListener(key);e2.setKeyListener(key);e3.setKeyListener(key);e4.setKeyListener(key);
            e5.setKeyListener(key);e6.setKeyListener(key);e7.setKeyListener(key);

            //get setting preferences of the user
            boolean preferences[]=getPreferences();
            if(preferences[0]) ll.addView(e);
            if(preferences[1]) ll.addView(t);
            if(preferences[2]) ll.addView(e5);
            if(preferences[3]) ll.addView(e6);
            if(preferences[4]) ll.addView(e7);

            builder.setView(ll);
            //on clicking the 'done' button
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog alertDialog=builder.create();
            alertDialog.show();
            Button done=alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean error=false;
                    String distance=e.getText().toString();
                    if(distance.length()==0)distance="0";
                    String hrs=e2.getText().toString();
                    if(hrs.length()==0)hrs="0";
                    if(hrs.length()==1)hrs="0"+hrs;
                    String min=e3.getText().toString();
                    if(min.length()==0)min="0";
                    if(min.length()==1)min="0"+min;
                    String sec=e4.getText().toString();
                    if(sec.length()==0)sec="0";
                    if(sec.length()==1)sec="0"+sec;
                    int hours=Integer.parseInt(hrs),minutes=Integer.parseInt(min),seconds=Integer.parseInt(sec);
                    if(hours>24||(hours==24&&(minutes!=0||seconds!=0))) {
                        e2.setError("Error: Value too large");
                        error=true;
                    }
                    else if(minutes>60||(minutes==60&&seconds!=0)){
                        e3.setError("Error: Value too large");
                        error=true;
                    }
                    else if(seconds>=60){
                        e4.setError("Error: Value too large");
                        error=true;
                    }
                    String speed=e5.getText().toString();
                    if(speed.length()==0)speed="0";
                    String calories=e6.getText().toString();
                    if(calories.length()==0)calories="0";
                    String incline=e7.getText().toString();
                    if(incline.length()==0)incline="0";
                    else if(Integer.parseInt(incline)>=90){
                        e7.setError("Error: Value too large");
                        error=true;
                    }
                    String time=hrs+" : "+min+" : "+sec;
                    Button b=(Button)(((RelativeLayout)(v.getParent())).getChildAt(0));
                    String date=b.getText().toString();
                    if(!error) {
                        cardioData data = new cardioData(context);
                        //add the value into the database and into the recycler view
                        long id = data.insert(exercise.getName(), date, distance, time, speed, calories, incline);
                        exercise.addDate(date, distance, time, speed, calories, incline, id);
                        int position = exercise.indexOf(date);
                        if (position != -1) {
                            cardioAdapter.this.notifyItemChanged(position);
                            recyclerView.scrollToPosition(position);
                        }
                        data.close();
                        alertDialog.dismiss();
                    }
                }
            });
        }
        //if an entry needs to be deleted
        else if(v.getId()==R.id.delete){
            //set up a dialog box to warn user of deleting the data
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete the workout for this day?");
            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Button b=(Button)(((RelativeLayout)(v.getParent())).getChildAt(0));
                    String date=b.getText().toString();
                    for(cardioExercise.Dates d:exercise.getDates()){
                        if(d.getDate().equalsIgnoreCase(date)){
                            //delete the entry in the database and update the view
                            cardioData db=new cardioData(context);
                            List<cardioExercise.Dates.Values>values=d.getValues();
                            for(cardioExercise.Dates.Values v:values){
                                db.delete(v.getId());
                            }
                            exercise.getDates().remove(d);
                            cardioAdapter.this.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        //updating the data entries of a line
        else if(v instanceof LinearLayout){
            final LinearLayout line=(LinearLayout)v;
            final int index=((LinearLayout)(line.getParent())).indexOfChild(v);
            //enter the new data values
            AlertDialog.Builder builder=new AlertDialog.Builder(context);

            final EditText e1=new EditText(context),e2=new EditText(context),e3=new EditText(context),
                    e4=new EditText(context),e5=new EditText(context),e6=new EditText(context),e7=new EditText(context);

            Button b=(Button)(((RelativeLayout)(v.getParent().getParent())).getChildAt(0));
            final String date=b.getText().toString();
            LinearLayout ll=new LinearLayout(context),llH=new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);llH.setOrientation(LinearLayout.HORIZONTAL);

            int idx=((LinearLayout)v.getParent()).indexOfChild(v);
            int pos=exercise.indexOf(date);

            boolean pref[]=getPreferences();//get user preferences
            String pdis,ptime[],pspeed,pcal,pinc,phrs,pmin,psec;
            //get previous data values to get row id in the database
            pdis=exercise.getDates().get(pos).getValues().get(idx).getDistance();
            ptime=exercise.getDates().get(pos).getValues().get(idx).getTime().split(" : ");
            pspeed=exercise.getDates().get(pos).getValues().get(idx).getSpeed();
            pcal=exercise.getDates().get(pos).getValues().get(idx).getCalories();
            pinc=exercise.getDates().get(pos).getValues().get(idx).getIncline();
            phrs=ptime[0];pmin=ptime[1];psec=ptime[2];
            //layout parameters for the edit texts
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
            boolean unit=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
            if(!unit){
                pdis=convertToMetric("distance",pdis);
                pspeed=convertToMetric("speed",pspeed);
            }
            int wd=(int)(metrics.widthPixels*0.2);
            e1.setText(pdis);e1.setLayoutParams(params);
            e2.setText(phrs);e2.setWidth(wd);
            e3.setText(pmin);e3.setWidth(wd);
            e4.setText(psec);e4.setWidth(wd);
            e5.setText(pspeed);e5.setLayoutParams(params);
            e6.setText(pcal);e6.setLayoutParams(params);
            e7.setText(pinc);e7.setLayoutParams(params);
            TextView tv1;
            params=new LinearLayout.LayoutParams(-1,-2);
            params.setLayoutDirection(LinearLayout.HORIZONTAL);
            params.setMarginStart(20);
            LinearLayout layout=new LinearLayout(context);layout.setLayoutParams(params);
            //add values as per user preference
            //distance is selected
            if(pref[0]){
                tv1=getTextView(R.string.Distance);
                layout.addView(tv1);layout.addView(e1);
                ll.addView(layout);
            }
            //time is selected
            if(pref[1]) {
                TextView t1=getTextView(R.string.hrs),t2=getTextView(R.string.min),t3=getTextView(R.string.sec);
                llH.addView(t1); llH.addView(e2);llH.addView(t2);llH.addView(e3);llH.addView(t3);llH.addView(e4);
                llH.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                llH.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                ll.addView(llH);
            }
            //speed is selected
            if(pref[2]){
                tv1=getTextView(R.string.Speed);
                layout=new LinearLayout(context);layout.setLayoutParams(params);
                layout.addView(tv1);layout.addView(e5);
                ll.addView(layout);
            }
            //calories is selected
            if(pref[3]){
                tv1=getTextView(R.string.Calories);
                layout=new LinearLayout(context);layout.setLayoutParams(params);
                layout.addView(tv1);layout.addView(e6);
                ll.addView(layout);
            }
            //incline is selected
            if(pref[4]){
                tv1=getTextView(R.string.Incline);
                layout=new LinearLayout(context);layout.setLayoutParams(params);
                layout.addView(tv1);layout.addView(e7);
                ll.addView(layout);
            }
            //set up the edit texts
            e1.setSingleLine();e2.setSingleLine();e3.setSingleLine();e4.setSingleLine();e5.setSingleLine();
            e6.setSingleLine();e7.setSingleLine();
            KeyListener key= DigitsKeyListener.getInstance(true,true);
            e1.setKeyListener(key);e2.setKeyListener(key);e3.setKeyListener(key);e4.setKeyListener(key);e5.setKeyListener(key);
            e6.setKeyListener(key);e7.setKeyListener(key);

            builder.setView(ll);
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog=builder.create();
            dialog.show();
            Button done=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=exercise.indexOf(date);
                    boolean error=false;
                    long id=exercise.getDates().get(position).getValues().get(index).getId();
                    String ndis=e1.getText().toString().trim(),nhrs=e2.getText().toString().trim(),nmin=e3.getText().toString().trim(),
                            nsec=e4.getText().toString().trim(),nspeed=e5.getText().toString().trim(),ncal=e6.getText().toString().trim(),
                            ninc=e7.getText().toString().trim();
                    Log.d("Cardio",nhrs+"\t"+nmin+"\t"+nsec);
                    if(ndis.length()==0)ndis="0";
                    if(nhrs.length()==0)nhrs="00";else if(nhrs.length()==1)nhrs="0"+nhrs;
                    if(nmin.length()==0)nmin="00";else if(nmin.length()==1)nmin="0"+nmin;
                    if(nsec.length()==0)nsec="00";else if(nsec.length()==1)nsec="0"+nsec;
                    int hrs=Integer.parseInt(nhrs),min=Integer.parseInt(nmin),sec=Integer.parseInt(nsec);
                    if(hrs>24||(hrs==24&&(min!=0||sec!=0))){
                        e2.setError("Error: Value too large");
                        error=true;
                    }
                    else if(min>60||(min==60&&sec!=0)){
                        e3.setError("Error: Value too large");
                        error=true;
                    }
                    else if(sec>=60){
                        e4.setError("Error: Value too large");
                        error=true;
                    }
                    String ntime=nhrs+" : "+nmin+" : "+nsec;
                    if(nspeed.length()==0)nspeed="0";
                    if (ncal.length()==0)ncal="0";
                    if(ninc.length()==0)ninc="0";
                    else if(Integer.parseInt(ninc)>90){
                        e7.setError("Error: Value too large");
                        error=true;
                    }
                    boolean unit=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
                    if(!unit){
                        ndis=convertToImperial("distance",ndis);
                        nspeed=convertToImperial("speed",nspeed);
                    }


                    //update the values in the database and update the recycler view
                    if(!error) {
                        cardioData cd = new cardioData(context);
                        cd.update(ndis, ntime, nspeed, ncal, ninc, id);
                        cd.close();
                        exercise.setDate(date, ndis, ntime, nspeed, ncal, ninc, id);
                        cardioAdapter.this.notifyItemChanged(position);
                        dialog.dismiss();
                    }
                }
            });
        }

    }

    /*
    method to obtain a text view with set features and text
    input: id of the string
    output: textview
     */
    private TextView getTextView(int id){
        TextView tv=new TextView(context);
        tv.setPadding(0,0,20,0);
        String textview_text=context.getString(id);
        SpannableString s=new SpannableString(textview_text);
        s.setSpan(new StyleSpan(Typeface.ITALIC),0,textview_text.length(),0);
        s.setSpan(new RelativeSizeSpan(1.2f),0,textview_text.length(),0);
        tv.setText(s);
        return tv;
    }

    /*
    long click method for deleting the entry
    input: view long-clicked by the user
    output: click has been processed or not
     */
    @Override
    public boolean onLongClick(final View v) {
        final LinearLayout line=(LinearLayout)v;
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        //user warning of deleting the entry
        builder.setMessage("Are you sure you want to delete this workout?");
        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Button b=(Button)(((RelativeLayout)((line.getParent()).getParent())).getChildAt(0));
                String date=b.getText().toString();
                int position=exercise.indexOf(date);
                int index=((ViewGroup)(line.getParent())).indexOfChild(v);long id=-1;
                if(position!=-1&&index!=-1) {
                    id = exercise.getDates().get(position).getValues().get(index).getId();
                    cardioData cd=new cardioData(context);
                    cd.delete(id);
                    cd.close();
                }
                if(id!=-1) {
                    exercise.getDates().get(position).getValues().remove(index);
                    cardioAdapter.this.notifyItemChanged(position);
                }
            }
        });
        builder.setNeutralButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

        return true;
    }

    //custom methods

    /*
    method to return the view after populating with the required attributes
    input: values to be added into the view
    output:linearlayout with the attribute entries
     */
    private LinearLayout getView(cardioExercise.Dates.Values values){
        LinearLayout line=new LinearLayout(context);
        //get display metrics to measure appropriate button size and view size
        int ht1=(int)(0.02*metrics.heightPixels),ht2=(int)(0.018*metrics.heightPixels);
        int wd1=(int)(0.02*metrics.widthPixels);
        LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                p2=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p1.setMargins(5,0,wd1,0);
        p2.setMargins(0,10,0,10);

        String distance=values.getDistance(),time=values.getTime(),speed=values.getSpeed(),
                calories=values.getCalories(),incline=values.getIncline();

        boolean unit=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
        if(!unit){
            distance=convertToMetric("distance",distance);
            speed=convertToMetric("speed",speed);
        }
        //customize the entries for ui
        SpannableString s1,s2,s3,s4,s5;
        s1=new SpannableString(distance+(unit?(distance.equals("1")?" mile":" miles"):
                (distance.equals("1")?" km":" kms")));
        s1.setSpan(new AbsoluteSizeSpan(ht1),0,distance.length(),0);
        s1.setSpan(new StyleSpan(Typeface.BOLD),0,distance.length(),0);
        s1.setSpan(new ForegroundColorSpan(Color.GRAY),distance.length(),s1.length(),0);
        s1.setSpan(new AbsoluteSizeSpan(ht2),distance.length(),s1.length(),0);

        s2=new SpannableString(time);
        s2.setSpan(new AbsoluteSizeSpan(ht1),0,time.length(),0);
        s2.setSpan(new StyleSpan(Typeface.BOLD),0,time.length(),0);

        s3=new SpannableString(speed+(unit?(speed.equals("1")?" mile/hr":" miles/hr"):
                (speed.equals("1")?" km/hr":" kms/hr")));
        s3.setSpan(new AbsoluteSizeSpan(ht1),0,speed.length(),0);
        s3.setSpan(new StyleSpan(Typeface.BOLD),0,speed.length(),0);
        s3.setSpan(new ForegroundColorSpan(Color.GRAY),speed.length(),s3.length(),0);
        s3.setSpan(new AbsoluteSizeSpan(ht2),speed.length(),s3.length(),0);

        s4=new SpannableString(calories+" cals");
        s4.setSpan(new AbsoluteSizeSpan(ht1),0,calories.length(),0);
        s4.setSpan(new StyleSpan(Typeface.BOLD),0,calories.length(),0);
        s4.setSpan(new ForegroundColorSpan(Color.GRAY),calories.length(),s4.length(),0);
        s4.setSpan(new AbsoluteSizeSpan(ht2),calories.length(),s4.length(),0);

        s5=new SpannableString(incline+" \u00B0");
        s5.setSpan(new AbsoluteSizeSpan(ht1),0,incline.length(),0);
        s5.setSpan(new StyleSpan(Typeface.BOLD),0,incline.length(),0);
        s5.setSpan(new ForegroundColorSpan(Color.GRAY),incline.length(),s5.length(),0);
        s5.setSpan(new AbsoluteSizeSpan(ht1),incline.length(),s5.length(),0);

        //add the customized entries to views
        TextView t1,t2,t3,t4,t5;
        t1=new TextView(context);t1.setText(s1);t1.setLayoutParams(p1);
        t2=new TextView(context);t2.setText(s2);t2.setLayoutParams(p1);
        t3=new TextView(context);t3.setText(s3);t3.setLayoutParams(p1);
        t4=new TextView(context);t4.setText(s4);t4.setLayoutParams(p1);
        t5=new TextView(context);t5.setText(s5);

        boolean preferences[]=getPreferences();

        if(preferences[0])line.addView(t1);
        if(preferences[1])line.addView(t2);
        if(preferences[2])line.addView(t3);
        if(preferences[3])line.addView(t4);
        if(preferences[4])line.addView(t5);

        //add the views
        line.setLayoutParams(p2);
        line.setOnClickListener(this);
        line.setOnLongClickListener(this);
        return line;
    }

    //method to simulate click
    void simulateClick(String date) {
        final int position = exercise.indexOf(date);
        if (position != -1) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    cardioAdapter.cardioViewHolder holder = (cardioViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    if (holder != null)
                        holder.insert.performClick();
                }
            });
        }
    }
    //get user preference according to the settings page values
    private boolean[] getPreferences(){
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(context);
        boolean preferences[]=new boolean[5];
        preferences[0]=pref.getBoolean("distance",true);
        preferences[1]=pref.getBoolean("time",true);
        preferences[2]=pref.getBoolean("speed",false);
        preferences[3]=pref.getBoolean("calories",true);
        preferences[4]=pref.getBoolean("incline",false);
        return preferences;
    }
    /*
    method to convert imperial to si unit
    input: what attribute to convert( weight, distance etc), value of the attribute
    output: the converted value in string format
     */
    private String convertToMetric(String what,String value){
        if(what.equalsIgnoreCase("weight")){
            double w=Double.parseDouble(value);
            double res=w*0.45359237000000041107;
            res=Math.round(res*10D)/10D;
            if((int)res==res)return (int)res+"";
            else return res+"";
        }
        double d=Double.parseDouble(value);
        double res=d*1.6093440000000001078;
        res=Math.round(res*10D)/10D;
        if((int)res==res)return (int)res+"";
        else return res+"";
    }
    /*
    method to convert attribute from its metric value to its imperial value
    input: the attribute name, attribute value
    output: the converted value in string format
     */
    private String convertToImperial(String what,String value){
        if(what.equalsIgnoreCase("weight")){
            double w=Double.parseDouble(value);
            double result=w*2.2046226218487774418;
            result=Math.round(result*10D)/10D;
            if((int)result==result)return (int)result+"";
            else return result+"";
        }
        double d=Double.parseDouble(value);
        double result=d*0.6213711922373897911;
        result=Math.round(result*10D)/10D;
        if((int)result==result)return (int)result+"";
        else return result+"";
    }


}
