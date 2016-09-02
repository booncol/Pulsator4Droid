package pl.bclogic.pulsator4droid.library;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by booncol on 04.07.2016.
 *
 */
public class PulsatorLayout extends RelativeLayout {

    public static final int INFINITE = 0;

    private static final int DEFAULT_COUNT = 4;
    private static final int DEFAULT_COLOR = Color.rgb(0, 116, 193);
    private static final int DEFAULT_DURATION = 7000;
    private static final int DEFAULT_REPEAT = INFINITE;
    private static final boolean DEFAULT_START_FROM_SCRATCH = true;

    private int mCount;
    private int mDuration;
    private int mRepeat;
    private boolean mStartFromScratch;

    private final List<View> mViews = new ArrayList<>();
    private AnimatorSet mAnimatorSet;
    private Paint mPaint;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    private boolean mIsStarted;
    private int color;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     */
    public PulsatorLayout(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public PulsatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style
     *                     resource that supplies default values for the view. Can be 0 to not look
     *                     for defaults.
     */
    public PulsatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // get attributes
        TypedArray attr = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.Pulsator4Droid, 0, 0);

        mCount = DEFAULT_COUNT;
        mDuration = DEFAULT_DURATION;
        mRepeat = DEFAULT_REPEAT;
        mStartFromScratch = DEFAULT_START_FROM_SCRATCH;

        color = DEFAULT_COLOR;

        try {
            mCount = attr.getInteger(R.styleable.Pulsator4Droid_pulse_count, DEFAULT_COUNT);
            mDuration = attr.getInteger(R.styleable.Pulsator4Droid_pulse_duration,
                    DEFAULT_DURATION);
            mRepeat = attr.getInteger(R.styleable.Pulsator4Droid_pulse_repeat, DEFAULT_REPEAT);
            mStartFromScratch = attr.getBoolean(R.styleable.Pulsator4Droid_pulse_startFromScratch,
                    DEFAULT_START_FROM_SCRATCH);
            color = attr.getColor(R.styleable.Pulsator4Droid_pulse_color, DEFAULT_COLOR);
        } finally {
            attr.recycle();
        }

        // create paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);

        // create views
        build();
    }

    /**
     * Start pulse animation.
     */
    public synchronized void start() {
        if (mAnimatorSet == null || mIsStarted) {
            return;
        }

        mAnimatorSet.start();

        if (!mStartFromScratch) {
            ArrayList<Animator> animators = mAnimatorSet.getChildAnimations();
            for (Animator animator : animators) {
                ObjectAnimator objectAnimator = (ObjectAnimator) animator;

                long delay = objectAnimator.getStartDelay();
                objectAnimator.setStartDelay(0);
                objectAnimator.setCurrentPlayTime(mDuration - delay);
            }
        }
    }

    /**
     * Stop pulse animation.
     */
    public synchronized void stop() {
        if (mAnimatorSet == null || !mIsStarted) {
            return;
        }

        mAnimatorSet.end();
    }

    public synchronized boolean isStarted() {
        return (mAnimatorSet != null && mIsStarted);
    }

    /**
     * Get number of pulses.
     *
     * @return Number of pulses
     */
    public int getCount() {
        return mCount;
    }

    /**
     * Get pulse duration.
     *
     * @return Duration of single pulse in milliseconds
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * Set number of pulses.
     *
     * @param count Number of pulses
     */
    public void setCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }

        if (count != mCount) {
            mCount = count;
            reset();
            invalidate();
        }
    }

    /**
     * Set single pulse duration.
     *
     * @param millis Pulse duration in milliseconds
     */
    public void setDuration(int millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        if (millis != mDuration) {
            mDuration = millis;
            reset();
            invalidate();
        }
    }

    /**
     * Gets the current color of the pulse effect in integer
     * Defaults to Color.rgb(0, 116, 193);
     * @return an integer representation of color
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the current color of the pulse effect in integer
     * Takes effect immediately
     * Usage: Color.parseColor("<hex-value>") or getResources().getColor(R.color.colorAccent)
     * @param color : an integer representation of color
     */
    public void setColor(int color) {
        this.color = color;
        if(mPaint != null) {
            mPaint.setColor(color);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        mCenterX = width * 0.5f;
        mCenterY = height * 0.5f;
        mRadius = Math.min(width, height) * 0.5f;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Remove all views and animators.
     */
    private void clear() {
        // remove animators
        stop();

        // remove old views
        for (View view : mViews) {
            removeView(view);
        }
        mViews.clear();
    }

    /**
     * Build pulse views and animators.
     */
    private void build() {
        // create views and animators
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        int repeatCount = (mRepeat == INFINITE) ? ObjectAnimator.INFINITE : mRepeat;

        List<Animator> animators = new ArrayList<>();
        for (int index = 0; index < mCount; index++) {
            // setup view
            PulseView pulseView = new PulseView(getContext());
            pulseView.setScaleX(0);
            pulseView.setScaleY(0);
            pulseView.setAlpha(1);

            addView(pulseView, index, layoutParams);
            mViews.add(pulseView);

            long delay = index * mDuration / mCount;

            // setup animators
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(pulseView, "ScaleX", 0f, 1f);
            scaleXAnimator.setRepeatCount(repeatCount);
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(delay);
            animators.add(scaleXAnimator);

            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(pulseView, "ScaleY", 0f, 1f);
            scaleYAnimator.setRepeatCount(repeatCount);
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleYAnimator.setStartDelay(delay);
            animators.add(scaleYAnimator);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(pulseView, "Alpha", 1f, 0f);
            alphaAnimator.setRepeatCount(repeatCount);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(delay);
            animators.add(alphaAnimator);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.setInterpolator(new LinearInterpolator());
        mAnimatorSet.setDuration(mDuration);
        mAnimatorSet.addListener(mAnimatorListener);
    }

    /**
     * Reset views and animations.
     */
    private void reset() {
        boolean isStarted = isStarted();

        clear();
        build();

        if (isStarted) {
            start();
        }
    }

    private class PulseView extends View {

        public PulseView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        }

    }

    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animator) {
            mIsStarted = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mIsStarted = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mIsStarted = false;
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }

    };

}
