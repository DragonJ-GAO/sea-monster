package me.add1.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;

/**
 * Created by DragonJ on 15/2/4.
 */
public class CircleBitmapDrawable extends Drawable {

    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;
    private BitmapState mBitmapState;
    private Bitmap mBitmap;
    private int mTargetDensity;

    private final Rect mDstRect = new Rect(); // Gravity.apply() sets this

    private boolean mApplyGravity;
    private boolean mRebuildShader;
    private boolean mMutated;

    // These are scaled to match the target density.
    private int mBitmapWidth;
    private int mBitmapHeight;

    @Deprecated
    public CircleBitmapDrawable() {
        mBitmapState = new BitmapState((Bitmap) null);
    }

    public CircleBitmapDrawable(Resources res) {
        mBitmapState = new BitmapState((Bitmap) null);
        mBitmapState.mTargetDensity = mTargetDensity;
    }

    @Deprecated
    public CircleBitmapDrawable(Bitmap bitmap) {
        this(new BitmapState(bitmap), null);
    }

    public CircleBitmapDrawable(Resources res, Bitmap bitmap) {
        this(new BitmapState(bitmap), res);
        mBitmapState.mTargetDensity = mTargetDensity;
    }

    @Deprecated
    public CircleBitmapDrawable(String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
        }
    }

    /**
     * Create a drawable by opening a given file path and decoding the bitmap.
     */
    public CircleBitmapDrawable(Resources res, String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
        mBitmapState.mTargetDensity = mTargetDensity;
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
        }
    }

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     *
     * @deprecated Use {@link #BitmapDrawable(android.content.res.Resources, java.io.InputStream)}
     *             to ensure that the drawable has correctly set its target
     *             density.
     */
    @Deprecated
    public CircleBitmapDrawable(java.io.InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null);
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
        }
    }

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     */
    public CircleBitmapDrawable(Resources res, java.io.InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null);
        mBitmapState.mTargetDensity = mTargetDensity;
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
        }
    }

    public final Paint getPaint() {
        return mBitmapState.mPaint;
    }

    public final Bitmap getBitmap() {
        return mBitmap;
    }

    private void computeBitmapSize() {
        mBitmapWidth = mBitmap.getScaledWidth(mTargetDensity);
        mBitmapHeight = mBitmap.getScaledHeight(mTargetDensity);
    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (bitmap != null) {
            computeBitmapSize();
        } else {
            mBitmapWidth = mBitmapHeight = -1;
        }
    }

    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }

    public void setTargetDensity(DisplayMetrics metrics) {
        mTargetDensity = metrics.densityDpi;
        if (mBitmap != null) {
            computeBitmapSize();
        }
    }

    public void setTargetDensity(int density) {
        mTargetDensity = density == 0 ? DisplayMetrics.DENSITY_DEFAULT : density;
        if (mBitmap != null) {
            computeBitmapSize();
        }
    }

    public int getGravity() {
        return mBitmapState.mGravity;
    }

    public void setGravity(int gravity) {
        mBitmapState.mGravity = gravity;
        mApplyGravity = true;
    }

    public void setAntiAlias(boolean aa) {
        mBitmapState.mPaint.setAntiAlias(aa);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mBitmapState.mPaint.setFilterBitmap(filter);
    }

    @Override
    public void setDither(boolean dither) {
        mBitmapState.mPaint.setDither(dither);
    }

    public Shader.TileMode getTileModeX() {
        return mBitmapState.mTileModeX;
    }

    public Shader.TileMode getTileModeY() {
        return mBitmapState.mTileModeY;
    }

    public void setTileModeX(Shader.TileMode mode) {
        setTileModeXY(mode, mBitmapState.mTileModeY);
    }

    public final void setTileModeY(Shader.TileMode mode) {
        setTileModeXY(mBitmapState.mTileModeX, mode);
    }

    public void setTileModeXY(Shader.TileMode xmode, Shader.TileMode ymode) {
        final BitmapState state = mBitmapState;
        if (state.mPaint.getShader() == null || state.mTileModeX != xmode || state.mTileModeY != ymode) {
            state.mTileModeX = xmode;
            state.mTileModeY = ymode;
        }
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mBitmapState.mChangingConfigurations;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mApplyGravity = true;
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap bitmap = mBitmap;

        if (bitmap != null) {

            int radius = mBitmapWidth > mBitmapHeight ? mBitmapHeight / 2 : mBitmapWidth / 2;

            canvas.save();

            final BitmapState state = mBitmapState;
            Shader.TileMode tmx = state.mTileModeX;
            Shader.TileMode tmy = state.mTileModeY;

            Shader s = new BitmapShader(bitmap, tmx == null ? Shader.TileMode.CLAMP : tmx, tmy == null ? Shader.TileMode.CLAMP : tmy);
            state.mPaint.setShader(s);
            state.mPaint.setAntiAlias(true);

            copyBounds(mDstRect);

            Shader shader = state.mPaint.getShader();

            if (mApplyGravity) {
                mDstRect.set(getBounds());
                mApplyGravity = false;
            }
            canvas.drawCircle(mBitmapWidth / 2, mBitmapHeight / 2, radius, state.mPaint);

        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mBitmapState.mPaint.setColorFilter(cf);
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mBitmapState = new BitmapState(mBitmapState);
            mMutated = true;
        }
        return this;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    @Override
    public int getOpacity() {
        if (mBitmapState.mGravity != Gravity.FILL) {
            return PixelFormat.TRANSLUCENT;
        }
        Bitmap bm = mBitmap;
        return (bm == null || bm.hasAlpha() || mBitmapState.mPaint.getAlpha() < 255) ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
    }

    @Override
    public final ConstantState getConstantState() {
        mBitmapState.mChangingConfigurations = super.getChangingConfigurations();
        return mBitmapState;
    }

    final static class BitmapState extends ConstantState {
        Bitmap mBitmap;
        int mChangingConfigurations;
        int mGravity = Gravity.FILL;
        Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);
        Shader.TileMode mTileModeX;
        Shader.TileMode mTileModeY;
        int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

        BitmapState(Bitmap bitmap) {
            mBitmap = bitmap;
            mPaint.setAntiAlias(true);
        }

        BitmapState(BitmapState bitmapState) {
            this(bitmapState.mBitmap);
            mChangingConfigurations = bitmapState.mChangingConfigurations;
            mGravity = bitmapState.mGravity;
            mTileModeX = bitmapState.mTileModeX;
            mTileModeY = bitmapState.mTileModeY;
            mTargetDensity = bitmapState.mTargetDensity;
            mPaint = new Paint(bitmapState.mPaint);
            mPaint.setAntiAlias(true);
        }

        @Override
        public Drawable newDrawable() {
            return new CircleBitmapDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new CircleBitmapDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

    private CircleBitmapDrawable(BitmapState state, Resources res) {
        mBitmapState = state;
        if (res != null) {
            mTargetDensity = res.getDisplayMetrics().densityDpi;
        } else if (state != null) {
            mTargetDensity = state.mTargetDensity;
        } else {
            mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        }
        setBitmap(state.mBitmap);
    }

    @Override
    public void setAlpha(int alpha) {
        mBitmapState.mPaint.setAlpha(alpha);
    }
}