package me.add1.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;

/**
 * Created by dragonj on 16/3/2.
 */
public class RadiusBitmapDrawable extends Drawable {

    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;
    private BitmapState mBitmapState;
    private Bitmap mBitmap;
    private int mTargetDensity;

    private final Rect mDstRect = new Rect(); // Gravity.apply() sets this

    private boolean mApplyGravity;
    private boolean mRebuildShader;
    private boolean mMutated;
    private float mRadius = 3f;

    // These are scaled to match the target density.
    private int mBitmapWidth;
    private int mBitmapHeight;

    @Deprecated
    public RadiusBitmapDrawable() {
        mBitmapState = new BitmapState((Bitmap) null);
    }

    public RadiusBitmapDrawable(Resources res, float radius) {
        mBitmapState = new BitmapState((Bitmap) null);
        mRadius = radius;
        mBitmapState.mTargetDensity = mTargetDensity;
    }

    @Deprecated
    public RadiusBitmapDrawable(Bitmap bitmap, float radius) {
        this(new BitmapState(bitmap), null, radius);
    }

    public RadiusBitmapDrawable(Resources res, Bitmap bitmap, float radius) {
        this(new BitmapState(bitmap), res, radius);
        mBitmapState.mTargetDensity = mTargetDensity;
    }

    @Deprecated
    public RadiusBitmapDrawable(String filepath, float radius) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null, radius);
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
        }
    }

    /**
     * Create a drawable by opening a given file path and decoding the bitmap.
     */
    public RadiusBitmapDrawable(Resources res, String filepath, float radius) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null, radius);
        mBitmapState.mTargetDensity = mTargetDensity;
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
        }
    }

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     *
     * @deprecated Use {@link #RadiusBitmapDrawable(android.content.res.Resources, java.io.InputStream, float)}
     * to ensure that the drawable has correctly set its target
     * density.
     */
    @Deprecated
    public RadiusBitmapDrawable(java.io.InputStream is, float radius) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null, radius);
        if (mBitmap == null) {
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
        }
    }

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     */
    public RadiusBitmapDrawable(Resources res, java.io.InputStream is, float radius) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null, radius);
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
//
//            final BitmapState state = mBitmapState;
//            Shader.TileMode tmx = state.mTileModeX;
//            Shader.TileMode tmy = state.mTileModeY;
//
//            Shader s = new BitmapShader(bitmap, tmx == null ? Shader.TileMode.CLAMP : tmx, tmy == null ? Shader.TileMode.CLAMP : tmy);
//            state.mPaint.setShader(s);
//            state.mPaint.setAntiAlias(true);
//
//
//
//            copyBounds(mDstRect);
//
//            if (mApplyGravity) {
//                mDstRect.set(getBounds());
//                mApplyGravity = false;
//            }
//
//            canvas.drawRect(mDstRect, state.mPaint);
//
//
//            state.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
//
//
//            canvas.drawCircle(mDstRect.centerX(), mDstRect.centerY(), Math.min(mDstRect.centerX(), mDstRect.centerY()), state.mPaint);
            copyBounds(mDstRect);

            RectF rectF = new RectF(mDstRect);

            int sc = canvas.saveLayer(rectF, null,
                    Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                            | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                            | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            Rect srcRect = new Rect(mDstRect.left + 5, mDstRect.top + 5, mDstRect.right - 20, mDstRect.bottom - 20);

            canvas.drawRect(srcRect, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
            paint.setColor(Color.RED);


            canvas.drawCircle(mDstRect.centerX() + 10, mDstRect.centerY() + 10, Math.min(mDstRect.centerX(), mDstRect.centerY()) - 10, paint);

            canvas.restoreToCount(sc);
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
            return new RadiusBitmapDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new RadiusBitmapDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

    private RadiusBitmapDrawable(BitmapState state, Resources res) {
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


    private RadiusBitmapDrawable(BitmapState state, Resources res, float radius) {
        mBitmapState = state;
        mRadius = radius;
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
