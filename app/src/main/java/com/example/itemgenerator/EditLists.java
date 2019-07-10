package com.example.itemgenerator;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class EditLists extends AppCompatActivity {

    ImageButton add;
    EditText textEntry;
    Spinner itemType;

    LinearLayout armorView;
    LinearLayout weaponView;
    LinearLayout eleView;

    String[] customArmor;
    String[] customWeapon;
    String[] customElement;

    int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lists);

        // Get objects and views
        add = findViewById(R.id.add_button);
        textEntry = findViewById(R.id.custom_item);
        itemType = findViewById(R.id.select_type);
        armorView = findViewById(R.id.custom_armor);
        weaponView = findViewById(R.id.custom_weapons);
        eleView = findViewById(R.id.custom_elements);

        // Get saved custom items from shared preferences
        String savedArmor = MainActivity.sharedPrefs.getString(MainActivity.LIST_ARMOR, " ");
        String savedWeapons = MainActivity.sharedPrefs.getString(MainActivity.LIST_WEAPONS, " ");
        String savedEle = MainActivity.sharedPrefs.getString(MainActivity.LIST_ELEMENTS, " ");

        // Check for saved items
        if(!savedArmor.equals(" ")){
            customArmor = savedArmor.split(",");
            displayCustomItems(customArmor, 0);
        }

        if(!savedWeapons.equals(" ")){
            customWeapon = savedWeapons.split(",");
            displayCustomItems(customWeapon, 1);
        }

        if(!savedEle.equals(" ")){
            customElement = savedEle.split(",");
            displayCustomItems(customElement, 2);
        }

        // Create action for add button
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String updating = " ";
                SharedPreferences.Editor edit = MainActivity.sharedPrefs.edit();

                // Do nothing if no text has been entered
                if(textEntry.getText().toString().matches(""))
                    return;

                // Get custom entry type, 0: Armor, 1: Weapon, 2: Element
                type = itemType.getSelectedItemPosition();

                // Get list which will be added to based on custom item type
                if(type == 0)
                    updating = MainActivity.sharedPrefs.getString(MainActivity.LIST_ARMOR, " ");
                else if(type == 1)
                    updating = MainActivity.sharedPrefs.getString(MainActivity.LIST_WEAPONS, " ");
                else
                    updating = MainActivity.sharedPrefs.getString(MainActivity.LIST_ELEMENTS, " ");

                // Update list
                if(updating.matches(" "))
                    updating = textEntry.getText().toString();
                else
                    updating = updating.concat("," + textEntry.getText().toString());

                if(type == 0)
                    edit.putString(MainActivity.LIST_ARMOR, updating);
                else if(type == 1)
                    edit.putString(MainActivity.LIST_WEAPONS, updating);
                else
                    edit.putString(MainActivity.LIST_ELEMENTS, updating);

                // Save updates
                edit.commit();

                // Add to display
                String[] newItem = {textEntry.getText().toString()};
                displayCustomItems(newItem, type);
            }
        });
    }

    // Displays contents of items array in appropriate LinearLayout view based on itemType
    void displayCustomItems(String[] items, int itemType){
        LinearLayout parentView;

        // Get view that item will be displayed in
        if(itemType == 0)
            parentView = armorView;
        else if(itemType == 1)
            parentView = weaponView;
        else
            parentView = eleView;

        // Create each entry
        for(int i = 0; i < items.length; i++) {
            LinearLayout entryLayout = new LinearLayout(this);
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);
            entryLayout.setGravity(Gravity.CENTER);

            TextView entry = new TextView(this);
            entry.setText(items[i]);
            entry.setGravity(Gravity.CENTER);
            entry.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            entryLayout.addView(entry);

            // Add deletion button
            ImageButton delete = new ImageButton(this);
            delete.setOnClickListener(altClick);
            delete.setImageResource(android.R.drawable.ic_delete);
            delete.setBackgroundColor(Color.TRANSPARENT);
            entryLayout.addView(delete);

            // Add entry to layout view
            parentView.addView(entryLayout);
        }
    }

    // OnClickListener for delete button
    private View.OnClickListener altClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onClickAlt(view);
        }
    };

    // Deletion of custom item
    private void onClickAlt(View view){
        int pos = 0;
        int itemType = 0;

        // Get parent views from button
        ViewGroup parent = (ViewGroup) view.getParent();
        ViewGroup grandparent = (ViewGroup) parent.getParent();

        // Get item type by finding out view parent type, defaults to armor type
        if(parent.getParent() == findViewById(R.id.custom_weapons))
            itemType = 1;
        if(parent.getParent() == findViewById(R.id.custom_elements))
            itemType = 2;

        // Find int position in linear layout for removal
        for(int i = 1; i < grandparent.getChildCount(); i++){
            if(grandparent.getChildAt(i) == parent){
                pos = i;
                break;
            }
        }

        // Update list of saved items
        updateList(pos - 1, itemType);    // account for weird indexing

        // Remove entry view
        ((ViewManager) grandparent).removeView(parent);
    }

    // Update shared preferences and current view to handle item removing
    private void updateList(int pos, int type){
        SharedPreferences.Editor edit = MainActivity.sharedPrefs.edit();
        String updated = " ";
        String[] oldList;

        // Get list type
        if(type == 0)
            oldList = customArmor;
        else if(type == 1)
            oldList = customWeapon;
        else
            oldList = customElement;

        // Loop through current list of items
        for(int i = 0; i < oldList.length; i++) {
            // Skip deleted item
            if (i == pos)
                continue;

            // Re-add item otherwise
            if(updated.equals(" "))
                updated = oldList[i];
            else
                updated = updated.concat("," + oldList[i]);
        }

        // Update shared preferences
        if(type == 0)
            edit.putString(MainActivity.LIST_ARMOR, updated);
        if(type == 1)
            edit.putString(MainActivity.LIST_WEAPONS, updated);
        else
            edit.putString(MainActivity.LIST_ELEMENTS, updated);

        edit.commit();

        // Reload shared preferences
        String savedArmor = MainActivity.sharedPrefs.getString(MainActivity.LIST_ARMOR, " ");
        String savedWeapons = MainActivity.sharedPrefs.getString(MainActivity.LIST_WEAPONS, " ");
        String savedEle = MainActivity.sharedPrefs.getString(MainActivity.LIST_ELEMENTS, " ");
        customArmor = savedArmor.split(",");
        customWeapon = savedWeapons.split(",");
        customElement = savedEle.split(",");
    }

}
