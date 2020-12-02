package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0018R$string;
import com.android.systemui.statusbar.notification.row.NotificationGuts;

public class AppOpsInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private String mAppName;
    private ArraySet<Integer> mAppOps;
    private int mAppUid;
    private NotificationGuts mGutsContainer;
    private MetricsLogger mMetricsLogger;
    private View.OnClickListener mOnOk = new View.OnClickListener() {
        public final void onClick(View view) {
            AppOpsInfo.this.lambda$new$0$AppOpsInfo(view);
        }
    };
    private OnSettingsClickListener mOnSettingsClickListener;
    private String mPkg;
    private PackageManager mPm;
    private StatusBarNotification mSbn;
    private UiEventLogger mUiEventLogger;

    public interface OnSettingsClickListener {
        void onClick(View view, String str, int i, ArraySet<Integer> arraySet);
    }

    public View getContentView() {
        return this;
    }

    public boolean needsFalsingProtection() {
        return false;
    }

    public boolean shouldBeSaved() {
        return false;
    }

    public boolean willBeRemoved() {
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$AppOpsInfo(View view) {
        this.mGutsContainer.closeControls(view, false);
    }

    public AppOpsInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void bindGuts(PackageManager packageManager, OnSettingsClickListener onSettingsClickListener, StatusBarNotification statusBarNotification, UiEventLogger uiEventLogger, ArraySet<Integer> arraySet) {
        String packageName = statusBarNotification.getPackageName();
        this.mPkg = packageName;
        this.mSbn = statusBarNotification;
        this.mPm = packageManager;
        this.mAppName = packageName;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mAppOps = arraySet;
        this.mUiEventLogger = uiEventLogger;
        bindHeader();
        bindPrompt();
        bindButtons();
        logUiEvent(NotificationAppOpsEvent.NOTIFICATION_APP_OPS_OPEN);
        MetricsLogger metricsLogger = new MetricsLogger();
        this.mMetricsLogger = metricsLogger;
        metricsLogger.visibility(1345, true);
    }

    private void bindHeader() {
        Drawable drawable;
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPkg, 795136);
            if (applicationInfo != null) {
                this.mAppUid = this.mSbn.getUid();
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
                drawable = this.mPm.getApplicationIcon(applicationInfo);
            } else {
                drawable = null;
            }
        } catch (PackageManager.NameNotFoundException unused) {
            drawable = this.mPm.getDefaultActivityIcon();
        }
        ((ImageView) findViewById(C0012R$id.pkgicon)).setImageDrawable(drawable);
        ((TextView) findViewById(C0012R$id.pkgname)).setText(this.mAppName);
    }

    private void bindPrompt() {
        ((TextView) findViewById(C0012R$id.prompt)).setText(getPrompt());
    }

    private void bindButtons() {
        findViewById(C0012R$id.settings).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                AppOpsInfo.this.lambda$bindButtons$1$AppOpsInfo(view);
            }
        });
        TextView textView = (TextView) findViewById(C0012R$id.ok);
        textView.setOnClickListener(this.mOnOk);
        textView.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButtons$1 */
    public /* synthetic */ void lambda$bindButtons$1$AppOpsInfo(View view) {
        this.mOnSettingsClickListener.onClick(view, this.mPkg, this.mAppUid, this.mAppOps);
    }

    private String getPrompt() {
        ArraySet<Integer> arraySet = this.mAppOps;
        if (arraySet == null || arraySet.size() == 0) {
            return "";
        }
        if (this.mAppOps.size() == 1) {
            if (this.mAppOps.contains(26)) {
                return this.mContext.getString(C0018R$string.appops_camera);
            }
            if (this.mAppOps.contains(27)) {
                return this.mContext.getString(C0018R$string.appops_microphone);
            }
            return this.mContext.getString(C0018R$string.appops_overlay);
        } else if (this.mAppOps.size() != 2) {
            return this.mContext.getString(C0018R$string.appops_camera_mic_overlay);
        } else {
            if (!this.mAppOps.contains(26)) {
                return this.mContext.getString(C0018R$string.appops_mic_overlay);
            }
            if (this.mAppOps.contains(27)) {
                return this.mContext.getString(C0018R$string.appops_camera_mic);
            }
            return this.mContext.getString(C0018R$string.appops_camera_overlay);
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mGutsContainer != null && accessibilityEvent.getEventType() == 32) {
            if (this.mGutsContainer.isExposed()) {
                accessibilityEvent.getText().add(this.mContext.getString(C0018R$string.notification_channel_controls_opened_accessibility, new Object[]{this.mAppName}));
                return;
            }
            accessibilityEvent.getText().add(this.mContext.getString(C0018R$string.notification_channel_controls_closed_accessibility, new Object[]{this.mAppName}));
        }
    }

    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    public boolean handleCloseControls(boolean z, boolean z2) {
        logUiEvent(NotificationAppOpsEvent.NOTIFICATION_APP_OPS_CLOSE);
        MetricsLogger metricsLogger = this.mMetricsLogger;
        if (metricsLogger != null) {
            metricsLogger.visibility(1345, false);
        }
        return false;
    }

    public int getActualHeight() {
        return getHeight();
    }

    private void logUiEvent(NotificationAppOpsEvent notificationAppOpsEvent) {
        StatusBarNotification statusBarNotification = this.mSbn;
        if (statusBarNotification != null) {
            this.mUiEventLogger.logWithInstanceId(notificationAppOpsEvent, statusBarNotification.getUid(), this.mSbn.getPackageName(), this.mSbn.getInstanceId());
        }
    }
}