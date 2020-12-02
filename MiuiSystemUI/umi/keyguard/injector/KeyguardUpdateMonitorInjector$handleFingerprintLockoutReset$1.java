package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardUpdateMonitorInjector.kt */
final class KeyguardUpdateMonitorInjector$handleFingerprintLockoutReset$1 extends Lambda implements Function1<MiuiKeyguardUpdateMonitorCallback, Unit> {
    public static final KeyguardUpdateMonitorInjector$handleFingerprintLockoutReset$1 INSTANCE = new KeyguardUpdateMonitorInjector$handleFingerprintLockoutReset$1();

    KeyguardUpdateMonitorInjector$handleFingerprintLockoutReset$1() {
        super(1);
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke((MiuiKeyguardUpdateMonitorCallback) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback) {
        Intrinsics.checkParameterIsNotNull(miuiKeyguardUpdateMonitorCallback, "callback");
        miuiKeyguardUpdateMonitorCallback.onFingerprintLockoutReset();
    }
}