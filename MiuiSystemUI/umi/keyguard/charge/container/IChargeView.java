package com.android.keyguard.charge.container;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.systemui.Dependency;
import com.miui.systemui.util.HapticFeedBackImpl;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class IChargeView extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    protected int mChargeSpeed;
    protected ViewGroup mContentContainer;
    protected Context mContext;
    protected AnimatorSet mDismissAnimatorSet;
    protected AnimatorSet mEnterAnimatorSet;
    private boolean mInitScreenOn;
    protected Interpolator mQuartOutInterpolator;
    protected Point mScreenSize;
    /* access modifiers changed from: private */
    public boolean mStartingDismissAnim;
    protected WindowManager mWindowManager;
    protected int mWireState;

    /* access modifiers changed from: protected */
    public void addChildView() {
    }

    /* access modifiers changed from: protected */
    public float getVideoTranslationY() {
        return 0.0f;
    }

    /* access modifiers changed from: protected */
    public void hideSystemUI() {
    }

    /* access modifiers changed from: protected */
    public void initAnimator() {
    }

    /* access modifiers changed from: protected */
    public void setComponentTransparent(boolean z) {
    }

    public void setProgress(int i) {
    }

    /* access modifiers changed from: protected */
    public void setViewState() {
    }

    /* access modifiers changed from: protected */
    public void startAnimationOnChildView() {
    }

    /* access modifiers changed from: protected */
    public void stopChildAnimation() {
    }

    public void switchContainerViewAnimation(int i) {
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamForScreenSizeChange() {
    }

    /* access modifiers changed from: protected */
    public void updateSizeForScreenSizeChange() {
    }

    public IChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public IChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mQuartOutInterpolator = new QuartEaseOutInterpolater();
        init(context);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        this.mChargeSpeed = 0;
        hideSystemUI();
        this.mContentContainer = new RelativeLayout(context);
        new RelativeLayout.LayoutParams(-1, -1).addRule(13);
        addChildView();
        addView(this.mContentContainer, getContainerLayoutParams());
    }

    private RelativeLayout.LayoutParams getContainerLayoutParams() {
        return new RelativeLayout.LayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void startAnimation(boolean z) {
        Log.d("IChargeView", "startAnimation: mInitScreenOn " + z);
        this.mInitScreenOn = z;
        MiuiBatteryStatus miuiBatteryStatus = ChargeUtils.sBatteryStatus;
        this.mWireState = miuiBatteryStatus.wireState;
        this.mChargeSpeed = miuiBatteryStatus.chargeSpeed;
        AnimatorSet animatorSet = this.mDismissAnimatorSet;
        if (animatorSet != null && this.mStartingDismissAnim) {
            animatorSet.cancel();
        }
        this.mStartingDismissAnim = false;
        hideSystemUI();
        setAlpha(this.mInitScreenOn ? 0.0f : 1.0f);
        setViewState();
        setVisibility(0);
        requestFocus();
        initAnimator();
        if (this.mEnterAnimatorSet.isStarted()) {
            this.mEnterAnimatorSet.cancel();
        }
        this.mEnterAnimatorSet.start();
        startAnimationOnChildView();
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(74, false, 0);
    }

    public void startDismiss(String str) {
        if (!this.mStartingDismissAnim) {
            AnimatorSet animatorSet = this.mEnterAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            Log.i("IChargeView", "startDismiss: reason: " + str);
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.mDismissAnimatorSet = animatorSet2;
            animatorSet2.addListener(new Animator.AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    boolean unused = IChargeView.this.mStartingDismissAnim = false;
                    IChargeView.this.dismiss();
                }

                public void onAnimationCancel(Animator animator) {
                    boolean unused = IChargeView.this.mStartingDismissAnim = false;
                }
            });
            this.mStartingDismissAnim = true;
        }
    }

    /* access modifiers changed from: private */
    public void dismiss() {
        stopChildAnimation();
        setComponentTransparent(true);
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (!this.mInitScreenOn) {
            animatedFraction = 1.0f;
        }
        setAlpha(animatedFraction);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
            requestLayout();
        }
    }
}
