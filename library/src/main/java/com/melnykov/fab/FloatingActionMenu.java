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
	private int mAnimationDuration = 250;
	private boolean mAnimating = false;
	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	// etc
	private boolean mVerticalMenu = false;


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
		if(getVisibility()==GONE && (mLayout==null || mLayout.getVisibility()==GONE)) return;

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
				onScrollChanged(mObservableScrollView.getScrollY());
			}
		});

		mObservableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				int tmp = mObservableScrollView.computeVerticalScrollRange() - mObservableScrollView.getHeight();
				if(mMaxScrollY==tmp) return;

				mMaxScrollY = tmp;

				positionButton(true, false);
			}
		});
		setVisibility(GONE);
		setVisibility(VISIBLE);
	}


	@SuppressWarnings("unused")
	public void setLayout(@NonNull RelativeLayout layout)
	{
		mLayout = layout;
		mLayout.setVisibility(GONE);
	}


	public void setBottomOffset(int offset, boolean animate, final boolean once)
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

		positionButton(true, false);
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
			int height = mObservableScrollView.getHeight() - getMarginBottom() * 2 - maxBottomPadding().intValue() + mCurrentBottomOffset - getHeight();

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
			int width = mObservableScrollView.getWidth() - getMarginRight() * 2 - getWidth();

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

		if(mLayout.getVisibility()==GONE) return;

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
				mLayout.setVisibility(GONE);
				setVisibility(VISIBLE);
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
		if(visible)
		{
			if(mLayout.getVisibility()!=VISIBLE)
			{
				setVisibility(VISIBLE);
			}
			else
			{
				setVisibility(GONE);
			}
		}
		else
		{
			setVisibility(GONE);
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
