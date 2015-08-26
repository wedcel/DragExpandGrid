package com.wedcel.dragexpandgrid.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.wedcel.dragexpandgrid.R;
import com.wedcel.dragexpandgrid.model.DragIconInfo;
import com.wedcel.dragexpandgrid.other.DragGridAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * 类: CustomBehindView <p>
 * 描述: TODO <p>
 * 作者: wedcel wedcel@gmail.com<p>
 * 时间: 2015年8月25日 下午4:08:40 <p>
 */
public class CustomBehindView extends GridView {
	/*** DragGridView的item长按响应的时间， 默认是1000毫秒，也可以自行设置 **/
	private long dragResponseMS = 100;
	/** 是否可以拖拽，默认不可以 **/
	private boolean isDrag = false;

	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;
	/** 正在拖拽的position **/
	private int mDragPosition;
	/*** 刚开始拖拽的item对应的View **/
	private View mStartDragItemView = null;
	/** 用于拖拽的镜像，这里直接用一个ImageView **/
	private ImageView mDragImageView;
	private WindowManager mWindowManager;
	/** item镜像的布局参数 **/
	private WindowManager.LayoutParams mWindowLayoutParams;
	/** 我们拖拽的item对应的Bitmap **/
	private Bitmap mDragBitmap;
	/** 按下的点到所在item的上边缘的距离 **/
	private int mPoint2ItemTop;
	/** 按下的点到所在item的左边缘的距离 **/
	private int mPoint2ItemLeft;
	/** DragGridView距离屏幕顶部的偏移量 **/
	private int mOffset2Top;
	/** DragGridView距离屏幕左边的偏移量 **/
	private int mOffset2Left;
	/** 状态栏的高度 **/
	private int mStatusHeight;
	/** DragGridView自动向下滚动的边界值 **/
	private int mDownScrollBorder;
	/** DragGridView自动向上滚动的边界值 **/
	private int mUpScrollBorder;
	/** DragGridView自动滚动的速度 **/
	private static final int speed = 20;

	private boolean mAnimationEnd = true;

	private int mNumColumns = AUTO_FIT;
	private DragGridAdapter mDragAdapter;
	private ArrayList<DragIconInfo> mIconInfoList = new ArrayList<DragIconInfo>();
	private CustomGroup mCustomGroup;
	private Context mContext;

	public CustomBehindView(Context context, CustomGroup customGroup) {
		super(context);
		this.mContext = context;
		this.mCustomGroup = customGroup;
		this.setNumColumns(CustomGroup.COLUMNUM);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); // 获取状态栏的高度
	}

	private Handler mHandler = new Handler();

	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			isDrag = true;
			mStartDragItemView.setVisibility(View.INVISIBLE);
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		if (adapter instanceof DragGridAdapter) {
			mDragAdapter = (DragGridAdapter) adapter;
		} else {
			throw new IllegalStateException("the adapter must be implements DragGridAdapter");
		}
	}

	@Override
	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
		this.mNumColumns = numColumns;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	/**
	 * 设置响应拖拽的毫秒数，默认是1000毫秒
	 *
	 * @param dragResponseMS
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownX = (int) ev.getX();
				mDownY = (int) ev.getY();
				int tempPosit = pointToPosition(mDownX, mDownY);

				if (tempPosit == AdapterView.INVALID_POSITION) {
					return true;
				}
				if (mCustomGroup.isEditModel() && tempPosit != mDragPosition) {
					mCustomGroup.setEditModel(false, 0);
					return true;
				}
				mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
				mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
				mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
				mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
				mOffset2Top = (int) (ev.getRawY() - mDownY);
				mOffset2Left = (int) (ev.getRawX() - mDownX);


				mDownScrollBorder = getHeight() / 5;
				mUpScrollBorder = getHeight() * 4 / 5;

				mStartDragItemView.setDrawingCacheEnabled(true);
				mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
				mStartDragItemView.destroyDrawingCache();

				break;
			case MotionEvent.ACTION_MOVE:
				int moveX = (int) ev.getX();
				int moveY = (int) ev.getY();
				if (isFirstLongDrag && !hasFirstCalculate) {
					mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());

					mPoint2ItemTop = moveY - mStartDragItemView.getTop();
					mPoint2ItemLeft = moveX - mStartDragItemView.getLeft();

					mOffset2Top = (int) (ev.getRawY() - moveY);
					mOffset2Left = (int) (ev.getRawX() - moveX);
					hasFirstCalculate = true;
				}

				if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
					mHandler.removeCallbacks(mLongClickRunnable);
				}
				break;
			case MotionEvent.ACTION_UP:
				mHandler.removeCallbacks(mLongClickRunnable);
				mHandler.removeCallbacks(mScrollRunnable);
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isFirstLongDrag;
	private boolean hasFirstCalculate = false;

	public void drawWindowView(final int position, final MotionEvent event) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mDragPosition = position;
				if (mDragPosition != AdapterView.INVALID_POSITION) {
					isFirstLongDrag = true;
					mDragAdapter.setModifyPosition(mDragPosition);
					mDownX = (int) event.getX();
					mDownY = (int) event.getY();
					mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
					createFirstDragImage();
				}
			}
		}, 100);

	}

	private void createFirstDragImage() {
		removeDragImage();
		isDrag = true;
		ImageView ivDelet = (ImageView) mStartDragItemView.findViewById(R.id.delet_iv);
		LinearLayout llContainer = (LinearLayout) mStartDragItemView.findViewById(R.id.edit_ll);
		if (ivDelet != null) {
			ivDelet.setVisibility(View.VISIBLE);
		}
		if (llContainer != null) {
			llContainer.setBackgroundColor(mContext.getResources().getColor(R.color.item_bg));
		}
		mStartDragItemView.setDrawingCacheEnabled(true);
		mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
		mStartDragItemView.destroyDrawingCache();
		if (llContainer != null) {
			llContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = 1;

		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		int[] location = new int[2];
		mStartDragItemView.getLocationOnScreen(location);
		mWindowLayoutParams.x = location[0];// (x-mLastX-xtox)+dragItemView.getLeft()+8;
		mWindowLayoutParams.y = location[1] - mStatusHeight;
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		mDragImageView = new ImageView(getContext());
		mDragImageView.setBackgroundColor(mContext.getResources().getColor(R.color.item_bg));
		mDragImageView.setImageBitmap(mDragBitmap);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
		mStartDragItemView.setVisibility(View.INVISIBLE);// 隐藏该item
	}

	private boolean isTouchInItem(View dragView, int x, int y) {
		if (dragView == null) {
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}

		if (y < topOffset || y > topOffset + dragView.getHeight()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isDrag && mDragImageView != null) {
			switch (ev.getAction()) {
				case MotionEvent.ACTION_MOVE:
					//				LogUtil.d("CustomBehindView onTouchEvent", "ACTION_MOVE");
					moveX = (int) ev.getX();
					moveY = (int) ev.getY();

					// 拖动item
					onDragItem(moveX, moveY);
					break;
				case MotionEvent.ACTION_UP:
					int dropx = (int) ev.getX();
					int dropy = (int) ev.getY();
					onStopDrag(dropx, dropy);
					isDrag = false;
					isFirstLongDrag = false;
					hasFirstCalculate = false;
					break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 *
	 * 方法: cancleEditModel <p>
	 * 描述: 是否修改了<p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:19:25
	 */
	public void cancleEditModel() {
		removeDragImage();
		mCustomGroup.setEditModel(false, 0);
	}

	/**、
	 *
	 * 方法: createDragImage <p>
	 * 描述: TODO <p>
	 * 参数: @param bitmap
	 * 参数: @param downX  按下的点相对父控件的X坐标
	 * 参数: @param downY  按下的点相对父控件的Y坐标<p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:19:39
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = 1;
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		mDragImageView = new ImageView(getContext());
		mDragImageView.setBackgroundColor(mContext.getResources().getColor(R.color.item_bg));
		mDragImageView.setImageBitmap(bitmap);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}

	/**
	 *
	 * 方法: removeDragImage <p>
	 * 描述:  从界面上面移除拖动镜像 <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:19:52
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	/**
	 *
	 * 方法: onDragItem <p>
	 * 描述:  拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动 <p>
	 * 参数: @param moveX
	 * 参数: @param moveY <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:20:08
	 */
	private void onDragItem(int moveX, int moveY) {
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // 更新镜像的位置
		onSwapItem(moveX, moveY);

		// GridView自动滚动
		mHandler.post(mScrollRunnable);
	}

	/**
	 * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动 当moveY的值小于向下滚动的边界值，触发GridView自动向下滚动
	 * 否则不进行滚动
	 */
	@SuppressLint("NewApi")
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			int scrollY;
			if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
				mHandler.removeCallbacks(mScrollRunnable);
			}

			if (moveY > mUpScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else if (moveY < mDownScrollBorder) {
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}
			smoothScrollBy(scrollY, 10);
		}
	};
	private CustomBehindParent parentView;

	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 *
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		// 获取我们手指移动到的那个item的position
		final int tempPosition = pointToPosition(moveX, moveY);

		// 假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION && mAnimationEnd) {
			if (tempPosition != mIconInfoList.size() - 1) {
				mDragAdapter.reorderItems(mDragPosition, tempPosition);
				mDragAdapter.setHideItem(tempPosition);

				final ViewTreeObserver observer = getViewTreeObserver();
				observer.addOnPreDrawListener(new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						observer.removeOnPreDrawListener(this);
						animateReorder(mDragPosition, tempPosition);
						mDragPosition = tempPosition;
						return true;
					}
				});
			}
		}
	}


	private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}


	private void animateReorder(final int oldPosition, final int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition; pos < newPosition; pos++) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				System.out.println(pos);

				if ((pos + 1) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view, -view.getWidth() * (mNumColumns - 1), 0, view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view, view.getWidth(), 0, 0, 0));
				}
			}
		} else {
			for (int pos = oldPosition; pos > newPosition; pos--) {
				View view = getChildAt(pos - getFirstVisiblePosition());
				if ((pos + mNumColumns) % mNumColumns == 0) {
					resultList.add(createTranslationAnimations(view, view.getWidth() * (mNumColumns - 1), 0, -view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view, -view.getWidth(), 0, 0, 0));
				}
			}
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(300);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimationEnd = true;
			}
		});
		resultSet.start();
	}

	/**
	 *
	 * 方法: onStopDrag <p>
	 * 描述: 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除 <p>
	 * 参数: @param dropx
	 * 参数: @param dropy <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:20:35
	 */
	private void onStopDrag(int dropx, int dropy) {

		View view = getChildAt(mDragPosition - getFirstVisiblePosition());

		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		mDragAdapter.setHideItem(-1);
		removeDragImage();
	}

	/**
	 *
	 * 方法: getStatusHeight <p>
	 * 描述: 得到标题栏高度 <p>
	 * 参数: @param context
	 * 参数: @return <p>
	 * 返回: int <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:20:46
	 */
	private  int getStatusHeight(Context context) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
				statusHeight = context.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	/**
	 *
	 * 方法: refreshIconInfoList <p>
	 * 描述: TODO <p>
	 * 参数: @param iconInfoList <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午7:00:14
	 */
	public void refreshIconInfoList(ArrayList<DragIconInfo> iconInfoList) {
		mIconInfoList.clear();
		mIconInfoList.addAll(iconInfoList);
		mDragAdapter = new DragGridAdapter(mContext, mIconInfoList, this);
		this.setAdapter(mDragAdapter);
		mDragAdapter.notifyDataSetChanged();
	}

	/**
	 *
	 * 方法: getEditList <p>
	 * 描述: TODO <p>
	 * 参数: @return <p>
	 * 返回: ArrayList<DragIconInfo> <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午7:00:24
	 */
	public ArrayList<DragIconInfo> getEditList() {
		return mIconInfoList;
	}

	/**
	 *
	 * 方法: notifyDataSetChange <p>
	 * 描述: 刷新数据 <p>
	 * 参数: @param iconInfoList <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午7:00:42
	 */
	public void notifyDataSetChange(ArrayList<DragIconInfo> iconInfoList) {
		mIconInfoList.clear();
		mIconInfoList.addAll(iconInfoList);
		mDragAdapter.resetModifyPosition();
		mDragAdapter.notifyDataSetChanged();
	}

	/**
	 *
	 * 方法: deletInfo <p>
	 * 描述: 删除 <p>
	 * 参数: @param position
	 * 参数: @param iconInfo <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午6:56:47
	 */
	public void deletInfo(int position, DragIconInfo iconInfo) {
		deletAnimation(position);
		mCustomGroup.deletHomePageInfo(iconInfo);
	}

	/**
	 *
	 * 方法: deletAnimation <p>
	 * 描述: 删除动画 <p>
	 * 参数: @param position <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:21:37
	 */
	private void deletAnimation(final int position) {
		final View view = getChildAt(position);
		view.setDrawingCacheEnabled(true);
		Bitmap mDragBitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.destroyDrawingCache();
		final ImageView animView = new ImageView(mContext);
		animView.setImageBitmap(mDragBitmap);
		LayoutParams ivlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parentView.addView(animView, ivlp);
		final int aimPosit = mIconInfoList.size() - 1;

		AnimatorSet animatorSet = createTranslationAnim(position, aimPosit, view, animView);
		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animatorSet.setDuration(500);
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				view.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				animView.setVisibility(View.GONE);
				animView.clearAnimation();
				parentView.removeView(animView);
				mDragAdapter.reorderItems(position, aimPosit);
				mDragAdapter.deleteItem(aimPosit);
				//mDragAdapter.setHideItem(aimPosit);

				final ViewTreeObserver observer = getViewTreeObserver();
				observer.addOnPreDrawListener(new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						observer.removeOnPreDrawListener(this);
						animateReorder(position, aimPosit);
						return true;
					}
				});
			}
		});
		animatorSet.start();
	}


	/**
	 *
	 * 方法: createTranslationAnim <p>
	 * 描述: TODO <p>
	 * 参数: @param position
	 * 参数: @param aimPosit
	 * 参数: @param view
	 * 参数: @param animView
	 * 参数: @return <p>
	 * 返回: AnimatorSet <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:49:23
	 */
	private AnimatorSet createTranslationAnim(int position, int aimPosit, View view, ImageView animView) {
		int startx = view.getLeft();
		int starty = view.getTop();
		View aimView = getChildAt(aimPosit);
		int endx = aimView.getLeft();
		int endy = aimView.getTop();

		ObjectAnimator animX = ObjectAnimator.ofFloat(animView, "translationX", startx, endx);
		ObjectAnimator animY = ObjectAnimator.ofFloat(animView, "translationY", starty, endy);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(animView, "scaleX", 1f, 0.5f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(animView, "scaleY", 1f, 0.5f);
		ObjectAnimator alpaAnim = ObjectAnimator.ofFloat(animView, "alpha", 1f, 0.0f);

		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY, scaleX, scaleY, alpaAnim);
		return animSetXY;
	}

	public void setDeletAnimView(CustomBehindParent customBehindParent) {
		this.parentView = customBehindParent;

	}

	/**
	 *
	 * 方法: isModifyedOrder <p>
	 * 描述: 是否修改 <p>
	 * 参数: @return <p>
	 * 返回: boolean <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:35:20
	 */
	public boolean isModifyedOrder() {
		return mDragAdapter.isHasModifyedOrder();
	}

	/**
	 *
	 * 方法: cancleModifyedOrderState <p>
	 * 描述: TODO <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:35:10
	 */
	public void cancleModifyedOrderState() {
		mDragAdapter.setHasModifyedOrder(false);
	}

	/**
	 *
	 * 方法: resetHidePosition <p>
	 * 描述: TODO <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:35:05
	 */
	public void resetHidePosition() {
		mDragAdapter.setHideItem(-1);
	}

	/**
	 *
	 * 方法: isValideEvent <p>
	 * 描述: 标记是否是在这个view里面的点击事件 防止事件冲突 <p>
	 * 参数: @param ev
	 * 参数: @param scrolly
	 * 参数: @return <p>
	 * 返回: boolean <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:34:01
	 */
	public boolean isValideEvent(MotionEvent ev, int scrolly) {
		int left = ((View)(getParent().getParent())).getLeft();
		int top = ((View)(getParent().getParent())).getTop();
		int x_ = (int) ev.getX();
		int y_ = (int) ev.getY();
		int tempx = x_-left;
		int tempy = y_-top+scrolly;
		int position = pointToPosition(tempx,tempy);
		Rect rect = new Rect();
		getHitRect(rect);
		if (position == AdapterView.INVALID_POSITION) {
			return false;
		}else{
			return true;
		}
	}

	/**
	 *
	 * 方法: clearDragView <p>
	 * 描述: 清除拖动 <p>
	 * 参数:  <p>
	 * 返回: void <p>
	 * 异常  <p>
	 * 作者: wedcel wedcel@gmail.com <p>
	 * 时间: 2015年8月25日 下午4:28:13
	 */
	public void clearDragView() {
		removeDragImage();
	}
}
