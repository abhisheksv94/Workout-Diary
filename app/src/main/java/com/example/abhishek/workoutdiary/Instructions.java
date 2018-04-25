package com.example.abhishek.workoutdiary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Abhishek on 30/11/17.
 */

public class Instructions extends Fragment {
    //fragment to display the instructions to the user

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.instructions_layout,container,false);
        TextView instructionsHeader=view.findViewById(R.id.instructionsTextHeader);
        TextView instructions=view.findViewById(R.id.instructionsText);
        String s=getString(R.string.instruction);
        String header=getString(R.string.instructionHeader);
        instructions.setText(s);
        instructionsHeader.setText(header);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem m1=menu.findItem(R.id.preference),m2=menu.findItem(R.id.workoutGraph),
                m3=menu.findItem(R.id.instructions);
        m1.setVisible(false);m2.setVisible(false);m3.setVisible(false);
    }
}
