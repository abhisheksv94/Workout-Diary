package com.example.abhishek.workoutdiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private List<String> exerciseList,cardioList;
    private customDatabase db;
    private cardioData data;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nav,cardioNav;
    private boolean unit;

    /*
    initialize variables and set the drawer listener
    load the previous state of the activity if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout=findViewById(R.id.drawerlayout);
        db=new customDatabase(this);
        data=new cardioData(this);
        exerciseList=db.getExercises();
        cardioList=data.getExerciseNames();
        nav=findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(this);
        cardioNav=findViewById(R.id.CardioNav);
        cardioNav.setNavigationItemSelectedListener(this);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        try {
            //noinspection ConstantConditions
            //to display the 3 lines icon in the home screen
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException N) {
            Toast.makeText(MainActivity.this, "Oops! Something went wrong!", Toast.LENGTH_SHORT).show();
        }
        //if state wasn't saved before navigate to first exercise
        unit=getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.sharedPreferenceBoolean),false);
        if(nav.getMenu().size()>1){
            onNavigationItemSelected(nav.getMenu().getItem(1));
        }
        else if(cardioNav.getMenu().size()>1){
            onNavigationItemSelected(cardioNav.getMenu().getItem(1));
        }
        prepareFAB();
        for(String s:exerciseList){
            refreshMenu(s,false);
        }
        for(String s:cardioList)
            refreshMenu(s,true);
        //get previous state if any
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String name=preferences.getString(getString(R.string.saveState),"");
        if(name.length()>0){
            if(name.equalsIgnoreCase("graph")){
                Bundle b=new Bundle();
                b.putString("exercise",preferences.getString("graphSavedExercise",""));
                b.putString("type",preferences.getString("graphSavedType",""));
                graphFrag frag=new graphFrag();
                if(preferences.getString("graphSavedExercise","").length()>0) {
                    frag.setArguments(b);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, frag).
                            addToBackStack("Graph").commit();
                }
            }
            else if(name.equalsIgnoreCase("Settings")){
                FrameLayout f=findViewById(R.id.frameLayout);f.removeAllViews();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new Settings()).
                        addToBackStack("Settings").commit();
            }
            else{
                if(preferences.getString("fragmentType","").equals("strength")){
                    onNavigationItemSelected(nav.getMenu().getItem(exerciseList.indexOf(name)+1));
                }
                else{
                    if(cardioList.indexOf(name)!=-1)
                        onNavigationItemSelected(cardioNav.getMenu().getItem(cardioList.indexOf(name)+1));
                }
            }
        }
    }
    //to refresh the menu for the navigation view and repopulate the view
    /*
    input: exercise name and whether its cardio or not
    output: index of recently added exercise
     */
    private int refreshMenu(String s,boolean iscardio){
        Menu menu;int index;
        if(!iscardio) {
            menu = nav.getMenu();
            int len = menu.size();
            for (int i = 1; i <= len; i++) {
                menu.removeItem(i);
            }
            for (int i = 0; i < exerciseList.size(); i++) {
                exerciseAdded(exerciseList.get(i), i, false);
            }
            index=exerciseList.indexOf(s)+1;
        }
        else{
            menu=cardioNav.getMenu();
            int len=menu.size();
            for(int i=1;i<=len;i++){
                menu.removeItem(i);
            }
            for(int i=0;i<cardioList.size();i++){
                exerciseAdded(cardioList.get(i),i,true);
            }
            index=cardioList.indexOf(s)+1;
        }
        return index;
    }

    /*
    prepare the floating action button response in the navigation view
    sets up the listener to add a new exercise and navigates to the exercise fragment
     */
    private void prepareFAB() {
        FloatingActionButton fab = findViewById(R.id.newItem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final EditText e = new EditText(MainActivity.this);
                e.setSingleLine();
                e.setHint("Enter new Exercise");
                e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                final Spinner sp=new Spinner(MainActivity.this);
                String vals[]=new String[]{"Strength","Cardio"};
                SpinnerAdapter spinnerAdapter=new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,
                        vals);
                sp.setAdapter(spinnerAdapter);
                LinearLayout l = new LinearLayout(MainActivity.this);
                l.setOrientation(LinearLayout.VERTICAL);
                l.addView(e);
                l.addView(sp);
                builder.setView(l);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (e.getText() == null) dialog.cancel();
                        String s = e.getText().toString();
                        String type=sp.getSelectedItem().toString();
                        if(type.equalsIgnoreCase("Strength")) {
                            if (!exerciseList.contains(s)) {
                                exerciseList.add(s);
                                int index=refreshMenu(s, false);
                                onNavigationItemSelected(nav.getMenu().getItem(index));
                            }
                            else
                                Toast.makeText(MainActivity.this,"Exercise is already present",Toast.LENGTH_LONG).show();
                        }
                        else{
                            if(!cardioList.contains(s)){
                                cardioList.add(s);
                                int index=refreshMenu(s,true);
                                onNavigationItemSelected(cardioNav.getMenu().getItem(index));
                            }
                            else
                                Toast.makeText(MainActivity.this,"Exercise is already present",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.create().show();
            }
        });
    }
    /*
    adds the exercise to the navigation view after making some ui changes to the text
    also adds dummy headers for the strength and cardio lists
    also adjusts the display height according to which list has more exercises
    input: exercise name, order number in the navigation view, cardio exercise or not
     */
    private void exerciseAdded(String s,int order, boolean iscardio){
        SpannableString string=new SpannableString(s);
        string.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),0,s.length(),0);
        Menu menu=nav.getMenu(),menu1=cardioNav.getMenu();
        if(menu.size()==0){
            SpannableString strength=new SpannableString("Strength");
            strength.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this,R.color.gray)),
                    0,strength.length(),0);
            menu.add(0,R.id.Strength,Menu.NONE, strength);
            menu.findItem(R.id.Strength).setCheckable(false);
        }
        if(menu1.size()==0){
            SpannableString cardio=new SpannableString("Cardio");
            cardio.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this,R.color.gray)),
                    0,cardio.length(),0);
            menu1.add(0,R.id.Cardio,0,cardio);
            menu1.findItem(R.id.Cardio).setCheckable(false);
        }
        if(iscardio){
            if(cardioNav.getVisibility()==View.INVISIBLE)
                cardioNav.setVisibility(View.VISIBLE);
            menu1.add(0,order+1,order,string);
        }
        else{
            if(nav.getVisibility()==View.INVISIBLE)
                nav.setVisibility(View.VISIBLE);
            menu.add(0,order+1,order,string);
        }
        ViewGroup.LayoutParams p1,p2;
        p1=nav.getLayoutParams();p2=cardioNav.getLayoutParams();
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height=metrics.heightPixels-(int)(22.3/100*metrics.heightPixels);
        if(cardioNav.getMenu().size()>nav.getMenu().size()){
            p2.height=(int)((2.0/3)*height);
            p1.height=(int)((1.0/3)*height);
        }
        else if(nav.getMenu().size()>cardioNav.getMenu().size()){
            p1.height=(int)((2.0/3)*height);
            p2.height=(int)((1.0/3)*height);
        }
        else {
            p1.height=p2.height=height/2;
        }
        cardioNav.requestLayout();nav.requestLayout();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }
    /*
    creates menu items for settings , graph and instructions
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        menu.add(0,R.id.workoutGraph,0,"See Progress");
        menu.add(0,R.id.instructions,0,"Instructions");
        menu.add(0,R.id.preference,0,"Settings");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return toggle.onOptionsItemSelected(item);
    }
    /*
    navigates to the exercise selected
    also adds the previous page into the backstack for the back button to be useful
    input: menu item selected: exercise, settings, graph, instructions
    output: whether it was selected
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final android.support.v4.app.FragmentManager manager=getSupportFragmentManager();
        if(item.getItemId()==R.id.Strength||item.getItemId()==R.id.Cardio){
            return true;
        }
        else if(item.getItemId()==R.id.preference){
            Fragment fragment=new Settings();
            manager.beginTransaction().replace(R.id.frameLayout,fragment,"Settings").addToBackStack("Settings").commit();
            return true;
        }
        setTitle(item.getTitle());
        FrameLayout f = findViewById(R.id.frameLayout);
        f.removeAllViews();Fragment fragment;
        String exercise=item.getTitle().toString();
        if(exerciseList.contains(exercise)) {
            fragment = new strengthFrag();
        }
        else
            fragment=new cardioFrag();
        Bundle b=new Bundle();
        b.putString("name",exercise);
        fragment.setArguments(b);
        manager.beginTransaction().replace(R.id.frameLayout,fragment,exercise).addToBackStack(
                exercise).commit();
        manager.addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int index=manager.getBackStackEntryCount()-1;
                if(index<0){
                    MainActivity.this.finishAndRemoveTask();
                }
                else{
                    String title=manager.getBackStackEntryAt(index).getName();
                    setTitle(title);
                }
            }
        });
        drawerLayout.closeDrawer(findViewById(R.id.navMain));
        return true;
    }
}
