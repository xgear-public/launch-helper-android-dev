package by.xgear.launchhelepr;

import java.io.File;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.litl.leveldb.DB;

public class LaunchWidget extends AppWidgetProvider {


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		File mPath = new File(context.getFilesDir(), Utils.DB_PATH);
		DB mDb = new DB(mPath);
        mDb.open();
        
        for(int appWidgetId : appWidgetIds) {
        	AppDataHolder holder = Utils.loadWidgetData(mDb, appWidgetId);
        	if(holder != null) {
        		
				String appName = holder.getName();
				
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_layout);
				remoteViews.setImageViewBitmap(R.id.app_image, holder.getIcon());

				if(!TextUtils.isEmpty(appName))
					remoteViews.setTextViewText(R.id.app_name, appName);
				
				Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(holder.getPackageName());
				PendingIntent pIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
				remoteViews.setOnClickPendingIntent(R.id.app_image, pIntent);
				remoteViews.setOnClickPendingIntent(R.id.app_name, pIntent);
				
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        		
        	}
        }
        
		mDb.close();
	}

	
}
