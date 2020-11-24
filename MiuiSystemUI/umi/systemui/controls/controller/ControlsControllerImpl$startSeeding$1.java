package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import android.util.Log;
import com.android.systemui.controls.controller.ControlsBindingController;
import java.util.List;
import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$startSeeding$1 implements ControlsBindingController.LoadCallback {
    final /* synthetic */ Consumer $callback;
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ boolean $didAnyFail;
    final /* synthetic */ List $remaining;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$startSeeding$1(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, Consumer consumer, List list, boolean z) {
        this.this$0 = controlsControllerImpl;
        this.$componentName = componentName;
        this.$callback = consumer;
        this.$remaining = list;
        this.$didAnyFail = z;
    }

    public void accept(@NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(list, "controls");
        this.this$0.executor.execute(new ControlsControllerImpl$startSeeding$1$accept$1(this, list));
    }

    public void error(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "message");
        Log.e("ControlsControllerImpl", "Unable to seed favorites: " + str);
        this.this$0.executor.execute(new ControlsControllerImpl$startSeeding$1$error$1(this));
    }
}
