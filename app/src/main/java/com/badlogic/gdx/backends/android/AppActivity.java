package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Method;

public class AppActivity extends AppCompatActivity implements AndroidApplicationBase {
    protected AndroidGraphics graphics;
    protected AndroidInput input;
    protected AndroidAudio audio;
    protected AndroidFiles files;
    protected AndroidNet net;
    protected ApplicationListener listener;
    public Handler handler;
    protected boolean firstResume = true;
    protected final Array<Runnable> runnables = new Array();
    protected final Array<Runnable> executedRunnables = new Array();
    protected final Array<LifecycleListener> lifecycleListeners = new Array();
    private final Array<AndroidEventListener> androidEventListeners = new Array();
    protected int logLevel = 2;
    protected boolean useImmersiveMode = false;
    protected boolean hideStatusBar = false;
    private int wasFocusChanged = -1;
    private boolean isWaitingForAudio = false;
    AndroidClipboard clipboard;

    public AppActivity() {
    }

    public void initialize(ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        this.initialize(listener, config);
    }

    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
        this.init(listener, config, false);
    }

    public View initializeForView(ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        return this.initializeForView(listener, config);
    }

    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
        this.init(listener, config, true);
        return this.graphics.getView();
    }

    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView) {
        if (this.getVersion() < 8) {
            throw new GdxRuntimeException("LibGDX requires Android API Level 8 or later.");
        } else {
            this.graphics = new AndroidGraphics(this, config, (ResolutionStrategy) (config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy));
            this.input = AndroidInputFactory.newAndroidInput(this, this, this.graphics.view, config);
            this.audio = new AndroidAudio(this, config);
            this.getFilesDir();
            this.files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
            this.net = new AndroidNet(this);
            this.listener = listener;
            this.handler = new Handler();
            this.useImmersiveMode = config.useImmersiveMode;
            this.hideStatusBar = config.hideStatusBar;
            this.addLifecycleListener(new LifecycleListener() {
                public void resume() {

                }

                public void pause() {
                    AppActivity.this.audio.pause();
                }

                public void dispose() {
                    AppActivity.this.audio.dispose();
                }
            });
            Gdx.app = this;
            Gdx.input = this.getInput();
            Gdx.audio = this.getAudio();
            Gdx.files = this.getFiles();
            Gdx.graphics = this.getGraphics();
            Gdx.net = this.getNet();
            if (!isForView) {
                try {
                    this.requestWindowFeature(1);
                } catch (Exception var8) {
                    this.log("AndroidApplication", "Content already displayed, cannot request FEATURE_NO_TITLE", var8);
                }
                this.getWindow().setFlags(1024, 1024);
                this.getWindow().clearFlags(2048);
                this.setContentView(this.graphics.getView(), this.createLayoutParams());
            }
            this.createWakeLock(config.useWakelock);
            this.hideStatusBar(this.hideStatusBar);
            this.useImmersiveMode(this.useImmersiveMode);
            if (this.useImmersiveMode && this.getVersion() >= 19) {
                try {
                    Class e = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
                    Object o = e.newInstance();
                    Method method = e.getDeclaredMethod("createListener", new Class[]{AndroidApplicationBase.class});
                    method.invoke(o, new Object[]{this});
                } catch (Exception var7) {
                    this.log("AndroidApplication", "Failed to create AndroidVisibilityListener", var7);
                }
            }
        }
    }

    protected FrameLayout.LayoutParams createLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        layoutParams.gravity = 17;
        return layoutParams;
    }

    protected void createWakeLock(boolean use) {
        if (use) {
            this.getWindow().addFlags(128);
        }
    }

    protected void hideStatusBar(boolean hide) {
        if (hide && this.getVersion() >= 11) {
            View rootView = this.getWindow().getDecorView();
            try {
                Method e = View.class.getMethod("setSystemUiVisibility", new Class[]{Integer.TYPE});
                if (this.getVersion() <= 13) {
                    e.invoke(rootView, new Object[]{Integer.valueOf(0)});
                }
                e.invoke(rootView, new Object[]{Integer.valueOf(1)});
            } catch (Exception var4) {
                this.log("AndroidApplication", "Can\'t hide status bar", var4);
            }
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.useImmersiveMode(this.useImmersiveMode);
        this.hideStatusBar(this.hideStatusBar);
        if (hasFocus) {
            this.wasFocusChanged = 1;
            if (this.isWaitingForAudio) {
                this.audio.resume();
                this.isWaitingForAudio = false;
            }
        } else {
            this.wasFocusChanged = 0;
        }

    }

    @TargetApi(19)
    public void useImmersiveMode(boolean use) {
        if (use && this.getVersion() >= 19) {
            View view = this.getWindow().getDecorView();
            try {
                Method e = View.class.getMethod("setSystemUiVisibility", new Class[]{Integer.TYPE});
                short code = 5894;
                e.invoke(view, new Object[]{Integer.valueOf(code)});
            } catch (Exception var5) {
                this.log("AndroidApplication", "Can\'t set immersive mode", var5);
            }
        }
    }

    protected void onPause() {
//        boolean isContinuous = this.graphics.isContinuousRendering();
//        boolean isContinuousEnforced = AndroidGraphics.enforceContinuousRendering;
//        AndroidGraphics.enforceContinuousRendering = true;
//        this.graphics.setContinuousRendering(true);
//        this.graphics.pause();
//        this.input.onPause();
//        if (this.isFinishing()) {
//            this.graphics.clearManagedCaches();
//            this.graphics.destroy();
//        }
//        AndroidGraphics.enforceContinuousRendering = isContinuousEnforced;
//        this.graphics.setContinuousRendering(isContinuous);
//        this.graphics.onPauseGLSurfaceView();
        super.onPause();
    }

    protected void onResume() {
        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();
        this.input.onResume();
        if (this.graphics != null) {
            this.graphics.onResumeGLSurfaceView();
        }
        if (!this.firstResume) {
            this.graphics.resume();
        } else {
            this.firstResume = false;
        }
        this.isWaitingForAudio = true;
        if (this.wasFocusChanged == 1 || this.wasFocusChanged == -1) {
            this.audio.resume();
            this.isWaitingForAudio = false;
        }
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public ApplicationListener getApplicationListener() {
        return this.listener;
    }

    public Audio getAudio() {
        return this.audio;
    }

    public Files getFiles() {
        return this.files;
    }

    public Graphics getGraphics() {
        return this.graphics;
    }

    public AndroidInput getInput() {
        return this.input;
    }

    public Net getNet() {
        return this.net;
    }

    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    public int getVersion() {
        return Build.VERSION.SDK_INT;
    }

    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

    public Preferences getPreferences(String name) {
        return new AndroidPreferences(this.getSharedPreferences(name, 0));
    }

    public Clipboard getClipboard() {
        if (this.clipboard == null) {
            this.clipboard = new AndroidClipboard(this);
        }
        return this.clipboard;
    }

    public void postRunnable(Runnable runnable) {
        Array var2 = this.runnables;
        synchronized (this.runnables) {
            this.runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        boolean keyboardAvailable = false;
        if (config.hardKeyboardHidden == 1) {
            keyboardAvailable = true;
        }
        this.input.keyboardAvailable = keyboardAvailable;
    }

    public void exit() {
        this.handler.post(new Runnable() {
            public void run() {
                AppActivity.this.finish();
            }
        });
    }

    public void debug(String tag, String message) {
        if (this.logLevel >= 3) {
            Log.d(tag, message);
        }
    }

    public void debug(String tag, String message, Throwable exception) {
        if (this.logLevel >= 3) {
            Log.d(tag, message, exception);
        }
    }

    public void log(String tag, String message) {
        if (this.logLevel >= 2) {
            Log.i(tag, message);
        }
    }

    public void log(String tag, String message, Throwable exception) {
        if (this.logLevel >= 2) {
            Log.i(tag, message, exception);
        }
    }

    public void error(String tag, String message) {
        if (this.logLevel >= 1) {
            Log.e(tag, message);
        }
    }

    public void error(String tag, String message, Throwable exception) {
        if (this.logLevel >= 1) {
            Log.e(tag, message, exception);
        }
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevel() {
        return this.logLevel;
    }

    public void addLifecycleListener(LifecycleListener listener) {
        Array var2 = this.lifecycleListeners;
        synchronized (this.lifecycleListeners) {
            this.lifecycleListeners.add(listener);
        }
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        Array var2 = this.lifecycleListeners;
        synchronized (this.lifecycleListeners) {
            this.lifecycleListeners.removeValue(listener, true);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Array var4 = this.androidEventListeners;
        synchronized (this.androidEventListeners) {
            for (int i = 0; i < this.androidEventListeners.size; ++i) {
                ((AndroidEventListener) this.androidEventListeners.get(i)).onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void addAndroidEventListener(AndroidEventListener listener) {
        Array var2 = this.androidEventListeners;
        synchronized (this.androidEventListeners) {
            this.androidEventListeners.add(listener);
        }
    }

    public void removeAndroidEventListener(AndroidEventListener listener) {
        Array var2 = this.androidEventListeners;
        synchronized (this.androidEventListeners) {
            this.androidEventListeners.removeValue(listener, true);
        }
    }

    public Context getContext() {
        return this;
    }

    public Array<Runnable> getRunnables() {
        return this.runnables;
    }

    public Array<Runnable> getExecutedRunnables() {
        return this.executedRunnables;
    }

    public Array<LifecycleListener> getLifecycleListeners() {
        return this.lifecycleListeners;
    }

    public Window getApplicationWindow() {
        return this.getWindow();
    }

    public Handler getHandler() {
        return this.handler;
    }

    static {
        GdxNativesLoader.load();
    }
}
