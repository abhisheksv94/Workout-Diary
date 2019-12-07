package com.example.abhishek.workoutdiary;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Abhishek on 05/12/17.
 */

public class strengthFrag extends Fragment {
    private String exerciseName;
    private Exercise exercise;
    private RecyclerView recyclerView;
    private customAdapter adapter;
    private customDatabase db;
    private Context context;
    private FloatingActionButton fab;
    private View view;
    private DisplayMetrics metrics;
    private RemoteViews remoteViews;
    private boolean ticking=false,isRegistered=false;
    private long timeDifference;
    private NotificationManager manager;
    private Notification.Builder builder;
    private int notification_id=100;
    private Chronometer timer;
    private Button start,stop;
    private PopupWindow window;


    //creates the fragment view: the recycler view and floating action button
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.strength_frag,container,false);
        recyclerView=view.findViewById(R.id.recyclerView);
        context=getContext();
        fab=view.findViewById(R.id.fragmentFAB);
        return view;
    }
    /*
    initializes the datamembers
    sets the listener for the floating action button
    sets the options menu
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        metrics=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        db=new customDatabase(context);
        exerciseName=getArguments().getString("name","");
        getActivity().setTitle(exerciseName);
        exercise=db.getExerciseValues(exerciseName);
        adapter=new customAdapter(context,recyclerView,exercise,metrics);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.manager=context.getSystemService(NotificationManager.class);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog=new DatePickerDialog(context);
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar c=Calendar.getInstance();
                        c.set(Calendar.YEAR,year);c.set(Calendar.MONTH,month);c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        DateFormat df=DateFormat.getDateInstance(DateFormat.LONG);
                        final String date=df.format(c.getTime());
                        int s=exercise.addDate(date);
                        adapter.notifyDataSetChanged();
                        int position=exercise.getPosition(date);
                        if(position!=-1)
                            recyclerView.scrollToPosition(position);
                        if(s==-1)
                            adapter.simulateClick(date);
                    }
                });
                datePickerDialog.show();
            }
        });
        getActivity().setTitle(exerciseName);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    //saves state of the app
    @Override
    public void onPause() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(getString(R.string.saveState),exerciseName);
        editor.putString("fragmentType","strength");
        editor.apply();
        if(window!=null&&window.isShowing()){
            createNotification();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        //saves the popup window status
        if(manager!=null){
            manager.cancel(notification_id);
            if(isRegistered){
                context.unregisterReceiver(receiver);
                isRegistered=false;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.preference){
            //settings option has been selected
            Settings settings=new Settings();
            getFragmentManager().beginTransaction().replace(R.id.frameLayout,settings,"Settings").addToBackStack("Settings").commit();
            return true;
        }
        //progress graph option has been selected
        else if(item.getItemId()==R.id.workoutGraph){
            List<Exercise.Dates> dates=exercise.getDates();
            if(dates.size()>0&&dates.get(0).getValues().size()>0) {
                Bundle b = new Bundle();
                b.putString("exercise", exerciseName);
                b.putString("type", "strength");
                graphFrag frag = new graphFrag();
                frag.setArguments(b);
                getFragmentManager().beginTransaction().replace(R.id.frameLayout, frag, exerciseName).addToBackStack(exerciseName).commit();
                return true;
            }
            else{
                Toast.makeText(context,"There are no values provided",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //instructions option has been selected
        else if(item.getItemId()==R.id.instructions){
            Instructions instructions=new Instructions();
            getFragmentManager().beginTransaction().replace(R.id.frameLayout,instructions,"Instructions").
                    addToBackStack("Instructions").commit();
            return true;
        }
        //stop watch was selected
        else if(item.getItemId()==R.id.stopwatch){
            //if the watch icon has been selected
            window=new PopupWindow(buildStopWatch(),(int)(0.6*metrics.widthPixels), -2);
            window.setElevation(100f);
            window.setFocusable(true);
            window.setOutsideTouchable(true);
            window.showAtLocation(view,Gravity.CENTER_VERTICAL,0,0);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ticking=false;
                    timeDifference=0;
                }
            });
            if(window.isShowing())start.performClick();
        }
        return super.onOptionsItemSelected(item);
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
                    long base= SystemClock.elapsedRealtime()-timeDifference;
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
