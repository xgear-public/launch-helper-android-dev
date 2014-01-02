package by.xgear.launchhelepr;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RemoteViews;

public class SettingsActivity extends Activity {
	
	private ListView appList;
	private int mAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
		    mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);
		appList = (ListView)findViewById(R.id.app_list);
		final CommentsListAdapter adapter = new CommentsListAdapter(pkgAppsList);
		appList.setAdapter(adapter);
		appList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ResolveInfo rs = adapter.getItem(position);
				ApplicationInfo info = rs.activityInfo.applicationInfo;
				Drawable icon = info.loadIcon(getPackageManager());
				String appName = info.loadLabel(getPackageManager()).toString();
				
				RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.widget_layout);
				if(icon != null)
					remoteViews.setImageViewBitmap(R.id.app_image, ((BitmapDrawable) icon).getBitmap());

				if(!TextUtils.isEmpty(appName))
					remoteViews.setTextViewText(R.id.app_name, appName);
				
				Intent launchIntent = getPackageManager().getLaunchIntentForPackage(info.packageName);
				PendingIntent pIntent = PendingIntent.getActivity(view.getContext(), 0, launchIntent, 0);
				remoteViews.setOnClickPendingIntent(R.id.app_image, pIntent);
				
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}
	
}
