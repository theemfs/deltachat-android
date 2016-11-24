/*
 * This part of the Delta Chat fronted is based on Telegram which is covered by the following note:
 *
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package com.b44t.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.b44t.messenger.AndroidUtilities;
import com.b44t.messenger.LocaleController;
import com.b44t.messenger.MrMailbox;
import com.b44t.messenger.ApplicationLoader;
import com.b44t.messenger.FileLog;
import com.b44t.messenger.R;
import com.b44t.messenger.TLRPC;
import com.b44t.ui.Components.LayoutHelper;
import com.b44t.ui.ActionBar.Theme;

public class DrawerProfileCell extends FrameLayout {

    private TextView nameTextView;
    private TextView subtitleTextView;
    private ImageView shadowView;
    private Rect srcRect = new Rect();
    private Rect destRect = new Rect();
    private Paint paint = new Paint();
    private int currentColor;

    public DrawerProfileCell(Context context) {
        super(context);
        setBackgroundColor(Theme.ACTION_BAR_COLOR);

        shadowView = new ImageView(context);
        shadowView.setVisibility(INVISIBLE);
        shadowView.setScaleType(ImageView.ScaleType.FIT_XY);
        shadowView.setImageResource(R.drawable.bottom_shadow);
        addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 100/*EDIT BY MR, was 70*/, Gravity.LEFT | Gravity.BOTTOM));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22 /*EDIT BY MR, was 15*/);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 16, 28));

        subtitleTextView = new TextView(context);
        subtitleTextView.setTextColor(0xffc2e5ff);
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        subtitleTextView.setLines(1);
        subtitleTextView.setMaxLines(1);
        subtitleTextView.setSingleLine(true);
        subtitleTextView.setGravity(Gravity.LEFT);
        addView(subtitleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 16, 9));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mrHeight = 180; // EDIT BY MR -- was: 148
        if (Build.VERSION.SDK_INT >= 21) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(mrHeight) + AndroidUtilities.statusBarHeight, MeasureSpec.EXACTLY));
        } else {
            try {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(mrHeight), MeasureSpec.EXACTLY));
            } catch (Exception e) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(mrHeight));
                FileLog.e("messenger", e);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable backgroundDrawable = ApplicationLoader.getCachedWallpaper();
        int color = ApplicationLoader.getServiceMessageColor();
        if (currentColor != color) {
            currentColor = color;
            shadowView.getDrawable().setColorFilter(new PorterDuffColorFilter(color | 0xff000000, PorterDuff.Mode.MULTIPLY));
        }

        if ( backgroundDrawable != null) {
            subtitleTextView.setTextColor(0xffffffff);
            shadowView.setVisibility(VISIBLE);
            if (backgroundDrawable instanceof ColorDrawable) {
                backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                backgroundDrawable.draw(canvas);
            } else if (backgroundDrawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                float scaleX = (float) getMeasuredWidth() / (float) bitmap.getWidth();
                float scaleY = (float) getMeasuredHeight() / (float) bitmap.getHeight();
                float scale = scaleX < scaleY ? scaleY : scaleX;
                int width = (int) (getMeasuredWidth() / scale);
                int height = (int) (getMeasuredHeight() / scale);
                int x = (bitmap.getWidth() - width) / 2;
                int y = (bitmap.getHeight() - height) / 2;
                srcRect.set(x, y, x + width, y + height);
                destRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                canvas.drawBitmap(bitmap, srcRect, destRect, paint);
            }
        } else {
            shadowView.setVisibility(INVISIBLE);
            subtitleTextView.setTextColor(0xffc2e5ff);
            super.onDraw(canvas);
        }
    }

    public void setUser(TLRPC.User user) {
        if (user == null) {
            return;
        }

        String displayname = MrMailbox.getConfig("displayname", LocaleController.getString("MyAccount", R.string.MyAccount));
        String addr;
        if( MrMailbox.isConfigured()!=0) {
            addr = MrMailbox.getConfig("addr", "");
        }
        else {
            addr = LocaleController.getString("AccountNotConfigured", R.string.AccountNotConfigured);
        }
        nameTextView.setText(displayname);
        subtitleTextView.setText(addr);
    }
}