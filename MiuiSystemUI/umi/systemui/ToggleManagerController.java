package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.FlashlightController;
import miui.app.ToggleManager;

public class ToggleManagerController extends CurrentUserTracker implements FlashlightController.FlashlightListener {
    protected Handler mBgHandler;
    protected BroadcastDispatcher mBroadcastDispatcher;
    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.miui.home".equals(intent.getSender()) && "com.miui.app.ExtraStatusBarManager.action_TRIGGER_TOGGLE".equals(intent.getAction())) {
                ToggleManagerController.this.mToggleManager.performToggle(intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_TOGGLE_ID", -1));
            } else if ("miui.intent.action.TOGGLE_TORCH".equals(intent.getAction())) {
                FlashlightController flashlightController = ToggleManagerController.this.mFlashlightController;
                flashlightController.setFlashlight(!flashlightController.isEnabled());
            }
        }
    };
    protected Context mContext;
    protected FlashlightController mFlashlightController;
    protected ToggleManager mToggleManager;

    public void onFlashlightAvailabilityChanged(boolean z) {
    }

    public void onFlashlightError() {
    }

    public ToggleManagerController(Context context, BroadcastDispatcher broadcastDispatcher, Handler handler) {
        super(broadcastDispatcher);
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mFlashlightController = (FlashlightController) Dependency.get(FlashlightController.class);
        this.mBgHandler = handler;
    }

    public void start() {
        startTracking();
        this.mFlashlightController.addCallback(this);
        this.mBgHandler.post(new Runnable() {
            public void run() {
                ToggleManagerController toggleManagerController = ToggleManagerController.this;
                toggleManagerController.mToggleManager = ToggleManager.createInstance(toggleManagerController.mContext, KeyguardUpdateMonitor.getCurrentUser());
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("com.miui.app.ExtraStatusBarManager.action_TRIGGER_TOGGLE");
                intentFilter.addAction("miui.intent.action.TOGGLE_TORCH");
                ToggleManagerController toggleManagerController2 = ToggleManagerController.this;
                toggleManagerController2.mBroadcastDispatcher.registerReceiverWithHandler(toggleManagerController2.mBroadcastReceiver, intentFilter, toggleManagerController2.mBgHandler, UserHandle.ALL);
            }
        });
    }

    public void onUserSwitched(final int i) {
        this.mBgHandler.post(new Runnable() {
            public void run() {
                ToggleManagerController.this.mToggleManager.updateAllToggles(i);
            }
        });
    }

    public void onFlashlightChanged(boolean z) {
        Log.d("ToggleManagerController", "setTorchState: enabled: " + z);
        Settings.Global.putInt(this.mContext.getContentResolver(), "torch_state", z ? 1 : 0);
    }
}
