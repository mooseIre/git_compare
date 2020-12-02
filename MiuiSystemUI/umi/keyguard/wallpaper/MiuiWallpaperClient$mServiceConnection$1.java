package com.android.keyguard.wallpaper;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.miui.miwallpaper.IMiuiKeyguardWallpaperService;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient$mServiceConnection$1 implements ServiceConnection {
    final /* synthetic */ MiuiWallpaperClient this$0;

    MiuiWallpaperClient$mServiceConnection$1(MiuiWallpaperClient miuiWallpaperClient) {
        this.this$0 = miuiWallpaperClient;
    }

    public void onServiceConnected(@NotNull ComponentName componentName, @NotNull IBinder iBinder) {
        Intrinsics.checkParameterIsNotNull(componentName, "name");
        Intrinsics.checkParameterIsNotNull(iBinder, "service");
        Log.d(this.this$0.getTAG(), "on MiuiKeyguardWallpaperRemoteStateService connected");
        this.this$0.mWallpaperService = IMiuiKeyguardWallpaperService.Stub.asInterface(iBinder);
        IMiuiKeyguardWallpaperService access$getMWallpaperService$p = this.this$0.mWallpaperService;
        if (access$getMWallpaperService$p != null) {
            access$getMWallpaperService$p.bindSystemUIProxy(new MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1(this));
        } else {
            Log.d(this.this$0.getTAG(), "mWallpaperService == null");
        }
        this.this$0.mBinding = true;
    }

    public void onServiceDisconnected(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "name");
        this.this$0.bindService();
        this.this$0.mWallpaperService = null;
        this.this$0.mBinding = false;
    }
}