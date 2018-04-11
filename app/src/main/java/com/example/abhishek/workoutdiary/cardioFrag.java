package com.example.abhishek.workoutdiary;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Abhishek on 05/12/17.
 */

public class cardioFrag extends Fragment {
    private String exerciseName;
    private cardioExercise exercise;
    private RecyclerView recyclerView;
    private cardioAdapter adapter;
    private cardioData db;
    private Context context;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.strength_frag,container,false);
        recyclerView=view.findViewById(R.id.recyclerView);
        context=getContext();
        fab=view.findViewById(R.id.fragmentFAB);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DisplayMetrics metrics=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
        if(adapter.checkPopupWindow())adapter.createNotification();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.onResume();//saves the popup window status
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
