package com.melnykov.fab;

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
import android.widget.ImageButton;


public class FloatingActionButton extends ImageButton
{
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_MINI = 1;

	private int mScrollY;
	private int mColorNormal;
	private int mColorPressed;
	private boolean mShadow;
	private int mType;


	@IntDef({TYPE_NORMAL, TYPE_MINI})
	public @interface TYPE
	{
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
	public FloatingActionButton(Context context)
	{
		this(context, null);
	}


	public FloatingActionButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}


	@SuppressWarnings("unused")
	public FloatingActionButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}


	private void init(Context context, AttributeSet attributeSet)
	{
		mColorNormal = getColor(android.R.color.transparent);
		mColorPressed = getColor(android.R.color.transparent);
		mType = TYPE_NORMAL;
		mShadow = true;
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


	/**
	 * A {@link android.os.Parcelable} representing the {@link com.melnykov.fab.FloatingActionButton}'s
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
}
