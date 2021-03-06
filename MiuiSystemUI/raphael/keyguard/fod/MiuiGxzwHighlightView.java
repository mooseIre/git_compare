package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.widget.ImageView;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;

class MiuiGxzwHighlightView extends ImageView {
    private boolean mGradualGreenCircle = false;
    private boolean mGreenCircle = false;
    private int mGreenCircleColor;
    private boolean mGreenHalo = false;
    private boolean mInvertColor = false;
    private Paint mPaint;
    private boolean mSupportHalo = false;

    public MiuiGxzwHighlightView(Context context) {
        super(context);
    }

    public MiuiGxzwHighlightView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MiuiGxzwHighlightView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setTouchCenter(float f, float f2) {
        invalidate();
    }

    public void setOvalInfo(float f, float f2, float f3) {
        invalidate();
    }

    public void setVisibility(int i) {
        Log.i("MiuiGxzwHighlightView", "setVisibility: visibility = " + i);
        super.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mGradualGreenCircle) {
            return;
        }
        if ((!this.mSupportHalo || this.mInvertColor) && !this.mGreenHalo) {
            int width = getWidth();
            int height = getHeight();
            Slog.i("MiuiGxzwHighlightView", "MiuiGxzwHighlightView: onDraw GXZW_ICON_WIDTH= " + MiuiGxzwUtils.GXZW_ICON_WIDTH + ";GXZW_ICON_HEIGHT=" + MiuiGxzwUtils.GXZW_ICON_HEIGHT + ";width=" + width + ";heigth=" + height);
            canvas.drawCircle((float) (width / 2), (float) (height / 2), (float) (Math.min(MiuiGxzwUtils.GXZW_ICON_WIDTH, MiuiGxzwUtils.GXZW_ICON_HEIGHT) / 2), this.mPaint);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
        this.mGreenCircle = getContext().getResources().getBoolean(C0010R$bool.config_enableGreenCircle);
        this.mGreenCircleColor = getContext().getResources().getColor(C0011R$color.gxzw_circle_color);
        this.mGradualGreenCircle = getContext().getResources().getBoolean(C0010R$bool.config_enableGradualGreenCircle);
        this.mSupportHalo = MiuiGxzwUtils.supportHalo(getContext());
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setColor(this.mGreenCircleColor);
        this.mPaint.setAntiAlias(true);
        if (this.mGradualGreenCircle) {
            setImageResource(C0013R$drawable.gxzw_green_light);
        } else if (this.mSupportHalo) {
            setImageResource(MiuiGxzwUtils.getHaloRes());
        }
        setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void setInvertColorStatus(boolean z) {
        this.mInvertColor = z;
        if (!z || this.mGreenCircle) {
            this.mPaint.setColor(this.mGreenCircleColor);
        } else {
            this.mPaint.setColor(-16777216);
        }
        boolean healthAppAuthen = MiuiGxzwManager.getInstance().getHealthAppAuthen();
        this.mGreenHalo = healthAppAuthen;
        if (healthAppAuthen) {
            setImageResource(MiuiGxzwUtils.getHealthHaloRes());
        } else if ((this.mSupportHalo && z) || this.mGreenCircle) {
            setImageDrawable(null);
        } else if (this.mSupportHalo) {
            setImageResource(MiuiGxzwUtils.getHaloRes());
        }
        invalidate();
    }
}
