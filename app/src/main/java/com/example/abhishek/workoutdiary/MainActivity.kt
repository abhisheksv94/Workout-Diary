package com.example.abhishek.workoutdiary

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.abhishek.workoutdiary.Interfaces.DrawerListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DrawerListener {
    private var exerciseList = mutableListOf<String>()
    private var cardioList = mutableListOf<String>()
    private var db: customDatabase = customDatabase(this)
    private var data = cardioData(this)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var nav: NavigationView
    private lateinit var cardioNav: NavigationView
    private var unit = false
    /*
    initialize variables and set the drawer listener
    load the previous state of the activity if any
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerlayout)
        //        Rect r = drawerLayout.cop
//        drawerLayout.setSystemGestureExclusionRects(rr);
        exerciseList = db.exercises
        cardioList = data.exerciseNames
        nav = findViewById(R.id.nav)
        nav.setNavigationItemSelectedListener(this)
        cardioNav = findViewById(R.id.CardioNav)
        cardioNav.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        //if state wasn't saved before navigate to first exercise
        unit = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.sharedPreferenceBoolean), false)
        if (nav.menu.size() > 1) {
            onNavigationItemSelected(nav.menu.getItem(1))
        } else if (cardioNav.menu.size() > 1) {
            onNavigationItemSelected(cardioNav.menu.getItem(1))
        }
        prepareFAB()
        val drawerOpenButton = findViewById<FloatingActionButton>(R.id.open_drawer)
        drawerOpenButton.setOnClickListener { onDrawerOpen() }
        for (s in exerciseList) {
            refreshMenu(s, false)
        }
        for (s in cardioList) refreshMenu(s, true)
        //get previous state if any
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val name = preferences.getString(getString(R.string.saveState), "")
        if (name != null && name.isNotEmpty()) {
            if (name.equals("graph", ignoreCase = true)) {
                val b = Bundle()
                b.putString("exercise", preferences.getString("graphSavedExercise", ""))
                b.putString("type", preferences.getString("graphSavedType", ""))
                val frag = graphFrag()
                val graphSavedExercise = preferences.getString("graphSavedExercise", "")
                if (graphSavedExercise != null && graphSavedExercise.isNotEmpty()) {
                    frag.arguments = b
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, frag).addToBackStack("Graph").commit()
                }
            } else if (name.equals("Settings", ignoreCase = true)) {
                val f = findViewById<FrameLayout>(R.id.frameLayout)
                f.removeAllViews()
                supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Settings()).addToBackStack("Settings").commit()
            } else if (exerciseList.size > 0 || cardioList.size > 0) {
                if (preferences.getString("fragmentType", "") == "strength") {
                    if (exerciseList.contains(name)) onNavigationItemSelected(nav.menu.getItem(exerciseList.indexOf(name) + 1)) else onNavigationItemSelected(nav.menu.getItem(1))
                } else {
                    if (cardioList.contains(name)) onNavigationItemSelected(cardioNav.menu.getItem(cardioList.indexOf(name) + 1)) else onNavigationItemSelected(cardioNav.menu.getItem(1))
                }
            }
        }
    }

    //to refresh the menu for the navigation view and repopulate the view
/*
    input: exercise name and whether its cardio or not
    output: index of recently added exercise
     */
    private fun refreshMenu(s: String, iscardio: Boolean): Int {
        val menu: Menu
        val index: Int
        if (!iscardio) {
            menu = nav.menu
            val len = menu.size()
            for (i in 1..len) {
                menu.removeItem(i)
            }
            for (i in exerciseList.indices) {
                exerciseAdded(exerciseList[i], i, false)
            }
            index = exerciseList.indexOf(s) + 1
        } else {
            menu = cardioNav.menu
            val len = menu.size()
            for (i in 1..len) {
                menu.removeItem(i)
            }
            for (i in cardioList.indices) {
                exerciseAdded(cardioList[i], i, true)
            }
            index = cardioList.indexOf(s) + 1
        }
        return index
    }

    /*
    prepare the floating action button response in the navigation view
    sets up the listener to add a new exercise and navigates to the exercise fragment
     */
    private fun prepareFAB() {
        val fab = findViewById<FloatingActionButton>(R.id.newItem)
        fab.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            val e = EditText(this@MainActivity)
            e.setSingleLine()
            e.hint = "Enter new Exercise"
            e.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            val sp = Spinner(this@MainActivity)
            val vals = arrayOf("Strength", "Cardio")
            val spinnerAdapter: SpinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                    vals)
            sp.adapter = spinnerAdapter
            val l = LinearLayout(this@MainActivity)
            l.orientation = LinearLayout.VERTICAL
            l.addView(e)
            l.addView(sp)
            builder.setView(l)
            builder.setPositiveButton("Done") { dialog, _ ->
                if (e.text == null) dialog.cancel()
                val s = e.text.toString()
                val type = sp.selectedItem.toString()
                if (type.equals("Strength", ignoreCase = true)) {
                    if (!exerciseList.contains(s)) {
                        exerciseList.add(s)
                        val index = refreshMenu(s, false)
                        onNavigationItemSelected(nav.menu.getItem(index))
                    } else Toast.makeText(this@MainActivity, "Exercise is already present", Toast.LENGTH_LONG).show()
                } else {
                    if (!cardioList.contains(s)) {
                        cardioList.add(s)
                        val index = refreshMenu(s, true)
                        onNavigationItemSelected(cardioNav.menu.getItem(index))
                    } else Toast.makeText(this@MainActivity, "Exercise is already present", Toast.LENGTH_LONG).show()
                }
            }
            builder.create().show()
        }
    }

    /*
    adds the exercise to the navigation view after making some ui changes to the text
    also adds dummy headers for the strength and cardio lists
    also adjusts the display height according to which list has more exercises
    input: exercise name, order number in the navigation view, cardio exercise or not
     */
    private fun exerciseAdded(s: String, order: Int, iscardio: Boolean) {
        val string = SpannableString(s)
        string.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length, 0)
        val menu = nav.menu
        val menu1 = cardioNav.menu
        if (menu.size() == 0) {
            val strength = SpannableString("Strength")
            strength.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray)),
                    0, strength.length, 0)
            menu.add(0, R.id.Strength, Menu.NONE, strength)
            menu.findItem(R.id.Strength).isCheckable = false
        }
        if (menu1.size() == 0) {
            val cardio = SpannableString("Cardio")
            cardio.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray)),
                    0, cardio.length, 0)
            menu1.add(0, R.id.Cardio, 0, cardio)
            menu1.findItem(R.id.Cardio).isCheckable = false
        }
        if (iscardio) {
            if (cardioNav.visibility == View.INVISIBLE) cardioNav.visibility = View.VISIBLE
            menu1.add(0, order + 1, order, string)
        } else {
            if (nav.visibility == View.INVISIBLE) nav.visibility = View.VISIBLE
            menu.add(0, order + 1, order, string)
        }
        val p1: ViewGroup.LayoutParams = nav.layoutParams
        val p2: ViewGroup.LayoutParams = cardioNav.layoutParams
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val height = metrics.heightPixels - (22.3 / 100 * metrics.heightPixels).toInt()
        val menuSizeDiff = cardioNav.menu.size() - nav.menu.size()
        when  {
            menuSizeDiff > 0 -> {
                p2.height = (2.0 / 3 * height).toInt()
                p1.height = (1.0 / 3 * height).toInt()
            }
            menuSizeDiff < 0 -> {
                p1.height = (2.0 / 3 * height).toInt()
                p2.height = (1.0 / 3 * height).toInt()
            }
            else -> {
                p2.height = height / 2
                p1.height = p2.height
            }
        }
        cardioNav.requestLayout()
        nav.requestLayout()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    /*
    creates menu items for settings , graph and instructions
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (exerciseList.size == 0 && cardioList.size == 0) return false
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        menu.add(0, R.id.workoutGraph, 0, "See Progress")
        menu.add(0, R.id.instructions, 0, "Instructions")
        menu.add(0, R.id.preference, 0, "Settings")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return toggle.onOptionsItemSelected(item)
    }

    /*
    navigates to the exercise selected
    also adds the previous page into the backstack for the back button to be useful
    input: menu item selected: exercise, settings, graph, instructions
    output: whether it was selected
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val manager = supportFragmentManager
        if (item.itemId == R.id.Strength || item.itemId == R.id.Cardio) {
            return true
        } else if (item.itemId == R.id.preference) {
            val fragment: Fragment = Settings()
            manager.beginTransaction().replace(R.id.frameLayout, fragment, "Settings").addToBackStack("Settings").commit()
            return true
        }
        val f = findViewById<FrameLayout>(R.id.frameLayout)
        f.removeAllViews()
        val fragment: Fragment
        val exercise = item.title.toString()
        fragment = if (exerciseList.contains(exercise)) {
            strengthFrag()
        } else cardioFrag()
        val b = Bundle()
        b.putString("name", exercise)
        fragment.arguments = b
        manager.beginTransaction().replace(R.id.frameLayout, fragment, exercise).addToBackStack(
                exercise).commit()
        manager.addOnBackStackChangedListener {
            val index = manager.backStackEntryCount - 1
            if (index < 0) {
                finishAndRemoveTask()
            } else {
                val title = manager.getBackStackEntryAt(index).name
                setTitle(title)
            }
        }
        drawerLayout.closeDrawer(findViewById<View>(R.id.navMain))
        return true
    }

    override fun onDrawerOpen() {
        drawerLayout.openDrawer(GravityCompat.START)
    }
}