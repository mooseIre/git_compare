package com.android.systemui.media;

/* compiled from: MediaCarouselScrollHandler.kt */
final class MediaCarouselScrollHandler$onTouch$1 implements Runnable {
    final /* synthetic */ MediaCarouselScrollHandler this$0;

    MediaCarouselScrollHandler$onTouch$1(MediaCarouselScrollHandler mediaCarouselScrollHandler) {
        this.this$0 = mediaCarouselScrollHandler;
    }

    public final void run() {
        MediaCarouselScrollHandler.access$getDismissCallback$p(this.this$0).invoke();
    }
}
