package me.lancer.spineruntimesdemo.activity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AppActivity;

import me.lancer.spineruntimesdemo.R;
import me.lancer.spineruntimesdemo.model.PowerUp;

public class PowerUpActivity extends AppActivity {

    PowerUp powerUp;
    View powerUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.r = cfg.g = cfg.b = cfg.a = 8;
        powerUp = new PowerUp();
        powerUpView = initializeForView(powerUp, cfg);
        if (powerUpView instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) powerUpView;
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderOnTop(true);
        }
        addAlien();
    }

    public void addAlien() {
        final WindowManager windowManager = getWindowManager();
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        powerUpView.setOnTouchListener(new View.OnTouchListener() {

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
                    windowManager.updateViewLayout(powerUpView, layoutParams);
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    powerUp.setAnimate("animation");
                }
                return true;
            }
        });
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        layoutParams.flags = 40;
        layoutParams.width = dp2Px(144);
        layoutParams.height = dp2Px(144);
        layoutParams.format = -3;
        windowManager.addView(powerUpView, layoutParams);
    }

    public int dp2Px(float value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    @Override
    protected void onDestroy() {
        getWindowManager().removeViewImmediate(powerUpView);
        super.onDestroy();
    }
}
