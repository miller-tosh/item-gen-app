package com.example.itemgenerator;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Saved settings strings
    public static final String TYPE_ITEMS = "itemSettings";
    public static final String TYPE_BONUS_ELE = "eleBonusSettings";     // element damage
    public static final String TYPE_BONUS = "bonusSettings";            // bonus dice type
    public static final String LIST_ARMOR = "armorSettings";
    public static final String LIST_WEAPONS = "weaponSettings";
    public static final String LIST_ELEMENTS = "eleSettings";
    public static final String SETTINGS_INITIALIZED = "initialized";

    // Fields and widgets which pertain to the stats of a generated item
    static boolean itemShown = false;   // a generated item is currently being displayed and can be saved
    static boolean updateSettings = true;   // need to update the lists to included edited items
    static TextView itemName;
    static TextView itemType;
    static TextView itemLevel;
    static TextView itemBonus;
    Button saveResult;
    Spinner changeLevel;

    public static SharedPreferences sharedPrefs;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create shared preferences to edit/save settings
        sharedPrefs = getApplicationContext().getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPrefs.edit();

        if(sharedPrefs.getBoolean(SETTINGS_INITIALIZED, false))
            initializeSettings();

        // Get item descriptor text views
        itemName = findViewById(R.id.item_name);
        itemType = findViewById(R.id.item_element);
        itemLevel = findViewById(R.id.dice_number);
        itemBonus = findViewById(R.id.dice_type);

        saveResult = findViewById(R.id.save_button);
        changeLevel = findViewById(R.id.level_spinner);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get permissions to use camera (if permission not already granted)
        if (!(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);

        // Change level
        changeLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String level = (i + 1) + " ";
                itemLevel.setText(level);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Request to save results
        saveResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Only save the item once (unless you rescan a code)
                if (itemShown) {
                    SharedPreferences.Editor edit = sharedPrefs.edit();

                    String items = sharedPrefs.getString(TYPE_ITEMS, " ");
                    String ele = sharedPrefs.getString(TYPE_BONUS_ELE, " ");
                    String bonus = sharedPrefs.getString(TYPE_BONUS, " ");

                    if(items.equals(" "))
                        items = itemName.getText().toString();
                    else
                        items = items.concat("," + itemName.getText().toString());
                    if(ele.equals(" "))
                        ele = itemType.getText().toString();
                    else
                        ele = ele.concat("," + itemType.getText().toString());
                    if(bonus.equals(" "))
                        bonus = itemBonus.getText().toString();
                    else
                        bonus = bonus.concat("," + itemBonus.getText().toString());

                    edit.putString(TYPE_ITEMS, items);
                    edit.putString(TYPE_BONUS_ELE, ele);
                    edit.putString(TYPE_BONUS, bonus);

                    edit.commit();

                    itemShown = false;

                    Toast.makeText(view.getContext(), "Item Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        // Scan QR code
        if (id == R.id.nav_camera) {
            if(updateSettings)
                addSettings();
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                startActivity(new Intent(getApplicationContext(), Scanner.class));
            else
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
        // View saved items
        else if (id == R.id.nav_saved) {
            startActivity(new Intent(getApplicationContext(), SavedItems.class));
        }

        // Edit generator lists
        else if (id == R.id.nav_edit) {
            startActivity(new Intent(getApplicationContext(), EditLists.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // The following method written by Rohan Patil, requests permission to use camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Takes saved preferences and adds user-added items/elements to lists used for generation
    public void addSettings(){
        String[] aList;
        String[] wList;
        String[] eList;

        String armor = sharedPrefs.getString(LIST_ARMOR, " ");
        String weapons = sharedPrefs.getString(LIST_WEAPONS, " ");
        String ele = sharedPrefs.getString(LIST_ELEMENTS, " ");

        // Split into arrays and concatenate onto generator lists
        if(!armor.equals(" ")){
            aList = armor.split(",");
            ItemGenerator.editArmor = ArrayUtils.concat(ItemGenerator.Armor, aList);
        }

        if(!weapons.equals(" ")){
            wList = weapons.split(",");
            ItemGenerator.editWeapons = ArrayUtils.concat(ItemGenerator.Weapons, wList);
        }

        if(!ele.equals(" ")){
            eList = ele.split(",");
            ItemGenerator.editEle = ArrayUtils.concat(ItemGenerator.Elements, eList);
        }

        updateSettings = false;
    }

    // If settings haven't been initialized yet
    public void initializeSettings(){
        SharedPreferences.Editor edit = sharedPrefs.edit();

        edit.putString(TYPE_ITEMS, " ");
        edit.putString(TYPE_BONUS_ELE, " ");
        edit.putString(TYPE_BONUS, " ");
        edit.putString(LIST_ARMOR, " ");
        edit.putString(LIST_WEAPONS, " ");
        edit.putString(LIST_ELEMENTS, " ");
        edit.putBoolean(SETTINGS_INITIALIZED, true);

        edit.apply();
    }
}
