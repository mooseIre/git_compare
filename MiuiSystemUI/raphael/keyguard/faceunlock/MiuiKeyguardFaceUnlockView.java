package com.android.keyguard.faceunlock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.keyguard.Ease$Sine;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.util.HapticFeedBackImpl;
import com.miui.systemui.util.MiuiAnimationUtils;

public class MiuiKeyguardFaceUnlockView extends LinearLayout {
    /* access modifiers changed from: private */
    public Handler mAnimationHandler;
    private Context mContext;
    private final Runnable mDelayedHide;
    /* access modifiers changed from: private */
    public boolean mFaceUnlockAnimationRuning;
    private View.OnClickListener mFaceUnlockClickListener;
    /* access modifiers changed from: private */
    public MiuiFaceUnlockManager mFaceUnlockManager;
    private boolean mIsKeyguardFaceUnlockView;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    private boolean mLightClock;
    /* access modifiers changed from: private */
    public boolean mLockScreenMagazinePreViewVisibility;
    /* access modifiers changed from: private */
    public final PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public boolean mWaitWakeupAimation;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver;
    private final IMiuiKeyguardWallpaperController.IWallpaperChangeCallback mWallpaperChangeCallback;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiKeyguardFaceUnlockView(boolean z) {
        this.mLightClock = z;
        updateFaceUnlockIconStatus();
    }

    public MiuiKeyguardFaceUnlockView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardFaceUnlockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAnimationHandler = new Handler();
        this.mLightClock = false;
        this.mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
            public void onStartedWakingUp() {
                if (MiuiKeyguardFaceUnlockView.this.getVisibility() == 0) {
                    boolean unused = MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = false;
                    MiuiKeyguardFaceUnlockView.this.startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
                    return;
                }
                boolean unused2 = MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = true;
                MiuiKeyguardFaceUnlockView.this.mAnimationHandler.postDelayed(new Runnable() {
                    public final void run() {
                        MiuiKeyguardFaceUnlockView.AnonymousClass1.this.lambda$onStartedWakingUp$0$MiuiKeyguardFaceUnlockView$1();
                    }
                }, 200);
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onStartedWakingUp$0 */
            public /* synthetic */ void lambda$onStartedWakingUp$0$MiuiKeyguardFaceUnlockView$1() {
                boolean unused = MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = false;
            }

            public void onFinishedGoingToSleep() {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }
        };
        this.mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
            public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
                if (biometricSourceType != BiometricSourceType.FACE) {
                    return;
                }
                if (i == 10001) {
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                } else if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation()) {
                    MiuiKeyguardFaceUnlockView.this.startFaceUnlockAnimation();
                }
            }

            public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
                if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation() && MiuiKeyguardFaceUnlockView.this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess() && !MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.isBouncerShowing() && biometricSourceType == BiometricSourceType.FACE) {
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("mesh_light", false);
                    MiuiKeyguardFaceUnlockView.this.startFaceUnlockSuccessAnimation();
                }
            }

            public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }

            public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                if (biometricSourceType != BiometricSourceType.FACE) {
                    return;
                }
                if (i == 9) {
                    MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                } else if (i == 10002) {
                    MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                }
            }

            public void onKeyguardBouncerChanged(boolean z) {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }

            public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
                boolean unused = MiuiKeyguardFaceUnlockView.this.mLockScreenMagazinePreViewVisibility = z;
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                if (z) {
                    MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.cancelFaceAuth();
                }
            }
        };
        this.mWallpaperChangeCallback = new IMiuiKeyguardWallpaperController.IWallpaperChangeCallback() {
            public final void onWallpaperChange(boolean z) {
                MiuiKeyguardFaceUnlockView.this.lambda$new$0$MiuiKeyguardFaceUnlockView(z);
            }
        };
        this.mFaceUnlockClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFaceDetectionRunning()) {
                    ObjectAnimator ofFloat = ObjectAnimator.ofFloat(MiuiKeyguardFaceUnlockView.this, "scaleX", new float[]{1.0f, 1.2f, 0.9f, 1.0f});
                    ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(MiuiKeyguardFaceUnlockView.this, "scaleY", new float[]{1.0f, 1.2f, 0.9f, 1.0f});
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setInterpolator(Ease$Sine.easeInOut);
                    animatorSet.setDuration(400);
                    animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.requestFaceAuth(2);
                            MiuiKeyguardFaceUnlockView.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                        }
                    });
                    animatorSet.start();
                }
            }
        };
        this.mDelayedHide = new Runnable() {
            public void run() {
                boolean unused = MiuiKeyguardFaceUnlockView.this.mFaceUnlockAnimationRuning = false;
            }
        };
        this.mContext = context;
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mFaceUnlockManager = (MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this.mFaceUnlockClickListener);
        updateFaceUnlockViewForNotch();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mFaceUnlockManager.addFaceUnlockView(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        this.mFaceUnlockManager.removeFaceUnlockView(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).removeObserver(this.mWakefulnessObserver);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this.mWallpaperChangeCallback);
    }

    private void updateFaceUnlockViewForNotch() {
        int i;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        Resources resources = this.mContext.getResources();
        if (DeviceConfig.IS_NOTCH) {
            i = C0012R$dimen.miui_face_unlock_view_notch_top;
        } else {
            i = C0012R$dimen.miui_face_unlock_view_top;
        }
        marginLayoutParams.topMargin = resources.getDimensionPixelSize(i);
        setLayoutParams(marginLayoutParams);
    }

    public void setKeyguardFaceUnlockView(boolean z) {
        this.mIsKeyguardFaceUnlockView = z;
    }

    /* access modifiers changed from: private */
    public boolean shouldFaceUnlockViewExecuteAnimation() {
        return (!this.mUpdateMonitor.isBouncerShowing() && this.mIsKeyguardFaceUnlockView) || (this.mUpdateMonitor.isBouncerShowing() && !this.mIsKeyguardFaceUnlockView);
    }

    /* access modifiers changed from: private */
    public void startFaceUnlockAnimation() {
        if (!this.mFaceUnlockAnimationRuning) {
            this.mFaceUnlockAnimationRuning = true;
            AnimationDrawable animationDrawable = new AnimationDrawable();
            for (int i = 1; i <= 30; i++) {
                String str = (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) ? "face_unlock_error" : "face_unlock_black_error";
                animationDrawable.addFrame(getResources().getDrawable(this.mContext.getResources().getIdentifier(str + i, "drawable", this.mContext.getPackageName())), 16);
            }
            setBackground(animationDrawable);
            animationDrawable.setOneShot(true);
            animationDrawable.start();
            this.mAnimationHandler.postDelayed(this.mDelayedHide, 1480);
        }
    }

    /* access modifiers changed from: private */
    public void stopShakeHeadAnimation() {
        this.mAnimationHandler.removeCallbacks(this.mDelayedHide);
        this.mFaceUnlockAnimationRuning = false;
    }

    /* access modifiers changed from: private */
    public void startFaceUnlockSuccessAnimation() {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        for (int i = 1; i <= 20; i++) {
            String str = (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) ? "face_unlock_success" : "face_unlock_black_success";
            animationDrawable.addFrame(getResources().getDrawable(this.mContext.getResources().getIdentifier(str + i, "drawable", this.mContext.getPackageName())), 16);
        }
        animationDrawable.setOneShot(true);
        setBackground(animationDrawable);
        animationDrawable.start();
    }

    public void setVisibility(int i) {
        if (i == 0 && getVisibility() != i && this.mWaitWakeupAimation) {
            startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
            this.mWaitWakeupAimation = false;
        }
        super.setVisibility(i);
    }

    public void updateFaceUnlockIconStatus() {
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(this.mContext)) {
            if (!shouldFaceUnlockViewExecuteAnimation() || this.mFaceUnlockManager.isDisableLockScreenFaceUnlockAnim() || !shouldShowFaceUnlockImage()) {
                setVisibility(4);
            } else {
                setVisibility(0);
            }
            boolean isFaceUnlock = ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock();
            if (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) {
                setBackground(getResources().getDrawable(isFaceUnlock ? C0013R$drawable.face_unlock_success20 : C0013R$drawable.face_unlock_error1));
            } else {
                setBackground(getResources().getDrawable(isFaceUnlock ? C0013R$drawable.face_unlock_black_success20 : C0013R$drawable.face_unlock_black_error1));
            }
        }
    }

    private boolean shouldShowFaceUnlockImage() {
        Class cls = KeyguardUpdateMonitorInjector.class;
        boolean z = this.mFaceUnlockManager.isFaceAuthEnabled() && !this.mUpdateMonitor.userNeedsStrongAuth() && ((KeyguardUpdateMonitorInjector) Dependency.get(cls)).isKeyguardShowing() && !this.mFaceUnlockManager.isFaceUnlockLocked() && !this.mUpdateMonitor.isSimPinSecure();
        boolean isKeyguardOccluded = ((KeyguardUpdateMonitorInjector) Dependency.get(cls)).isKeyguardOccluded();
        if (this.mUpdateMonitor.isBouncerShowing()) {
            if (!z || (isKeyguardOccluded && MiuiKeyguardUtils.isTopActivityCameraApp(this.mContext))) {
                return false;
            }
            return true;
        } else if (!z || isKeyguardOccluded || MiuiFaceUnlockUtils.isSupportLiftingCamera(this.mContext) || ((!this.mUpdateMonitor.isFaceDetectionRunning() && !((KeyguardUpdateMonitorInjector) Dependency.get(cls)).isFaceUnlock()) || this.mLockScreenMagazinePreViewVisibility)) {
            return false;
        } else {
            return true;
        }
    }
}