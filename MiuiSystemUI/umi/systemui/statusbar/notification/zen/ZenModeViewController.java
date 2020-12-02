package com.android.systemui.statusbar.notification.zen;

import android.service.notification.ZenModeConfig;
import android.view.View;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.ZenModeController;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ZenModeViewController.kt */
public final class ZenModeViewController implements ZenModeController.Callback {
    private final NotificationRowComponent.Builder builder;
    private final KeyguardBypassController bypassController;
    private boolean manuallyDismissed;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private final SysuiStatusBarStateController statusBarStateController;
    @Nullable
    private ZenModeView view;
    @NotNull
    public ActivatableNotificationViewController viewController;
    @Nullable
    private Function2<? super Boolean, ? super Boolean, Unit> visibilityChangedListener;
    private final ZenModeController zenModeController;

    public ZenModeViewController(@NotNull ZenModeController zenModeController2, @NotNull KeyguardBypassController keyguardBypassController, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull NotificationRowComponent.Builder builder2) {
        Intrinsics.checkParameterIsNotNull(zenModeController2, "zenModeController");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(builder2, "builder");
        this.zenModeController = zenModeController2;
        this.bypassController = keyguardBypassController;
        this.statusBarStateController = sysuiStatusBarStateController;
        this.notifLockscreenUserManager = notificationLockscreenUserManager;
        this.builder = builder2;
        sysuiStatusBarStateController.addCallback(new StatusBarStateController.StateListener(this) {
            final /* synthetic */ ZenModeViewController this$0;

            {
                this.this$0 = r1;
            }

            public void onStateChanged(int i) {
                this.this$0.updateVisibility();
            }
        });
        this.zenModeController.addCallback(this);
    }

    public final void setVisibilityChangedListener(@Nullable Function2<? super Boolean, ? super Boolean, Unit> function2) {
        this.visibilityChangedListener = function2;
    }

    @Nullable
    public final ZenModeView getView() {
        return this.view;
    }

    public final void attach(@NotNull ZenModeView zenModeView) {
        Intrinsics.checkParameterIsNotNull(zenModeView, "zenModeView");
        this.view = zenModeView;
        NotificationRowComponent build = this.builder.activatableNotificationView(zenModeView).build();
        Intrinsics.checkExpressionValueIsNotNull(build, "builder.activatableNotif…View(zenModeView).build()");
        ActivatableNotificationViewController activatableNotificationViewController = build.getActivatableNotificationViewController();
        Intrinsics.checkExpressionValueIsNotNull(activatableNotificationViewController, "builder.activatableNotif…otificationViewController");
        this.viewController = activatableNotificationViewController;
        if (activatableNotificationViewController != null) {
            activatableNotificationViewController.init();
            updateVisibility();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("viewController");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void updateVisibility() {
        Function2<? super Boolean, ? super Boolean, Unit> function2;
        View contentView;
        View contentView2;
        int i = 0;
        boolean z = true;
        if (!(this.statusBarStateController.getState() == 1 || this.statusBarStateController.getState() == 2 || this.statusBarStateController.getState() == 3) || !isDndOn() || this.manuallyDismissed) {
            z = false;
        }
        if (!this.bypassController.getBypassEnabled()) {
            boolean shouldShowLockscreenNotifications = this.notifLockscreenUserManager.shouldShowLockscreenNotifications();
        }
        ZenModeView zenModeView = this.view;
        int visibility = (zenModeView == null || (contentView2 = zenModeView.getContentView()) == null) ? 8 : contentView2.getVisibility();
        if (!z) {
            i = 8;
        }
        ZenModeView zenModeView2 = this.view;
        if (!(zenModeView2 == null || (contentView = zenModeView2.getContentView()) == null)) {
            contentView.setVisibility(i);
        }
        ZenModeView zenModeView3 = this.view;
        if (zenModeView3 != null) {
            zenModeView3.resetTranslation();
        }
        if (visibility != i && (function2 = this.visibilityChangedListener) != null) {
            Unit invoke = function2.invoke(Boolean.valueOf(z), Boolean.valueOf(this.manuallyDismissed));
        }
    }

    public final void onSwipeToDismiss() {
        this.manuallyDismissed = true;
        updateVisibility();
    }

    public void onConfigChanged(@Nullable ZenModeConfig zenModeConfig) {
        this.manuallyDismissed = false;
        updateVisibility();
    }

    private final boolean isDndOn() {
        return this.zenModeController.getZen() != 0;
    }
}