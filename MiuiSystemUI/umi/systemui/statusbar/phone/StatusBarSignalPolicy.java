package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.C0007R$bool;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StatusBarSignalPolicy implements NetworkController.SignalCallback, SecurityController.SecurityControllerCallback, TunerService.Tunable {
    protected boolean mActivityEnabled;
    protected boolean mBlockAirplane;
    protected boolean mBlockEthernet;
    protected boolean mBlockMobile;
    protected boolean mBlockWifi;
    protected final Context mContext;
    protected boolean mForceBlockWifi;
    protected final Handler mHandler = Handler.getMain();
    protected final StatusBarIconController mIconController;
    protected boolean mIsAirplaneMode = false;
    protected ArrayList<MobileIconState> mMobileStates = new ArrayList<>();
    protected final NetworkController mNetworkController;
    protected final SecurityController mSecurityController;
    protected final String mSlotAirplane;
    protected final String mSlotEthernet;
    protected final String mSlotMobile;
    protected final String mSlotVpn;
    protected final String mSlotWifi;
    protected WifiIconState mWifiIconState = new WifiIconState();

    public abstract void initMiuiSlot();

    public void setMobileDataEnabled(boolean z) {
    }

    public void setNoSims(boolean z, boolean z2) {
    }

    /* access modifiers changed from: protected */
    public abstract void updateWifiIconWithState(WifiIconState wifiIconState);

    public StatusBarSignalPolicy(Context context, StatusBarIconController statusBarIconController) {
        this.mContext = context;
        this.mSlotAirplane = context.getString(17041382);
        this.mSlotMobile = this.mContext.getString(17041399);
        this.mSlotWifi = this.mContext.getString(17041414);
        this.mSlotEthernet = this.mContext.getString(17041392);
        this.mSlotVpn = this.mContext.getString(17041413);
        this.mActivityEnabled = this.mContext.getResources().getBoolean(C0007R$bool.config_showActivity);
        this.mIconController = statusBarIconController;
        initMiuiSlot();
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mSecurityController = (SecurityController) Dependency.get(SecurityController.class);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
        this.mNetworkController.addCallback(this);
        this.mSecurityController.addCallback(this);
    }

    /* access modifiers changed from: private */
    public void updateVpn() {
        boolean isVpnEnabled = this.mSecurityController.isVpnEnabled();
        this.mIconController.setIcon(this.mSlotVpn, currentVpnIconId(this.mSecurityController.isVpnBranded()), this.mContext.getResources().getString(C0018R$string.accessibility_vpn_on));
        this.mIconController.setIconVisibility(this.mSlotVpn, isVpnEnabled);
    }

    private int currentVpnIconId(boolean z) {
        return z ? C0010R$drawable.stat_sys_branded_vpn : C0010R$drawable.stat_sys_vpn_ic;
    }

    public void onStateChanged() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                StatusBarSignalPolicy.this.updateVpn();
            }
        });
    }

    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, str2);
            boolean contains = iconBlacklist.contains(this.mSlotAirplane);
            boolean contains2 = iconBlacklist.contains(this.mSlotMobile);
            boolean contains3 = iconBlacklist.contains(this.mSlotWifi);
            boolean contains4 = iconBlacklist.contains(this.mSlotEthernet);
            if (contains != this.mBlockAirplane || contains2 != this.mBlockMobile || contains4 != this.mBlockEthernet || contains3 != this.mBlockWifi) {
                this.mBlockAirplane = contains;
                this.mBlockMobile = contains2;
                this.mBlockEthernet = contains4;
                this.mBlockWifi = contains3 || this.mForceBlockWifi;
                this.mNetworkController.removeCallback(this);
                this.mNetworkController.addCallback(this);
            }
        }
    }

    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        boolean z5 = true;
        boolean z6 = iconState.visible && !this.mBlockWifi;
        boolean z7 = z2 && this.mActivityEnabled && z6;
        boolean z8 = z3 && this.mActivityEnabled && z6;
        WifiIconState copy = this.mWifiIconState.copy();
        copy.visible = z6;
        copy.resId = iconState.icon;
        copy.activityIn = z7;
        copy.activityOut = z8;
        copy.slot = this.mSlotWifi;
        copy.airplaneSpacerVisible = this.mIsAirplaneMode;
        copy.contentDescription = iconState.contentDescription;
        MobileIconState firstMobileState = getFirstMobileState();
        if (firstMobileState == null || firstMobileState.typeId == 0) {
            z5 = false;
        }
        copy.signalSpacerVisible = z5;
        updateWifiIconWithState(copy);
        this.mWifiIconState = copy;
    }

    private void updateShowWifiSignalSpacer(WifiIconState wifiIconState) {
        MobileIconState firstMobileState = getFirstMobileState();
        wifiIconState.signalSpacerVisible = (firstMobileState == null || firstMobileState.typeId == 0) ? false : true;
    }

    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
        MobileIconState state = getState(i4);
        if (state != null) {
            int i5 = state.typeId;
            boolean z5 = true;
            boolean z6 = i != i5 && (i == 0 || i5 == 0);
            state.visible = iconState.visible && !this.mBlockMobile;
            state.strengthId = iconState.icon;
            state.typeId = i;
            state.contentDescription = iconState.contentDescription;
            state.typeContentDescription = charSequence;
            state.roaming = z4;
            state.activityIn = z && this.mActivityEnabled;
            if (!z2 || !this.mActivityEnabled) {
                z5 = false;
            }
            state.activityOut = z5;
            state.volteId = i3;
            this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
            if (z6) {
                WifiIconState copy = this.mWifiIconState.copy();
                updateShowWifiSignalSpacer(copy);
                if (!Objects.equals(copy, this.mWifiIconState)) {
                    updateWifiIconWithState(copy);
                    this.mWifiIconState = copy;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public MobileIconState getState(int i) {
        Iterator<MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            MobileIconState next = it.next();
            if (next.subId == i) {
                return next;
            }
        }
        Log.e("StatusBarSignalPolicy", "Unexpected subscription " + i);
        return null;
    }

    /* access modifiers changed from: protected */
    public MobileIconState getFirstMobileState() {
        if (this.mMobileStates.size() > 0) {
            return this.mMobileStates.get(0);
        }
        return null;
    }

    public void setSubs(List<SubscriptionInfo> list) {
        if (!hasCorrectSubs(list)) {
            this.mIconController.removeAllIconsForSlot(this.mSlotMobile);
            this.mMobileStates.clear();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                this.mMobileStates.add(new MobileIconState(list.get(i).getSubscriptionId()));
            }
        }
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> list) {
        int size = list.size();
        if (size != this.mMobileStates.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.mMobileStates.get(i).subId != list.get(i).getSubscriptionId()) {
                return false;
            }
        }
        return true;
    }

    public void setEthernetIndicators(NetworkController.IconState iconState) {
        boolean z = iconState.visible && !this.mBlockEthernet;
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (!z || i <= 0) {
            this.mIconController.setIconVisibility(this.mSlotEthernet, false);
            return;
        }
        this.mIconController.setIcon(this.mSlotEthernet, i, str);
        this.mIconController.setIconVisibility(this.mSlotEthernet, true);
    }

    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        boolean z = iconState.visible && !this.mBlockAirplane;
        this.mIsAirplaneMode = z;
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (!z || i <= 0) {
            this.mIconController.setIconVisibility(this.mSlotAirplane, false);
            return;
        }
        this.mIconController.setIcon(this.mSlotAirplane, i, str);
        this.mIconController.setIconVisibility(this.mSlotAirplane, true);
    }

    private static abstract class SignalIconState {
        public boolean activityIn;
        public boolean activityOut;
        public String contentDescription;
        public String slot;
        public boolean visible;

        private SignalIconState() {
        }

        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SignalIconState signalIconState = (SignalIconState) obj;
            if (this.visible == signalIconState.visible && this.activityOut == signalIconState.activityOut && this.activityIn == signalIconState.activityIn && Objects.equals(this.contentDescription, signalIconState.contentDescription) && Objects.equals(this.slot, signalIconState.slot)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Boolean.valueOf(this.visible), Boolean.valueOf(this.activityOut), this.slot});
        }

        /* access modifiers changed from: protected */
        public void copyTo(SignalIconState signalIconState) {
            signalIconState.visible = this.visible;
            signalIconState.activityIn = this.activityIn;
            signalIconState.activityOut = this.activityOut;
            signalIconState.slot = this.slot;
            signalIconState.contentDescription = this.contentDescription;
        }
    }

    public static class WifiIconState extends SignalIconState {
        public int activityResId;
        public boolean activityVisible;
        public boolean airplaneSpacerVisible;
        public int resId;
        public boolean showWifiStandard;
        public boolean signalSpacerVisible;
        public boolean wifiNoNetwork;
        public int wifiStandard;

        public WifiIconState() {
            super();
        }

        public boolean equals(Object obj) {
            if (obj == null || WifiIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            WifiIconState wifiIconState = (WifiIconState) obj;
            if (this.resId == wifiIconState.resId && this.airplaneSpacerVisible == wifiIconState.airplaneSpacerVisible && this.signalSpacerVisible == wifiIconState.signalSpacerVisible && this.activityResId == wifiIconState.activityResId && this.activityVisible == wifiIconState.activityVisible && this.wifiStandard == wifiIconState.wifiStandard && this.showWifiStandard == wifiIconState.showWifiStandard && this.wifiNoNetwork == wifiIconState.wifiNoNetwork) {
                return true;
            }
            return false;
        }

        public void copyTo(WifiIconState wifiIconState) {
            super.copyTo(wifiIconState);
            wifiIconState.resId = this.resId;
            wifiIconState.airplaneSpacerVisible = this.airplaneSpacerVisible;
            wifiIconState.signalSpacerVisible = this.signalSpacerVisible;
            wifiIconState.activityVisible = this.activityVisible;
            wifiIconState.activityResId = this.activityResId;
            wifiIconState.wifiStandard = this.wifiStandard;
            wifiIconState.showWifiStandard = this.showWifiStandard;
            wifiIconState.wifiNoNetwork = this.wifiNoNetwork;
        }

        public WifiIconState copy() {
            WifiIconState wifiIconState = new WifiIconState();
            copyTo(wifiIconState);
            return wifiIconState;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(super.hashCode()), Integer.valueOf(this.resId), Boolean.valueOf(this.airplaneSpacerVisible), Boolean.valueOf(this.signalSpacerVisible), Boolean.valueOf(this.activityVisible), Integer.valueOf(this.activityResId), Integer.valueOf(this.wifiStandard), Boolean.valueOf(this.showWifiStandard), Boolean.valueOf(this.wifiNoNetwork)});
        }

        public String toString() {
            return "WifiIconState(resId=" + this.resId + ", visible=" + this.visible + ", activityVisible = " + this.activityVisible + ", activityResId = " + this.activityResId + ", wifiStandard=" + this.wifiStandard + ", showWifiStandard=" + this.showWifiStandard + ", wifiNoNetwork=" + this.wifiNoNetwork + ")";
        }
    }

    public static class MobileIconState extends SignalIconState {
        public boolean airplane;
        public boolean dataConnected;
        public int fiveGDrawableId;
        public boolean hideVolte;
        public boolean hideVowifi;
        public String networkName;
        public boolean roaming;
        public boolean showDataTypeDataDisconnected;
        public boolean showDataTypeWhenWifiOn;
        public boolean showMobileDataTypeInMMS;
        public boolean speechHd;
        public int strengthId;
        public int subId;
        public CharSequence typeContentDescription;
        public int typeId;
        public boolean volte;
        public int volteId;
        public boolean volteNoSerivce;
        public boolean vowifi;
        public int vowifiId;
        public boolean wifiAvailable;

        private MobileIconState(int i) {
            super();
            this.subId = i;
        }

        public boolean equals(Object obj) {
            if (obj == null || MobileIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            MobileIconState mobileIconState = (MobileIconState) obj;
            if (this.subId == mobileIconState.subId && this.strengthId == mobileIconState.strengthId && this.typeId == mobileIconState.typeId && this.roaming == mobileIconState.roaming && Objects.equals(this.typeContentDescription, mobileIconState.typeContentDescription) && this.volteId == mobileIconState.volteId && this.airplane == mobileIconState.airplane && this.dataConnected == mobileIconState.dataConnected && this.wifiAvailable == mobileIconState.wifiAvailable && this.volte == mobileIconState.volte && this.hideVolte == mobileIconState.hideVolte && this.vowifiId == mobileIconState.vowifiId && this.vowifi == mobileIconState.vowifi && this.hideVowifi == mobileIconState.hideVowifi && this.speechHd == mobileIconState.speechHd && this.volteNoSerivce == mobileIconState.volteNoSerivce && this.fiveGDrawableId == mobileIconState.fiveGDrawableId && this.showDataTypeWhenWifiOn == mobileIconState.showDataTypeWhenWifiOn && this.showDataTypeDataDisconnected == mobileIconState.showDataTypeDataDisconnected && this.showMobileDataTypeInMMS == mobileIconState.showMobileDataTypeInMMS && Objects.equals(this.networkName, mobileIconState.networkName)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(super.hashCode()), Integer.valueOf(this.subId), Integer.valueOf(this.strengthId), Integer.valueOf(this.typeId), Boolean.valueOf(this.roaming), this.typeContentDescription, Integer.valueOf(this.volteId), Boolean.valueOf(this.airplane), Boolean.valueOf(this.dataConnected), Boolean.valueOf(this.wifiAvailable), this.networkName, Boolean.valueOf(this.volte), Boolean.valueOf(this.hideVolte), Integer.valueOf(this.vowifiId), Boolean.valueOf(this.vowifi), Boolean.valueOf(this.hideVowifi), Boolean.valueOf(this.speechHd), Boolean.valueOf(this.volteNoSerivce), Integer.valueOf(this.fiveGDrawableId), Boolean.valueOf(this.showDataTypeWhenWifiOn), Boolean.valueOf(this.showDataTypeDataDisconnected), Boolean.valueOf(this.showMobileDataTypeInMMS)});
        }

        public MobileIconState copy() {
            MobileIconState mobileIconState = new MobileIconState(this.subId);
            copyTo(mobileIconState);
            return mobileIconState;
        }

        public void copyTo(MobileIconState mobileIconState) {
            super.copyTo(mobileIconState);
            mobileIconState.subId = this.subId;
            mobileIconState.strengthId = this.strengthId;
            mobileIconState.typeId = this.typeId;
            mobileIconState.roaming = this.roaming;
            mobileIconState.typeContentDescription = this.typeContentDescription;
            mobileIconState.volteId = this.volteId;
            mobileIconState.airplane = this.airplane;
            mobileIconState.networkName = this.networkName;
            mobileIconState.dataConnected = this.dataConnected;
            mobileIconState.wifiAvailable = this.wifiAvailable;
            mobileIconState.volte = this.volte;
            mobileIconState.hideVolte = this.hideVolte;
            mobileIconState.volteNoSerivce = this.volteNoSerivce;
            mobileIconState.vowifiId = this.vowifiId;
            mobileIconState.vowifi = this.vowifi;
            mobileIconState.hideVowifi = this.hideVowifi;
            mobileIconState.speechHd = this.speechHd;
            mobileIconState.fiveGDrawableId = this.fiveGDrawableId;
            mobileIconState.showDataTypeWhenWifiOn = this.showDataTypeWhenWifiOn;
            mobileIconState.showDataTypeDataDisconnected = this.showDataTypeDataDisconnected;
            mobileIconState.showMobileDataTypeInMMS = this.showMobileDataTypeInMMS;
        }

        public static List<MobileIconState> copyStates(List<MobileIconState> list) {
            ArrayList arrayList = new ArrayList();
            for (MobileIconState next : list) {
                MobileIconState mobileIconState = new MobileIconState(next.subId);
                next.copyTo(mobileIconState);
                arrayList.add(mobileIconState);
            }
            return arrayList;
        }

        public String toString() {
            return "MobileIconState(subId=" + this.subId + ", strengthId=" + this.strengthId + ", roaming=" + this.roaming + ", typeId=" + this.typeId + ", volteId=" + this.volteId + ", visible=" + this.visible + ")";
        }
    }
}