package com.chaoxing.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.chaoxing.camera.listener.CaptureListener;
import com.chaoxing.camera.listener.ErrorListener;
import com.chaoxing.camera.listener.JCameraListener;
import com.chaoxing.camera.listener.ReturnListener;
import com.chaoxing.camera.listener.TypeListener;
import com.chaoxing.camera.state.CameraMachine;
import com.chaoxing.camera.util.FileUtil;
import com.chaoxing.camera.util.LogUtil;
import com.chaoxing.camera.util.ScreenUtils;
import com.chaoxing.camera.util.ToastUtils;
import com.chaoxing.camera.util.Utils;
import com.chaoxing.camera.view.CameraView;
import com.chaoxing.camera.view.GlanceAndSelectImageView;
import com.cjt2325.camera.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.0.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class JCameraView extends FrameLayout implements CameraInterface.CameraOpenOverCallback, SurfaceHolder
        .Callback, CameraView {
    private static final String TAG = "JCameraView";
    public static int ZOOM_CLASS = 1;

    //Camera状态机
    private CameraMachine machine;

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;
    public static final int TYPE_VIDEO = 0x002;
    public static final int TYPE_SHORT = 0x003;
    public static final int TYPE_DEFAULT = 0x004;

    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;


    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;      //只能拍照
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;     //只能录像
    public static final int BUTTON_STATE_BOTH = 0x103;              //两者都可以

    public static final int SHOW_MODE_DEFAULT = 0xFF00;
    public static final int SHOW_MODE_MORE_IMAGE = 0xFF01;


    //回调监听
    private JCameraListener jCameraLisenter;

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    private MediaPlayer mMediaPlayer;

    private int layout_width;
    private float screenProp = 0f;

    private Bitmap captureBitmap;   //捕获的图片
    private Bitmap firstFrame;      //第一帧图片
    private String videoUrl;        //视频URL


    //切换摄像头按钮的参数
    private int iconSize = 0;       //图标大小
    private int iconMargin = 0;     //右上边距
    private int iconSrc = 0;        //图标资源
    private int duration = 0;       //录制时间

    //屏幕相对于相机缩放最大值的倍数
    public static int ZOOM_GRADIENT = 1;

    private boolean firstTouch = true;
    private float firstTouchLength = 0;
    private int showMode = SHOW_MODE_DEFAULT;
    private GlanceAndSelectImageView selectImageView;
    private String saveImageTempPath;
    private String defaultFilePath = Environment.getExternalStorageDirectory() + File.separator + "tempImages" + File.separator;
    private View rlBottonRoom;


    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //get AttributeSet
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0);
        iconSize = a.getDimensionPixelSize(R.styleable.JCameraView_iconSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics()));
        iconMargin = a.getDimensionPixelSize(R.styleable.JCameraView_iconMargin, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        iconSrc = a.getResourceId(R.styleable.JCameraView_iconSrc, R.drawable.ic_sync_black_24dp);
        duration = a.getInteger(R.styleable.JCameraView_duration_max, 10 * 1000);       //没设置默认为10s
        a.recycle();
        initData();
        initView();
    }

    private void initData() {
//        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        manager.getDefaultDisplay().getMetrics(outMetrics);
        layout_width = ScreenUtils.getScreenWidth(mContext);
        machine = new CameraMachine(getContext(), this, this);
    }

    private void initView() {
        setWillNotDraw(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.camera_view, this);
        mVideoView = (VideoView) view.findViewById(R.id.video_preview);
        mPhoto = (ImageView) view.findViewById(R.id.image_photo);
        mSwitchCamera = (ImageView) view.findViewById(R.id.image_switch);
        mSwitchCamera.setImageResource(iconSrc);
        mCaptureLayout = (CaptureLayout) view.findViewById(R.id.capture_layout);
        mCaptureLayout.setDuration(duration);
        mFoucsView = (FoucsView) view.findViewById(R.id.fouce_view);
        mVideoView.getHolder().addCallback(this);
        selectImageView = ((GlanceAndSelectImageView) view.findViewById(R.id.gsSelectView));
        selectImageView.setVisibility(GONE);
        rlBottonRoom = view.findViewById(R.id.rlBottonRoom);
        //切换摄像头
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                machine.swtich(mVideoView.getHolder(), screenProp);
            }
        });
        //拍照 录像
        mCaptureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                mSwitchCamera.setVisibility(INVISIBLE);
                machine.capture();
            }

            @Override
            public void recordStart() {
                mSwitchCamera.setVisibility(INVISIBLE);
                machine.record(mVideoView.getHolder().getSurface(), screenProp);
            }

            @Override
            public void recordShort(final long time) {
              /*  mCaptureLayout.setTextWithAnimation("录制时间过短");
                mSwitchCamera.setVisibility(VISIBLE);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        machine.stopRecord(true, time);
                    }
                }, 1500 - time);*/
                machine.stopRecord(true, time);
                machine.capture();
            }

            @Override
            public void recordEnd(long time) {
                machine.stopRecord(false, time);
            }

            @Override
            public void recordZoom(float zoom) {
                LogUtil.i("recordZoom");
                machine.zoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            @Override
            public void recordError(final String str) {
                if (errorLisenter != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            errorLisenter.AudioPermissionError(str);
                        }
                    });
                }
            }

            @Override
            public void onlyTakePicturesToast() {
                if (errorLisenter != null) {
                    errorLisenter.singerOptartionToast();
                }
            }

            @Override
            public void onlyRecordToast() {
                if (errorLisenter != null) {
                    errorLisenter.singerOptartionToast();
                }
            }
        });
        //确认 取消
        mCaptureLayout.setTypeLisenter(new TypeListener() {
            @Override
            public void cancel() {
                machine.cancle(mVideoView.getHolder(), screenProp);
                selectImageView.setVisibility(GONE);
            }

            @Override
            public void confirm() {
                machine.confirm();
            }

            @Override
            public void edit() {
                if (jCameraLisenter != null) {
                    jCameraLisenter.editImage(captureBitmap);
                }
            }
        });
        //退出
        mCaptureLayout.setReturnLisenter(new ReturnListener() {
            @Override
            public void onReturn() {
                if (jCameraLisenter != null) {
                    jCameraLisenter.quit();
                }
            }
        });
        hideTagView();
    }

    private void hideTagView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3000);
                mCaptureLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mCaptureLayout.startAlphaAnimation();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = mVideoView.getMeasuredWidth();
        float heightSize = mVideoView.getMeasuredHeight();
        screenProp = heightSize / widthSize;
        Log.i(TAG, "screenSize: width:" + widthSize + "    height:" + heightSize);
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
        initZoomGradient();
        setSuitableParams();
    }

    private void setSuitableParams() {
        float previewProp = CameraInterface.getInstance().getPreviewProp();
        if (previewProp == 0 || mVideoView == null) {   //获取的size宽高比
            return;
        }
        int measuredHeight = mVideoView.getMeasuredHeight();
        int measuredWidth = mVideoView.getMeasuredWidth();
        float clacWidth = measuredHeight / previewProp;   //计算出要显示的预览界面的宽度。
        ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams((int) clacWidth, measuredHeight);
        }

        ViewGroup.LayoutParams params = mPhoto.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams((int) clacWidth, measuredHeight);
        }

        if (clacWidth > 800 && Math.abs(clacWidth - measuredWidth) > clacWidth * 0.1F) {  //计算的宽度大于 800 并且和显示正常的布局的误差超过10%
            layoutParams.width = (int) clacWidth;
            params.width = (int) clacWidth;
        }else{
            return;
        }


        final ViewGroup.LayoutParams finalLayoutParams = layoutParams;
        final ViewGroup.LayoutParams finalPhotoParams = params;
        mVideoView.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoView != null && finalLayoutParams != null) {
                    mVideoView.setLayoutParams(finalLayoutParams);
                }
                if (mPhoto != null && finalPhotoParams != null) {
                    mPhoto.setLayoutParams(finalPhotoParams);
                }
            }
        });

    }

    private void initZoomGradient() {
        double sqrt = Math.sqrt(Math.pow(ScreenUtils.getScreenHeight(mContext), 2) + Math.pow(ScreenUtils.getScreenWidth(mContext), 2));
        int maxZoom = CameraInterface.getInstance().getMaxZoom();
        ZOOM_GRADIENT = (int) (sqrt / maxZoom);
    }

    //生命周期onResume
    public void onResume() {
        LogUtil.i("JCameraView onResume");
//        resetState(TYPE_DEFAULT); //重置状态
        CameraInterface.getInstance().registerSensorManager(mContext);
        CameraInterface.getInstance().setSwitchView(mSwitchCamera);
//        machine.start(mVideoView.getHolder(), screenProp);
    }

    //生命周期onPause
    public void onPause() {
        LogUtil.i("JCameraView onPause");
//        stopVideo();
//        resetState(TYPE_PICTURE);
        CameraInterface.getInstance().isPreview(false);
        CameraInterface.getInstance().unregisterSensorManager(mContext);
    }

    //SurfaceView生命周期
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.i("JCameraView SurfaceCreated");
        new Thread() {
            @Override
            public void run() {
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.i("JCameraView SurfaceDestroyed");
        CameraInterface.getInstance().doDestroyCamera();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                if (event.getPointerCount() == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    firstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                            point_2_Y, 2));

                    if (firstTouch) {
                        firstTouchLength = result;
                        firstTouch = false;
                    }
                    if (((int) (result - firstTouchLength) / ZOOM_GRADIENT) != 0) {
                        firstTouch = true;
                        machine.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                    }
//                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                firstTouch = true;
                break;
        }
        return true;
    }

    //对焦框指示器动画
    private void setFocusViewWidthAnimation(float x, float y) {
        machine.foucs(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });
    }

    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            mVideoView.setLayoutParams(videoViewParam);
        }
    }

    /**************************************************
     * 对外提供的API                     *
     **************************************************/

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }


    public void setJCameraLisenter(JCameraListener jCameraLisenter) {
        this.jCameraLisenter = jCameraLisenter;
    }


    private ErrorListener errorLisenter;

    //启动Camera错误回调
    public void setErrorLisenter(ErrorListener errorLisenter) {
        this.errorLisenter = errorLisenter;
        CameraInterface.getInstance().setErrorLinsenter(errorLisenter);
    }

    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        changeShowStyleForState(state);
        this.mCaptureLayout.setButtonFeatures(state);
    }

    private void changeShowStyleForState(int state) {
        if (state == BUTTON_STATE_ONLY_CAPTURE) {
            mCaptureLayout.setTextWithAnimation("轻触拍照", false);
        } else if (state == BUTTON_STATE_ONLY_RECORDER) {
            mCaptureLayout.setTextWithAnimation("按住录像", false);
        } else if (state == BUTTON_STATE_BOTH) {
            mCaptureLayout.setTextWithAnimation("轻触拍照，按住录像", false);
        }
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    @Override
    public void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                //初始化VideoView
                FileUtil.deleteFile(videoUrl);
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
        }
        mSwitchCamera.setVisibility(VISIBLE);
        mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                if (jCameraLisenter != null) {
                    jCameraLisenter.recordSuccess(videoUrl, firstFrame);
                }
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                if (jCameraLisenter != null) {
                    if (showMode == SHOW_MODE_MORE_IMAGE) {
                        if (selectImageView.getAllImage().isEmpty()) {
                            jCameraLisenter.captureSuccess(captureBitmap);
                        } else {
                            jCameraLisenter.captureSuccess(selectImageView.getAllImage());
                        }
                    } else {
                        jCameraLisenter.captureSuccess(captureBitmap);
                    }
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        mCaptureLayout.resetCaptureLayout();
    }

    /**
     * 外部设置bitmap
     *
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap, boolean showEdit) {
        if (captureBitmap != null) {
            captureBitmap.recycle();
            captureBitmap = null;
        }
        captureBitmap = bitmap;
        mPhoto.setImageBitmap(bitmap);
        mPhoto.setVisibility(VISIBLE);
        mCaptureLayout.startAlphaAnimation();
        mPhoto.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCaptureLayout.startTypeBtnAnimator(true);
            }
        }, 200);
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        if (isVertical) {
            mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        captureBitmap = bitmap;
        mPhoto.setImageBitmap(bitmap);
        mPhoto.setVisibility(VISIBLE);
        mCaptureLayout.startAlphaAnimation();
        mCaptureLayout.startTypeBtnAnimator(true);
        if (showMode == SHOW_MODE_MORE_IMAGE) {
            String imageFile = saveImageTempPath + getImageFileNameByData();
            boolean b = Utils.saveBitmap(bitmap, imageFile, Bitmap.CompressFormat.JPEG, 100);
            if (b) {
                selectImageView.setVisibility(VISIBLE);
                selectImageView.addImage(Uri.fromFile(new File(imageFile)));
            } else {
                ToastUtils.showShortText(mContext, "未知错误!");
            }
        }
    }

    private String getImageFileNameByData() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
        return df.format(new Date()) + ".jpg";
    }

    public void setShowMode(int showMode) {
        setShowMode(showMode, 0);
    }

    public void setShowMode(int showMode, int maxCount) {
        setShowMode(showMode, defaultFilePath, maxCount);
    }

    public void setShowMode(int showMode, String tempImagePath, int maxCount) {
        this.showMode = showMode;
        saveImageTempPath = tempImagePath;
        selectImageView.setMaxCount(maxCount);
        if (mCaptureLayout != null) {
            mCaptureLayout.setShowMode(showMode);
        }
    }

    @Override
    public void playVideo(Bitmap firstFrame, final String url) {
        videoUrl = url;
        JCameraView.this.firstFrame = firstFrame;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void setTip(String tip) {
        mCaptureLayout.setTip(tip);
    }

    public void setDuration(int duration) {
        this.duration = duration;
        if (mCaptureLayout != null) {
            mCaptureLayout.setDuration(duration);
        }
    }

    @Override
    public void startPreviewCallback() {
        LogUtil.i("startPreviewCallback");
        handlerFoucs(mFoucsView.getWidth() / 2, mFoucsView.getHeight() / 2);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        if (y > rlBottonRoom.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > layout_width - mFoucsView.getWidth() / 2) {
            x = layout_width - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > rlBottonRoom.getTop() - mFoucsView.getWidth() / 2) {
            y = rlBottonRoom.getTop() - mFoucsView.getWidth() / 2;
        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }
}
