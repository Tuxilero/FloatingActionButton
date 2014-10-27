package com.melnykov.fab;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.melnykov.fab.view.ObservableScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FloatingActionMenu extends ImageButton implements ObservableScrollView.Callbacks, View.OnClickListener
{
	private static final int TRANSLATE_DURATION_MILLIS = 150;
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_MINI = 1;

	private int mScrollY;
	private int mColorNormal;
	private int mColorPressed;
	private boolean mShadow;
	private int mType;
	private ObservableScrollView mObservableScrollView;
	private boolean mObservableInit = true;
	private float mBottomOffset;
	private HashMap<String, Float> mBottomPaddingList = new HashMap<String, Float>();
	private int mMaxScrollY;
	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
	private ArrayList<FloatingActionButton> mButtonList = new ArrayList<FloatingActionButton>();
	private RelativeLayout mLayout;
	private RelativeLayout mOverlayLayout;
	private boolean mAnimating = false;
	private int bottomThreshold = 0;


	@IntDef({TYPE_NORMAL, TYPE_MINI})
	public @interface TYPE
	{
	}


	@Override
	public void onScrollChanged(int scrollY)
	{
		if(getVisibility()==GONE && (mLayout==null || mLayout.getVisibility()==GONE)) return;

		if(scrollY==mScrollY) return;

		if(scrollY>mScrollY)
		{
			// Scrolling up
			hide();
		}
		else if(scrollY<mScrollY)
		{
			// Scrolling down
			hide();
		}
		mScrollY = scrollY;

		if(mObservableInit)
		{
			mObservableInit = false;
			return;
		}
		if(mMaxScrollY - mScrollY<bottomThreshold) setBottomOffset((mMaxScrollY - mScrollY - bottomThreshold), false);
		else setBottomOffset(0, false);
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
		show();
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = getDimension(mType==TYPE_NORMAL ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
		if(mShadow)
		{
			int shadowSize = getDimension(R.dimen.fab_shadow_size);
			size += shadowSize * 2;
		}
		setMeasuredDimension(size, size);
	}


	@Override
	public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.mScrollY = mScrollY;

		return savedState;
	}


	@Override
	public void onRestoreInstanceState(Parcelable state)
	{
		if(state instanceof SavedState)
		{
			SavedState savedState = (SavedState) state;
			mScrollY = savedState.mScrollY;
			super.onRestoreInstanceState(savedState.getSuperState());
		}
		else
		{
			super.onRestoreInstanceState(state);
		}
	}


	@SuppressWarnings("unused")
	public FloatingActionMenu(Context context)
	{
		this(context, null);
	}


	public FloatingActionMenu(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}


	@SuppressWarnings("unused")
	public FloatingActionMenu(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}


	private void init(Context context, AttributeSet attributeSet)
	{
		setOnClickListener(this);
		//		mHandler = new Handler();
		mColorNormal = getColor(android.R.color.transparent);
		mColorPressed = getColor(android.R.color.transparent);
		mType = TYPE_NORMAL;
		mShadow = true;
		mBottomOffset = getTranslationY();
		if(attributeSet!=null)
		{
			initAttributes(context, attributeSet);
		}
		updateBackground();
	}


	private void initAttributes(Context context, AttributeSet attributeSet)
	{
		TypedArray attr = getTypedArray(context, attributeSet, R.styleable.FloatingActionButton);
		if(attr!=null)
		{
			try
			{
				mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, getColor(android.R.color.transparent));
				mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, getColor(android.R.color.transparent));
				mShadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_shadow, true);
				mType = attr.getInt(R.styleable.FloatingActionButton_fab_type, TYPE_NORMAL);
			}
			finally
			{
				attr.recycle();
			}
		}
	}


	private void updateBackground()
	{
		StateListDrawable drawable = new StateListDrawable();
		drawable.addState(new int[]{android.R.attr.state_pressed}, createDrawable(mColorPressed));
		drawable.addState(new int[]{}, createDrawable(mColorNormal));
		setBackgroundCompat(drawable);
	}


	private Drawable createDrawable(int color)
	{
		OvalShape ovalShape = new OvalShape();
		ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
		shapeDrawable.getPaint().setColor(color);

		if(mShadow)
		{
			LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{getResources().getDrawable(R.drawable.shadow), shapeDrawable});
			int shadowSize = getDimension(mType==TYPE_NORMAL ? R.dimen.fab_shadow_size : R.dimen.fab_mini_shadow_size);
			layerDrawable.setLayerInset(1, shadowSize, shadowSize, shadowSize, shadowSize);
			return layerDrawable;
		}
		else
		{
			return shapeDrawable;
		}
	}


	private TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr)
	{
		return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
	}


	private int getColor(@ColorRes int id)
	{
		return getResources().getColor(id);
	}


	private int getDimension(@DimenRes int id)
	{
		return getResources().getDimensionPixelSize(id);
	}


	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackgroundCompat(Drawable drawable)
	{
		if(Build.VERSION.SDK_INT>=16)
		{
			setBackground(drawable);
		}
		else
		{
			setBackgroundDrawable(drawable);
		}
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


	public void setColorNormal(int color)
	{
		if(color!=mColorNormal)
		{
			mColorNormal = color;
			updateBackground();
		}
	}


	@SuppressWarnings("unused")
	public void setColorNormalResId(@ColorRes int colorResId)
	{
		setColorNormal(getColor(colorResId));
	}


	@SuppressWarnings("unused")
	public int getColorNormal()
	{
		return mColorNormal;
	}


	public void setColorPressed(int color)
	{
		if(color!=mColorPressed)
		{
			mColorPressed = color;
			updateBackground();
		}
	}


	@SuppressWarnings("unused")
	public void setColorPressedResId(@ColorRes int colorResId)
	{
		setColorPressed(getColor(colorResId));
	}


	@SuppressWarnings("unused")
	public int getColorPressed()
	{
		return mColorPressed;
	}


	@SuppressWarnings("unused")
	public void setShadow(boolean shadow)
	{
		if(shadow!=mShadow)
		{
			mShadow = shadow;
			updateBackground();
		}
	}


	@SuppressWarnings("unused")
	public boolean hasShadow()
	{
		return mShadow;
	}


	@SuppressWarnings("unused")
	public void setType(@TYPE int type)
	{
		if(type!=mType)
		{
			mType = type;
			updateBackground();
		}
	}


	@TYPE
	@SuppressWarnings("unused")
	public int getType()
	{
		return mType;
	}


	@SuppressWarnings("unused")
	public void attachToView(@NonNull ObservableScrollView view)
	{
		mObservableInit = true;
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
				mMaxScrollY = mObservableScrollView.computeVerticalScrollRange() - mObservableScrollView.getHeight();
				if(mMaxScrollY - mScrollY<bottomThreshold) setBottomOffset((mMaxScrollY - mScrollY - bottomThreshold), true);
				else setBottomOffset(0, true);
			}
		});
		setVisibility(GONE);
		setVisibility(VISIBLE);
	}


	public void setLayout(RelativeLayout layout)
	{
		mLayout = layout;
		mLayout.setVisibility(GONE);
	}


	public void setOverlayLayout(RelativeLayout layout)
	{
		mOverlayLayout = layout;
		mOverlayLayout.setVisibility(GONE);
		mOverlayLayout.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				hide();
				mOverlayLayout.setVisibility(GONE);
				return false;
			}
		});
	}


	/**
	 * A {@link android.os.Parcelable} representing the {@link com.melnykov.fab.FloatingActionMenu}'s
	 * state.
	 */
	public static class SavedState extends BaseSavedState
	{

		private int mScrollY;


		public SavedState(Parcelable parcel)
		{
			super(parcel);
		}


		private SavedState(Parcel in)
		{
			super(in);
			mScrollY = in.readInt();
		}


		@Override
		public void writeToParcel(@NonNull Parcel out, int flags)
		{
			super.writeToParcel(out, flags);
			out.writeInt(mScrollY);
		}


		public static final Creator<SavedState> CREATOR = new Creator<SavedState>()
		{

			@Override
			public SavedState createFromParcel(Parcel in)
			{
				return new SavedState(in);
			}


			@Override
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
	}


	public void setBottomOffset(int offset, boolean animate)
	{
		float toastOffset = maxBottomPadding() - offset;
		toastOffset = (toastOffset<0) ? toastOffset : 0;

		if(animate)
			animate().setInterpolator(mInterpolator).setDuration(TRANSLATE_DURATION_MILLIS).translationY(mBottomOffset + offset + toastOffset);
		else setTranslationY(mBottomOffset + offset + toastOffset);

		if(animate)
			mLayout.animate().setInterpolator(mInterpolator).setDuration(TRANSLATE_DURATION_MILLIS).translationY(mBottomOffset + offset + toastOffset);
		else mLayout.setTranslationY(mBottomOffset + offset + toastOffset);
	}


	@SuppressWarnings("unused")
	public void addBottomPadding(int padding, String tag)
	{
		addBottomPadding((float) padding, tag);
	}


	@SuppressWarnings("unused")
	public void addBottomPadding(Float padding, String tag)
	{
		if(!mBottomPaddingList.containsKey(tag)) mBottomPaddingList.put(tag, padding);
		else if(mBottomPaddingList.get(tag)<padding) mBottomPaddingList.put(tag, padding);

		if(mMaxScrollY - mScrollY<bottomThreshold) setBottomOffset((mMaxScrollY - mScrollY - bottomThreshold), true);
		else setBottomOffset(0, true);
	}


	@SuppressWarnings("unused")
	public void removeBottomPadding(String tag)
	{
		mBottomPaddingList.remove(tag);
		if(mMaxScrollY - mScrollY<bottomThreshold) setBottomOffset((mMaxScrollY - mScrollY - bottomThreshold), true);
		else setBottomOffset(0, true);
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
	public void addActionButton(FloatingActionButton button)
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

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
		bottomThreshold = threshold;
	}


	public void show()
	{
		if(mAnimating) return;

		mAnimating = true;

		mLayout.setVisibility(VISIBLE);

		if(mOverlayLayout!=null) mOverlayLayout.setVisibility(VISIBLE);

		setVisibility(GONE);
		int width = mObservableScrollView.getWidth() - getMarginRight() * 2 - getWidth();

		int position;
		if(mButtonList.size()==0) position = 0;
		else position = width / (mButtonList.size() - 1);

		AnimatorSet menuRollup = new AnimatorSet();
		for(int i = 0; i<mButtonList.size(); i++)
		{
			ObjectAnimator anim = ObjectAnimator.ofFloat(mButtonList.get(i), "translationX", -position * i);
			anim.setDuration(TRANSLATE_DURATION_MILLIS);
			menuRollup.play(anim);
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

				if(mMaxScrollY - mScrollY<bottomThreshold) setBottomOffset((mMaxScrollY - mScrollY - bottomThreshold), true);
				else setBottomOffset(0, true);
			}


			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});
		menuRollup.start();
	}


	public void hide()
	{
		if(mAnimating) return;

		mAnimating = true;

		AnimatorSet bouncer = new AnimatorSet();
		for(FloatingActionButton button : mButtonList)
		{
			ObjectAnimator anim = ObjectAnimator.ofFloat(button, "translationX", 0);
			anim.setDuration(TRANSLATE_DURATION_MILLIS);
			bouncer.play(anim);
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
			}


			@Override
			public void onAnimationCancel(Animator animation)
			{
			}
		});

		bouncer.start();
	}


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
}
