package com.demo.animationtest.anima;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.SoftReference;

/**
 * 由于原生帧动画容易出OOM的问题，为解决该问题，本类提供替代帧动画的一种解决方案。
 * 分步逐帧进行加载显示
 * 原始参考：https://github.com/VDshixiaoming/AnimationTest
 */
public class FramesSequenceAnimation implements Runnable {

    //循环读取帧---循环播放帧
    private Frames[] mFrames; // 帧数组
    private int mIndex; // 当前帧
    private boolean mShouldRun; // 开始/停止播放用
    private boolean mIsRunning; // 动画是否正在播放，防止重复播放
    private boolean mRestar = true; //重复播放
    private SoftReference<ImageView> mSoftReferenceImageView; // 软引用ImageView，以便及时释放掉
    private Handler mHandler;
    private OnAnimationStoppedListener mOnAnimationStoppedListener; //播放停止监听

    private Bitmap mBitmap = null;
    private BitmapFactory.Options mBitmapOptions;//Bitmap管理类，可有效减少Bitmap的OOM问题

    /**
     * 私有，不能直接创建
     */
    private FramesSequenceAnimation() {
    }

    /**
     * @param imageView   显示载体
     * @param resId       动画资源
     * @param durationsId 对应每一帧的显示时长
     * @param restar      是否重复播放
     * @return progress dialog animation
     */
    public static FramesSequenceAnimation createAnima(ImageView imageView, int resId,
                                                      int durationsId, boolean restar) {
        return new FramesSequenceAnimation(imageView, getFramess(imageView.getContext(), resId, durationsId), restar);
    }

    /**
     * @param imageView 显示载体
     * @param frames    动画帧数组
     * @param restar    是否重复播放
     */
    private FramesSequenceAnimation(ImageView imageView, Frames[] frames, boolean restar) {
        mHandler = new Handler();
        mFrames = frames;
        mIndex = -1;
        mSoftReferenceImageView = new SoftReference<>(imageView);
        mShouldRun = false;
        mIsRunning = false;
        mRestar = restar;
        //必须设置第一帧，接下来获取出来
        imageView.setImageResource(mFrames[0].drawableId);
        // 当图片大小类型相同时进行复用，避免频繁GC
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            Bitmap.Config config = bmp.getConfig();
            mBitmap = Bitmap.createBitmap(width, height, config);
            mBitmapOptions = new BitmapFactory.Options();
            //设置Bitmap内存复用
            mBitmapOptions.inBitmap = mBitmap;//Bitmap复用内存块，类似对象池，避免不必要的内存分配和回收
            mBitmapOptions.inMutable = true;//解码时返回可变Bitmap
            mBitmapOptions.inSampleSize = 1;//缩放比例
        }
    }

    //循环读取下一帧
    private Frames getNext() {
        mIndex++;
        if (mIndex < 0 || mIndex >= mFrames.length)
            mIndex = 0;
        return mFrames[mIndex];
    }

    /**
     * 如果需要循环
     */
    private boolean chexkReStart() {
        //最后一帧
        if (!mRestar && mIndex == mFrames.length - 1) {
            mShouldRun = false;
            return false;
        }
        return true;
    }

    /**
     * 停止播放
     */
    public synchronized void stop() {
        mShouldRun = false;
        mHandler.removeCallbacks(this);
    }

    /**
     * 播放动画，同步锁防止多线程读帧时，数据安全问题
     */
    public synchronized void start() {
        mShouldRun = true;
        if (mIsRunning)
            return;
        mHandler.post(this);
    }

    @Override
    public void run() {
        ImageView imageView = mSoftReferenceImageView.get();
        if (!mShouldRun || imageView == null || !chexkReStart()) {
            mIsRunning = false;
            if (mOnAnimationStoppedListener != null) {
                mOnAnimationStoppedListener.AnimationStopped();
            }
            return;
        }
        mIsRunning = true;
        Frames frames = getNext();
        //新开线程去读下一帧
        mHandler.postDelayed(this, frames.duration);
        if (imageView.isShown()) {
            if (mBitmap != null) { // so Build.VERSION.SDK_INT >= 11
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeResource(imageView.getResources(), frames.drawableId, mBitmapOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(frames.drawableId);
                    mBitmap.recycle();
                    mBitmap = null;
                }
            } else {
                imageView.setImageResource(frames.drawableId);
            }
        }
    }

    /**
     * 设置停止播放监听
     *
     * @param listener 监听器
     */
    public void setOnAnimStopListener(OnAnimationStoppedListener listener) {
        this.mOnAnimationStoppedListener = listener;
    }

    /**
     * 从xml中读取帧数组
     *
     * @param resId       动画资源
     * @param durationsId 对应每一帧的显示时长
     * @param context     上下文
     * @return 帧数组
     */
    private static Frames[] getFramess(Context context, int resId, int durationsId) {
        TypedArray drawableIds = context.getResources().obtainTypedArray(resId);
        int[] durationIds = context.getResources().getIntArray(durationsId);
        int len1 = drawableIds.length();
        int len2 = durationIds.length;
        if (len1 != len2) {
            //Log.w("FramesSequenceAnimation", "xml该动画资源中drawable个数与duration个数不一致");
        }
        int len = len1 > len2 ? len2 : len1;
        Frames[] framess = new Frames[drawableIds.length()];

        for (int i = 0; i < len; i++) {
            int drawableId = drawableIds.getResourceId(i, 0);
            if (drawableId != 0) {
                Frames frames = new Frames();
                frames.drawableId = drawableId;
                frames.duration = durationIds[i];
                framess[i] = frames;
            }
        }
        drawableIds.recycle();
        return framess;
    }

    private static class Frames {
        int drawableId;
        int duration;
    }

    /**
     * 停止播放监听
     */
    public interface OnAnimationStoppedListener {
        void AnimationStopped();
    }
}