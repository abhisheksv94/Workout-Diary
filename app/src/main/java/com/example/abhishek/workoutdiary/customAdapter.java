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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 04/12/17.
 */

public class customAdapter extends RecyclerView.Adapter<customAdapter.customViewHolder> implements View.OnClickListener,
View.OnLongClickListener{
    private Context context;
    private RecyclerView recyclerView;
    private Exercise exercise;
    private DisplayMetrics metrics;
    private RemoteViews remoteViews;
    private boolean ticking=false,isRegistered=false;
    private long timeDifference;
    private NotificationManager manager;
    private Notification.Builder builder;
    private int notification_id=100;
    private Chronometer timer;
    private Button start,stop;
    private AlertDialog dialog;
    customAdapter(Context context, RecyclerView recyclerView, Exercise exercise, DisplayMetrics metrics){
        this.context=context;
        this.recyclerView=recyclerView;
        this.exercise=exercise;
        this.metrics=metrics;
        this.timeDifference=0;
        this.manager=context.getSystemService(NotificationManager.class);
    }
    //check if popup window is showing or not
    boolean checkPopupWindow(){
        return dialog!=null&&dialog.isShowing();
    }
    //when the app resumes clear the notification and unregister the receiver if it is registered
    void onResume(){
        if(manager!=null){
            manager.cancel(notification_id);
            if(isRegistered){
                context.unregisterReceiver(receiver);
                isRegistered=false;
            }
        }
    }
    //simulate a click
    void simulateClick(String date){
        List<Exercise.Dates>d=exercise.getDates();
        for(int i=0;i<d.size();i++){
            if(d.get(i).getDate().equalsIgnoreCase(date)){
                final int position=i;
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        customViewHolder holder=(customViewHolder)recyclerView.findViewHolderForAdapterPosition(position);
                        if(holder!=null)
                            holder.insert.performClick();
                    }
                });
                break;
            }
        }
    }

    @Override
    public customViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View v=inflater.inflate(R.layout.adapter_viewholder_layout,parent,false);
        return new customViewHolder(v);
    }

    @Override
    public void onBindViewHolder(customViewHolder holder,int position) {
        String date=exercise.get(position);
        holder.getButton().setText(date);
        holder.insert.setOnClickListener(this);
        holder.delete.setOnClickListener(this);
        final LinearLayout ll=holder.getValueHolder();
        if(ll.getChildCount()>0)
            ll.removeAllViews();//to remove pre-existing views if any
        List<Exercise.Dates.Values>values=exercise.getDates().get(position).getValues();
        for(int i=0;i<values.size();i++){
            ll.addView(getView(values.get(i),i));//add the new entries
        }
        if(ll.getChildCount()==0)
            holder.insert.performClick();//click new entry button if a new card has just been created

    }
    /*
    method to get a line of entries in a view
    input: exercise values, entry number for that date
    output: linearlayout with the entries in it
     */
    private LinearLayout getView(Exercise.Dates.Values values,int i){
        int ht1=(int)(0.02*metrics.heightPixels),ht2=(int)(0.018*metrics.heightPixels);
        int wd1=(int)(0.15*metrics.widthPixels);

        String weight=values.getWeight(),reps=values.getReps();

        boolean unit= PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
        if(!unit){
            weight=convertToMetric(weight);
        }
        //ui customization
        SpannableString w=new SpannableString(weight+(unit?" lbs":" kgs"));
        w.setSpan(new AbsoluteSizeSpan(ht1),0,weight.length(),0);
        w.setSpan(new StyleSpan(Typeface.BOLD),0,weight.length(),0);
        w.setSpan(new AbsoluteSizeSpan(ht2),weight.length(),w.length(),0);
        w.setSpan(new ForegroundColorSpan(Color.GRAY),weight.length(),w.length(),0);
        TextView t=new TextView(context);
        t.setText(w);
        LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p1.setMargins(5,10,wd1,10);
        t.setLayoutParams(p1);

        SpannableString r=new SpannableString(reps+" reps");
        r.setSpan(new AbsoluteSizeSpan(ht1),0,reps.length(),0);
        r.setSpan(new StyleSpan(Typeface.BOLD),0,reps.length(),0);
        r.setSpan(new AbsoluteSizeSpan(ht2),reps.length(),r.length(),0);
        r.setSpan(new ForegroundColorSpan(Color.GRAY),reps.length(),r.length(),0);
        SpannableString set=new SpannableString("Set "+(i+1));
        set.setSpan(new StyleSpan(Typeface.BOLD_ITALIC),0,set.length(),0);
        set.setSpan(new AbsoluteSizeSpan(ht1),0,set.length(),0);
        TextView setTV=new TextView(context);
        setTV.setText(set);
        setTV.setLayoutParams(p1);
        TextView t2=new TextView(context);
        t2.setLayoutParams(p1);
        t2.setText(r);

        LinearLayout line=new LinearLayout(context);
        LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        p2.setMargins(0,10,0,10);
        line.setLayoutParams(p2);
        line.setOrientation(LinearLayout.HORIZONTAL);line.addView(setTV);
        line.addView(t);line.addView(t2);
        line.setOnClickListener(this);//to update
        line.setOnLongClickListener(this);//to delete
        return line;
    }

    @Override
    public int getItemCount() {
        return exercise.getDates().size();
    }
    //method to delete a line of entries
    @Override
    public boolean onLongClick(final View v) {
        if(v instanceof LinearLayout){
            //dialog for warning to the user about deleting the entry
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete this set?");
            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Button b=(Button)(((RelativeLayout)((v.getParent()).getParent())).getChildAt(0));
                    String date=b.getText().toString();
                    int position=exercise.indexOf(date);
                    if(position!=-1){
                        //delete the entry from the database and update the view
                        customAdapter.customViewHolder holder=(customViewHolder)recyclerView.findViewHolderForAdapterPosition(position);
                        LinearLayout ll=holder.getValueHolder();
                        for(int i=0;i<ll.getChildCount();i++){
                            if(ll.getChildAt(i)==v){
                                long id=exercise.getDates().get(position).getID(i);
                                customDatabase db=new customDatabase(context);
                                boolean f=db.delete(id);
                                exercise.getDates().get(position).getValues().remove(i);
                                customAdapter.this.notifyItemChanged(position);
                                break;
                            }
                        }
                    }
                }
            });
            builder.create().show();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(final View v) {
        if(v.getId()==R.id.insert){
            //to insert a new value for a particular date
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            LinearLayout ll=new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            final EditText e=new EditText(context);
            e.setSingleLine();
            e.setEnabled(true);
            e.requestFocus();
            final EditText e2=new EditText(context);
            e2.setSingleLine();
            ll.addView(e);ll.addView(e2);
            KeyListener key=DigitsKeyListener.getInstance(false,true);
            e.setKeyListener(key);e2.setKeyListener(key);
            e.setHint("Enter Weight");e2.setHint("Enter Reps");
            builder.setView(ll);
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog=builder.create();dialog.show();
            final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            Button positive=dialog.getButton(AlertDialog.BUTTON_POSITIVE);final View view=v;
            e.post(new Runnable() {
                @Override
                public void run() {

                    if (imm != null) {
                        imm.showSoftInput(e, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String weight=e.getText().toString();
                    String reps=e2.getText().toString();
                    if(weight.length()==0){
                        e.setHint("You must enter a value for weight");
                        e.setError("Value required");
                    }
                    if(reps.length()==0){
                        e2.setHint(("You must enter a value for reps"));
                        e2.setError("Value required");
                    }
                    else{
                        Button b = (Button) (((RelativeLayout) (view.getParent())).getChildAt(0));
                        String date = b.getText().toString();
                        customDatabase db = new customDatabase(context);
                        long id = db.insert(exercise.getName(), date, weight, reps);
                        exercise.addDate(date, weight, reps, id);
                        final int position = exercise.indexOf(date);
                        if (position != -1) {
                            customAdapter.this.notifyItemChanged(position);
                            recyclerView.scrollToPosition(position);
                        }
                        db.close();
                        dialog.dismiss();
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
                        }
                    }
                }
            });
        }
        else if(v.getId()==R.id.delete){
            //to delete all the entries for a particular date
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete the workout for this day?");
            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Button b=(Button)(((RelativeLayout)(v.getParent())).getChildAt(0));
                    String date=b.getText().toString();
                    for(Exercise.Dates d:exercise.getDates()){
                        if(d.getDate().equalsIgnoreCase(date)){
                            customDatabase db=new customDatabase(context);
                            List<Exercise.Dates.Values>values=d.getValues();
                            for(Exercise.Dates.Values v:values){
                                db.delete(v.getId());
                            }
                            exercise.getDates().remove(d);
                            customAdapter.this.notifyDataSetChanged();
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
        else if(v instanceof LinearLayout){
            //update method
            final LinearLayout line=(LinearLayout)v;
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            TextView t1=(TextView)line.getChildAt(1);
            TextView t2=(TextView)line.getChildAt(2);
            final EditText e1=new EditText(context),e2=new EditText(context);
            String pweight=t1.getText().toString().substring(0,t1.getText().toString().indexOf(' ')),
                    preps=t2.getText().toString().substring(0,t2.getText().toString().indexOf(' '));

            final boolean unit=PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);

            e1.setText(pweight);e2.setText(preps);
            e1.setSingleLine();e2.setSingleLine();
            KeyListener key= DigitsKeyListener.getInstance(false,true);
            e1.setKeyListener(key);e2.setKeyListener(key);
            LinearLayout ll=new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(e1);ll.addView(e2);
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
            final AlertDialog dialog=builder.create();dialog.show();
            Button b=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nweight=e1.getText().toString(),nreps=e2.getText().toString();
                    if(nweight.length()==0){
                        e1.setHint("You must enter a value for weight");
                        e1.setError("Value required");
                    }
                    if(nreps.length()==0){
                        e2.setHint("You must enter a value for reps");
                        e2.setError("Value required");
                    }
                    else{
                        Button dt=(Button)(((RelativeLayout)((line.getParent()).getParent())).getChildAt(0));
                        String date=dt.getText().toString();
                        int index=((ViewGroup)(line.getParent())).indexOfChild(line);
                        long id=exercise.getID(date,index);
                        if(!unit){
                            nweight=convertToImperial(nweight);
                        }
                        if(id!=-1){
                            //update the database and the recycler view
                            exercise.set(date,nweight,nreps,id);
                            customDatabase db=new customDatabase(context);
                            db.update(id,nweight,nreps);
                            db.close();
                            customAdapter.this.notifyItemChanged(exercise.indexOf(date));
                        }
                        dialog.dismiss();
                    }
                }
            });
        }
//        else if(v.getId()==R.id.clock){
            //if the watch icon has been selected
//            window=new PopupWindow(buildStopWatch(),(int)(0.6*metrics.widthPixels), -2);
//            window.setElevation(100f);
//            window.setFocusable(true);
//            window.setOutsideTouchable(true);
//            window.showAtLocation(v,Gravity.CENTER_VERTICAL,0,0);
//            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                @Override
//                public void onDismiss() {
//                    ticking=false;
//                    timeDifference=0;
//                }
//            });
//            dialog = new AlertDialog.Builder(v.getContext())
//                    .setView(buildStopWatch())
//                    .setOnDismissListener(new AlertDialog.OnDismissListener(){
//                        @Override
//                        public void onDismiss(DialogInterface dialogInterface) {
//                            ticking = false;
//                            timeDifference = 0;
//                        }
//                    })
//                    .create();
//            dialog.show();
//
//            if(dialog.isShowing())start.performClick();
//        }
    }
    class customViewHolder extends RecyclerView.ViewHolder{
        private Button dateName;//contains the date
        private ImageButton insert,delete;//insert a value, delete the date, stopwatch
        private LinearLayout valueHolder;//container for the entries

        customViewHolder(View itemView) {
            super(itemView);
            insert=itemView.findViewById(R.id.insert);
            delete=itemView.findViewById(R.id.delete);
            dateName=itemView.findViewById(R.id.date);
            valueHolder=itemView.findViewById(R.id.values);
        }
        Button getButton(){
            return dateName;
        }
        LinearLayout getValueHolder(){
            return valueHolder;
        }
    }
    //converts to si unit
    private String convertToMetric(String value){
        double w=Double.parseDouble(value);
        double res=w*0.45359237000000041107;
        res=Math.round(res*10D)/10D;
        if((int)res==res)return (int)res+"";
        else return res+"";
    }
    //converts to imperial units
    private String convertToImperial(String value){
        double w=Double.parseDouble(value);
        double result=w*2.2046226218487774418;
        result=Math.round(result*10D)/10D;
        if((int)result==result)return (int)result+"";
        else return result+"";
    }
    //registers the receiver if not registered, creates the intent filter and creates and launches a notification
    void createNotification(){
        if(manager!=null) {
            if(!isRegistered) {
                IntentFilter filter=new IntentFilter();
                filter.addAction("Start");
                filter.addAction("Pause");
                filter.addAction("Reset");
                filter.addAction("Resume");
                context.registerReceiver(receiver, filter);
                isRegistered = true;
            }
            NotificationChannel channel = new NotificationChannel("100", "Channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            if (ticking) {
                //if the clock is ticking then get the new time-difference between now and the base time of the timer
                timeDifference = SystemClock.elapsedRealtime() - timer.getBase();
            }
            //Log.d("Stopwatch", ticking + ": ticking value");
            long base = SystemClock.elapsedRealtime() - timeDifference;
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.small_chronometer_layout);
            remoteViews.setChronometer(R.id.Chronometer_timer, base, null, ticking);
            //2 ways to create an intent to launch an activity from a notification without starting a new activity

            /*Intent intent=context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if(intent!=null) {
                intent.setPackage(null).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }*/
            Intent intent = new Intent(context, context.getClass())
                    .setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //build the notification
            builder = new Notification.Builder(context, "100")
                    .setStyle(new Notification.DecoratedCustomViewStyle())
                    .setContentTitle("Stopwatch")
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                    .setSmallIcon(R.drawable.ic_timer_black_24dp)
                    .setCustomContentView(remoteViews)
                    .setActions(getActions(ticking?"Start":"Pause"))
                    .setOnlyAlertOnce(true);
            manager.notify(notification_id, builder.build());
        }
    }
    //broadcast receiver to receive broadcasts for the notification
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
                        manager.notify(notification_id,builder.build());
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
                        manager.notify(notification_id,builder.build());
                        timer.setBase(cr.getBase());
                        stop.performClick();//to keep the timer in sync with the notification stopwatch
                    }
                    break;
                case "Reset":
                    remoteViews.setChronometer(R.id.Chronometer_timer,SystemClock.elapsedRealtime(),null,ticking);
                    timeDifference=0;
                    builder.setCustomContentView(remoteViews);
                    builder.setActions(getActions("Start"));
                    manager.notify(notification_id,builder.build());
                    //reset the timer as well
                    stop.performClick();
                    break;
                case "Resume":
                    //if the timer wasn't running before in the app or the notification
                    if(!ticking) {
                        long base = SystemClock.elapsedRealtime() - timeDifference;
                        remoteViews.setChronometer(R.id.Chronometer_timer, base, null, true);
                        builder.setCustomContentView(remoteViews);
                        builder.setActions(getActions("Resume"));
                        manager.notify(notification_id, builder.build());
                        timer.setBase(base);
                        start.performClick();//keep the app timer in sync with the notification timer
                    }
            }
        }
    };

    /*
    get actions for the notification based on the button clicked by the user
    input: name of the button clicked
    output: an array of actions to be displayed in the notification
     */
    private Notification.Action[] getActions(String firstAction){
        Intent i1 = new Intent("Start"), i2 = new Intent("Pause"), i3 = new Intent("Reset"),
                i4=new Intent("Resume");
        PendingIntent p1 = PendingIntent.getBroadcast(context, 1, i1, 0),
                p2 = PendingIntent.getBroadcast(context, 1, i2, 0),
                p3 = PendingIntent.getBroadcast(context, 1, i3, 0),
                p4=PendingIntent.getBroadcast(context,1,i4,0);
        ArrayList<Notification.Action> actions=new ArrayList<>();
        Notification.Action a1 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_launcher_background), "Start", p1).build(),
                a2 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_launcher_background), "Pause", p2).build(),
                a3 = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_launcher_background), "Reset", p3).build(),
                a4=new Notification.Action.Builder(Icon.createWithResource(context,R.drawable.ic_launcher_background),"Resume",p4).build();
        switch (firstAction){
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

    //method to build the popup window layout
    private LinearLayout buildStopWatch(){
        LinearLayout layout;
        LayoutInflater inflater=LayoutInflater.from(context);
        layout=(LinearLayout) inflater.inflate(R.layout.stopwatch,null);
        timer=layout.findViewById(R.id.timer);
        start=layout.findViewById(R.id.start);
        stop=layout.findViewById(R.id.stop);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the clock hasn't started in the notifications or the popup window
                if(!ticking){
                    long base=SystemClock.elapsedRealtime()-timeDifference;
                    timer.setBase(base);
                    timer.start();
                    ticking=true;
                    stop.setText(R.string.StopWatch_pause);//change the text of the pause button to 'pause'
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the stop button text was reset then reset the timer
                if(stop.getText().toString().equals(context.getString(R.string.StopWatch_reset))){
                    timeDifference=0;
                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.stop();
                    ticking=false;
                    start.setText(R.string.StopWatch_start);//set the start button text to 'start'
                    stop.setText(R.string.StopWatch_pause);//set the pause button text to 'pause'
                }
                //if the clock was ticking and the stop button text was 'stop' then stop the clock
                if(ticking){
                   timeDifference=SystemClock.elapsedRealtime()-timer.getBase();
                   timer.stop();
                   ticking=false;
                   start.setText(R.string.StopWatch_resume);//set the start button text to 'resume'
                   stop.setText(R.string.StopWatch_reset);//set the stop button text to 'reset'
                }
            }
        });
        return layout;
    }
}
