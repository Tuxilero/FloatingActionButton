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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		FloatingActionMenu FAB = (FloatingActionMenu) findViewById(R.id.fab);

		fabInit(FAB);
	}


	private void fabInit(FloatingActionMenu FAB)
	{
		if(FAB == null) return;

		final RelativeLayout menuLayout = (RelativeLayout) findViewById(R.id.fab_menu_layout); // horizontal
		final RelativeLayout overlayLayout = (RelativeLayout) findViewById(R.id.fab_hide_overlay);
		final ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.container_content);

		if(FAB_AS_BUTTON)
		{
			FAB.setLayout(menuLayout);
			FAB.setImageDrawable(getResources().getDrawable(R.drawable.btn_main));

			FAB.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d("FAB","FAB Button clicked !!!");
				}
			});

			FAB.setVisible(false);

			FAB.attachToView(scrollView);
		}
		else
		{
			FAB.setLayout(menuLayout); // Layout for buttons
//			FAB.setOverlayLayout(overlayLayout); // if set, hide menu on click.
			FAB.setBottomThreshold(150);
			FAB.setImageDrawable(getResources().getDrawable(R.drawable.btn_main));

			if(!FAB.haveActionButtons())
			{
				// Button 1
				final FloatingActionButton button = new FloatingActionButton(getBaseContext());
				button.setImageDrawable(getResources().getDrawable(R.drawable.btn_1));
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB","FAB Button 1 clicked !!!");
						button.setImageDrawable(getResources().getDrawable(R.drawable.btn_1));
						button.setShadow(!button.hasShadow());
					}
				});


				// Button 2
				final FloatingActionButton button2 = new FloatingActionButton(getBaseContext());
				button2.setImageDrawable(getResources().getDrawable(R.drawable.btn_2));
				button2.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB","FAB Button 2 clicked !!!");
						button2.setImageDrawable(getResources().getDrawable(R.drawable.btn_1));
					}
				});

				// Button 3
				final FloatingActionButton button3 = new FloatingActionButton(getBaseContext());
				button3.setImageDrawable(getResources().getDrawable(R.drawable.btn_3));
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
				button4.setImageDrawable(getResources().getDrawable(R.drawable.btn_4));
				button4.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Log.d("FAB", "FAB Button 4 clicked !!!");
					}
				});

				FAB.addActionButton(button4);
				FAB.addActionButton(button3);
				FAB.addActionButton(button2);
				FAB.addActionButton(button);
			}

			FAB.setVisible(false);

			FAB.attachToView(scrollView);
			FAB.setVertical(true);
		}
	}
}