package me.lancer.spineruntimesdemo.activity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AppActivity;

import me.lancer.spineruntimesdemo.R;
import me.lancer.spineruntimesdemo.model.Dragon;

public class DragonActivity extends AppActivity {

    Dragon dragon;
    View dragonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.r = cfg.g = cfg.b = cfg.a = 8;
        dragon = new Dragon();
        dragonView = initializeForView(dragon, cfg);
        if (dragonView instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) dragonView;
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderOnTop(true);
        }
        addDragon();
    }

    public void addDragon() {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        dragonView.setOnTouchListener(new View.OnTouchListener() {

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
                    windowManager.updateViewLayout(dragonView, layoutParams);
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    dragon.animate();
                }
                return true;
            }
        });
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        layoutParams.flags = 40;
        layoutParams.width = dp2Px(144);
        layoutParams.height = dp2Px(144);
        layoutParams.format = -3;
        windowManager.addView(dragonView, layoutParams);
    }

    public int dp2Px(float value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    @Override
    protected void onDestroy() {
        getWindowManager().removeView(dragonView);
        super.onDestroy();
    }
}
