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
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

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


    //creates the fragment view: the recycler view and floating action button
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.strength_frag,container,false);
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
        DisplayMetrics metrics=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        db=new customDatabase(context);
        exerciseName=getArguments().getString("name","");
        getActivity().setTitle(exerciseName);
        exercise=db.getExerciseValues(exerciseName);
        adapter=new customAdapter(context,recyclerView,exercise,metrics);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu,menu);
        if(menu.size()==0)menu.add(0,R.id.preference,0,"Settings");
    }

    //saves state of the app
    @Override
    public void onPause() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(getString(R.string.saveState),exerciseName);
        editor.putString("fragmentType","strength");
        editor.apply();
        if(adapter.checkPopupWindow())
            adapter.createNotification();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.onResume();//saves the popup window status
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
            Bundle b=new Bundle();
            b.putString("exercise",exerciseName);
            b.putString("type","strength");
            graphFrag frag=new graphFrag();
            frag.setArguments(b);
            getFragmentManager().beginTransaction().replace(R.id.frameLayout,frag,exerciseName).addToBackStack(exerciseName).commit();
            return true;
        }
        //instructions option has been selected
        else if(item.getItemId()==R.id.instructions){
            Instructions instructions=new Instructions();
            getFragmentManager().beginTransaction().replace(R.id.frameLayout,instructions,"Instructions").
                    addToBackStack("Instructions").commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
