package com.nomi.wobblyview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

/**
 * Created by Ryota Niinomi on 15/09/27.
 */
public class WobblyView extends View implements View.OnTouchListener {

    private static final int BOUNCE_DURATION = 500;

    private float mPosX;
    private float mPosY;
    private float mMouseX;
    private float mMouseY;
    private float mWidth;
    private float mRadius;
    private ValueAnimator mBounceAnimator;

    public WobblyView(Context context) {
        this(context, null);
    }

    public WobblyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WobblyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPosX = canvas.getWidth() / 2;
        mPosY = canvas.getHeight() / 2;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 0, 0, 0));

        // Change radian to degree
        float degree = (float)(Math.atan2(mMouseY - mPosY, mMouseX - mPosX) * (180.0 / Math.PI));
        canvas.translate(mPosX, mPosY);
        canvas.rotate(degree);

        float _width = mWidth > mRadius ? mRadius - (mWidth / mRadius) * 1.5f : mRadius;
        if (_width < mRadius / 1.1f) {
            _width = mRadius / 1.1f;
        }

        // Circle
        Path path = new Path();
        RectF rectF = new RectF(-_width, -_width, mRadius, _width);
        path.addArc(rectF, 0, 360);

        // Stretchy part
        Path wavePath = new Path();
        float handleX = mWidth;
        float startHandleY = -_width + _width / 1.3f;
        float endHandleY = _width - _width / 1.3f;
        wavePath.moveTo(0, -_width);
        wavePath.cubicTo(handleX, startHandleY, handleX, endHandleY, 0, _width);
        canvas.drawPath(wavePath, paint);

        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        mMouseX = x;
        mMouseY = y;
        mWidth = (float)Math.sqrt(Math.pow(mPosX - mMouseX, 2) + Math.pow(mPosY - mMouseY, 2));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                setBounceAnimation();
                break;
        }

        return true;
    }

    private void init() {
        mRadius = 100;
        mWidth = mRadius;

        setOnTouchListener(this);
    }

    private void setBounceAnimation() {
        if (mBounceAnimator == null) {
            float width = mWidth;
            mBounceAnimator = ValueAnimator.ofFloat(width, mRadius);
            mBounceAnimator.setDuration(BOUNCE_DURATION);
            mBounceAnimator.setInterpolator(new BounceInterpolator());
            mBounceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mWidth = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mBounceAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mBounceAnimator = null;
                }
            });
            mBounceAnimator.start();
        }
    }
}
