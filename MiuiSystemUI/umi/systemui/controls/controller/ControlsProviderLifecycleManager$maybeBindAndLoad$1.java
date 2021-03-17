package com.android.systemui.controls.controller;

import android.service.controls.IControlsSubscriber;
import android.util.Log;

/* access modifiers changed from: package-private */
/* compiled from: ControlsProviderLifecycleManager.kt */
public final class ControlsProviderLifecycleManager$maybeBindAndLoad$1 implements Runnable {
    final /* synthetic */ IControlsSubscriber.Stub $subscriber;
    final /* synthetic */ ControlsProviderLifecycleManager this$0;

    ControlsProviderLifecycleManager$maybeBindAndLoad$1(ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub stub) {
        this.this$0 = controlsProviderLifecycleManager;
        this.$subscriber = stub;
    }

    public final void run() {
        String str = this.this$0.TAG;
        Log.d(str, "Timeout waiting onLoad for " + this.this$0.getComponentName());
        this.$subscriber.onError(this.this$0.getToken(), "Timeout waiting onLoad");
        this.this$0.unbindService();
    }
}
