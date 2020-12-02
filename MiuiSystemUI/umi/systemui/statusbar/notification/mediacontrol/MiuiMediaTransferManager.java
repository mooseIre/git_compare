package com.android.systemui.statusbar.notification.mediacontrol;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRoute2Info;
import android.media.MediaRouter;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.media.InfoMediaManager;
import com.android.settingslib.media.LocalMediaManager;
import com.android.settingslib.media.MediaDevice;
import com.android.settingslib.media.PhoneMediaDevice;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.miui.systemui.util.ReflectUtil;
import java.util.ArrayList;
import java.util.List;

public class MiuiMediaTransferManager {
    private static final boolean MIUI_MEDIA_SEAMLESS_ENABLED = (Build.VERSION.SDK_INT > 28);
    /* access modifiers changed from: private */
    public final ActivityStarter mActivityStarter = ((ActivityStarter) Dependency.get(ActivityStarter.class));
    private MediaDevice mCurDevice;
    private LocalMediaManager mLocalMediaManager;
    private final MediaRouter.SimpleCallback mMediaCallback = new MediaRouter.SimpleCallback() {
        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            MiuiMediaTransferManager miuiMediaTransferManager = MiuiMediaTransferManager.this;
            miuiMediaTransferManager.updateCurrentDevice(TextUtils.equals(miuiMediaTransferManager.mPhoneName, routeInfo.getName()));
        }
    };
    private final LocalMediaManager.DeviceCallback mMediaDeviceCallback = new LocalMediaManager.DeviceCallback() {
        public void onDeviceListUpdate(List<MediaDevice> list) {
            MiuiMediaTransferManager.this.updatePhoneDevice(list);
            MiuiMediaTransferManager.this.updateCurrentDevice(false);
        }

        public void onSelectedDeviceStateChanged(MediaDevice mediaDevice, int i) {
            boolean unused = MiuiMediaTransferManager.this.updatePhoneDevice(mediaDevice);
            MiuiMediaTransferManager.this.updateAllChips(false);
        }
    };
    private MediaRouter mMediaRouter;
    private final View.OnClickListener mOnClickHandler = new View.OnClickListener() {
        public void onClick(View view) {
            handleMediaTransfer();
        }

        private void handleMediaTransfer() {
            MiuiMediaTransferManager.this.mActivityStarter.startActivity(new Intent().setAction("miui.bluetooth.mible.MiuiAudioRelayActivity"), false, true, 268435456);
        }
    };
    /* access modifiers changed from: private */
    public String mPhoneName;
    private List<ImageView> mViews = new ArrayList();

    public MiuiMediaTransferManager(Context context) {
        LocalBluetoothManager localBluetoothManager = (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
        this.mLocalMediaManager = new LocalMediaManager(context, localBluetoothManager, new InfoMediaManager(context, context.getPackageName(), (Notification) null, localBluetoothManager), (String) null);
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
    }

    /* access modifiers changed from: private */
    public void updateCurrentDevice(boolean z) {
        this.mCurDevice = getCurrentConnectedDevice();
        updateAllChips(z);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x000d A[LOOP:0: B:4:0x000d->B:7:0x001d, LOOP_START] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updatePhoneDevice(java.util.List<com.android.settingslib.media.MediaDevice> r2) {
        /*
            r1 = this;
            java.lang.String r0 = r1.mPhoneName
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0009
            return
        L_0x0009:
            java.util.Iterator r2 = r2.iterator()
        L_0x000d:
            boolean r0 = r2.hasNext()
            if (r0 == 0) goto L_0x001f
            java.lang.Object r0 = r2.next()
            com.android.settingslib.media.MediaDevice r0 = (com.android.settingslib.media.MediaDevice) r0
            boolean r0 = r1.updatePhoneDevice((com.android.settingslib.media.MediaDevice) r0)
            if (r0 == 0) goto L_0x000d
        L_0x001f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager.updatePhoneDevice(java.util.List):void");
    }

    /* access modifiers changed from: private */
    public boolean updatePhoneDevice(MediaDevice mediaDevice) {
        if (!TextUtils.isEmpty(this.mPhoneName)) {
            return true;
        }
        if (!(mediaDevice instanceof PhoneMediaDevice)) {
            return false;
        }
        try {
            MediaRoute2Info mediaRoute2Info = (MediaRoute2Info) ReflectUtil.getObjectFieldAndSuper(mediaDevice, "mRouteInfo", MediaRoute2Info.class);
            if (mediaRoute2Info == null) {
                return false;
            }
            this.mPhoneName = mediaRoute2Info.getName().toString();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("MiuiMediaTransferManager", "Can't find phone name!");
            return false;
        }
    }

    private MediaDevice getCurrentConnectedDevice() {
        MediaDevice currentConnectedDevice = this.mLocalMediaManager.getCurrentConnectedDevice();
        updatePhoneDevice(currentConnectedDevice);
        return currentConnectedDevice;
    }

    public void setRemoved(View view) {
        if (MIUI_MEDIA_SEAMLESS_ENABLED && this.mLocalMediaManager != null && view != null) {
            ImageView imageView = (ImageView) view.findViewById(C0015R$id.media_seamless_image);
            if (!this.mViews.remove(imageView)) {
                Log.e("MiuiMediaTransferManager", "Tried to remove unknown view " + imageView);
            } else if (this.mViews.size() == 0) {
                this.mLocalMediaManager.unregisterCallback(this.mMediaDeviceCallback);
                this.mMediaRouter.removeCallback(this.mMediaCallback);
            }
        }
    }

    public void applyMediaTransferView(ViewGroup viewGroup) {
        if (MIUI_MEDIA_SEAMLESS_ENABLED && this.mLocalMediaManager != null && viewGroup != null) {
            ImageView imageView = (ImageView) viewGroup.findViewById(C0015R$id.media_seamless_image);
            if (imageView == null) {
                Log.e("MiuiMediaTransferManager", "There is no {ImageView @media_seamless_image} in root");
                return;
            }
            imageView.setVisibility(0);
            imageView.setOnClickListener(this.mOnClickHandler);
            if (!this.mViews.contains(imageView)) {
                this.mViews.add(imageView);
                if (this.mViews.size() == 1) {
                    this.mLocalMediaManager.registerCallback(this.mMediaDeviceCallback);
                    this.mMediaRouter.addCallback(8388615, this.mMediaCallback, 3);
                }
            }
            this.mLocalMediaManager.startScan();
            updatePhoneDevice(this.mLocalMediaManager.getSelectableMediaDevice());
            this.mCurDevice = getCurrentConnectedDevice();
            updateChip(imageView, false);
        }
    }

    /* access modifiers changed from: private */
    public void updateAllChips(boolean z) {
        for (ImageView updateChip : this.mViews) {
            updateChip(updateChip, z);
        }
    }

    private void updateChip(ImageView imageView, boolean z) {
        MediaDevice mediaDevice;
        if (z || (mediaDevice = this.mCurDevice) == null || (mediaDevice instanceof PhoneMediaDevice)) {
            imageView.setImageResource(C0013R$drawable.ic_media_seamless);
            imageView.setContentDescription((CharSequence) null);
            return;
        }
        imageView.setImageResource(C0013R$drawable.ic_media_seamless_others);
        imageView.setContentDescription(this.mCurDevice.getName());
    }
}
