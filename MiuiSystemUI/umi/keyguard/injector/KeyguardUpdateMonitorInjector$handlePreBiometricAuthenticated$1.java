package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardUpdateMonitorInjector.kt */
public final class KeyguardUpdateMonitorInjector$handlePreBiometricAuthenticated$1 extends Lambda implements Function1<MiuiKeyguardUpdateMonitorCallback, Unit> {
    final /* synthetic */ int $userId;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    KeyguardUpdateMonitorInjector$handlePreBiometricAuthenticated$1(int i) {
        super(1);
        this.$userId = i;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback) {
        invoke(miuiKeyguardUpdateMonitorCallback);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback) {
        Intrinsics.checkParameterIsNotNull(miuiKeyguardUpdateMonitorCallback, "callback");
        miuiKeyguardUpdateMonitorCallback.onPreBiometricAuthenticated(this.$userId);
    }
}
