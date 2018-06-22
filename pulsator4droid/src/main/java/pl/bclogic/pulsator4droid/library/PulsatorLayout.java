package pl.bclogic.pulsator4droid.library;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by booncol on 04.07.2016.
 */
public class PulsatorLayout extends RelativeLayout {

    private static final String TAG = PulsatorLayout.class.getSimpleName();

    private static final String PROPERTY_SCALE_X = "ScaleX";
    private static final String PROPERTY_SCALE_Y = "ScaleY";
    private static final String PROPERTY_ALPHA = "Alpha";

    public static final int INFINITE = 0;

    public static final int INTERP_LINEAR = 0;
    public static final int INTERP_ACCELERATE = 1;
    public static final int INTERP_DECELERATE = 2;
    public static final int INTERP_ACCELERATE_DECELERATE = 3;

    private static final float INITIAL_ALPHA = 1f;
    private static final float END_ALPHA = 0f;

    private static final int DEFAULT_COUNT = 4;
    private static final int DEFAULT_COLOR = Color.rgb(0, 116, 193);
    private static final int DEFAULT_DURATION = 7000;
    private static final int DEFAULT_REPEAT = INFINITE;
    private static final boolean DEFAULT_START_FROM_SCRATCH = true;
    private static final int DEFAULT_INTERPOLATOR = INTERP_LINEAR;
    public static final float DEFAULT_MIN_SCALE = 0.0f;
    public static final float DEFAULT_MAX_SCALE = 1.0f;

    private int mCount;
    private int mDuration;
    private int mRepeat;
    private boolean mStartFromScratch;
    private int mColor;
    private int mInterpolator;
    private float mMinScale;
    private float mMaxScale;

    private final List<View> mViews = new ArrayList<>();
    /**
     * {@link android.animation.AnimatorSet} seems to be having issues with
     * {@link android.animation.ValueAnimator#setCurrentPlayTime(long)} being used for its encapsulated animations. We
     * have to handle them (start them) manually one by one to avoid that.
     * More precisely Android versions O and P do not take current play time setting into consideration and play all the
     * animations at the same timing when started using an {@link android.animation.AnimatorSet}.
     * (The play time can be fast-forwarded for the whole set, but not before API 26.)
     */
    private List<Animator> mAnimators;
    private Paint mPaint;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    private boolean mIsStarted;

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
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public PulsatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     *
     * @param context      The Context the view is running in, through which it can access the current
     *                     theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
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
        mColor = DEFAULT_COLOR;
        mInterpolator = DEFAULT_INTERPOLATOR;
        mMinScale = DEFAULT_MIN_SCALE;
        mMaxScale = DEFAULT_MAX_SCALE;

        try {
            mCount = attr.getInteger(R.styleable.Pulsator4Droid_pulse_count, DEFAULT_COUNT);
            mDuration = attr.getInteger(R.styleable.Pulsator4Droid_pulse_duration,
                    DEFAULT_DURATION);
            mRepeat = attr.getInteger(R.styleable.Pulsator4Droid_pulse_repeat, DEFAULT_REPEAT);
            mStartFromScratch = attr.getBoolean(R.styleable.Pulsator4Droid_pulse_startFromScratch,
                    DEFAULT_START_FROM_SCRATCH);
            mColor = attr.getColor(R.styleable.Pulsator4Droid_pulse_color, DEFAULT_COLOR);
            mInterpolator = attr.getInteger(R.styleable.Pulsator4Droid_pulse_interpolator,
                    DEFAULT_INTERPOLATOR);
            mMinScale = attr.getFloat(R.styleable.Pulsator4Droid_pulse_minScale, DEFAULT_MIN_SCALE);
            mMaxScale = attr.getFloat(R.styleable.Pulsator4Droid_pulse_maxScale, DEFAULT_MAX_SCALE);
        } finally {
            attr.recycle();
        }

        // create paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);

        // create views
        build();
    }

    /**
     * Start pulse animation.
     */
    public synchronized void start() {
        if (mAnimators == null || mIsStarted) {
            return;
        }

        for (int x = 0; x < mAnimators.size(); x++) {
            ObjectAnimator objectAnimator = (ObjectAnimator) mAnimators.get(x);

            if (!mStartFromScratch) {
                // instead of delaying the animation, fast-forward it
                long delay = objectAnimator.getStartDelay();
                objectAnimator.setStartDelay(0);

                // This is where it starts to get tricky. The documentation of
                // ValueAnimator#setCurrentPlayTime(long) is a bit confusing about whether it should be called before
                // or after starting the animation itself. The truth is, it seems the behavior differs between Android
                // versions. If it gets called at a wrong time, only some of the animations will start while others will
                // not or the animated object won't be visible at all.
                boolean shouldStartBeforeSettingCurrentTime = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1;

                if (shouldStartBeforeSettingCurrentTime) {
                    objectAnimator.start();
                }
                objectAnimator.setCurrentPlayTime(mDuration - delay);
                if (!shouldStartBeforeSettingCurrentTime) {
                    objectAnimator.start();
                }
            } else {
                objectAnimator.start();
            }
        }
    }

    /**
     * Stop pulse animation.
     */
    public synchronized void stop() {
        if (mAnimators == null || !mIsStarted) {
            return;
        }
        for (Animator animator : mAnimators) {
            animator.end();
        }
    }

    public synchronized boolean isStarted() {
        return (mAnimators != null && mIsStarted);
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
     *
     * @return an integer representation of color
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Sets the current color of the pulse effect in integer
     * Takes effect immediately
     * Usage: Color.parseColor("<hex-value>") or getResources().getColor(R.color.colorAccent)
     *
     * @param color : an integer representation of color
     */
    public void setColor(int color) {
        if (color != mColor) {
            this.mColor = color;

            if (mPaint != null) {
                mPaint.setColor(color);
            }
        }
    }

    /**
     * Get current interpolator type used for animating.
     *
     * @return Interpolator type as int
     */
    public int getInterpolator() {
        return mInterpolator;
    }

    /**
     * Set current interpolator used for animating.
     *
     * @param type Interpolator type as int
     */
    public void setInterpolator(int type) {
        if (type != mInterpolator) {
            mInterpolator = type;
            reset();
            invalidate();
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

        mAnimators = new ArrayList<>(3 * mCount);
        for (int index = 0; index < mCount; index++) {
            // setup view
            PulseView pulseView = new PulseView(getContext());
            pulseView.setScaleX(mMinScale);
            pulseView.setScaleY(mMinScale);
            pulseView.setAlpha(INITIAL_ALPHA);

            addView(pulseView, index, layoutParams);
            mViews.add(pulseView);

            long delay = index * mDuration / mCount;

            // setup animators
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(pulseView, PROPERTY_SCALE_X, mMinScale, mMaxScale);
            scaleXAnimator.setStartDelay(delay);
            mAnimators.add(scaleXAnimator);

            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(pulseView, PROPERTY_SCALE_Y, mMinScale, mMaxScale);
            scaleYAnimator.setStartDelay(delay);
            mAnimators.add(scaleYAnimator);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(pulseView, PROPERTY_ALPHA, INITIAL_ALPHA, END_ALPHA);
            alphaAnimator.setStartDelay(delay);
            mAnimators.add(alphaAnimator);
        }

        for (Animator animator : mAnimators) {
            ObjectAnimator objectAnimator = (ObjectAnimator) animator;
            objectAnimator.setRepeatCount(repeatCount);
            objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
            objectAnimator.setInterpolator(createInterpolator(mInterpolator));
            objectAnimator.setDuration(mDuration);
        }

        if (mAnimators.isEmpty()) {
            mAnimators = null;
        } else {
            mAnimators.get(0).addListener(mAnimatorStartListener);
            mAnimators.get(mAnimators.size() - 1).addListener(mAnimatorEndListener);
        }
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

    /**
     * Create interpolator from type.
     *
     * @param type Interpolator type as int
     * @return Interpolator object of type
     */
    private static Interpolator createInterpolator(int type) {
        switch (type) {
            case INTERP_ACCELERATE:
                return new AccelerateInterpolator();
            case INTERP_DECELERATE:
                return new DecelerateInterpolator();
            case INTERP_ACCELERATE_DECELERATE:
                return new AccelerateDecelerateInterpolator();
            default:
                return new LinearInterpolator();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAnimators != null) {
            for (Animator animator : mAnimators) {
                animator.cancel();
            }
            mAnimators = null;
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

    private class AnimatorSimpleListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    private final AnimatorSimpleListener mAnimatorStartListener = new AnimatorSimpleListener() {

        @Override
        public void onAnimationStart(Animator animator) {
            mIsStarted = true;
        }

    };

    private final AnimatorSimpleListener mAnimatorEndListener = new AnimatorSimpleListener() {

        @Override
        public void onAnimationEnd(Animator animator) {
            mIsStarted = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mIsStarted = false;
        }

    };

}
