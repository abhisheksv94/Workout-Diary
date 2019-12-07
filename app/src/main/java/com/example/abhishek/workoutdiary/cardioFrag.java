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

public class cardioFrag extends Fragment {
    private String exerciseName;
    private cardioExercise exercise;
    private RecyclerView recyclerView;
    private cardioAdapter adapter;
    private cardioData db;
    private DisplayMetrics metrics;
    private View view;
    private Context context;
    private FloatingActionButton fab;
    private Chronometer timer;
    private Button start,pause;
    private boolean ticking=false,isRegistered=false;
    private long timeDifference=0;
    private int Notification_ID=100;
    private PopupWindow window;
    private NotificationManager manager;
    private Notification.Builder builder;
    private RemoteViews remoteViews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.strength_frag,container,false);
        recyclerView=view.findViewById(R.id.recyclerView);
        context=getContext();
        fab=view.findViewById(R.id.fragmentFAB);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        metrics=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        manager=context.getSystemService(NotificationManager.class);
        db=new cardioData(context);
        exerciseName=getArguments().getString("name","");
        getActivity().setTitle(exerciseName);
        exercise=db.getExercise(exerciseName);
        adapter=new cardioAdapter(context,exercise,recyclerView,metrics);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //floating action button for a new exercise value
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
                        int position=exercise.indexOf(date);
                        if(position!=-1)
                            recyclerView.scrollToPosition(position);
                        if(s==-1)
                            adapter.simulateClick(date);
                    }
                });
                datePickerDialog.show();
            }
        });
        setHasOptionsMenu(true);
        getActivity().setTitle(exerciseName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(menu.size()==0)menu.add(0,R.id.preference,0,"Settings");
    }

    //saves app status
    @Override
    public void onPause() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(getString(R.string.saveState),exerciseName);
        editor.putString("fragmentType","cardio");
        editor.apply();
        //create the notification if the stopwatch was showing
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
            manager.cancel(Notification_ID);
            if(isRegistered){
                context.unregisterReceiver(receiver);
                isRegistered=false;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //if setting option was selected
            case R.id.preference:
                getFragmentManager().beginTransaction().replace(R.id.frameLayout,new Settings(),
                        "Settings").addToBackStack("Settings").commit();
                return true;
                //if the graph option was selected
            case R.id.workoutGraph:
                List<cardioExercise.Dates>dates=exercise.getDates();
                if(dates.size()==0||dates.get(0).getValues().size()==0){
                    Toast.makeText(context,"No values provided to graph",Toast.LENGTH_SHORT).show();
                    return false;
                }
                Bundle b=new Bundle();
                b.putString("exercise",exerciseName);
                b.putString("type","cardio");
                graphFrag frag=new graphFrag();
                frag.setArguments(b);
                getFragmentManager().beginTransaction().replace(R.id.frameLayout,frag,exerciseName).addToBackStack(exerciseName).commit();
                return true;
                //if the user selected the instructions option
            case R.id.instructions:
                Instructions instructions=new Instructions();
                getFragmentManager().beginTransaction().replace(R.id.frameLayout,instructions,"Instructions").
                        addToBackStack("Instructions").commit();
                return true;
            case R.id.stopwatch:
                //if the watch icon has been selected
                window=new PopupWindow(buildStopWatch(),(int)(0.6*metrics.widthPixels), -2);
                window.setElevation(100f);
                window.setFocusable(true);
                window.setOutsideTouchable(true);
                window.showAtLocation(view, Gravity.CENTER_VERTICAL,0,0);
                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        ticking=false;
                        timeDifference=0;
                    }
                });
                if(window.isShowing())start.performClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        NotificationChannel channel=new NotificationChannel("100","Stopwatch", NotificationManager.IMPORTANCE_LOW);
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
}
