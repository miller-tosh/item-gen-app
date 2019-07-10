package com.example.itemgenerator;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.util.ArrayUtils;

public class SavedItems extends AppCompatActivity implements View.OnClickListener {

    String[] items;
    String[] element;
    String[] bonus;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        String savedItem = MainActivity.sharedPrefs.getString(MainActivity.TYPE_ITEMS, " ");
        String savedEle = MainActivity.sharedPrefs.getString(MainActivity.TYPE_BONUS_ELE, " ");
        String savedBonus = MainActivity.sharedPrefs.getString(MainActivity.TYPE_BONUS, " ");

        // Will be potentially added to
        linearLayout = findViewById(R.id.settings_layout);

        // There are saved items
        if(!savedItem.equals(" ")){
            findViewById(R.id.no_saved_items).setVisibility(View.INVISIBLE);

            items = savedItem.split(",");
            element = savedEle.split(",");
            bonus = savedBonus.split(",");

            // Make entries for saved items
            for(int i = 0; i < items.length; i++){

                // Create linear layout for each entry
                LinearLayout entryLayout = new LinearLayout(this);
                entryLayout.setOrientation(LinearLayout.HORIZONTAL);
                entryLayout.setGravity(Gravity.CENTER);

                // Create actual entry
                TextView entry = new TextView(this);
                String itemText = "<b>Item: </b>" + items[i] + "<br><b>Element Type: </b>" + element[i] + "<br><b>Dice Type: </b>" + bonus[i];
                entry.setText(Html.fromHtml(itemText));
                entry.setGravity(Gravity.CENTER);
                entry.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                entryLayout.addView(entry);

                // Add deletion button
                ImageButton delete = new ImageButton(this);
                delete.setOnClickListener(this);
                delete.setImageResource(android.R.drawable.ic_delete);
                delete.setBackgroundColor(Color.TRANSPARENT);
                entryLayout.addView(delete);

                // Add entry to layout view
                linearLayout.addView(entryLayout);
            }
        }
    }

    @Override
    public void onClick(View view){
        int pos = 0;

        // Get parent view from button
        ViewGroup parent = (ViewGroup) view.getParent();

        // Find int position in linear layout for removal
        for(int i = 0; i < linearLayout.getChildCount(); i++){
            if(linearLayout.getChildAt(i) == parent){
                pos = i;
                break;
            }
        }

        // Update list of saved items
        updateList(pos - 1);    // account for weird indexing

        // Remove entry view
        ((ViewManager) linearLayout).removeView(parent);
    }

    // Update shared preferences and current view to handle item removing
    private void updateList(int pos){
        String updatedItems = " ";
        String updatedEle = " ";
        String updatedBonus = " ";
        SharedPreferences.Editor edit = MainActivity.sharedPrefs.edit();

        // Loop through current list of items
        for(int i = 0; i < items.length; i++){
            // Skip deleted item
            if(i == pos)
                continue;

            // Re-add item otherwise
            if(updatedItems.equals(" "))
                updatedItems = items[i];
            else
                updatedItems = updatedItems.concat("," + items[i]);
            if(updatedEle.equals(" "))
                updatedEle = element[i];
            else
                updatedEle = updatedEle.concat("," + element[i]);
            if(updatedBonus.equals(" "))
                updatedBonus = bonus[i];
            else
                updatedBonus = updatedBonus.concat("," + bonus[i]);
        }

        // Update shared preferences
        edit.putString(MainActivity.TYPE_ITEMS, updatedItems);
        edit.putString(MainActivity.TYPE_BONUS_ELE, updatedEle);
        edit.putString(MainActivity.TYPE_BONUS, updatedBonus);

        edit.commit();

        // Reload shared preferences
        String savedItem = MainActivity.sharedPrefs.getString(MainActivity.TYPE_ITEMS, " ");
        String savedEle = MainActivity.sharedPrefs.getString(MainActivity.TYPE_BONUS_ELE, " ");
        String savedBonus = MainActivity.sharedPrefs.getString(MainActivity.TYPE_BONUS, " ");
        items = savedItem.split(",");
        element = savedEle.split(",");
        bonus = savedBonus.split(",");
    }
}
