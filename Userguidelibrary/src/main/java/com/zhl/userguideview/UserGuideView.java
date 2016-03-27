package com.zhl.userguideview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



/**
 * ������һ�����ڡ�Ӧ�������ԡ����û�ָ��view
 * Created by zhaohl on 2015-11-26.
 */
public class UserGuideView extends View {
    public static final int VIEWSTYLE_RECT=0;
    public static final int VIEWSTYLE_CIRCLE=1;
    public static final int VIEWSTYLE_OVAL=2;
    public static final int MASKBLURSTYLE_SOLID=0;
    public static final int MASKBLURSTYLE_NORMAL=1;
    private Bitmap fgBitmap;// ǰ��
    private Bitmap jtUpLeft,jtUpRight,jtDownRight,jtDownLeft;// ָʾ��ͷ
    private Canvas mCanvas;// �����ɰ��Ļ���
    private Paint mPaint;// �����ɰ�㻭��
    private int screenW, screenH;// ��Ļ���
    private View targetView;
    private boolean touchOutsideCancel = true;
    private int borderWitdh=10;
    private int margin=40;
    private int highLightStyle = VIEWSTYLE_RECT;
    public int maskblurstyle = MASKBLURSTYLE_SOLID;
    private Bitmap tipBitmap;
    private int radius;
    private int maskColor = 0x99000000;// �ɰ����ɫ
    private OnDismissListener onDismissListener;
    private int statusBarHeight = 0;// ״̬���߶�

    public UserGuideView(Context context){
        this(context,null);
    }
    public UserGuideView(Context context, AttributeSet set) {
        this(context, set, -1);
    }

    public UserGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        if(attrs!=null){
            TypedArray array =  context.obtainStyledAttributes(attrs, R.styleable.UserGuideView);
            highLightStyle = array.getInt(R.styleable.UserGuideView_HighlightViewStyle, VIEWSTYLE_RECT);
            maskblurstyle = array.getInt(R.styleable.UserGuideView_MaskBlurStyle, MASKBLURSTYLE_SOLID);
            BitmapDrawable drawable = (BitmapDrawable) array.getDrawable(R.styleable.UserGuideView_tipView);
            maskColor = array.getColor(R.styleable.UserGuideView_maskColor,maskColor);
            if(drawable!=null){
                tipBitmap = drawable.getBitmap();
            }
            array.recycle();
        }
        // �������
        cal(context);

        // ��ʼ������
        init(context);
    }

    /**
     * �������
     *
     * @param context
     *            �����Ļ�������
     */
    private void cal(Context context) {
        // ��ȡ��Ļ�ߴ�����
        int[] screenSize = MeasureUtil.getScreenSize((Activity) context);

        // ��ȡ��Ļ���
        screenW = screenSize[0];
        screenH = screenSize[1];
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
		 if(Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT){
        	statusBarHeight=44;
		}
    }

    /**
     * ��ʼ������
     */
    private void init(Context context) {

        // �ر�Ӳ������
//        setLayerType(LAYER_TYPE_SOFTWARE,null);
        // ʵ�������ʲ������俹��ݺͿ�����
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        // ���û���͸����Ϊ0�ǹؼ���
        mPaint.setARGB(0, 255, 0, 0);
        // ���û��ģʽΪDST_IN
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        BlurMaskFilter.Blur blurStyle  = null;
        switch (maskblurstyle){
            case MASKBLURSTYLE_SOLID:
                blurStyle = BlurMaskFilter.Blur.SOLID;
                break;
            case MASKBLURSTYLE_NORMAL:
                blurStyle = BlurMaskFilter.Blur.NORMAL;
                break;
        }

        mPaint.setMaskFilter(new BlurMaskFilter(15, blurStyle));

        // ����ǰ��ͼBitmap
        fgBitmap = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);

        // ����ע�뻭��
        mCanvas = new Canvas(fgBitmap);

        // ����ǰ������
        mCanvas.drawColor(maskColor);

        // ʵ������ͷͼƬ
        jtDownRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_right);
        jtDownLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_left);
        jtUpLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_left);
        jtUpRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_right);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(targetView==null){
            return;
        }
        // ����ǰ��
        canvas.drawBitmap(fgBitmap, 0, 0, null);
//        int left = targetView.getLeft();
//        int top = targetView.getTop();
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        int vWidth = targetView.getWidth();
        int vHeight = targetView.getHeight();

//        ViewGroup decorView = null;
//        Context context = this.getContext();
//        if(context instanceof Activity){
//            decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
//        }
//        View temp = targetView;
//        while(temp!=decorView){
//            temp = (View) temp.getParent();
//            if(temp==null){
//                break;
//            }
//            left+=temp.getLeft();
//            top+=temp.getTop();
//        }
//        top -= statusBarHeight;
//        right = left+vWidth;
//        bottom = top+vHeight;


        Rect tagetRect = new Rect();
        targetView.getGlobalVisibleRect(tagetRect);
        tagetRect.offset(0, -statusBarHeight);
        left = tagetRect.left;
        top = tagetRect.top;
        right = tagetRect.right;
        bottom = tagetRect.bottom;

        switch (highLightStyle){
            case VIEWSTYLE_RECT:
                RectF rect = new RectF(left-borderWitdh,top-borderWitdh,right+borderWitdh,bottom+borderWitdh);
                mCanvas.drawRoundRect(rect, 20, 20, mPaint);
                break;
            case VIEWSTYLE_CIRCLE:
                radius = vWidth > vHeight ? vWidth / 2 -100: vHeight / 2-100;
                if(radius<50){
                    radius = 100;
                }
                mCanvas.drawCircle(left+vWidth / 2, top+vHeight / 2,radius, mPaint);
                break;
            case VIEWSTYLE_OVAL:
                RectF rectf = new RectF(left,top,right,bottom);
                mCanvas.drawOval(rectf, mPaint);
                break;

        }

        if(bottom<screenH/2||(screenH/2-top>bottom-screenH/2)){// ƫ��
            int jtTop = highLightStyle==VIEWSTYLE_CIRCLE?bottom+radius-margin:bottom+margin;
            if(right<screenW/2||(screenW/2-left>right-screenW/2)){//ƫ��
                canvas.drawBitmap(jtUpLeft,left+vWidth/2,jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2,jtTop+jtUpLeft.getHeight(),null);
                }
            }else{
                canvas.drawBitmap(jtUpRight,left+vWidth/2-100-margin,jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2-100-tipBitmap.getWidth()/2,jtTop+jtUpRight.getHeight(),null);
                }
            }
        }else{
            int jtTop = highLightStyle==VIEWSTYLE_CIRCLE?top-radius-margin:top - jtDownLeft.getHeight()-margin;
            if(right<screenW/2||(screenW/2-left>right-screenW/2)){
                canvas.drawBitmap(jtDownLeft, left+vWidth / 2, jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2,jtTop-tipBitmap.getHeight(),null);
                }
            }else{
                canvas.drawBitmap(jtDownRight, left+vWidth / 2-100-margin, jtTop,null);
                if(tipBitmap!=null){
                    canvas.drawBitmap(tipBitmap,left+vWidth/2-100-tipBitmap.getWidth()/2-margin,jtTop-tipBitmap.getHeight(),null);
                }
            }
        }


    }

    /**
     * ������Ҫ������View
     * @param targetView
     */
    public void setHighLightView(View targetView){
            this.targetView = targetView;
            invalidate();
    }
    public void setTouchOutsideDismiss(boolean cancel){
        this.touchOutsideCancel = cancel;
    }

	/**
     * ���ö���ı߿���
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth){
    	this.borderWitdh = borderWidth;
    }
    /**
     * ������ʾ��ͼƬ
     * @param bitmap
     */
    public void setTipView(Bitmap bitmap){
        this.tipBitmap = bitmap;
    }

    /**
     * �����ɰ���ɫ
     * @param maskColor
     */
    public void setMaskColor(int maskColor){
        this.maskColor = maskColor;
    }

    /**
     * ����״̬���߶� Ĭ���Ǽ�ȥ��һ��״̬���߶� �����������android:windowTranslucentStatus=true
     * ��Ҫ����״̬���߶�Ϊ0
     * @param statusBarHeight
     */
    public void setStatusBarHeight(int statusBarHeight){
        this.statusBarHeight = statusBarHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://
                if(touchOutsideCancel){
                    this.setVisibility(View.GONE);
                    if(this.onDismissListener!=null){
                        onDismissListener.onDismiss();
                    }
                    return true;
                }
                break;
        }
        return true;
    }

    public void setOnDismissListener(OnDismissListener listener){
        this.onDismissListener = listener;
    }

    public interface OnDismissListener{
        public void onDismiss();
    }
}
