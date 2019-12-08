package com.example.abhishek.workoutdiary;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.abhishek.workoutdiary.Interfaces.DrawerListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created by Abhishek on 30/11/17.
 */

public class Instructions extends Fragment {
    //fragment to display the instructions to the user

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.instructions_layout,container,false);
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
        FloatingActionButton drawerOpenButton = view.findViewById(R.id.open_drawer);
        drawerOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity == null) return;
                try {
                    DrawerListener d = (DrawerListener)activity;
                    d.onDrawerOpen();
                }catch (ClassCastException E) {
                    Log.e("Something", "Could not cast activity");
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }
}
