package com.tencent.liteav.demo.play.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tencent.liteav.demo.play.R;


public class BeiSuPop {
    private Activity mContext;
    public PopupWindow popupWindow;
    private View contentView;
    private ViewHolder viewHolder;



    public BeiSuPop(Activity mContext) {
        this.mContext = mContext;
    }


    public View getContentView() {
        return contentView;
    }

    public ViewHolder createPopupLayout() {
        // 一个自定义的布局，作为显示的内容
        contentView = LayoutInflater.from(mContext).inflate(
                R.layout.beisu_pop_window, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(null);

        // 设置好参数之后再show
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
                dismissPop();
            }
        });
        backgroundAlpha(1f);
        return initHolder(contentView);
    }

    private ViewHolder initHolder(View contentView) {
        viewHolder=new ViewHolder(contentView);
        viewHolder.tvNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemChecked(1.0f,viewHolder.tvNormal);
                dismissPop();
            }
        });
        viewHolder.tv0_75.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemChecked(0.75f, viewHolder.tv0_75);
                dismissPop();
            }
        });
        viewHolder.tv1_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemChecked(1.5f, viewHolder.tv1_5);
                dismissPop();
            }
        });
        viewHolder.tv2_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemChecked(2.0f, viewHolder.tv2_0);
                dismissPop();
            }
        });
        return viewHolder;
    }


    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemChecked(float speed, TextView tvNormal);
    }

    public void showPopWindowOnTop(View view,OnItemClickListener onItemClickListener,boolean isFull){
        setOnItemClickListener(onItemClickListener);
        //获取需要在其上方显示的控件的位置信息
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = contentView.getMeasuredWidth();
        int popupHeight = contentView.getMeasuredHeight();
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //在控件上方显示
        if(isFull){
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, (location[0]) - popupWidth / 2, location[1] - popupHeight);
        }else{
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, (location[0]), location[1] - popupHeight);
        }

    }




    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        mContext.getWindow().setAttributes(lp);
    }


    public void dismissPop() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    public class ViewHolder{
        public FrameLayout fmImgAd;
        public ImageView ivImg;
        public ImageView ivClose;
        public LinearLayout llItems;
        public TextView tvNormal;
        public TextView tv0_75;
        public TextView tv1_5;
        public TextView tv2_0;

       public ViewHolder (View view){
           fmImgAd = (FrameLayout) view.findViewById(R.id.fm_img_ad);
           ivImg = (ImageView) view.findViewById(R.id.iv_img);
           ivClose = (ImageView) view.findViewById(R.id.iv_close);
           llItems = (LinearLayout) view.findViewById(R.id.ll_items);
           tvNormal = (TextView) view.findViewById(R.id.tv_normal);
           tv0_75 = (TextView) view.findViewById(R.id.tv_0_75);
           tv1_5 = (TextView) view.findViewById(R.id.tv_1_5);
           tv2_0 = (TextView) view.findViewById(R.id.tv_2_0);


       }

    }

}
