package com.android.keyguard.charge.video;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.container.IChargeView;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0020R$raw;

public class VideoChargeView extends IChargeView {
    private VideoView mVideoView;

    public VideoChargeView(Context context) {
        this(context, (AttributeSet) null);
    }

    public VideoChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setComponentTransparent(true);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        super.init(context);
        this.mContentContainer.setBackgroundColor(-16777216);
    }

    /* access modifiers changed from: protected */
    public void addChildView() {
        this.mVideoView = new VideoView(this.mContext);
        if (this.mContentContainer != null) {
            Point point = this.mScreenSize;
            this.mContentContainer.setTranslationY((float) ((Math.max(point.x, point.y) - this.mVideoView.getVideoHeight()) / 2));
            ViewGroup viewGroup = this.mContentContainer;
            VideoView videoView = this.mVideoView;
            viewGroup.addView(videoView, videoView.getVideoLayoutParams());
        }
    }

    /* access modifiers changed from: protected */
    public float getVideoTranslationY() {
        Point point = this.mScreenSize;
        return (float) ((Math.max(point.x, point.y) - this.mVideoView.getVideoHeight()) / 2);
    }

    /* access modifiers changed from: protected */
    public void setViewState() {
        super.setViewState();
        this.mVideoView.removeChargeView();
        this.mVideoView.removeRapidChargeView();
        this.mVideoView.removeStrongRapidChargeView();
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamForScreenSizeChange() {
        Point point = this.mScreenSize;
        this.mContentContainer.setTranslationY((float) ((Math.max(point.x, point.y) - this.mVideoView.getVideoHeight()) / 2));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mVideoView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = this.mVideoView.getVideoHeight();
        this.mVideoView.setLayoutParams(layoutParams);
    }

    public void startAnimationOnChildView() {
        Log.d("VideoRapidChargeView", "startAnimationOnChildView: mChargeSpeed=" + this.mChargeSpeed + " mWireState=" + this.mWireState);
        int i = this.mWireState;
        if (i == 11) {
            startWiredAnimation();
        } else if (i == 10) {
            startWirelessAnimation();
        }
    }

    private void startWiredAnimation() {
        int i = this.mChargeSpeed;
        if (i == 0) {
            this.mVideoView.setDefaultImage(C0013R$drawable.wired_charge_video_bg_img);
            this.mVideoView.setChargeUri(getChargeUri());
            this.mVideoView.addChargeView();
        } else if (i == 3) {
            this.mVideoView.setDefaultImage(C0013R$drawable.wired_strong_super_charge_video_bg_img);
            this.mVideoView.setStrongRapidChargeUri(getChargeUri());
            this.mVideoView.addStrongRapidChargeView();
        } else {
            this.mVideoView.setDefaultImage(C0013R$drawable.wired_super_charge_video_bg_img);
            this.mVideoView.setRapidChargeUri(getChargeUri());
            this.mVideoView.addRapidChargeView();
        }
    }

    private String getResourcePath() {
        return "android.resource://" + this.mContext.getPackageName() + "/";
    }

    private String getChargeUri() {
        String str = getResourcePath() + C0020R$raw.wired_quick_charge_video;
        int i = this.mWireState;
        if (i == 11) {
            int i2 = this.mChargeSpeed;
            if (i2 == 3) {
                return getResourcePath() + C0020R$raw.wired_strong;
            } else if (i2 == 0) {
                return getResourcePath() + C0020R$raw.wired_charge_video;
            } else {
                return getResourcePath() + C0020R$raw.wired_quick_charge_video;
            }
        } else if (i != 10) {
            return str;
        } else {
            int i3 = this.mChargeSpeed;
            if (i3 == 3) {
                return getResourcePath() + C0020R$raw.wireless_strong;
            } else if (i3 == 2) {
                return getResourcePath() + C0020R$raw.wireless_quick_charge_video;
            } else {
                return getResourcePath() + C0020R$raw.wireless_charge_video;
            }
        }
    }

    private void startWirelessAnimation() {
        int i = this.mChargeSpeed;
        if (i == 3) {
            this.mVideoView.setDefaultImage(C0013R$drawable.wireless_strong_super_charge_video_bg_img);
            this.mVideoView.setStrongRapidChargeUri(getChargeUri());
            this.mVideoView.addStrongRapidChargeView();
        } else if (i == 2) {
            this.mVideoView.setDefaultImage(C0013R$drawable.wireless_super_charge_video_bg_img);
            this.mVideoView.setRapidChargeUri(getChargeUri());
            this.mVideoView.addRapidChargeView();
        } else {
            this.mVideoView.setDefaultImage(C0013R$drawable.wireless_charge_video_bg_img);
            this.mVideoView.setChargeUri(getChargeUri());
            this.mVideoView.addChargeView();
        }
    }

    /* access modifiers changed from: protected */
    public void initAnimator() {
        super.initAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, 1});
        ofInt.setInterpolator(this.mQuartOutInterpolator);
        ofInt.setDuration(800);
        ofInt.addUpdateListener(this);
        AnimatorSet animatorSet = new AnimatorSet();
        this.mEnterAnimatorSet = animatorSet;
        animatorSet.play(ofInt);
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        super.onAnimationUpdate(valueAnimator);
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mContentContainer.setScaleX(1.0f);
        this.mContentContainer.setScaleY(1.0f);
        this.mContentContainer.setAlpha(animatedFraction);
        this.mVideoView.setScaleX(1.0f);
        this.mVideoView.setScaleY(1.0f);
        this.mVideoView.setAlpha(animatedFraction);
    }

    public void switchContainerViewAnimation(int i) {
        super.switchContainerViewAnimation(i);
        Log.d("VideoRapidChargeView", "switchVideoViewAnimation: chargeSpeed=" + i + " mWireState=" + this.mWireState);
        this.mChargeSpeed = i;
        if (i == 0) {
            this.mVideoView.setChargeUri(getChargeUri());
            this.mVideoView.switchToNormalChargeAnim();
        } else if (i == 1 || i == 2) {
            this.mVideoView.setRapidChargeUri(getChargeUri());
            this.mVideoView.switchToRapidChargeAnim();
        } else if (i == 3) {
            this.mVideoView.setStrongRapidChargeUri(getChargeUri());
            this.mVideoView.switchToStrongRapidChargeAnim();
        }
    }

    public void startDismiss(String str) {
        Property property = FrameLayout.SCALE_Y;
        Property property2 = FrameLayout.SCALE_X;
        Property property3 = FrameLayout.ALPHA;
        super.startDismiss(str);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property3, new float[]{getAlpha(), 0.0f})}).setDuration(600);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, new float[]{this.mContentContainer.getAlpha(), 0.0f});
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mContentContainer, new PropertyValuesHolder[]{ofFloat}).setDuration(600);
        if (this.mWireState == 10) {
            PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mContentContainer.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mContentContainer.getScaleY(), 0.0f});
            duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mContentContainer, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(600);
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mVideoView.getAlpha(), 0.0f});
            PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mVideoView.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property, new float[]{this.mVideoView.getScaleY(), 0.0f});
            ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mVideoView, new PropertyValuesHolder[]{ofFloat4, ofFloat5, ofFloat6}).setDuration(600);
            this.mDismissAnimatorSet.playTogether(new Animator[]{duration2, duration3});
        } else {
            this.mDismissAnimatorSet.play(duration2);
        }
        this.mDismissAnimatorSet.setInterpolator(this.mQuartOutInterpolator);
        if (!"dismiss_for_timeout".equals(str)) {
            this.mDismissAnimatorSet.play(duration).with(duration2);
        }
        this.mDismissAnimatorSet.start();
    }

    /* access modifiers changed from: protected */
    public void stopChildAnimation() {
        this.mVideoView.stopAnimation();
        this.mVideoView.removeChargeView();
        this.mVideoView.removeRapidChargeView();
        this.mVideoView.removeStrongRapidChargeView();
    }

    /* access modifiers changed from: protected */
    public void setComponentTransparent(boolean z) {
        super.setComponentTransparent(z);
        if (z) {
            setAlpha(0.0f);
            this.mContentContainer.setAlpha(0.0f);
            return;
        }
        setAlpha(1.0f);
        this.mContentContainer.setAlpha(1.0f);
    }
}
