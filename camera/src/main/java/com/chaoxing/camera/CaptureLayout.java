package com.chaoxing.camera;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaoxing.camera.listener.CaptureListener;
import com.chaoxing.camera.listener.ReturnListener;
import com.chaoxing.camera.listener.TypeListener;


/**
 * =====================================
 * 作    者: 陈嘉桐 445263848@qq.com
 * 版    本：1.0.4
 * 创建日期：2017/4/26
 * 描    述：集成各个控件的布局
 * =====================================
 */

public class CaptureLayout extends RelativeLayout {

    private int showMode = JCameraView.SHOW_MODE_DEFAULT;
    private CaptureListener captureLisenter;    //拍照按钮监听
    private TypeListener typeLisenter;          //拍照或录制后接结果按钮监听
    private ReturnListener returnListener;      //退出按钮监听
    private View btnEdit;
    private View rlBtnContener1;
    private View rlBtnContener;
    private ImageView btnCancle1;
    private boolean canTakePicture = true;

    public void setTypeLisenter(TypeListener typeLisenter) {
        this.typeLisenter = typeLisenter;
    }

    public void setCaptureLisenter(CaptureListener captureLisenter) {
        this.captureLisenter = captureLisenter;
    }

    public void setReturnLisenter(ReturnListener returnListener) {
        this.returnListener = returnListener;
    }

    private CaptureButton btn_capture;      //拍照按钮
    private ImageView btnConfim;            //确认按钮
    private ImageView btnCancel;            //取消按钮
    private ImageView btnBack;              //返回按钮
    private TextView txt_tip;               //提示文本

    private int layout_width;
    private int layout_height;
    private int button_size;

    private boolean isFirst = true;
    private boolean showEditBtn = true;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout_width = outMetrics.widthPixels;
        } else {
            layout_width = outMetrics.widthPixels / 2;
        }
        button_size = (int) (layout_width / 4.5f);
        layout_height = button_size + (button_size / 5) * 2 + 100;
        initView(context);
        initEvent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(layout_width, layout_height);
    }

    public void initEvent() {
        //默认Typebutton为隐藏
        btnCancel.setVisibility(INVISIBLE);
        btnConfim.setVisibility(INVISIBLE);
        btnEdit.setVisibility(INVISIBLE);
    }

    public void startTypeBtnAnimator(boolean showEditBtn) {
        //拍照录制结果后的动画
        btn_capture.setVisibility(INVISIBLE);
        btnBack.setVisibility(INVISIBLE);
        btnCancel.setVisibility(VISIBLE);
        btnConfim.setVisibility(VISIBLE);

        btnCancel.setClickable(true);
        btnConfim.setClickable(true);
        if (showEditBtn) {
            btnEdit.setVisibility(VISIBLE);
        } else {
            btnEdit.setVisibility(INVISIBLE);
        }
//        ObjectAnimator animator_cancel = ObjectAnimator.ofFloat(btnCancel, "translationX", layout_width / 4, 0);
//        ObjectAnimator animator_confirm = ObjectAnimator.ofFloat(btnConfim, "translationX", -layout_width / 4, 0);
//        ObjectAnimator animator_edit = ObjectAnimator.ofFloat(btnEdit, "alpha", 0f, 1f);
//
//        AnimatorSet set = new AnimatorSet();
//        if (showEditBtn) {
////            btnEdit.setVisibility(VISIBLE);
////            set.playTogether(animator_cancel, animator_confirm,animator_edit);
//        }else{
//            btnEdit.setVisibility(INVISIBLE);
//        }
////        set.playTogether(animator_cancel, animator_confirm);
//
//        set.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                btnCancel.setClickable(true);
//                btnConfim.setClickable(true);
//            }
//        });
//        set.setDuration(200);
//        set.start();
    }


    private void initView(Context context) {
        setWillNotDraw(false);
        View rootView = LayoutInflater.from(context).inflate(R.layout.study_jcamera_layout_capture, null);
        btn_capture = (CaptureButton) rootView.findViewById(R.id.captureBtn);
        setButtonCancelStyle(showMode);
        btn_capture.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                if (captureLisenter != null) {
                    captureLisenter.takePictures();
                }
            }

            @Override
            public void recordShort(long time) {
                if (captureLisenter != null) {
                    captureLisenter.recordShort(time);
                }
                startAlphaAnimation();
            }

            @Override
            public void recordStart() {
                if (captureLisenter != null) {
                    captureLisenter.recordStart();
                }
                startAlphaAnimation();
            }

            @Override
            public void recordEnd(long time) {
                if (captureLisenter != null) {
                    captureLisenter.recordEnd(time);
                }
                startAlphaAnimation();
                startTypeBtnAnimator(false);
            }

            @Override
            public void recordZoom(float zoom) {
                if (captureLisenter != null) {
                    captureLisenter.recordZoom(zoom);
                }
            }

            @Override
            public void recordError(String str) {
                if (captureLisenter != null) {
                    captureLisenter.recordError(str);
                }
            }

            @Override
            public void onlyTakePicturesToast() {
                if (captureLisenter != null) {
                    captureLisenter.onlyTakePicturesToast();
                }
            }

            @Override
            public void onlyRecordToast() {
                if (captureLisenter != null) {
                    captureLisenter.onlyRecordToast();
                }
            }
        });

        btnCancel = (ImageView) rootView.findViewById(R.id.btnCancle);
        if (showMode == JCameraView.SHOW_MODE_DEFAULT) {
            btnCancel.setBackgroundResource(R.drawable.study_jcamera_icon_camera_cancle_default);
        } else {
            btnCancel.setBackgroundResource(R.drawable.study_jcamera_icon_camera_cancle);
        }
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (typeLisenter != null) {
                    typeLisenter.cancel();
                }
                startAlphaAnimation();
            }
        });
        btnConfim = (ImageView) rootView.findViewById(R.id.btnSure);
        btnConfim.setBackgroundResource(R.drawable.study_jcamera_icon_camera_sure);
        btnConfim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (typeLisenter != null) {
                    typeLisenter.confirm();
                }
                startAlphaAnimation();
            }
        });

        btnEdit = rootView.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (typeLisenter != null) {
                    typeLisenter.edit();
                }
            }
        });

        btnBack = (ImageView) rootView.findViewById(R.id.btnBack);
        btnBack.setBackgroundResource(R.drawable.study_jcamera_icon_camera_back);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (captureLisenter != null) {
                    if (returnListener != null) {
                        returnListener.onReturn();
                    }
                }
            }
        });


        txt_tip = (TextView) rootView.findViewById(R.id.tvTag);
        addView(rootView);
    }

    private void setButtonCancelStyle(int showMode) {
        if (btnCancel == null) {
            return;
        }
        if (showMode == JCameraView.SHOW_MODE_DEFAULT) {
            btnCancel.setBackgroundResource(R.drawable.study_jcamera_icon_camera_cancle_default);
        } else {
            btnCancel.setBackgroundResource(R.drawable.study_jcamera_icon_camera_cancle);
        }
    }

    public void setShowMode(int mode) {
        showMode = mode;
        setButtonCancelStyle(mode);
    }

    /**************************************************
     * 对外提供的API                      *
     **************************************************/
    public void resetCaptureLayout() {
        btn_capture.resetState();
        btnCancel.setVisibility(INVISIBLE);
        btnConfim.setVisibility(INVISIBLE);
        btnEdit.setVisibility(INVISIBLE);
        btn_capture.setVisibility(VISIBLE);
        btnBack.setVisibility(VISIBLE);
    }


    public void startAlphaAnimation() {
        if (isFirst) {
            ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 1f, 0f);
            animator_txt_tip.setDuration(500);
            animator_txt_tip.start();
            isFirst = false;
        }
    }

    /**
     * @param tip
     * @param isShowNow 是否立刻下是修改信息
     */
    public void setTextWithAnimation(String tip, boolean isShowNow) {
        txt_tip.setText(tip);
        if (isShowNow) {
            ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f, 1f, 0f);
            animator_txt_tip.setDuration(2500);
            animator_txt_tip.start();
        }
    }

    public void setDuration(int duration) {
        btn_capture.setDuration(duration);
    }

    public void setButtonFeatures(int state) {
        btn_capture.setButtonFeatures(state);
    }

    public void setTip(String tip) {
        txt_tip.setText(tip);
    }

    public void showTip() {
        txt_tip.setVisibility(VISIBLE);
    }
}
