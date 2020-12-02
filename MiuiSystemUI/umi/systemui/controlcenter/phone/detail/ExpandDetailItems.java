package com.android.systemui.controlcenter.phone.detail;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0008R$color;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0019R$style;
import com.android.systemui.controlcenter.phone.detail.ExpandDetailItems;
import com.android.systemui.qs.MiuiQSDetailItems;

public class ExpandDetailItems extends MiuiQSDetailItems {
    /* access modifiers changed from: private */
    public Context mContext;
    private int mOrientation;

    public ExpandDetailItems(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mAdapter = new ExpandAdapter();
    }

    public static ExpandDetailItems convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        context.getColor(C0008R$color.qs_control_expand_item_selected_color);
        if (view instanceof MiuiQSDetailItems) {
            return (ExpandDetailItems) view;
        }
        return (ExpandDetailItems) LayoutInflater.from(context).inflate(C0014R$layout.qs_control_expand_detail_items, viewGroup, false);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            if (i2 == 2) {
                layoutParams.height = -2;
            } else {
                layoutParams.height = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.qs_control_expand_detail_items_height);
            }
            setLayoutParams(layoutParams);
        }
    }

    protected static class CompleteItemHolder extends MiuiQSDetailItems.ItemHolder {
        protected ImageView icon;
        protected ImageView icon2;
        protected TextView summary;
        protected TextView title;

        public CompleteItemHolder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(C0012R$id.status_icon);
            this.icon2 = (ImageView) view.findViewById(C0012R$id.select_icon);
            this.title = (TextView) view.findViewById(C0012R$id.title);
            this.summary = (TextView) view.findViewById(C0012R$id.summary);
        }
    }

    private class ExpandAdapter extends MiuiQSDetailItems.Adapter {
        private ExpandAdapter() {
            super();
        }

        public MiuiQSDetailItems.ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new CompleteItemHolder(LayoutInflater.from(ExpandDetailItems.this.mContext).inflate(C0014R$layout.qs_control_expand_detail_item, viewGroup, false));
        }

        public void onBindViewHolder(MiuiQSDetailItems.ItemHolder itemHolder, int i) {
            if (ExpandDetailItems.this.mItems[i].type == 1) {
                MiuiQSDetailItems.Item item = ExpandDetailItems.this.mItems[i];
                CompleteItemHolder completeItemHolder = (CompleteItemHolder) itemHolder;
                completeItemHolder.itemView.setVisibility(item.activated ? 0 : 4);
                completeItemHolder.itemView.setBackgroundColor(item.selected ? ExpandDetailItems.this.mContext.getColor(C0008R$color.qs_control_detail_selected_color) : 0);
                completeItemHolder.icon.setImageDrawable(item.drawable);
                completeItemHolder.itemView.setActivated(item.activated);
                completeItemHolder.itemView.setSelected(item.selected);
                completeItemHolder.title.setText(item.line1);
                completeItemHolder.title.setMaxLines(1);
                completeItemHolder.summary.setVisibility(0);
                completeItemHolder.icon2.setVisibility(item.selected ? 0 : 8);
                SpannableString spannableString = new SpannableString(item.line2 + " " + item.unit);
                if (item.selected) {
                    completeItemHolder.title.setTextAppearance(C0019R$style.TextAppearance_QSControl_ExpandItemTitleSelect);
                } else {
                    completeItemHolder.title.setTextAppearance(C0019R$style.TextAppearance_QSControl_ExpandItemTitle);
                }
                if (!TextUtils.isEmpty(item.line2)) {
                    if (item.selected) {
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, C0019R$style.TextAppearance_QSControl_ExpandItemSubTitleSelect), 0, item.line2.length() - 1, 18);
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, C0019R$style.TextAppearance_QSControl_ExpandItemUnitSelect), item.line2.length(), spannableString.length(), 18);
                    } else {
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, C0019R$style.TextAppearance_QSControl_ExpandItemSubTitle), 0, item.line2.length() - 1, 18);
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, C0019R$style.TextAppearance_QSControl_ExpandItemUnit), item.line2.length(), spannableString.length(), 18);
                    }
                    completeItemHolder.summary.setText(spannableString);
                }
                if (item.activated) {
                    completeItemHolder.itemView.setOnClickListener(new View.OnClickListener(item) {
                        public final /* synthetic */ MiuiQSDetailItems.Item f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onClick(View view) {
                            ExpandDetailItems.ExpandAdapter.this.lambda$onBindViewHolder$0$ExpandDetailItems$ExpandAdapter(this.f$1, view);
                        }
                    });
                } else {
                    completeItemHolder.itemView.setOnClickListener((View.OnClickListener) null);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onBindViewHolder$0 */
        public /* synthetic */ void lambda$onBindViewHolder$0$ExpandDetailItems$ExpandAdapter(MiuiQSDetailItems.Item item, View view) {
            if (ExpandDetailItems.this.mCallback != null) {
                ExpandDetailItems.this.mCallback.onDetailItemClick(item);
            }
        }
    }
}