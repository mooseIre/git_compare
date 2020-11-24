package com.android.keyguard.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class MiuiChargeManager implements Dumpable {
    /* access modifiers changed from: private */
    public MiuiBatteryStatus mBatteryStatus;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public boolean mIsChargeLevelAnimationRunning;
    Runnable mNotUpdateLevelRunnable = new Runnable() {
        public void run() {
            boolean unused = MiuiChargeManager.this.mNotUpdateLevelWhenBatteryChange = false;
            if ((MiuiChargeManager.this.mRealLevel < MiuiChargeManager.this.mBatteryStatus.level && !MiuiChargeManager.this.mBatteryStatus.isCharging()) || MiuiChargeManager.this.mRealLevel > MiuiChargeManager.this.mBatteryStatus.level) {
                MiuiChargeManager.this.mBatteryStatus.level = MiuiChargeManager.this.mRealLevel;
                MiuiChargeManager.this.notifyBatteryStatusChanged();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mNotUpdateLevelWhenBatteryChange;
    /* access modifiers changed from: private */
    public int mRealLevel;
    /* access modifiers changed from: private */
    public Runnable mUpdateChargingFromPowerCenterRunnable = new Runnable() {
        public void run() {
            MiuiChargeManager.this.getChargingStatusFromPowerCenter();
        }
    };
    /* access modifiers changed from: private */
    public int mWiredChargeType;
    /* access modifiers changed from: private */
    public int mWirelessChargeType;

    /* access modifiers changed from: private */
    public int checkWireState(int i, int i2) {
        boolean z = false;
        boolean z2 = i == 4;
        if (i == 1 || i == 2) {
            z = true;
        }
        if (i2 != 2 && i2 != 5 && i2 != 4) {
            return -1;
        }
        if (z2) {
            return 10;
        }
        return z ? 11 : -1;
    }

    private int formatBatteryLevel(int i) {
        if (i < 0) {
            return 0;
        }
        if (i > 100) {
            return 100;
        }
        return i;
    }

    public MiuiChargeManager(Context context) {
        this.mContext = context;
        this.mBatteryStatus = new MiuiBatteryStatus(1, 0, 0, 0, 0, -1, 1, -1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("miui.intent.action.ACTION_QUICK_CHARGE_TYPE");
        intentFilter.addAction("miui.intent.action.ACTION_WIRELESS_TX_TYPE");
        intentFilter.setPriority(1001);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    Dependency.get(MiuiChargeController.class);
                    int intExtra = intent.getIntExtra("status", 1);
                    int intExtra2 = intent.getIntExtra("plugged", 0);
                    int intExtra3 = intent.getIntExtra("level", 0);
                    int intExtra4 = intent.getIntExtra("health", 1);
                    int maxChargingWattage = MiuiBatteryStatus.getMaxChargingWattage(intent);
                    int unused = MiuiChargeManager.this.mRealLevel = intExtra3;
                    int access$100 = MiuiChargeManager.this.checkWireState(intExtra2, intExtra);
                    if (!MiuiBatteryStatus.isPluggedIn(MiuiChargeManager.this.mBatteryStatus.plugged) && MiuiBatteryStatus.isPluggedIn(intExtra2)) {
                        boolean unused2 = MiuiChargeManager.this.mIsChargeLevelAnimationRunning = false;
                        boolean unused3 = MiuiChargeManager.this.mNotUpdateLevelWhenBatteryChange = false;
                    }
                    boolean access$500 = MiuiChargeManager.this.isBatteryStatusChanged(intExtra3, intExtra2, intExtra);
                    MiuiChargeManager.this.mBatteryStatus.plugged = intExtra2;
                    MiuiChargeManager.this.mBatteryStatus.wireState = access$100;
                    MiuiChargeManager.this.mBatteryStatus.status = intExtra;
                    MiuiChargeManager.this.mBatteryStatus.health = intExtra4;
                    MiuiChargeManager.this.mBatteryStatus.maxChargingWattage = maxChargingWattage;
                    if (access$500) {
                        MiuiBatteryStatus access$200 = MiuiChargeManager.this.mBatteryStatus;
                        MiuiChargeManager miuiChargeManager = MiuiChargeManager.this;
                        access$200.chargeDeviceType = miuiChargeManager.getCurrentChargeDeviceType(miuiChargeManager.mBatteryStatus.wireState, MiuiChargeManager.this.mBatteryStatus.chargeDeviceType);
                        MiuiBatteryStatus access$2002 = MiuiChargeManager.this.mBatteryStatus;
                        MiuiChargeManager miuiChargeManager2 = MiuiChargeManager.this;
                        access$2002.chargeSpeed = miuiChargeManager2.getChargeSpeed(miuiChargeManager2.mBatteryStatus.wireState, MiuiChargeManager.this.mBatteryStatus.chargeDeviceType);
                        MiuiChargeManager.this.notifyBatteryStatusChanged();
                    }
                } else if ("miui.intent.action.ACTION_QUICK_CHARGE_TYPE".equals(intent.getAction())) {
                    int unused4 = MiuiChargeManager.this.mWiredChargeType = intent.getIntExtra("miui.intent.extra.quick_charge_type", -1);
                    if (MiuiChargeManager.this.mBatteryStatus.wireState == 11) {
                        MiuiChargeManager miuiChargeManager3 = MiuiChargeManager.this;
                        miuiChargeManager3.onChargeDeviceTypeChanged(miuiChargeManager3.mWiredChargeType);
                    }
                } else if ("miui.intent.action.ACTION_WIRELESS_TX_TYPE".equals(intent.getAction())) {
                    int unused5 = MiuiChargeManager.this.mWirelessChargeType = intent.getIntExtra("miui.intent.extra.wireless_tx_type", -1);
                    if (MiuiChargeManager.this.mBatteryStatus.wireState == 10) {
                        MiuiChargeManager miuiChargeManager4 = MiuiChargeManager.this;
                        miuiChargeManager4.onChargeDeviceTypeChanged(miuiChargeManager4.mWirelessChargeType);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public boolean isBatteryStatusChanged(int i, int i2, int i3) {
        if (i == this.mBatteryStatus.level || ((this.mIsChargeLevelAnimationRunning || this.mNotUpdateLevelWhenBatteryChange) && i != 100)) {
            MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
            if (i2 == miuiBatteryStatus.plugged && i3 == miuiBatteryStatus.status) {
                return false;
            }
            return true;
        }
        this.mBatteryStatus.level = i;
        return true;
    }

    /* access modifiers changed from: private */
    public void onChargeDeviceTypeChanged(int i) {
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
        if (miuiBatteryStatus != null && i >= 0) {
            int chargeSpeed = getChargeSpeed(miuiBatteryStatus.wireState, i);
            MiuiBatteryStatus miuiBatteryStatus2 = this.mBatteryStatus;
            miuiBatteryStatus2.chargeSpeed = chargeSpeed;
            int currentChargeDeviceType = getCurrentChargeDeviceType(miuiBatteryStatus2.wireState, i);
            MiuiBatteryStatus miuiBatteryStatus3 = this.mBatteryStatus;
            if (currentChargeDeviceType != miuiBatteryStatus3.chargeDeviceType) {
                miuiBatteryStatus3.chargeDeviceType = i;
                notifyBatteryStatusChanged();
            }
        }
    }

    public void setIsChargeLevelAnimationRunning(boolean z) {
        if (!this.mIsChargeLevelAnimationRunning && z) {
            this.mHandler.removeCallbacks(this.mNotUpdateLevelRunnable);
        }
        if (this.mIsChargeLevelAnimationRunning && !z) {
            this.mNotUpdateLevelWhenBatteryChange = true;
            this.mHandler.removeCallbacks(this.mNotUpdateLevelRunnable);
            this.mHandler.postDelayed(this.mNotUpdateLevelRunnable, 3000);
        }
        this.mIsChargeLevelAnimationRunning = z;
    }

    public void updateBattery(int i) {
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
        if (miuiBatteryStatus != null) {
            miuiBatteryStatus.level = i;
            notifyBatteryStatusChanged();
        }
    }

    /* access modifiers changed from: private */
    public int getCurrentChargeDeviceType(int i, int i2) {
        if (i == 10) {
            return this.mWirelessChargeType;
        }
        if (i == 11) {
            return this.mWiredChargeType;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public int getChargeSpeed(int i, int i2) {
        if (i != 10) {
            if (i == 11) {
                if (!ChargeUtils.isStrongSuperRapidCharge(i2)) {
                    if (ChargeUtils.isSuperRapidCharge(i2)) {
                        return 2;
                    }
                    if (ChargeUtils.isRapidCharge(i2)) {
                        return 1;
                    }
                }
            }
            return 0;
        } else if (!ChargeUtils.isWirelessStrongSuperRapidCharge(i2)) {
            if (ChargeUtils.isWirelessSuperRapidCharge(i2)) {
                return 2;
            }
            return 0;
        }
        return 3;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("MiuiChargeManager state:");
        printWriter.print("  isChargeAnimationDisabled =");
        printWriter.println(ChargeUtils.isChargeAnimationDisabled());
        if (this.mBatteryStatus != null) {
            printWriter.print("  mLevel =");
            printWriter.println(this.mBatteryStatus.level);
            printWriter.print("  mWireState =");
            printWriter.println(this.mBatteryStatus.wireState);
            printWriter.print("  mChargeSpeed =");
            printWriter.println(this.mBatteryStatus.chargeSpeed);
        }
    }

    /* access modifiers changed from: private */
    public void getChargingStatusFromPowerCenter() {
        new AsyncTask<Void, Void, Boolean>() {
            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                boolean z;
                try {
                    z = MiuiChargeManager.this.mContext.getContentResolver().call(Uri.parse(ChargeUtils.PROVIDER_POWER_CENTER), ChargeUtils.METHOD_GET_POWER_SUPPLY_INFO, (String) null, (Bundle) null).getBoolean(ChargeUtils.KEY_QUICK_CHARGE);
                } catch (Exception unused) {
                    Slog.e("MiuiChargeManager", "cannot find the path getPowerSupplyInfo of content://com.miui.powercenter.provider");
                    z = false;
                }
                return Boolean.valueOf(z);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                if (bool.booleanValue()) {
                    MiuiChargeManager.this.mHandler.removeCallbacks(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable);
                    if (!MiuiChargeManager.this.isSuperQuickCharging() && !MiuiChargeManager.this.isQuickCharging()) {
                        MiuiChargeManager.this.mBatteryStatus.chargeSpeed = 1;
                        MiuiChargeManager.this.onChargeDeviceTypeChanged(1);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public boolean isQuickCharging() {
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
        return miuiBatteryStatus != null && miuiBatteryStatus.chargeSpeed == 1;
    }

    public boolean isSuperQuickCharging() {
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
        return miuiBatteryStatus != null && miuiBatteryStatus.chargeSpeed == 2;
    }

    public boolean isStrongSuperQuickCharging() {
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
        return miuiBatteryStatus != null && miuiBatteryStatus.chargeSpeed == 3;
    }

    public boolean isUsbCharging() {
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
        return miuiBatteryStatus != null && miuiBatteryStatus.isUsbPluggedIn();
    }

    /* access modifiers changed from: private */
    public void notifyBatteryStatusChanged() {
        if (this.mBatteryStatus != null) {
            Slog.i("MiuiChargeManager", "notifyBatteryStatusChanged:  status: " + this.mBatteryStatus.status + " isPlugged: " + this.mBatteryStatus.plugged + " level: " + this.mBatteryStatus.level + " wireState: " + this.mBatteryStatus.wireState + " chargeSpeed: " + this.mBatteryStatus.chargeSpeed + " mWiredChargeType: " + this.mWiredChargeType + " mWirelessChargeType: " + this.mWirelessChargeType + " chargeDeviceType: " + this.mBatteryStatus.chargeDeviceType + " maxChargingWattage: " + this.mBatteryStatus.maxChargingWattage + " SUPPORT_BROADCAST_QUICK_CHARGE: " + true);
            MiuiBatteryStatus miuiBatteryStatus = this.mBatteryStatus;
            int i = miuiBatteryStatus.status;
            int i2 = miuiBatteryStatus.plugged;
            int formatBatteryLevel = formatBatteryLevel(miuiBatteryStatus.level);
            MiuiBatteryStatus miuiBatteryStatus2 = this.mBatteryStatus;
            ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).onBatteryStatusChange(new MiuiBatteryStatus(i, i2, formatBatteryLevel, miuiBatteryStatus2.wireState, miuiBatteryStatus2.chargeSpeed, miuiBatteryStatus2.chargeDeviceType, miuiBatteryStatus2.health, miuiBatteryStatus2.maxChargingWattage));
        }
    }
}
