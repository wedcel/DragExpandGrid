package com.wedcel.dragexpandgrid.other;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wedcel.dragexpandgrid.R;
import com.wedcel.dragexpandgrid.model.DragIconInfo;
import com.wedcel.dragexpandgrid.view.CustomBehindView;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * 类: DragGridAdapter <p>
 * 描述: TODO <p>
 * 作者: wedcel wedcel@gmail.com<p>
 * 时间: 2015年8月25日 下午5:02:40 <p>
 */
public class DragGridAdapter extends BaseAdapter{
	private final int INVALID_POSIT = -100;
	private int mHidePosition = INVALID_POSIT;
	private ArrayList<DragIconInfo> mIconInfoList;
	private Context mContext;
	private int modifyPosition = INVALID_POSIT;
	private CustomBehindView mCustomBehindView;
	private Handler mHandler = new Handler();
	private boolean hasModifyedOrder = false;
	public DragGridAdapter(Context context,ArrayList<DragIconInfo> list, CustomBehindView customBehindView) {
		this.mContext = context;
		this.mIconInfoList = list;
		this.mCustomBehindView = customBehindView;
	}

	public void setModifyPosition(int position){
		modifyPosition = position;
	}


	public boolean isHasModifyedOrder() {
		return hasModifyedOrder;
	}

	public void setHasModifyedOrder(boolean hasModifyedOrder) {
		this.hasModifyedOrder = hasModifyedOrder;
	}


	@Override
	public int getCount() {
		return mIconInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mIconInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHold viewHold = null;
		if (convertView == null) {
			viewHold = new ViewHold();
			convertView = View.inflate(mContext, R.layout.gridview_behind_itemview, null);
			viewHold.llContainer = (LinearLayout) convertView.findViewById(R.id.edit_ll);
			viewHold.ivIcon = (ImageView) convertView.findViewById(R.id.icon_iv);
			viewHold.tvName = (TextView) convertView.findViewById(R.id.name_tv);
			viewHold.ivDelet = (ImageButton) convertView.findViewById(R.id.delet_iv);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHold) convertView.getTag();
		}
		final DragIconInfo iconInfo = mIconInfoList.get(position);
		viewHold.ivIcon.setImageResource(iconInfo.getResIconId());
		viewHold.tvName.setText(iconInfo.getName());
		viewHold.ivDelet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				modifyPosition = INVALID_POSIT;
				mCustomBehindView.deletInfo(position,iconInfo);
			}
		});
		if(modifyPosition==position){
			viewHold.llContainer.setBackgroundColor(mContext.getResources().getColor(R.color.item_bg));
			viewHold.ivDelet.setVisibility(View.VISIBLE);
		}else{
			viewHold.llContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
			viewHold.ivDelet.setVisibility(View.GONE);
		}
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(position!=modifyPosition){
					modifyPosition = INVALID_POSIT;
					mCustomBehindView.cancleEditModel();
				}
			}
		});
		if(position == mHidePosition){
			convertView.setVisibility(View.INVISIBLE);
		}else{
			convertView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	/**
	 *
	 * 方法: reorderItems <p>
	 * 描述: TODO <p>
	 * 参数: @param oldPosition
	 * 参数: @param newPosition <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午5:02:53
	 */
	public void reorderItems(int oldPosition, int newPosition) {
		DragIconInfo temp = mIconInfoList.get(oldPosition);
		if (oldPosition < newPosition) {
			for (int i = oldPosition; i < newPosition; i++) {
				Collections.swap(mIconInfoList, i, i + 1);
			}
		} else if (oldPosition > newPosition) {
			for (int i = oldPosition; i > newPosition; i--) {
				Collections.swap(mIconInfoList, i, i - 1);
			}
		}
		mIconInfoList.set(newPosition, temp);
		modifyPosition = newPosition;
		hasModifyedOrder = true;
	}

	/**
	 *
	 * 方法: setHideItem <p>
	 * 描述:  拖动的时候会隐藏某个 <p>
	 * 参数: @param hidePosition <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午5:03:05
	 */
	public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition;
		notifyDataSetChanged();
	}

	public void deleteItem(int deletPosit){
		mIconInfoList.remove(deletPosit);
		notifyDataSetChanged();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				hasModifyedOrder = true;
				mCustomBehindView.cancleEditModel();
			}
		}, 500);
	}

	class ViewHold {
		public LinearLayout llContainer;
		public ImageView ivIcon,ivDelet;
		public TextView tvName;
	}

	/**
	 *
	 * 方法: resetModifyPosition <p>
	 * 描述: TODO <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午5:03:16
	 */
	public void resetModifyPosition() {
		modifyPosition = INVALID_POSIT;
	}
}
