package com.example.abhishek.workoutdiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 06/12/17.
 */

public class graphFrag extends Fragment {
    CombinedChart combinedChart;
    customDatabase cdb;
    cardioData cd;
    Context context;
    String exercise;
    View view;

    /*
    sets the graph and sets up the variables for the databases
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle){
        view=inflater.inflate(R.layout.graph_frag,container,false);
        combinedChart=view.findViewById(R.id.combinedChart);
        setHasOptionsMenu(true);

        context=getContext();
        cdb=new customDatabase(context);
        cd=new cardioData(context);
        Bundle b=getArguments();
        exercise=b.getString("exercise");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(exercise);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(combinedChart!=null) {
            boolean unit = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imperial",true);
            createGraph(unit);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //settings option is selected
            case R.id.preference:
                FragmentManager manager=getFragmentManager();
                manager.beginTransaction().replace(R.id.frameLayout,new Settings()).addToBackStack(exercise).commit();
                return true;
                //instructions option is selected
            case R.id.instructions:
                Instructions instructions=new Instructions();
                getFragmentManager().beginTransaction().replace(R.id.frameLayout,instructions,"Instructions").
                        addToBackStack("Instructions").commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //release resources
    @Override
    public void onDestroy() {
        cdb.close();
        cd.close();
        super.onDestroy();
    }

    //save state
    @Override
    public void onPause() {
        String type=getArguments().getString("type");
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(getString(R.string.saveState),"Graph");
        editor.putString("graphSavedExercise",exercise);
        editor.putString("graphSavedType",type);
        editor.apply();
        super.onPause();
    }

    //creates tje graph
    //input: user selected unit
    private void createGraph(boolean unit){
        fillcombinedChart(unit);
        combinedChart.setDrawGridBackground(false);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getAxisLeft().setDrawGridLines(false);
        combinedChart.setBackgroundColor(ContextCompat.getColor(context,R.color.gray));
        combinedChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        combinedChart.animateY(1000);
        combinedChart.setVisibleXRangeMaximum(4);
        combinedChart.moveViewToX(combinedChart.getXRange());
        combinedChart.setNestedScrollingEnabled(true);
        combinedChart.setDoubleTapToZoomEnabled(true);
        combinedChart.getXAxis().setAxisMaximum(combinedChart.getData().getXMax()+0.25f);
        combinedChart.getXAxis().setAxisMinimum(combinedChart.getData().getXMin()-0.25f);
        combinedChart.invalidate();
    }
    /*
    fill up the graph with the required data before displaying it
    input: user selected unit for the exercise
     */
    private void fillcombinedChart(boolean unit){
        String type=getArguments().getString("type");
        List<Entry> lineEntries=new ArrayList<>();
        final List<String>labels=new ArrayList<>();
        LineDataSet dataSet;
        //graph for strength exercise
        if(type!=null&&type.equalsIgnoreCase("strength")){
            //obtains the exercise from the database into the exercise object
            Exercise e=cdb.getExerciseValues(exercise);
            int len=e.getDates().size();
            for(int i=len-1;i>=0;i--){
                //obtains the total workout value for the particular date
                float total=(float)e.getDates().get(i).getTotal();
                if(!unit)total=convertToMetric("weight",total+"");
                //adds it into the entries for the y axis
                lineEntries.add(new Entry((float)lineEntries.size(),total));
                labels.add(convertFormat(e.getDates().get(i).getDate()));
            }
            //set up the x axis of the graph
            XAxis xAxis=combinedChart.getXAxis();
            xAxis.setAxisMinimum(0f);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return labels.get((int)value%labels.size());

                }
            });
            //set up the data
            if(unit)
                dataSet=new LineDataSet(lineEntries,"Total Work Capacity: Weight(lbs)*Reps");
            else
                dataSet=new LineDataSet(lineEntries,"Total Work Capacity: Weight(kg)*Reps");
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setLineWidth(2.5f);
            setMode(dataSet);
            LineData data=new LineData(dataSet);
            CombinedData combinedData=new CombinedData();
            combinedData.setData(data);
            combinedChart.setData(combinedData);
            //get display metrics to create graph of appropriate size
            DisplayMetrics metrics=new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float widthOffset=metrics.widthPixels*0.1f;
            //set display size
            combinedChart.setViewPortOffsets(widthOffset,10,widthOffset,widthOffset);
            //set up the y axis
            YAxis yAxis=combinedChart.getAxisLeft();
            yAxis.setAxisMinimum(dataSet.getYMin()-0.2f*dataSet.getYMin());
            dataSet.setDrawCircles(true);
            combinedChart.setDrawGridBackground(false);
            dataSet.setDrawFilled(true);
            dataSet.setColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            dataSet.setFillColor(ContextCompat.getColor(context,R.color.colorAccent));
            combinedChart.getXAxis().setDrawGridLines(true);
        }
        //cardio exercise graph
        else if(type!=null){
            //get cardio exercise from database
            cardioExercise e=cd.getExercise(exercise);
            List<BarEntry>barEntries=new ArrayList<>();
            int len=e.getDates().size();
            for(int i=len-1;i>=0;i--){
                //get workout value to display progress
                float total=(float)e.getDates().get(i).getTotal();
                if(!unit)
                    total=convertToMetric("distance",total+"");
                lineEntries.add(new Entry(lineEntries.size(),total));
                barEntries.add(new BarEntry(barEntries.size(),e.getDates().get(i).getTotal_cals()/50f));
                labels.add(convertFormat((e.getDates().get(i).getDate())));
            }
            //get display metrics to create graph of appropriate size
            DisplayMetrics metrics=new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float widthOffset=metrics.widthPixels*0.1f;
            //set up the display size
            combinedChart.setViewPortOffsets(widthOffset,10,widthOffset,widthOffset);
            //set up the x axis
            XAxis xAxis=combinedChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setAxisMinimum(0f);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return labels.get((int)value%labels.size());
                }
            });
            //set the data
            CombinedData combinedData=new CombinedData();
            if(unit)
                dataSet=new LineDataSet(lineEntries,"Total Distance Travelled(miles)");
            else
                dataSet=new LineDataSet(lineEntries,"Total Distance Travelled(kms)");
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setCircleColor(Color.DKGRAY);
            dataSet.setColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            dataSet.setLineWidth(2.5f);
            setMode(dataSet);
            dataSet.setDrawCircles(true);
            dataSet.setDrawFilled(true);
            dataSet.setColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            dataSet.setFillColor(ContextCompat.getColor(context,R.color.colorPrimary));
            LineData lineData=new LineData(dataSet);
            combinedData.setData(lineData);
            //get previous state of the app
            //get whether calories is required by the user or not for the graph
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("graph_calories",true)) {
                BarDataSet dataSet1 = new BarDataSet(barEntries, "Calories Consumed / 50");
                dataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
                BarData barData = new BarData(dataSet1);
                dataSet1.setDrawValues(false);
                barData.setBarWidth(0.1f);
                dataSet1.setColor(ContextCompat.getColor(context, R.color.colorAccent));
                combinedData.setData(barData);
                combinedChart.getXAxis().setDrawGridLines(false);
            }
            else{
                combinedData.setData(new BarData());
                combinedChart.getXAxis().setDrawGridLines(true);
            }
            combinedChart.setData(combinedData);
            combinedChart.getAxisLeft().setAxisMinimum(0f);
        }
    }
    /*
    method to convert pounds to kg and miles to meters
     */
    private float convertToMetric(String what,String val){
        if(what.equalsIgnoreCase("weight")){
            double w=Double.parseDouble(val);
            double res=w*0.45359237000000041107;
            res=Math.round(res*10D)/10D;
            return (float)res;
        }
        else{
            double d=Double.parseDouble(val);
            double res=d*1.6093440000000001078;
            res=Math.round(res*10D)/10D;
            return (float)res;
        }
    }
    //modes of the line graph
    private void setMode(LineDataSet dataSet){
        String mode=PreferenceManager.getDefaultSharedPreferences(context).getString("modes","Linear");
        if(mode.equalsIgnoreCase("linear"))
            dataSet.setMode(LineDataSet.Mode.LINEAR);
        else if(mode.equalsIgnoreCase("cubic"))
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        else if(mode.equalsIgnoreCase("stepped"))
            dataSet.setMode(LineDataSet.Mode.STEPPED);
        else
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
    }
    //method to convert the format of the date to standard format
    //input: date text
    //output:formatted date
    private String convertFormat(String date){
        DateFormat df=DateFormat.getDateInstance(DateFormat.LONG);
        DateFormat fd=new SimpleDateFormat("MM/dd/yy");
        try{
            return fd.format((df.parse(date)));
        }catch (ParseException p){
            return "Could not Convert";
        }
    }
}
