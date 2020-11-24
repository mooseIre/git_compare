package com.android.systemui.qs.tiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.SubscriptionManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SignalTileView;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.NetworkController;

public class CellularTile extends QSTileImpl<QSTile.SignalState> {
    private final ActivityStarter mActivityStarter;
    /* access modifiers changed from: private */
    public final NetworkController mController;
    /* access modifiers changed from: private */
    public final DataUsageController mDataController;
    /* access modifiers changed from: private */
    public final CellularDetailAdapter mDetailAdapter;
    /* access modifiers changed from: private */
    public final CellSignalCallback mSignalCallback = new CellSignalCallback();

    public int getMetricsCategory() {
        return 115;
    }

    public CellularTile(QSHost qSHost, NetworkController networkController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = networkController;
        this.mActivityStarter = activityStarter;
        this.mDataController = networkController.getMobileDataController();
        this.mDetailAdapter = new CellularDetailAdapter();
        this.mController.observe(getLifecycle(), this.mSignalCallback);
    }

    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    public QSIconView createTileView(Context context) {
        return new SignalTileView(context);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public Intent getLongClickIntent() {
        if (((QSTile.SignalState) getState()).state == 0) {
            return new Intent("android.settings.WIRELESS_SETTINGS");
        }
        return getCellularSettingIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (((QSTile.SignalState) getState()).state != 0) {
            if (this.mDataController.isMobileDataEnabled()) {
                maybeShowDisableDialog();
            } else {
                this.mDataController.setMobileDataEnabled(true);
            }
        }
    }

    private void maybeShowDisableDialog() {
        if (Prefs.getBoolean(this.mContext, "QsHasTurnedOffMobileData", false)) {
            this.mDataController.setMobileDataEnabled(false);
            return;
        }
        String mobileDataNetworkName = this.mController.getMobileDataNetworkName();
        if (TextUtils.isEmpty(mobileDataNetworkName)) {
            mobileDataNetworkName = this.mContext.getString(C0018R$string.mobile_data_disable_message_default_carrier);
        }
        AlertDialog create = new AlertDialog.Builder(this.mContext).setTitle(C0018R$string.mobile_data_disable_title).setMessage(this.mContext.getString(C0018R$string.mobile_data_disable_message, new Object[]{mobileDataNetworkName})).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(17039638, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                CellularTile.this.lambda$maybeShowDisableDialog$0$CellularTile(dialogInterface, i);
            }
        }).create();
        create.getWindow().setType(2009);
        SystemUIDialog.setShowForAllUsers(create, true);
        SystemUIDialog.registerDismissListener(create);
        SystemUIDialog.setWindowOnTop(create);
        create.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowDisableDialog$0 */
    public /* synthetic */ void lambda$maybeShowDisableDialog$0$CellularTile(DialogInterface dialogInterface, int i) {
        this.mDataController.setMobileDataEnabled(false);
        Prefs.putBoolean(this.mContext, "QsHasTurnedOffMobileData", true);
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        if (this.mDataController.isMobileDataSupported()) {
            showDetail(true);
        } else {
            this.mActivityStarter.postStartActivityDismissingKeyguard(getCellularSettingIntent(), 0);
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0018R$string.quick_settings_cellular_detail_title);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        CharSequence charSequence;
        CallbackInfo callbackInfo = (CallbackInfo) obj;
        if (callbackInfo == null) {
            callbackInfo = this.mSignalCallback.mInfo;
        }
        Resources resources = this.mContext.getResources();
        signalState.label = resources.getString(C0018R$string.mobile_data);
        boolean z = this.mDataController.isMobileDataSupported() && this.mDataController.isMobileDataEnabled();
        signalState.value = z;
        signalState.activityIn = z && callbackInfo.activityIn;
        signalState.activityOut = z && callbackInfo.activityOut;
        signalState.expandedAccessibilityClassName = Switch.class.getName();
        if (callbackInfo.noSim) {
            signalState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_no_sim);
        } else {
            signalState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_swap_vert);
        }
        if (callbackInfo.noSim) {
            signalState.state = 0;
            signalState.secondaryLabel = resources.getString(C0018R$string.keyguard_missing_sim_message_short);
        } else if (callbackInfo.airplaneModeEnabled) {
            signalState.state = 0;
            signalState.secondaryLabel = resources.getString(C0018R$string.status_bar_airplane);
        } else if (z) {
            signalState.state = 2;
            if (callbackInfo.multipleSubs) {
                charSequence = callbackInfo.dataSubscriptionName;
            } else {
                charSequence = "";
            }
            signalState.secondaryLabel = appendMobileDataType(charSequence, getMobileDataContentName(callbackInfo));
        } else {
            signalState.state = 1;
            signalState.secondaryLabel = resources.getString(C0018R$string.cell_data_off);
        }
        signalState.contentDescription = signalState.label;
        if (signalState.state == 1) {
            signalState.stateDescription = "";
        } else {
            signalState.stateDescription = signalState.secondaryLabel;
        }
    }

    private CharSequence appendMobileDataType(CharSequence charSequence, CharSequence charSequence2) {
        if (TextUtils.isEmpty(charSequence2)) {
            return Html.fromHtml(charSequence.toString(), 0);
        }
        if (TextUtils.isEmpty(charSequence)) {
            return Html.fromHtml(charSequence2.toString(), 0);
        }
        return Html.fromHtml(this.mContext.getString(C0018R$string.mobile_carrier_text_format, new Object[]{charSequence, charSequence2}), 0);
    }

    private CharSequence getMobileDataContentName(CallbackInfo callbackInfo) {
        if (callbackInfo.roaming && !TextUtils.isEmpty(callbackInfo.dataContentDescription)) {
            String string = this.mContext.getString(C0018R$string.data_connection_roaming);
            String charSequence = callbackInfo.dataContentDescription.toString();
            return this.mContext.getString(C0018R$string.mobile_data_text_format, new Object[]{string, charSequence});
        } else if (callbackInfo.roaming) {
            return this.mContext.getString(C0018R$string.data_connection_roaming);
        } else {
            return callbackInfo.dataContentDescription;
        }
    }

    public boolean isAvailable() {
        return this.mController.hasMobileDataFeature();
    }

    private static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean airplaneModeEnabled;
        CharSequence dataContentDescription;
        CharSequence dataSubscriptionName;
        boolean multipleSubs;
        boolean noSim;
        boolean roaming;

        private CallbackInfo() {
        }
    }

    private final class CellSignalCallback implements NetworkController.SignalCallback {
        /* access modifiers changed from: private */
        public final CallbackInfo mInfo;

        private CellSignalCallback() {
            this.mInfo = new CallbackInfo();
        }

        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
            if (iconState2 != null) {
                this.mInfo.dataSubscriptionName = CellularTile.this.mController.getMobileDataNetworkName();
                CallbackInfo callbackInfo = this.mInfo;
                if (charSequence3 == null) {
                    charSequence2 = null;
                }
                callbackInfo.dataContentDescription = charSequence2;
                CallbackInfo callbackInfo2 = this.mInfo;
                callbackInfo2.activityIn = z;
                callbackInfo2.activityOut = z2;
                callbackInfo2.roaming = z4;
                boolean z5 = true;
                if (CellularTile.this.mController.getNumberSubscriptions() <= 1) {
                    z5 = false;
                }
                callbackInfo2.multipleSubs = z5;
                CellularTile.this.refreshState(this.mInfo);
            }
        }

        public void setNoSims(boolean z, boolean z2) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.noSim = z;
            CellularTile.this.refreshState(callbackInfo);
        }

        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.airplaneModeEnabled = iconState.visible;
            CellularTile.this.refreshState(callbackInfo);
        }

        public void setMobileDataEnabled(boolean z) {
            CellularTile.this.mDetailAdapter.setMobileDataEnabled(z);
        }
    }

    static Intent getCellularSettingIntent() {
        Intent intent = new Intent("android.settings.NETWORK_OPERATOR_SETTINGS");
        if (SubscriptionManager.getDefaultDataSubscriptionId() != -1) {
            intent.putExtra("android.provider.extra.SUB_ID", SubscriptionManager.getDefaultDataSubscriptionId());
        }
        return intent;
    }

    private final class CellularDetailAdapter implements DetailAdapter {
        public int getMetricsCategory() {
            return R$styleable.AppCompatTheme_windowActionBar;
        }

        private CellularDetailAdapter() {
        }

        public CharSequence getTitle() {
            return CellularTile.this.mContext.getString(C0018R$string.quick_settings_cellular_detail_title);
        }

        public Boolean getToggleState() {
            if (CellularTile.this.mDataController.isMobileDataSupported()) {
                return Boolean.valueOf(CellularTile.this.mDataController.isMobileDataEnabled());
            }
            return null;
        }

        public Intent getSettingsIntent() {
            return CellularTile.getCellularSettingIntent();
        }

        public void setToggleState(boolean z) {
            MetricsLogger.action(CellularTile.this.mContext, 155, z);
            CellularTile.this.mDataController.setMobileDataEnabled(z);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            int i = 0;
            if (view == null) {
                view = LayoutInflater.from(CellularTile.this.mContext).inflate(C0014R$layout.data_usage, viewGroup, false);
            }
            DataUsageDetailView dataUsageDetailView = (DataUsageDetailView) view;
            DataUsageController.DataUsageInfo dataUsageInfo = CellularTile.this.mDataController.getDataUsageInfo();
            if (dataUsageInfo == null) {
                return dataUsageDetailView;
            }
            dataUsageDetailView.bind(dataUsageInfo);
            View findViewById = dataUsageDetailView.findViewById(C0012R$id.roaming_text);
            if (!CellularTile.this.mSignalCallback.mInfo.roaming) {
                i = 4;
            }
            findViewById.setVisibility(i);
            return dataUsageDetailView;
        }

        public void setMobileDataEnabled(boolean z) {
            CellularTile.this.fireToggleStateChanged(z);
        }
    }
}
