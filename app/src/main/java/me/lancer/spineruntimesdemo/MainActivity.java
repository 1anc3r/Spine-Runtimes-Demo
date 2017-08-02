package me.lancer.spineruntimesdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnSpineBoy, btnDragon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSpineBoy = (Button) findViewById(R.id.btn_spine_boy);
        btnSpineBoy.setOnClickListener(vOnClickListener);

        btnDragon = (Button) findViewById(R.id.btn_dragon);
        btnDragon.setOnClickListener(vOnClickListener);
    }

    View.OnClickListener vOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btnSpineBoy){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SpineBoyActivity.class);
                startActivity(intent);
            } else if (view == btnDragon){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, DragonActivity.class);
                startActivity(intent);
            }
        }
    };
}
