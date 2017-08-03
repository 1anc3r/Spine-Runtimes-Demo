package me.lancer.spineruntimesdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AppActivity;

import me.lancer.spineruntimesdemo.MainActivity;
import me.lancer.spineruntimesdemo.R;
import me.lancer.spineruntimesdemo.model.SpineBoy;

public class SpineBoyActivity extends AppActivity {

    SpineBoy spineBoy;
    View spineBoyView;

    Button btnWalk, btnRun, btnJump, btnIdle, btnShoot, btnHit, btnDeath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spine_boy);

        btnWalk = (Button) findViewById(R.id.btn_walk);
        btnWalk.setOnClickListener(vOnClickListener);

        btnRun = (Button) findViewById(R.id.btn_run);
        btnRun.setOnClickListener(vOnClickListener);

        btnJump = (Button) findViewById(R.id.btn_jump);
        btnJump.setOnClickListener(vOnClickListener);

        btnIdle = (Button) findViewById(R.id.btn_idle);
        btnIdle.setOnClickListener(vOnClickListener);

        btnShoot = (Button) findViewById(R.id.btn_shoot);
        btnShoot.setOnClickListener(vOnClickListener);

        btnHit = (Button) findViewById(R.id.btn_hit);
        btnHit.setOnClickListener(vOnClickListener);

        btnDeath = (Button) findViewById(R.id.btn_death);
        btnDeath.setOnClickListener(vOnClickListener);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.r = cfg.g = cfg.b = cfg.a = 8;
        spineBoy = new SpineBoy();
        spineBoyView = initializeForView(spineBoy, cfg);
        if (spineBoyView instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) spineBoyView;
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderOnTop(true);
        }
        addSpineBoy();
    }

    public void addSpineBoy() {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        spineBoyView.setOnTouchListener(new View.OnTouchListener() {

            float lastX, lastY;

            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                float x = event.getRawX();
                float y = event.getRawY();
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    layoutParams.x += (int) (x - lastX);
                    layoutParams.y += (int) (y - lastY);
                    windowManager.updateViewLayout(spineBoyView, layoutParams);
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    spineBoy.animate();
                }
                return true;
            }
        });
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.flags = 40;
        layoutParams.width = dp2Px(144);
        layoutParams.height = dp2Px(200);
        layoutParams.format = -3;
        windowManager.addView(spineBoyView, layoutParams);
    }

    public int dp2Px(float value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    View.OnClickListener vOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btnWalk) {
                spineBoy.walk();
            } else if (view == btnRun) {
                spineBoy.run();
            } else if (view == btnJump) {
                spineBoy.jump();
            } else if (view == btnIdle) {
                spineBoy.idle();
            } else if (view == btnShoot) {
                spineBoy.shoot();
            } else if (view == btnHit) {
                spineBoy.hit();
            } else if (view == btnDeath) {
                spineBoy.death();
            }
        }
    };

    @Override
    protected void onDestroy() {
        getWindowManager().removeView(spineBoyView);
        super.onDestroy();
    }
}
