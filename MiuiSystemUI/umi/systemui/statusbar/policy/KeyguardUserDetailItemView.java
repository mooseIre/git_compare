package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.qs.tiles.UserDetailItemView;

public class KeyguardUserDetailItemView extends UserDetailItemView {
    public KeyguardUserDetailItemView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardUserDetailItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardUserDetailItemView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardUserDetailItemView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public int getFontSizeDimen() {
        return C0009R$dimen.kg_user_switcher_text_size;
    }
}
