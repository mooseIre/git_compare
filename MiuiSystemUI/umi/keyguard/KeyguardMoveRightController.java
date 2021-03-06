package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import codeinjection.CodeInjection;
import com.android.keyguard.BaseKeyguardMoveController;
import com.android.keyguard.MiuiKeyguardCameraView;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.Dependency;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.graphics.DrawableUtils;
import miui.os.Build;

public class KeyguardMoveRightController extends BaseKeyguardMoveController {
    private String mCameraPreviewUri;
    private boolean mCameraViewShowing;
    private MiuiKeyguardCameraView mKeyguardCameraView;
    private MiuiKeyguardCameraView.CallBack mKeyguardCameraViewCallBack = new MiuiKeyguardCameraView.CallBack() {
        /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass1 */

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onAnimUpdate(float f) {
            KeyguardMoveRightController.this.mCallBack.onAnimUpdate(f);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onCompletedAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onCompletedAnimationEnd(true);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(false);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onCancelAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onCancelAnimationEnd(true, false);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onBackAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onBackAnimationEnd(true);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onVisibilityChanged(boolean z) {
            KeyguardMoveRightController.this.mCameraViewShowing = z;
            KeyguardMoveRightController.this.mCallBack.getMoveIconLayout(true).setVisibility(KeyguardMoveRightController.this.mCameraViewShowing ? 8 : 0);
            KeyguardMoveRightController keyguardMoveRightController = KeyguardMoveRightController.this;
            if (keyguardMoveRightController.mIsOnIconTouchDown) {
                keyguardMoveRightController.mCallBack.updateCanShowGxzw(!keyguardMoveRightController.mCameraViewShowing);
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void updatePreViewBackground() {
            KeyguardMoveRightController.this.updatePreViewBackground();
        }
    };
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass2 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedGoingToSleep(int i) {
            if (KeyguardMoveRightController.this.mCameraViewShowing) {
                KeyguardMoveRightController.this.reset();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.reset();
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.reset();
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onKeyguardShowingChanged(boolean z) {
            if (!z && KeyguardMoveRightController.this.mKeyguardCameraView != null) {
                KeyguardMoveRightController.this.mKeyguardCameraView.removeViewFromWindow();
                KeyguardMoveRightController.this.mKeyguardCameraView.releaseBitmapResource();
                KeyguardMoveRightController.this.mKeyguardCameraView = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            super.onStrongAuthStateChanged(i);
            KeyguardUpdateMonitor unused = KeyguardMoveRightController.this.mKeyguardUpdateMonitor;
            if (i == KeyguardUpdateMonitor.getCurrentUser() && !KeyguardMoveRightController.this.mUserAuthenticatedSinceBoot && KeyguardMoveRightController.this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
                KeyguardMoveRightController.this.mUserAuthenticatedSinceBoot = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass2.AnonymousClass1 */

                    public void run() {
                        KeyguardMoveRightController.this.updatePreViewBackground();
                    }
                }, 2000);
            }
        }
    };
    private boolean mUserAuthenticatedSinceBoot;

    public KeyguardMoveRightController(Context context, BaseKeyguardMoveController.CallBack callBack) {
        super(callBack, context);
        if (!MiuiKeyguardUtils.isPad()) {
            this.mEnableErrorTips = true;
        }
        this.mUserAuthenticatedSinceBoot = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        if (MiuiKeyguardUtils.hasNavigationBar(this.mContext)) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, new ContentObserver(new Handler()) {
                /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass3 */

                public void onChange(boolean z) {
                    KeyguardMoveRightController.this.updatePreViewBackground();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePreViewBackground() {
        if (!Build.IS_MIUI_LITE_VERSION) {
            new AsyncTask<Void, Void, Drawable>() {
                /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass4 */

                /* access modifiers changed from: protected */
                public Drawable doInBackground(Void... voidArr) {
                    if (!KeyguardMoveRightController.this.mUserAuthenticatedSinceBoot) {
                        return null;
                    }
                    if (!PackageUtils.IS_VELA_CAMERA) {
                        return getDrawableExceptVela(true);
                    }
                    KeyguardMoveRightController keyguardMoveRightController = KeyguardMoveRightController.this;
                    Context context = keyguardMoveRightController.mContext;
                    return keyguardMoveRightController.getDrawableFromPackageBy565(context, PackageUtils.PACKAGE_NAME_CAMERA, MiuiKeyguardUtils.getCameraImageName(context, MiuiKeyguardUtils.isFullScreenGestureOpened()));
                }

                private Drawable getDrawableExceptVela(boolean z) {
                    if (TextUtils.isEmpty(KeyguardMoveRightController.this.mCameraPreviewUri)) {
                        Context context = KeyguardMoveRightController.this.mContext;
                        Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(context, "content://" + PackageUtils.PACKAGE_NAME_CAMERA + ".splashProvider", "getCameraSplash", (String) null, (Bundle) null);
                        if (resultFromProvider != null) {
                            KeyguardMoveRightController.this.mCameraPreviewUri = String.valueOf(resultFromProvider.get("getCameraSplash"));
                        }
                    }
                    if (TextUtils.isEmpty(KeyguardMoveRightController.this.mCameraPreviewUri)) {
                        return null;
                    }
                    try {
                        return ImageDecoder.decodeDrawable(ImageDecoder.createSource(KeyguardMoveRightController.this.mContext.getContentResolver(), Uri.parse(KeyguardMoveRightController.this.mCameraPreviewUri), KeyguardMoveRightController.this.mContext.getResources()), $$Lambda$KeyguardMoveRightController$4$BLih8lMjXuQGgfkpxsSjkJl_48.INSTANCE);
                    } catch (Exception e) {
                        if (z) {
                            KeyguardMoveRightController.this.mCameraPreviewUri = CodeInjection.MD5;
                            Log.e("KeyguardMoveRightController", "updatePreViewBackground ContentProviderUtils.getResultFromProvider splashProvider fail,try again:" + e.getMessage() + e.getCause());
                            return getDrawableExceptVela(false);
                        }
                        Log.e("KeyguardMoveRightController", "updatePreViewBackground ContentProviderUtils.getResultFromProvider splashProvider  fail , wont try again" + e.getMessage() + e.getCause());
                        return null;
                    }
                }

                static /* synthetic */ void lambda$getDrawableExceptVela$0(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
                    imageDecoder.setAllocator(1);
                    imageDecoder.setMemorySizePolicy(0);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Drawable drawable) {
                    if (!DrawableUtils.isValidBitmapDrawable(drawable) || KeyguardMoveRightController.this.mKeyguardCameraView == null) {
                        Log.e("KeyguardMoveRightController", "updatePreViewBackground  onPostExecute resultDrawable is inValid");
                    } else {
                        KeyguardMoveRightController.this.mKeyguardCameraView.setPreviewImageDrawable(drawable);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public Drawable getDrawableFromPackageBy565(Context context, String str, String str2) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(str);
            int identifier = resourcesForApplication.getIdentifier(str2, "drawable", str);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return new BitmapDrawable(this.mContext.getResources(), BitmapFactory.decodeResource(resourcesForApplication, identifier, options));
        } catch (Exception unused) {
            Log.e("KeyguardMoveRightController", "something wrong when get image from" + str);
            return null;
        }
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (!this.mCallBack.isMoveInCenterScreen() || this.mCallBack.isRightMove()) {
            MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
            if (miuiKeyguardCameraView != null) {
                miuiKeyguardCameraView.reset();
                return;
            }
            return;
        }
        if (DebugConfig.DEBUG_KEYGUARD) {
            Log.d("KeyguardMoveRightController", "onTouchDown mTouchDownInitial = true");
        }
        this.mIsOnIconTouchDown = z;
        if (z) {
            if (this.mKeyguardCameraView == null) {
                this.mKeyguardCameraView = new MiuiKeyguardCameraView(this.mContext, this.mKeyguardCameraViewCallBack);
                updatePreViewBackground();
            }
            this.mKeyguardCameraView.onTouchDown(f, f2);
            this.mCallBack.getMoveIconLayout(true).setVisibility(8);
            this.mCallBack.updateCanShowGxzw(false);
        } else {
            this.mInitialTouchX = f;
            this.mInitialTouchY = f2;
        }
        this.mTouchDownInitial = true;
    }

    @Override // com.android.keyguard.BaseKeyguardMoveController
    public boolean onTouchMove(float f, float f2) {
        if (super.onTouchMove(f, f2)) {
            return true;
        }
        if (!this.mTouchDownInitial || !this.mIsOnIconTouchDown) {
            return false;
        }
        MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
        if (miuiKeyguardCameraView != null) {
            miuiKeyguardCameraView.onTouchMove(f, f2);
        }
        this.mCallBack.updateCanShowGxzw(false);
        return true;
    }

    @Override // com.android.keyguard.BaseKeyguardMoveController
    public void onTouchUp(float f, float f2) {
        MiuiKeyguardCameraView miuiKeyguardCameraView;
        if (this.mTouchDownInitial) {
            if (this.mIsOnIconTouchDown && (miuiKeyguardCameraView = this.mKeyguardCameraView) != null) {
                miuiKeyguardCameraView.onTouchUp(f, f2);
            }
            this.mCallBack.updateSwipingInProgress(false);
        }
        super.onTouchUp(f, f2);
    }

    public void reset() {
        MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
        if (miuiKeyguardCameraView != null) {
            miuiKeyguardCameraView.reset();
        }
        if (this.mCallBack.isMoveInCenterScreen()) {
            this.mCallBack.updateCanShowGxzw(true);
        }
        this.mCallBack.getMoveIconLayout(true).setVisibility(0);
    }
}
