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
import android.widget.Toast;

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
    customAdapter(Context context, RecyclerView recyclerView, Exercise exercise, DisplayMetrics metrics){
        this.context=context;
        this.recyclerView=recyclerView;
        this.exercise=exercise;
        this.metrics=metrics;
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
            Button positive=dialog.getButton(AlertDialog.BUTTON_POSITIVE);final View view=v;
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
            e1.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
            e2.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
            e1.setSingleLine();e2.setSingleLine();
            KeyListener key= DigitsKeyListener.getInstance(false,true);
            e1.setKeyListener(key);e2.setKeyListener(key);
            LinearLayout ll=new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            t1=getTextView(R.string.Weight);t2=getTextView(R.string.Reps);
            LinearLayout layout=new LinearLayout(context);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);

            params.setMarginStart(20);
            params.setLayoutDirection(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(params);
            layout.addView(t1);layout.addView(e1);
            ll.addView(layout);
            layout=new LinearLayout(context);layout.setLayoutParams(params);
            layout.addView(t2);layout.addView(e2);
            ll.addView(layout);
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
    }
    /*
    method to create a textview of appropriate styling
    input: integer denoting the id of the textview text
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


    class customViewHolder extends RecyclerView.ViewHolder{
        private Button dateName;//contains the date
        private ImageButton insert,delete;//insert a value, delete the date,
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

}
