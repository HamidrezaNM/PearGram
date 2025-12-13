/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.ActionBar;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.animation.DecelerateInterpolator;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;

public class BackDrawable extends Drawable {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint prevPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean reverseAngle;
    private long lastFrameTime;
    private boolean animationInProgress;
    private float finalRotation;
    private float currentRotation;
    private int currentAnimationTime;
    private boolean alwaysClose;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator();
    private int color = 0xffffffff;
    private int backColor = 0xff0a84ff;
    private int rotatedColor = 0xff757575;
    private float animationTime = 300.0f;
    private boolean rotated = true;
    private float floating;
    private int arrowRotation;
    private int unreadCount;
    private int unreadAlpha = 255;
    private float cancelAlpha;
    private float finalCancelAlpha;

    public float getRotation() {
        return finalRotation;
    }

    public BackDrawable(boolean close) {
        super();
        paint.setStrokeWidth(dp(2.25f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        prevPaint.setStrokeWidth(dp(2));
        prevPaint.setColor(Color.RED);
        backColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader);
        alwaysClose = close;
    }

    public void setColor(int value) {
        color = value;
        invalidateSelf();
    }

    public void setRotatedColor(int value) {
        rotatedColor = value;
        invalidateSelf();
    }

    public void setArrowRotation(int angle) {
        arrowRotation = angle;
        invalidateSelf();
    }

    public void setFloating(float value) {
        floating = value;
        invalidateSelf();
    }

    public void setRotation(float rotation, boolean animated) {
        lastFrameTime = 0;
//        if (currentRotation == 1) {
//            reverseAngle = true;
//        } else if (currentRotation == 0) {
//            reverseAngle = false;
//        }
        if (animated) {
            if (cancelAlpha < rotation) {
                currentAnimationTime = (int) (cancelAlpha * animationTime);
            } else {
                currentAnimationTime = (int) ((1.0f - cancelAlpha) * animationTime);
            }
            lastFrameTime = System.currentTimeMillis();
            finalCancelAlpha = rotation;
        } else {
            finalCancelAlpha = cancelAlpha = rotation;
        }
        invalidateSelf();
    }

    public void setAnimationTime(float value) {
        animationTime = value;
    }

    public void setRotated(boolean value) {
        rotated = value;
    }

    public void setUnreadCount(int value) { unreadCount = value; }
    public void setUnreadAlpha(int value) { unreadAlpha = value; }

    @Override
    public void draw(Canvas canvas) {
        if (cancelAlpha != finalCancelAlpha) {
            if (lastFrameTime != 0) {
                long dt = System.currentTimeMillis() - lastFrameTime;

                currentAnimationTime += dt;
                if (currentAnimationTime >= animationTime) {
                    cancelAlpha = finalCancelAlpha;
                } else {
                    if (cancelAlpha < finalCancelAlpha) {
                        cancelAlpha = interpolator.getInterpolation(currentAnimationTime / animationTime) * finalCancelAlpha;
                    } else {
                        cancelAlpha = 1.0f - interpolator.getInterpolation(currentAnimationTime / animationTime);
                    }
                }
            }
            lastFrameTime = System.currentTimeMillis();
            invalidateSelf();
        }

        paint.setTextSize(44);
        paint.setTextAlign(Paint.Align.CENTER);

        if (floating != 0) {
            TextPaint floatingPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

            floatingPaint.setColor(Color.WHITE);
            floatingPaint.setAlpha(Math.round(floating) * 80);

            paint.setTextSize((1 - floating) * 44);

            paint.setColor(ColorUtils.blendARGB(backColor, Color.WHITE, floating));

            paint.setStrokeWidth(dp(2.25f - (floating * .75f)));

            if(unreadCount != 0) {
                unreadAlpha = (int) (0xFF * (1 - floating));
            }

            canvas.drawCircle(dp(8f), dp(11.5f), dp(14f), floatingPaint);
        } else {
            paint.setColor(ColorUtils.blendARGB(backColor, rotatedColor, currentRotation));
        }

        canvas.save();
        canvas.translate(getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
        if (arrowRotation != 0) {
            canvas.rotate(arrowRotation);
        }
        float rotation = currentRotation;
        if (!alwaysClose) {
            canvas.rotate(currentRotation * (reverseAngle ? -225 : 135));
        } else {
            canvas.rotate(135 + currentRotation * (reverseAngle ? -180 : 180));
            rotation = 1.0f;
        }
//        canvas.drawLine(AndroidUtilities.dp(AndroidUtilities.lerp(-6.75f, -8f, rotation)), 0, AndroidUtilities.dp(8) - (paint.getStrokeWidth() / 2f) * (1f - rotation), 0, paint);
        float offsetX = -16f + (floating * 13f);
        float startYDiff = dp(-0.25f);
        float endYDiff = dp(AndroidUtilities.lerp(9f, 10f, rotation)) - (paint.getStrokeWidth() / 4f) * (1f - rotation);
        float startXDiff = dp(AndroidUtilities.lerp(-7f - 0.25f + offsetX, 0f + offsetX, rotation));
        float endXDiff = dp(offsetX + 1f);

        startYDiff *= (1f - floating * .2f);
        endYDiff *= (1f - floating * .2f);
        startXDiff *= (1f - floating * .2f);
        endXDiff *= (1f - floating * .2f);

        TextPaint cancelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        cancelPaint.setColor(backColor);
        cancelPaint.setTextSize(44);
        cancelPaint.setTextAlign(Paint.Align.CENTER);

        if (cancelAlpha != 0) {
            paint.setAlpha((int) (0xFF * (1 - cancelAlpha)));
            cancelPaint.setAlpha((int) (0xFF * cancelAlpha));
        }

        canvas.drawLine(startXDiff, -startYDiff, endXDiff, -endYDiff, paint);
        canvas.drawLine(startXDiff, startYDiff, endXDiff, endYDiff, paint);

        RectF rect = new RectF();
        TextPaint unreadPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        unreadPaint.setColor(Color.WHITE);
        unreadPaint.setTextSize(32);

        String counterText = Integer.toString(unreadCount);

        int counterWidth = (int) Math.ceil(unreadPaint.measureText(counterText));

        rect.set(dp(-8.5f), dp(-8.5f), counterWidth + dp(1.25f), dp(8.5f));

        int paintAlpha = cancelAlpha != 0 ? (int) (0xFF * (1 - cancelAlpha)) : unreadAlpha;
        if(unreadCount > 0) {
            paint.setAlpha(paintAlpha);
            unreadPaint.setAlpha(paintAlpha);
            canvas.drawRoundRect(rect, dp(11.5f), dp(11.5f), paint);
            canvas.drawText(counterText, dp(-3.75f), dp(4.5f), unreadPaint);
        }
        if(unreadCount == 0 || (unreadAlpha != 255 && floating == 0)) {
            if (unreadAlpha != 255 && unreadCount != 0)
                paint.setAlpha(255 - paintAlpha);
            canvas.drawText("Back", dp(10f), dp(0f) - (paint.getFontMetrics().ascent + paint.getFontMetrics().descent) / 2f, paint);
        }

        if (cancelAlpha != 0) {
            canvas.drawText("Cancel", dp(10f), dp(0f) - (paint.getFontMetrics().ascent + paint.getFontMetrics().descent) / 2f, cancelPaint);
        }

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return dp(24);
    }

    @Override
    public int getIntrinsicHeight() {
        return dp(24);
    }
}
