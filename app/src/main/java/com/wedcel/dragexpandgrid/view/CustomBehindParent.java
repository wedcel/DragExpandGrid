package com.wedcel.dragexpandgrid.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.wedcel.dragexpandgrid.R;
import com.wedcel.dragexpandgrid.model.DragIconInfo;

import java.util.ArrayList;


/**
 *
 * 类: CustomBehindParent <p>
 * 描述: TODO <p>
 * 作者: wedcel wedcel@gmail.com<p>
 * 时间: 2015年8月25日 下午6:51:29 <p>
 */
public class CustomBehindParent extends RelativeLayout {

	private Context mContext;
	private CustomBehindView mCustomBehindEditView;


	public CustomBehindParent(Context context,CustomGroup customGroup) {
		super(context);
		this.mContext = context;
		mCustomBehindEditView = new CustomBehindView(context, customGroup);
		mCustomBehindEditView.setHorizontalSpacing(1);
		mCustomBehindEditView.setVerticalSpacing(1);
		mCustomBehindEditView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mCustomBehindEditView.setBackgroundColor(mContext.getResources().getColor(R.color.gap_line));
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		addView(mCustomBehindEditView, lp);
		mCustomBehindEditView.setDeletAnimView(this);


	}

	/**
	 *
	 * 方法: refreshIconInfoList <p>
	 * 描述: TODO <p>
	 * 参数: @param iconInfoList <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:46:38
	 */
	public void refreshIconInfoList(ArrayList<DragIconInfo> iconInfoList) {
		mCustomBehindEditView.refreshIconInfoList(iconInfoList);
	}

	/**
	 *
	 * 方法: notifyDataSetChange <p>
	 * 描述: TODO <p>
	 * 参数: @param iconInfoList <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:51:15
	 */
	public void notifyDataSetChange(ArrayList<DragIconInfo> iconInfoList) {
		mCustomBehindEditView.notifyDataSetChange(iconInfoList);
	}

	/**
	 *
	 * 方法: drawWindowView <p>
	 * 描述: TODO <p>
	 * 参数: @param position
	 * 参数: @param event <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:51:42
	 */
	public void drawWindowView(int position, MotionEvent event) {
		mCustomBehindEditView.drawWindowView(position,event);
	}

	/**
	 *
	 * 方法: getEditList <p>
	 * 描述: TODO <p>
	 * 参数: @return <p>
	 * 返回: ArrayList<DragIconInfo> <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:51:39
	 */
	public ArrayList<DragIconInfo> getEditList() {
		return mCustomBehindEditView.getEditList();
	}

	/**
	 *
	 * 方法: childDispatchTouchEvent <p>
	 * 描述: TODO <p>
	 * 参数: @param ev <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:51:48
	 */
	public void childDispatchTouchEvent(MotionEvent ev) {
		mCustomBehindEditView.dispatchTouchEvent(ev);
	}

	public boolean isModifyedOrder(){
		return mCustomBehindEditView.isModifyedOrder();
	}

	public void cancleModifyOrderState(){
		mCustomBehindEditView.cancleModifyedOrderState();
	}

	/**
	 *
	 * 方法: resetHidePosition <p>
	 * 描述: TODO <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:51:51
	 */
	public void resetHidePosition() {
		mCustomBehindEditView.resetHidePosition();
	}

	/**
	 *
	 * 方法: isValideEvent <p>
	 * 描述: TODO <p>
	 * 参数: @param ev
	 * 参数: @param scrolly
	 * 参数: @return <p>
	 * 返回: boolean <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:50:55
	 */
	public boolean isValideEvent(MotionEvent ev, int scrolly) {
		return mCustomBehindEditView.isValideEvent(ev,scrolly);
	}

	/**
	 *
	 * 方法: clearDragView <p>
	 * 描述: TODO <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:51:55
	 */
	public void clearDragView() {
		mCustomBehindEditView.clearDragView();
	}

}
