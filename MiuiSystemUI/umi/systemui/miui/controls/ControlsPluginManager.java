package com.android.systemui.miui.controls;

import android.content.Context;
import android.util.Log;
import android.view.View;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.miui.controls.MiuiControlsPlugin;

public class ControlsPluginManager implements PluginListener<MiuiControlsPlugin> {
    private static boolean DEBUG = Constants.DEBUG;
    private static int STATE_ADDED = 1;
    private static int STATE_ADDING = 0;
    private static int STATE_REMOVED = 3;
    private static int STATE_REMOVING = 2;
    private int currentState = STATE_REMOVED;
    private MiuiControlsPlugin mMiuiControlsPlugin;

    public void addControlsPluginListener() {
        if (this.mMiuiControlsPlugin == null) {
            ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(this, (Class<?>) MiuiControlsPlugin.class, true);
        }
        this.currentState = STATE_ADDING;
    }

    public void removeControlsPluginListener() {
        this.currentState = STATE_REMOVING;
        ((PluginManager) Dependency.get(PluginManager.class)).removePluginListener(this);
    }

    public View getControlsView() {
        MiuiControlsPlugin miuiControlsPlugin = this.mMiuiControlsPlugin;
        if (miuiControlsPlugin == null) {
            return null;
        }
        try {
            return miuiControlsPlugin.getControlsView();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void hideControlView() {
        MiuiControlsPlugin miuiControlsPlugin = this.mMiuiControlsPlugin;
        if (miuiControlsPlugin != null) {
            miuiControlsPlugin.hideControlsView();
        }
    }

    public void onPluginConnected(MiuiControlsPlugin miuiControlsPlugin, Context context) {
        if (DEBUG) {
            Log.d("ControlsPluginManager", "onPluginConnected");
        }
        this.currentState = STATE_ADDED;
        this.mMiuiControlsPlugin = miuiControlsPlugin;
    }

    public void onPluginDisconnected(MiuiControlsPlugin miuiControlsPlugin) {
        if (DEBUG) {
            Log.d("ControlsPluginManager", "onPluginDisconnected");
        }
        this.mMiuiControlsPlugin = null;
        if (this.currentState == STATE_ADDING) {
            this.currentState = STATE_REMOVED;
            addControlsPluginListener();
            return;
        }
        this.currentState = STATE_REMOVED;
    }
}
