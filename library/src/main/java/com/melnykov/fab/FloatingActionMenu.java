package com.melnykov.fab;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.melnykov.fab.view.ObservableScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * 	Important:
 * 	For making this all work, you need to set layout correctly. Basically you need something like this:
 *
 * 	<RelativeLayout>
 *	    <com.melnykov.fab.view.ObservableScrollView>
 * 	        ...
 * 	    </com.melnykov.fab.view.ObservableScrollView>
 *
 *		<!-- Menu Layout. Make sure you have it match_parent, and in correct layout -->
 *		<!-- so it cant cover whole screen. -->
 *		<RelativeLayout
 * 			android:id="@+id/fab_menu_layout"
 *			android:layout_width="match_parent"
 *			android:layout_height="match_parent"
 *			android:layout_margin="16dp"
 *			android:visibility="gone"/>
 *
 *		<!-- FAB -->
 *		<com.melnykov.fab.FloatingActionMenu
 *			android:id="@+id/fab"
 *			android:layout_width="wrap_content"
 *			android:layout_height="wrap_content"
 *			android:layout_alignParentBottom="true"
 *			android:layout_alignParentRight="true"
 *			android:layout_alignParentEnd="true"
 *			android:layout_margin="16dp"/>
 *	</RelativeLayout>
 *
 *
 *	1)	If you want FAB act as button rewrite setOnClickListener.
 *		If you leave it as it is it will call openMenu() in onClick()
 *
 *	2)	After you create your FAB, you have to attach it to observable scrollView.
 *		You have to use method attachToView(ObservableScrollView) to do this.
 *
 *	3)	Menu buttons are displayed in RelativeLayout. So you have to create one.
 *		After that you can attach the layout with attachToView();
 *
 *	4)	There are two interesting methods:
 *		a) 	setBottomThreshold(pixelSize): This set the bottom threshold on which
 *			button stop scrolling and start to move up. This is for projects
 *			where you have some bottom panel, and you don't want FAB to cover it up.
 *
 *		b) 	addBottomPadding(pixelSize, stringTag): With this, you can set
 *			bottom padding of the button. You set it with a tag, so you can easily remove it later.
 *			FAB use max bottom padding from mBottomPaddingList. Usable when you need to set more
 *			bottom padding's. For example: you click on something, and bottom panel shows up.
 *			You add some bottom padding so FAB stay above it. After that you need to display some SnackBar
 *			which is taller than the panel, so you just add padding for SnackBar height, with another tag.
 *			After SnackBar disappear, you easily remove bottom padding with	removeBottomPadding(stringTag),
 *			and FAB return just above the panel that left in mBottomPaddingList.
 *
 *		c)	removeBottomPadding(stringTag): Mentioned above.
 *
 *		NOTE: I don't recommend use BottomThreshold or BottomPadding with vertical menu
 *		as it is not implemented yet, and will occur in graphical glitches.
 *
 *	5)	When you want to add some button to menu, just create FloatingActionButton, and add it to menu
 *		using addActionButton(FloatingActionButton).
 *
 *	6)	If you need access one of the menu button for example when you need to change button icon,
 *		You can get all the buttons with getActionButtonList().
 *
 *	7)	You can manually open or close menu with openMenu() and closeMenu().
 *
 *	8)	You can set your own animation duration with setAnimationDuration(int)
 *		and get current duration with getAnimationDuration().
 *
 *	9)	In special cases you can need not horizontal, but vertical menu. To achieve this
 *		just set setVertical(boolean). There is also isVertical().
 *
 *	10)	For menu closing there are two variants:
 *		a)	In case you closing menu on scroll you may need setScrollThresholdForMenuClose(int) which
 *			set the pixelSize you need to scroll for menu to close. you can get current ScrollThreshold with
 *			getScrollThresholdForMenuClose().
 *
 *		b)	Sometime you will need to close menu just on touch.
 *			You can do it just by setting closeMenuOnTouch(boolean).
 */


public class FloatingActionMenu extends FloatingActionButton implements ObservableScrollView.Callbacks, View.OnClickListener
{
	// scroll
	private int mMaxScrollY;
	private int mCurrentScrollY;
	private int mLastObservedScrollY;
	private int mScrollThresholdForMenuClose = 0;

	// bottom threshold and offset
	private int mBottomThreshold = 0;
	private float mBottomOffset;
	private int mCurrentBottomOffset = 0;

	// bottom padding list
	private HashMap<String, Float> mBottomPaddingList = new HashMap<String, Float>();

	// observable scrollView + layouts
	private ObservableScrollView mObservableScrollView;
	private RelativeLayout mLayout;

	// list of menu buttons
	private ArrayList<FloatingActionButton> mButtonList = new ArrayList<FloatingActionButton>();

	// animation
	private int mAnimationDuration = 100;
	private boolean mAnimating = false;
	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	// etc
	private boolean mVerticalMenu = false;
	private boolean mVisible = true;


	@SuppressWarnings("unused")
	public FloatingActionMenu(Context context)
	{
		this(context, null);
	}


	public FloatingActionMenu(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setOnClickListener(this);
		mBottomOffset = getTranslationY();
		updateBackground();
	}


	@Override
	public void onScrollChanged(int scrollY)
	{
		if(getVisibility()==GONE && (mLayout==null || mLayout.getVisibility()==GONE || mLayout.getVisibility()==INVISIBLE)) return;

		if(scrollY==mCurrentScrollY) return;

		if(!(scrollY>=(mLastObservedScrollY - mScrollThresholdForMenuClose) && (scrollY<=mLastObservedScrollY + mScrollThresholdForMenuClose)))
		{
			if(scrollY>mLastObservedScrollY)
			{
				// Scrolling up
				closeMenu();
			}
			else if(scrollY<mLastObservedScrollY)
			{
				// Scrolling down
				closeMenu();
			}
			mLastObservedScrollY = scrollY;
		}
		mCurrentScrollY = scrollY;

		if(!mAnimating) positionButton(false, false);
	}


	private void positionButton(boolean animating, boolean once)
	{
		if(mAnimating) return;
		if(mMaxScrollY - mCurrentScrollY<mBottomThreshold)
			setBottomOffset((mMaxScrollY - mCurrentScrollY - mBottomThreshold), animating, once);
		else setBottomOffset(0, animating, once);
	}


	@Override
	public void onDownMotionEvent()
	{
	}


	@Override
	public void onUpOrCancelMotionEvent()
	{
	}


	@Override
	public void onClick(View v)
	{
		openMenu();
	}


	@SuppressWarnings("unused")
	private int getMarginBottom()
	{
		int marginBottom = 0;
		final ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if(layoutParams instanceof ViewGroup.MarginLayoutParams)
		{
			marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
		}
		return marginBottom;
	}


	@SuppressWarnings("unused")
	private int getMarginRight()
	{
		int marginRight = 0;
		final ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if(layoutParams instanceof ViewGroup.MarginLayoutParams)
		{
			marginRight = ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin;
		}
		return marginRight;
	}


	@SuppressWarnings("unused")
	public void attachToView(@NonNull ObservableScrollView view)
	{
		mObservableScrollView = view;
		mObservableScrollView.setCallbacks(this);

		mObservableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				int tmp = mObservableScrollView.computeVerticalScrollRange() - mObservableScrollView.getHeight();
				if(mMaxScrollY==tmp) return;

				mCurrentScrollY = mObservableScrollView.getScrollY();
				mMaxScrollY = tmp;

				positionButton(true, false);

				onScrollChanged(mObservableScrollView.getScrollY());
			}
		});
		setVisibility(GONE);
		if(mLayout.getVisibility()!=VISIBLE) setVisibility(VISIBLE);
	}


	@SuppressWarnings("unused")
	public void setLayout(@NonNull RelativeLayout layout)
	{
		mLayout = layout;
		mLayout.setVisibility(INVISIBLE);
	}


	private void setBottomOffset(int offset, boolean animate, final boolean once)
	{
		mCurrentBottomOffset = offset;

		float toastOffset = maxBottomPadding() - offset;
		toastOffset = (toastOffset<0) ? toastOffset : 0;

		if(animate)
		{
			mAnimating = true;
			ViewPropertyAnimator anim = animate().setInterpolator(mInterpolator).setDuration(mAnimationDuration).translationY(mBottomOffset + offset + toastOffset);
			anim.setListener(new Animator.AnimatorListener()
			{

				@Override
				public void onAnimationStart(Animator animation)
				{
				}


				@Override
				public void onAnimationRepeat(Animator animation)
				{
				}


				@Override
				public void onAnimationEnd(Animator animation)
				{
					mAnimating = false;
					if(!once) positionButton(true, true);
				}


				@Override
				public void onAnimationCancel(Animator animation)
				{
				}
			});

		}
		else setTranslationY(mBottomOffset + offset + toastOffset);

		if(animate)
		{
			mAnimating = true;
			ViewPropertyAnimator anim = mLayout.animate().setInterpolator(mInterpolator).setDuration(mAnimationDuration).translationY(mBottomOffset + offset + toastOffset);

			anim.setListener(new Animator.AnimatorListener()
			{

				@Override
				public void onAnimationStart(Animator animation)
				{
				}


				@Override
				public void onAnimationRepeat(Animator animation)
				{
				}


				@Override
				public void onAnimationEnd(Animator animation)
				{
					mAnimating = false;
				}


				@Override
				public void onAnimationCancel(Animator animation)
				{
				}
			});
		}
		else mLayout.setTranslationY(mBottomOffset + offset + toastOffset);
	}


	@SuppressWarnings("unused")
	public void addBottomPadding(int padding, @NonNull String tag)
	{
		addBottomPadding((float) padding, tag);
	}


	public void addBottomPadding(Float padding, @NonNull String tag)
	{
		if(!mBottomPaddingList.containsKey(tag)) mBottomPaddingList.put(tag, padding);
		else if(mBottomPaddingList.get(tag)<padding) mBottomPaddingList.put(tag, padding);

		positionButton(true, false);
	}


	@SuppressWarnings("unused")
	public void removeBottomPadding(@NonNull String tag)
	{
		mBottomPaddingList.remove(tag);

		postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				positionButton(true, false);
			}
		}, 10);
	}


	private Float maxBottomPadding()
	{
		Map.Entry<String, Float> maxEntry = null;

		for(Map.Entry<String, Float> entry : mBottomPaddingList.entrySet())
		{
			if(maxEntry==null || entry.getValue()>maxEntry.getValue())
			{
				maxEntry = entry;
			}
		}
		if(maxEntry!=null) return maxEntry.getValue() * -1;
		return 0f;
	}


	@SuppressWarnings("unused")
	public void addActionButton(@NonNull FloatingActionButton button)
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		button.setLayoutParams(params);
		mButtonList.add(button);
		mLayout.addView(button);
	}


	@SuppressWarnings("unused")
	public ArrayList<FloatingActionButton> getActionButtonList()
	{
		return mButtonList;
	}


	@SuppressWarnings("unused")
	public boolean haveActionButtons()
	{
		return mButtonList.size()>0;
	}


	@SuppressWarnings("unused")
	public void setBottomThreshold(int threshold)
	{
		mBottomThreshold = threshold;
	}


	public void openMenu()
	{
		if(mAnimating) return;

		mAnimating = true;

		mLayout.setVisibility(VISIBLE);
		setVisibility(GONE);

		AnimatorSet menuRollup = new AnimatorSet();

		if(mVerticalMenu)
		{
			//			int height = mObservableScrollView.getHeight() - getMarginBottom() * 2 - maxBottomPadding().intValue() + mCurrentBottomOffset - getHeight();
			int height = mLayout.getHeight() - maxBottomPadding().intValue() + mCurrentBottomOffset - getHeight();

			int position;
			if(mButtonList.size()==0) position = 0;
			else position = height / (mButtonList.size() - 1);

			for(int i = 0; i<mButtonList.size(); i++)
			{
				ObjectAnimator anim = ObjectAnimator.ofFloat(mButtonList.get(i), "translationY", -position * i);
				anim.setDuration(mAnimationDuration);
				menuRollup.play(anim);
			}
		}
		else
		{
			//			int width = mObservableScrollView.getWidth() - getMarginRight() * 2 - getWidth();
			int width = mLayout.getWidth() - getWidth();

			int position;
			if(mButtonList.size()==0) position = 0;
			else position = width / (mButtonList.size() - 1);

			for(int i = 0; i<mButtonList.size(); i++)
			{
				ObjectAnimator anim = ObjectAnimator.ofFloat(mButtonList.get(i), "translationX", -position * i);
				anim.setDuration(mAnimationDuration);
				menuRollup.play(anim);
			}
		}


		menuRollup.addListener(new Animator.AnimatorListener()
		{

			@Override
			public void onAnimationStart(Animator animation)
			{
			}


			@Override
			public void onAnimationRepeat(Animator animation)
			{
			}


			@Override
			public void onAnimationEnd(Animator animation)
			{
				mAnimating = false;
				positionButton(true, false);
			}


			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});
		menuRollup.start();
	}


	public void closeMenu()
	{
		if(mAnimating) return;

		if(mLayout.getVisibility()==GONE || mLayout.getVisibility()==INVISIBLE) return;

		mAnimating = true;

		AnimatorSet bouncer = new AnimatorSet();
		for(FloatingActionButton button : mButtonList)
		{
			if(mVerticalMenu)
			{
				ObjectAnimator anim = ObjectAnimator.ofFloat(button, "translationY", 0);
				anim.setDuration(mAnimationDuration);
				bouncer.play(anim);
			}
			else
			{
				ObjectAnimator anim = ObjectAnimator.ofFloat(button, "translationX", 0);
				anim.setDuration(mAnimationDuration);
				bouncer.play(anim);
			}
		}

		bouncer.addListener(new Animator.AnimatorListener()
		{

			@Override
			public void onAnimationStart(Animator animation)
			{
			}


			@Override
			public void onAnimationRepeat(Animator animation)
			{
			}


			@Override
			public void onAnimationEnd(Animator animation)
			{
				mLayout.setVisibility(INVISIBLE);
				if(mVisible) setVisibility(VISIBLE);
				mAnimating = false;
				positionButton(true, false);
			}


			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});

		bouncer.start();
	}


	@SuppressWarnings("unused")
	public void setVisible(boolean visible)
	{
		mVisible = visible;
		if(visible)
		{
			if(mLayout.getVisibility()!=VISIBLE) setVisibility(VISIBLE);
		}
		else
		{
			setVisibility(GONE);
			mLayout.setVisibility(INVISIBLE);
		}
	}


	@SuppressWarnings("unused")
	public int getAnimationDuration()
	{
		return mAnimationDuration;
	}


	@SuppressWarnings("unused")
	public void setAnimationDuration(int dur)
	{
		mAnimationDuration = dur;
	}


	@SuppressWarnings("unused")
	public boolean isVertical()
	{
		return mVerticalMenu;
	}


	@SuppressWarnings("unused")
	public void setVertical(boolean vertical)
	{
		mVerticalMenu = vertical;
	}


	@SuppressWarnings("unused")
	public int getScrollThresholdForMenuClose()
	{
		return mScrollThresholdForMenuClose;
	}


	@SuppressWarnings("unused")
	public void setScrollThresholdForMenuClose(int val)
	{
		mScrollThresholdForMenuClose = val;
	}


	@SuppressWarnings("unused")
	public void closeMenuOnTouch(boolean close)
	{
		if(close)
		{
			mLayout.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					closeMenu();
					return false;
				}
			});
		}
		else
		{
			mLayout.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					return false;
				}
			});
		}
	}
}
