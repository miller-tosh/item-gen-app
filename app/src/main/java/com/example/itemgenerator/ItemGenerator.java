package com.example.itemgenerator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import java.math.BigInteger;
import java.util.Arrays;
import java.lang.Math;


public class ItemGenerator extends AppCompatActivity {

    // Number of elements used to decide item: {weapon vs armor, item name, element bonus, bonus value}
    public static final int GENERATION_ELEMENTS = 4;

    // Item and element lists credited to D&D 5e manual
    static final String[] Weapons = {"Club", "Dagger", "Greatclub", "Handaxe", "Javelin", "Light Hammer", "Mace", "Quarterstaff", "Sickle", "Spear",
    "Light Crossbow", "Dart", "Shortbow", "Sling", "Blowgun", "Heavy Crossbow", "Hand Crossbow", "Longbow", "Battleaxe", "Flail",
    "Glaive", "Greataxe", "Greatsword", "Halberd", "Lance", "Longsword", "Maul", "Morningstar", "Pike", "Rapier", "Scimitar", "Shortsword",
    "Trident", "Warpick", "Warhammer", "Whip", "Crystal", "Orb", "Rod", "Staff", "Wand", "Totem", "Wooden Staff", "Yew Wand", "Amulet",
    "Emblem", "Reliquary"};

    static final String[] Armor = {"Padded Armor", "Leather Armor", "Studded Leather Armor", "Hide Armor", "Chain Shirt", "Scale Mail", "Breastplate", "Half Plate",
    "Ring Mail", "Chain Mail", "Splint", "Plate", "Shield"};

    static final String[] Elements = {"Acid", "Bludgeoning", "Cold", "Fire", "Force", "Lightning", "Necrotic", "Piercing", "Poison", "Psychic",
    "Radiant", "Slashing", "Thunder"};

    static final String[] BonusDice = {"D4", "D6", "D8", "D12"};

    // Versions which will be updated with saved settings
    static String[] editWeapons = Weapons;
    static String[] editArmor = Armor;
    static String[] editEle = Elements;


    static public void generateItem(byte[] result){
        String item ;
        String ele ;
        String bonus;
        boolean weapon = true;
        int i;
        byte[] curBytes;    // bytes that will be worked with in each section
        int byteBuf;        // how many bytes will be used to get each item, depends on size of item/element list

        // Will be used for item generation
        byte[] rBytes = result;
        int byteCount = rBytes.length;
        int byteRange = byteCount / 4;            // used to divide bytes into four sections

        // Not enough bytes to generate an item
        if(byteCount < GENERATION_ELEMENTS){
            MainActivity.itemShown = false;
            return;
        }

        // Determine if weapon or armor
        curBytes = Arrays.copyOfRange(rBytes, 0, 1);
        for(i = 1; i < byteRange; i++){
            curBytes[0] = (byte) (curBytes[0] ^ rBytes[i]);
        }
        weapon = (curBytes[0] % 2) == 1;

        // Check for enough bytes to work with by getting number of bytes needed to represent length of element list
        if(byteRange < (byteBuf = (int) Math.ceil(editEle.length / (double) 8)))
            curBytes = Arrays.copyOfRange(rBytes, byteRange, byteRange + byteBuf);

        // XOR bytes in segment
        else{
            for(i = byteRange + byteBuf; i < rBytes.length; i += byteBuf)
                curBytes = xorBytes(curBytes, Arrays.copyOfRange(rBytes, i, i + byteBuf));
        }

        // Get item from element list after converting curBytes to int and taking mod
        int index = Math.abs((new BigInteger(curBytes).intValue())) % editEle.length;
        ele = editEle[index];

        // Item is a weapon, has damage
        if(weapon){
            // Check for enough bytes to work with by getting number of bytes needed to represent length of element list
            if(byteRange < (byteBuf = (int) Math.ceil(editWeapons.length / (double) 8)))
                curBytes = Arrays.copyOfRange(rBytes, byteRange * 2, (byteRange * 2) + byteBuf);    // -1 accounts for starting byte

                // XOR bytes in segment
            else{
                for(i = byteRange + byteBuf; i < rBytes.length; i += byteBuf)
                    curBytes = xorBytes(curBytes, Arrays.copyOfRange(rBytes, i, i + byteBuf));
            }

            // Get weapon from Weapons list
            index = Math.abs((new BigInteger(curBytes).intValue())) % editWeapons.length;
            item = editWeapons[index];

            ele = ele.concat(" Damage");
        }

        // Item is armor, has resistance
        else{
            // Check for enough bytes to work with by getting number of bytes needed to represent length of element list
            if(byteRange < (byteBuf = (int) Math.ceil(editArmor.length / (double) 8)))
                curBytes = Arrays.copyOfRange(rBytes, byteRange * 2, (byteRange * 2) + byteBuf);    // -1 accounts for starting byte

                // XOR bytes in segment
            else{
                for(i = byteRange + byteBuf; i < rBytes.length; i += byteBuf)
                    curBytes = xorBytes(curBytes, Arrays.copyOfRange(rBytes, i, i + byteBuf));
            }

            // Get item from element list after converting curBytes to int and taking mod
            index = Math.abs((new BigInteger(curBytes).intValue())) % editArmor.length;
            item = editArmor[index];

            ele = ele.concat(" Resistance");
        }

        // Determine bonus
        curBytes = Arrays.copyOfRange(rBytes, byteRange * 3, (byteRange * 3) + 1);
        for(i = (byteRange * 3) + 1; i < rBytes.length; i++){
            curBytes[0] = (byte) (curBytes[0] ^ rBytes[i]);
        }
        bonus = BonusDice[(Math.abs(curBytes[0])) % BonusDice.length];

        // Set values on main menu
        MainActivity.itemShown = true;
        MainActivity.itemName.setText(item);
        MainActivity.itemType.setText(ele);
        MainActivity.itemBonus.setText(bonus);
        MainActivity.itemLevel.setText("1 ");    // default value
    }

    // XOR a byte array, assumes arrays a and b are the same length
    static private byte[] xorBytes(byte[] a, byte[] b){
        byte[] c = a;
        for(int i = 0; i < a.length; i++)
            c[i] = (byte) (a[i] ^ b[i]);
        return c;
    }
}
