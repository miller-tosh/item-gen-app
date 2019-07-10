package com.example.itemgenerator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import java.math.BigInteger;
import java.util.Arrays;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    ZXingScannerView ScanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanView = new ZXingScannerView(this);
        setContentView(ScanView);
    }

    /*
    Call item generator using raw results of QR scan, then return to main menu
     */
    @Override
    public void handleResult(Result rawResult) {
        ItemGenerator.generateItem(rawResult.getRawBytes());
        onBackPressed();
    }

    @Override
    protected void onPause(){
        super.onPause();
        ScanView.stopCamera();
    }

    @Override
    protected void onResume(){
        super.onResume();
        ScanView.setResultHandler(this);
        ScanView.startCamera();
    }
}
