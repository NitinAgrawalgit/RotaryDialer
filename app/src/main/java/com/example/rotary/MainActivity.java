package com.example.rotary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText digits;
    private ImageView backSpace;
    private static int PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RotaryDialerView rotaryDialerView = (RotaryDialerView) findViewById(R.id.rotary_dialer);
        digits = (EditText) findViewById(R.id.digits);
        backSpace = findViewById(R.id.backspace_btn);

        /** To manually ask the user for permission for making calls.
         * This is mandatory according to new android guidelines */
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
        }

        digits.setShowSoftInputOnFocus(false);

        rotaryDialerView.addDialListener(new RotaryDialerView.DialListener() {
            public void onDial(int number) {
                digits.append(String.valueOf(number));
                if(digits.length() == 10){
                    Toast.makeText(MainActivity.this, "Dialing Number...", Toast.LENGTH_SHORT).show();
                    makeCall();
                }
            }
        });

        backSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation clickAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce);
                view.startAnimation(clickAnimation);

                String text = digits.getText().toString();
                if(text.length() > 0){
                    text = text.substring(0, text.length()-1);
                    digits.setText(text);
                }
            }
        });

        backSpace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Animation clickAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce_large);
                clickAnimation.setDuration(200);
                view.startAnimation(clickAnimation);

                digits.setText("");
                return true;
            }
        });
    }

    private void makeCall() {
        if (digits.getText().length() == 10) {
            String toDial = "tel:" + digits.getText().toString().trim();
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(toDial)));
        }
    }
}