package com.houzhi.childautomovi.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.houzhi.childautomovi.movi.ViewMovingInterface;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houzhi on 15-5-11.
 * random moving through the transformation of margin
 */
public class ChildAutoMoviLayout extends RelativeLayout {

    public static interface TagClickListener{
        void onTagClickListener( View view, int position, long id);
    }


    private TagClickListener mOnTagClickListener ;

    public TagClickListener getOnTagClickListener() {
        return mOnTagClickListener;
    }

    public void setOnTagClickListener(TagClickListener mOnTagClickListener) {
        this.mOnTagClickListener = mOnTagClickListener;
    }

    public static final int MESSAGE_MOVING = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_MOVING:
                    randomMovingAllChilds();
                    break;
            }
        }
    };
    /**
     * timer task
     */
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Message.obtain(handler, MESSAGE_MOVING).sendToTarget();
        }
    };


    /**
     * moving all child Views
     */
    private void randomMovingAllChilds() {
        if (movingInterface != null) {
            for (int i = 0; i != getChildCount(); ++i) {
                View view = getChildAt(i);
                if(validView(view))
                    movingInterface.move(view, getWidth(), getHeight());
            }
        }
    }

    private boolean validView(View view){
        return view.getVisibility() != View.GONE && view.isEnabled();
    }

    private boolean checkViewTouchInside(View view,MotionEvent ev){
        LayoutParams params = (LayoutParams)view.getLayoutParams();
        float x = ev.getX() ;
        float y = ev.getY() ;
        if( view.getBottom() >= y && view.getTop() <= y
                && view.getLeft() <= x && view.getRight() >= x ){
            return true ;
        }else {
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handle = super.dispatchTouchEvent(ev);
        if(handle){
            //if handled, then return.
            return handle ;
        }else{
            if (mOnTagClickListener != null) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    for (int i = 0; i != getChildCount(); ++i) {
                        View childView = getChildAt(i);

                        if (validView(childView)) {
                            boolean visible = checkViewTouchInside(childView, ev);
                            Log.d(VIEW_LOG_TAG, "childView " + i + " " + visible);
                            if (visible) {
//                    childView.dispatch

                                int position = i ;
                                mOnTagClickListener.onTagClickListener(childView, position, adapter.getItemId(position));
                                return true;
                            }
                        }
                    }
                }
            }
            return false ;
        }
    }


    private void randInitChilds() {
        if (movingInterface != null) {

            for (int i = 0; i != getChildCount(); ++i) {
                View view =  getChildAt(i);
                if(validView(view))
                    movingInterface.init(view, getWidth(), getHeight());
            }
        }
    }


    private ViewMovingInterface movingInterface;

    public void setMovingInterface(ViewMovingInterface movingInterface) {
        this.movingInterface = movingInterface;
    }

    public ChildAutoMoviLayout(Context context) {
        super(context);
    }

    public ChildAutoMoviLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private BaseAdapter adapter;



    public void setAdapter(BaseAdapter adapter,ViewMovingInterface movingInterface){
        this.movingInterface = movingInterface ;

        setAdapter(adapter);
    }



    DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            //TODO
            Log.i(VIEW_LOG_TAG,"onChanged");
            initChildViewByAdapter();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };


    /**
     * init childView
     */
    private void initChildViewByAdapter(){

        int adapterStart = 0 ;

        //exist
        for (int i = 0 ; i < getChildCount() ; ) {

            if (i >= adapter.getCount()) {
                removeViewAt(i);
                continue;
            }
            View curView = getChildAt(i);
            LayoutParams params = (LayoutParams) curView.getLayoutParams();
            View nView = adapter.getView(i, curView, this);

            if (nView != curView) {
                nView.setLayoutParams(params);
                removeViewAt(i);

                addView(nView, i);
//                assert getChildAt(i) == nView;
            }
            ++i;
        }
        adapterStart = getChildCount() ;
        //not exist
        for(;adapterStart < adapter.getCount(); ++ adapterStart ){
            View childView = adapter.getView(adapterStart, null, this);
            addView(childView);
            if(getWidth() > 0 )
                movingInterface.init(childView,getWidth(),getHeight());
        }
        invalidate();
    }

    /**
     * the adapter will not reuse childview.
     *
     * @param adapter
     */
    public void setAdapter(BaseAdapter adapter) {



        this.adapter = adapter;

        initChildViewByAdapter();

        adapter.registerDataSetObserver(dataSetObserver);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int counts = 0;

            @Override
            public void onGlobalLayout() {

                if (counts < 1 && getWidth() > 0) {
                    counts++;
                    Log.i("", "counts init tag initSign");
                    randInitChilds();
                }
            }
        });
    }


    private final Timer timer = new Timer();

    public void startMoving() {
        //init childView position

        timer.scheduleAtFixedRate(timerTask, 1000, 20);
    }

    public ChildAutoMoviLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
