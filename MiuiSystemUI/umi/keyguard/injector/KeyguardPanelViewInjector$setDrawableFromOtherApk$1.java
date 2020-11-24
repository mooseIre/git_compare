package com.android.keyguard.injector;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$setDrawableFromOtherApk$1 extends AsyncTask<Void, Void, Drawable> {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$setDrawableFromOtherApk$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    /* access modifiers changed from: protected */
    @Nullable
    public Drawable doInBackground(@NotNull Void... voidArr) {
        Intrinsics.checkParameterIsNotNull(voidArr, "params");
        if (!MiuiKeyguardUtils.isUserUnlocked()) {
            return null;
        }
        KeyguardPanelViewInjector keyguardPanelViewInjector = this.this$0;
        keyguardPanelViewInjector.mLeftViewBackgroundImageDrawable = PackageUtils.getDrawableFromPackage(keyguardPanelViewInjector.getMContext(), LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).getPreTransToLeftScreenDrawableResName());
        return PackageUtils.getDrawableFromPackage(this.this$0.getMContext(), LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).getPreLeftScreenDrawableResName());
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(@Nullable Drawable drawable) {
        MiuiKeyguardMoveLeftViewContainer access$getMLeftView$p = this.this$0.mLeftView;
        if (access$getMLeftView$p != null) {
            access$getMLeftView$p.setCustomBackground(drawable);
        }
        if (this.this$0.mLeftViewBackgroundImageDrawable != null) {
            ImageView access$getMLeftViewBackgroundView$p = KeyguardPanelViewInjector.access$getMLeftViewBackgroundView$p(this.this$0);
            Drawable access$getMLeftViewBackgroundImageDrawable$p = this.this$0.mLeftViewBackgroundImageDrawable;
            if (access$getMLeftViewBackgroundImageDrawable$p != null) {
                access$getMLeftViewBackgroundView$p.setBackgroundDrawable(access$getMLeftViewBackgroundImageDrawable$p);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            KeyguardPanelViewInjector.access$getMLeftViewBackgroundView$p(this.this$0).setBackgroundColor(KeyguardPanelViewInjector.access$getMWallpaperController$p(this.this$0).getWallpaperBlurColor());
        }
    }
}
