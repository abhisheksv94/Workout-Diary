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
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
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
        ImageButton insert,delete,clock;//insert and delete a value and the stopwatch button
        Button dateName;//date text
        LinearLayout valueHolder;//contains all the values as entered by the user
        cardioViewHolder(View itemView) {
            super(itemView);
            insert=itemView.findViewById(R.id.insert);
            delete=itemView.findViewById(R.id.delete);
            clock=itemView.findViewById(R.id.clock);
            dateName=itemView.findViewById(R.id.date);
            valueHolder=itemView.findViewById(R.id.values);
        }
    }
    private Context context;
    private RecyclerView recyclerView;
    private DisplayMetrics metrics;
    private cardioExercise exercise;
    private Chronometer timer;
    private Button start,pause;
    private boolean ticking=false,isRegistered=false;
    private long timeDifference=0;
    private int Notification_ID=100;
    private PopupWindow window;
    private NotificationManager manager;
    private Notification.Builder builder;
    private RemoteViews remoteViews;
    cardioAdapter(Context context,cardioExercise exercise, RecyclerView view, DisplayMetrics metrics){
        this.context=context;
        this.exercise=exercise;
        this.recyclerView=view;
        this.metrics=metrics;
        manager=context.getSystemService(NotificationManager.class);
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
        holder.clock.setOnClickListener(this);
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
                public void onClick(DialogInterface dialog, int which) {
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
                    String speed=e5.getText().toString();
                    if(speed.length()==0)speed="0";
                    String calories=e6.getText().toString();
                    if(calories.length()==0)calories="0";
                    String incline=e7.getText().toString();
                    if(incline.length()==0)incline="0";
                    String time=hrs+" : "+min+" : "+sec;
                    Button b=(Button)(((RelativeLayout)(v.getParent())).getChildAt(0));
                    String date=b.getText().toString();
                    cardioData data=new cardioData(context);
                    //add the value into the database and into the recycler view
                    long id=data.insert(exercise.getName(),date,distance,time,speed,calories,incline);
                    exercise.addDate(date,distance,time,speed,calories,incline,id);
                    int position=exercise.indexOf(date);
                    if(position!=-1){
                        cardioAdapter.this.notifyItemChanged(position);
                        recyclerView.scrollToPosition(position);
                    }
                    data.close();
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
            //get width of diplay for appropriate width measurement for the buttons
            int wd=(int)(metrics.widthPixels*0.25);

            boolean unit=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
            if(!unit){
                pdis=convertToMetric("distance",pdis);
                pspeed=convertToMetric("speed",pspeed);
            }
            e1.setText(pdis);
            e2.setText(phrs);
            e3.setText(pmin);
            e4.setText(psec);
            e5.setText(pspeed);
            e6.setText(pcal);
            e7.setText(pinc);
            //add values as per user preference
            //distance is selected
            if(pref[0]){ ll.addView(e1);}
            //time is selected
            if(pref[1]) {
                ViewGroup.LayoutParams p=new ViewGroup.LayoutParams(wd, ViewGroup.LayoutParams.WRAP_CONTENT);
                e2.setLayoutParams(p);e3.setLayoutParams(p);e4.setLayoutParams(p);

                llH.addView(e2);llH.addView(e3);llH.addView(e4);
                llH.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                llH.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                ll.addView(llH);
            }
            //speed is selected
            if(pref[2]){ll.addView(e5);}
            //calories is selected
            if(pref[3]){ ll.addView(e6);}
            //incline is selected
            if(pref[4]){ ll.addView(e7);}
            //set up the edit texts
            e1.setSingleLine();e2.setSingleLine();e3.setSingleLine();e4.setSingleLine();e5.setSingleLine();
            e6.setSingleLine();e7.setSingleLine();
            KeyListener key= DigitsKeyListener.getInstance(true,true);
            e1.setKeyListener(key);e2.setKeyListener(key);e3.setKeyListener(key);e4.setKeyListener(key);e5.setKeyListener(key);
            e6.setKeyListener(key);e7.setKeyListener(key);

            builder.setView(ll);
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int position=exercise.indexOf(date);
                    long id=exercise.getDates().get(position).getValues().get(index).getId();
                    String ndis=e1.getText().toString().trim(),nhrs=e2.getText().toString().trim(),nmin=e3.getText().toString().trim(),
                            nsec=e4.getText().toString().trim(),nspeed=e5.getText().toString().trim(),ncal=e6.getText().toString().trim(),
                    ninc=e7.getText().toString().trim();
                    if(ndis.length()==0)ndis="0";
                    if(nhrs.length()==0)nhrs="00";else if(nhrs.length()==1)nhrs="0"+nhrs;
                    if(nmin.length()==0)nmin="00";else if(nmin.length()==1)nmin="0"+nmin;
                    if(nsec.length()==0)nsec="00";else if(nsec.length()==1)nsec="0"+nsec;
                    String ntime=nhrs+" : "+nmin+" : "+nsec;
                    if(nspeed.length()==0)nspeed="0";
                    if (ncal.length()==0)ncal="0";
                    if(ninc.length()==0)ninc="0";
                    boolean unit=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
                    if(!unit){
                        ndis=convertToImperial("distance",ndis);
                        nspeed=convertToImperial("speed",nspeed);
                    }

                    //update the values in the database and update the recycler view
                    cardioData cd=new cardioData(context);
                    cd.update(ndis,ntime,nspeed,ncal,ninc,id);
                    cd.close();
                    exercise.setDate(date,ndis,ntime,nspeed,ncal,ninc,id);
                    cardioAdapter.this.notifyItemChanged(position);
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
        else if(v.getId()==R.id.clock){
            //create popup window for the stopwatch with the stopwatch layout and wrap height and width
            window=new PopupWindow(buildStopWatch(),-2,-2);
            //set attributes of the window
            window.setElevation(100f);
            window.setFocusable(true);
            window.setOutsideTouchable(true);

            //set the on dismiss listener to reflect that the stopwatch has been dismissed
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ticking=false;
                    timeDifference=0;
                }
            });
            //show the window
            window.showAtLocation(v,Gravity.CENTER_VERTICAL,0,0);
            //if window is showing start the stopwatch
            if(window.isShowing())start.performClick();
        }
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
    /*
    METHOD TO BUILD THE LAYOUT FOR THE POPUP WINDOW
    input: null
    output: linear layout with the stop watch views populated within
     */
    private LinearLayout buildStopWatch(){
        LinearLayout layout;
        View view=LayoutInflater.from(context).inflate(R.layout.stopwatch,null);
        layout=(LinearLayout)view;
        //set the timer to the xml layout
        timer=view.findViewById(R.id.timer);
        //set the buttons
        start=view.findViewById(R.id.start);pause=view.findViewById(R.id.stop);
        //set the on-click listeners for the buttons
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ticking){
                    timer.setBase(SystemClock.elapsedRealtime()-timeDifference);
                    timer.start();
                    ticking=true;//clock is running
                    //set the pause button to show 'pause'
                    pause.setText(R.string.StopWatch_pause);
                }
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the pause button was present and the clock was running then save the time difference and stop the clock
                if(pause.getText().toString().equals("Pause")&&ticking){
                    timeDifference=SystemClock.elapsedRealtime()-timer.getBase();
                    timer.stop();
                    ticking=false;//the clock has stopped
                    //set the start button to show 'resume'
                    start.setText(R.string.StopWatch_resume);
                    //set the pause button to show 'reset
                    pause.setText(R.string.StopWatch_reset);
                }
                else if(pause.getText().toString().equals("Reset")){
                    //reset the timer
                    timer.setBase(SystemClock.elapsedRealtime());
                    timeDifference=0;
                    //set the start button to show 'start'
                    start.setText(R.string.StopWatch_start);
                    //set the pause button to show 'pause'
                    pause.setText(R.string.StopWatch_pause);
                }
            }
        });
        return layout;
    }
    /*
    method to register the receiver and create the intent filter if not registered, creates and launches the notification
    input: null
    output: null
     */
    void createNotification(){
        //if manager is null notification can't be created
        if(manager==null)return;

        //register the receiver and create the intent-filter if the receiver isn't registered
        if(!isRegistered){
            IntentFilter filter=new IntentFilter();
            filter.addAction("Start");
            filter.addAction("Pause");
            filter.addAction("Reset");
            filter.addAction("Resume");
            context.registerReceiver(receiver,filter);
            isRegistered=true;
        }

        //create the notification channel
        NotificationChannel channel=new NotificationChannel("100","Stopwatch",NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
        //if the timer is already running then update the time difference
        if(ticking)
            timeDifference=SystemClock.elapsedRealtime()-timer.getBase();
        //set up the remote view
        remoteViews=new RemoteViews(context.getPackageName(),R.layout.small_chronometer_layout);
        remoteViews.setChronometer(R.id.Chronometer_timer,SystemClock.elapsedRealtime()-timeDifference,null,ticking);
        //2 ways to create an intent to launch an activity from a notification without starting a new activity

            /*Intent intent=context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if(intent!=null) {
                intent.setPackage(null).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }*/
        Intent intent = new Intent(context, context.getClass())
                .setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //build the notification
        builder=new Notification.Builder(context,"100")
                .setSmallIcon(R.drawable.ic_timer_black_24dp)
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .setContentIntent(PendingIntent.getActivity(context,1,intent,0))
                .setCustomContentView(remoteViews)
                .setContentTitle("Stopwatch")
                .setActions(getActions(ticking?"Start":"Resume"))
                .setOnlyAlertOnce(true);

        //launch the notification to the status bar
        manager.notify(Notification_ID,builder.build());
    }
    /*
    method to get a set of actions corresponding to the button pressed
    input: name of the button pressed
    output: array of actions
     */
    private Notification.Action[] getActions(String firstAction){
        //create the intents for the actions
        Intent i1 = new Intent("Start"), i2 = new Intent("Pause"), i3 = new Intent("Reset"),
                i4=new Intent("Resume");
        //create the pending intents for the intents
        PendingIntent p1 = PendingIntent.getBroadcast(context, 1, i1, 0),
                p2 = PendingIntent.getBroadcast(context, 1, i2, 0),
                p3 = PendingIntent.getBroadcast(context, 1, i3, 0),
                p4=PendingIntent.getBroadcast(context,1,i4,0);
        //actions required
        ArrayList<Notification.Action> actions=new ArrayList<>();
        //list of all actions required
        Notification.Action a1 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_launcher_background), "Start", p1).build(),
                a2 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_launcher_background), "Pause", p2).build(),
                a3 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_launcher_background), "Reset", p3).build(),
                a4=new Notification.Action.Builder(Icon.createWithResource(context,R.drawable.ic_launcher_background),"Resume",p4).build();
        switch (firstAction) {
            case "Start":
                //add action to start and pause
                actions.add(a1);
                actions.add(a2);
                break;
            case "Pause":
                //add action to resume and reset
                actions.add(a4);
                actions.add(a3);
                break;
            case "Resume":
                //add action to resume and pause
                actions.add(a4);
                actions.add(a2);
                break;
        }
        return actions.toArray(new Notification.Action[actions.size()]);
    }
    /*
    register a receiver for the notification
     */
    private BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action==null||manager==null||builder==null)return;
            switch (action){
                case "Start":
                    //if the clock is not ticking already
                    if(!ticking){
                        long base=SystemClock.elapsedRealtime();
                        remoteViews.setChronometer(R.id.Chronometer_timer,base,null,true);
                        builder.setCustomContentView(remoteViews);
                        manager.notify(Notification_ID,builder.build());
                        timer.setBase(base);
                        start.performClick();//to keep the timer in the app i sync with the notification timer
                    }
                    break;
                case "Pause":
                    //if the clock is running and the pause button is pressed in the notification
                    if(ticking){
                        Chronometer cr=(Chronometer)(((ViewGroup)remoteViews.apply(context,null)).getChildAt(0));
                        timeDifference=SystemClock.elapsedRealtime()-cr.getBase();//for new base
                        remoteViews.setChronometer(R.id.Chronometer_timer,cr.getBase(),null,false);
                        builder.setCustomContentView(remoteViews);
                        builder.setActions(getActions("Pause"));//new actions names
                        manager.notify(Notification_ID,builder.build());
                        timer.setBase(cr.getBase());
                        pause.performClick();//to keep the timer in sync with the notification stopwatch
                    }
                    break;
                case "Reset":
                    //reset the clock to show 0
                    remoteViews.setChronometer(R.id.Chronometer_timer,SystemClock.elapsedRealtime(),null,ticking);
                    timeDifference=0;
                    builder.setCustomContentView(remoteViews);
                    builder.setActions(getActions("Start"));
                    manager.notify(Notification_ID,builder.build());
                    //reset the timer as well
                    pause.performClick();
                    break;
                case "Resume":
                    //if the timer wasn't running before in the app or the notification
                    if(!ticking) {
                        long base = SystemClock.elapsedRealtime() - timeDifference;
                        remoteViews.setChronometer(R.id.Chronometer_timer, base, null, true);
                        builder.setCustomContentView(remoteViews);
                        builder.setActions(getActions("Resume"));
                        manager.notify(Notification_ID, builder.build());
                        timer.setBase(base);
                        start.performClick();//keep the app timer in sync with the notification timer
                    }
            }
        }
    };
    //check if popup window is showing or not
    boolean checkPopupWindow(){
        return window!=null&&window.isShowing();
    }
    //when the app resumes clear the notification
    void onResume(){
        if(manager!=null){
            manager.cancel(Notification_ID);
            if(isRegistered){
                context.unregisterReceiver(receiver);
                isRegistered=false;
            }
        }
    }
}
