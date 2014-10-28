package com.melnykov.fab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageButton;


public class FloatingActionButton extends ImageButton
{
	final private int SHADOW_DIVIDE_COEFFICIENT = 7;
	private boolean mShadow;
	private int mButtonSize;
	private Drawable mDrawable = null;


	public FloatingActionButton(Context context)
	{
		this(context, null);
	}


	public FloatingActionButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mButtonSize = getDimension(R.dimen.fab_size_normal);
		mShadow = true;
		setScaleType(ScaleType.FIT_XY);
		updateBackground();
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = mButtonSize;
		if(mShadow)
		{
			int shadowSize = mButtonSize / SHADOW_DIVIDE_COEFFICIENT;
			size += shadowSize * 2;
		}
		setMeasuredDimension(size, size);
	}


	@Override
	public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.mButtonSize = mButtonSize;
		savedState.mShadow = mShadow;
		return savedState;
	}


	@Override
	public void onRestoreInstanceState(Parcelable state)
	{
		if(state instanceof SavedState)
		{
			SavedState savedState = (SavedState) state;
			mButtonSize = savedState.mButtonSize;
			mShadow = savedState.mShadow;
			super.onRestoreInstanceState(savedState.getSuperState());
		}
		else
		{
			super.onRestoreInstanceState(state);
		}
	}


	public void setImageDrawable(Drawable drawable)
	{
		mDrawable = drawable;
		updateBackground();
	}


	@SuppressWarnings("deprecation")
	public void updateBackground()
	{
		StateListDrawable drawable = new StateListDrawable();
		drawable.addState(new int[]{android.R.attr.state_pressed}, createDrawable(mDrawable));
		drawable.addState(new int[]{}, createDrawable(mDrawable));
		if(Build.VERSION.SDK_INT>=16)
		{
			setBackground(drawable);
		}
		else
		{
			setBackgroundDrawable(drawable);
		}
	}


	private Drawable createDrawable(Drawable drawable)
	{
		if(drawable==null) drawable = new ColorDrawable(Color.TRANSPARENT);

		if(mShadow)
		{
			LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{getResources().getDrawable(R.drawable.shadow), drawable});
			int shadowSize = mButtonSize / SHADOW_DIVIDE_COEFFICIENT;
			layerDrawable.setLayerInset(1, shadowSize, shadowSize, shadowSize, shadowSize);
			return layerDrawable;
		}
		else
		{
			return drawable;
		}
	}


	private int getDimension(@DimenRes int id)
	{
		return getResources().getDimensionPixelSize(id);
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
	public void setButtonSize(int size)
	{
		if(size!=mButtonSize)
		{
			mButtonSize = size;
			updateBackground();
		}
	}


	@SuppressWarnings("unused")
	public int getButtonSize()
	{
		return mButtonSize;
	}


	public static class SavedState extends BaseSavedState
	{
		private int mButtonSize;
		private boolean mShadow;


		public SavedState(Parcelable parcel)
		{
			super(parcel);
		}


		private SavedState(Parcel in)
		{
			super(in);
			mButtonSize = in.readInt();
			mShadow = in.readByte()!=0;
		}


		@Override
		public void writeToParcel(@NonNull Parcel out, int flags)
		{
			super.writeToParcel(out, flags);
			out.writeInt(mButtonSize);
			out.writeByte((byte) (mShadow ? 1 : 0));
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
