package com.melnykov.fab.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.FloatingActionMenu;
import com.melnykov.fab.view.ObservableScrollView;


public class MainActivity extends Activity
{

	private boolean FAB_AS_BUTTON = false;


	private FloatingActionMenu mFAB = null;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.container_content);

		if(mFAB==null) mFAB = (FloatingActionMenu) findViewById(R.id.fab);

		ListView listView = new ListView(this);
		ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.planets));
		listView.setAdapter(listAdapter);

		scrollView.addView(listView);

		fabInit();

		if(mFAB!=null) mFAB.attachToView(scrollView);
	}


	private void fabInit()
	{
		final RelativeLayout menuLayout = (RelativeLayout) findViewById(R.id.fab_menu_layout);
		final RelativeLayout overlayLayout = (RelativeLayout) findViewById(R.id.fab_hide_overlay);

		if(FAB_AS_BUTTON)
		{
			mFAB.setLayout(menuLayout);
			mFAB.setImageDrawable(getResources().getDrawable(R.drawable.selector_fab_button_main));

			mFAB.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d("FAB","FAB Button clicked !!!");
				}
			});

			mFAB.setVisible(false);
		}
		else
		{
			mFAB.setLayout(menuLayout);
			mFAB.setOverlayLayout(overlayLayout);
			mFAB.setBottomThreshold(50);
			mFAB.setImageDrawable(getResources().getDrawable(R.drawable.selector_fab_button_main));

			if(!mFAB.haveActionButtons())
			{
				// Button 1
				final FloatingActionButton button = new FloatingActionButton(getBaseContext());
				button.setImageDrawable(getResources().getDrawable(R.drawable.selector_fab_button_1));
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB","FAB Button 1 clicked !!!");
					}
				});

				// Button 2
				final FloatingActionButton button2 = new FloatingActionButton(getBaseContext());
				button2.setImageDrawable(getResources().getDrawable(R.drawable.selector_fab_button_2));
				button2.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB","FAB Button 2 clicked !!!");
					}
				});

				// Button 3
				final FloatingActionButton button3 = new FloatingActionButton(getBaseContext());
				button3.setImageDrawable(getResources().getDrawable(R.drawable.selector_fab_button_3));
				button3.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB", "FAB Button 3 clicked !!!");
					}
				});

				// Button 4
				final FloatingActionButton button4 = new FloatingActionButton(getBaseContext());
				button4.setImageDrawable(getResources().getDrawable(R.drawable.selector_fab_button_4));
				button4.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB", "FAB Button 4 clicked !!!");
					}
				});

				mFAB.addActionButton(button4);
				mFAB.addActionButton(button3);
				mFAB.addActionButton(button2);
				mFAB.addActionButton(button);
			}

			mFAB.setVisible(false);
		}
	}
}