package by.xgear.launchhelepr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.litl.leveldb.DB;

public class SettingsActivity extends Activity {
	
	private ListView appList;
	private int mAppWidgetId;
	private File mPath;
	private DB mDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		mPath = new File(getFilesDir(), Utils.DB_PATH);
		mDb = new DB(mPath);
        mDb.open();
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
				Drawable icon = getIcon(rs);
				String appName = info.loadLabel(getPackageManager()).toString();
				
				RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.widget_layout);
				if(icon != null)
					remoteViews.setImageViewBitmap(R.id.app_image, ((BitmapDrawable) icon).getBitmap());

				if(!TextUtils.isEmpty(appName))
					remoteViews.setTextViewText(R.id.app_name, appName);
				
				Intent launchIntent = getPackageManager().getLaunchIntentForPackage(info.packageName);
				PendingIntent pIntent = PendingIntent.getActivity(view.getContext(), 0, launchIntent, 0);
				remoteViews.setOnClickPendingIntent(R.id.app_image, pIntent);
				remoteViews.setOnClickPendingIntent(R.id.app_name, pIntent);
				
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
				appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

				saveWidgetData(new AppDataHolder(
						((BitmapDrawable) icon).getBitmap(), appName, info.packageName, mAppWidgetId));
				
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}
	
	private void saveWidgetData(AppDataHolder dataHolder) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		dataHolder.getIcon().compress(Bitmap.CompressFormat.PNG, 100, stream);
        mDb.put(Utils.bytes(Utils.IMG+dataHolder.getWidgetId()), stream.toByteArray());
        mDb.put(Utils.bytes(Utils.NAME+dataHolder.getWidgetId()), Utils.bytes(dataHolder.getName()));
        mDb.put(Utils.bytes(Utils.PACKAGE_NAME+dataHolder.getWidgetId()), Utils.bytes(dataHolder.getPackageName()));
	}
	
	@Override
	protected void onDestroy() {        
		mDb.close();
		super.onDestroy();
	}

	private Drawable getIcon(ResolveInfo rs) {
    	if(Build.VERSION.SDK_INT < 15) {
			return getIconForOldVersions(rs);
    	} else {
    		Drawable d = getFullResIcon(rs);
    		return d != null ? d : getIconForOldVersions(rs);
    	}
    }
    
    private Drawable getIconForOldVersions(ResolveInfo rs) {
		ApplicationInfo info = rs.activityInfo.applicationInfo;
		return info.loadIcon(getPackageManager());
    }
    
    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            int iconDpi = activityManager.getLauncherLargeIconDensity();
            d = resources.getDrawableForDensity(iconId, iconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ActivityInfo info) {
        Resources resources;
        try {
            resources = getPackageManager().getResourcesForApplication(info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    /*
        public Drawable getFullResIcon(String packageName, int iconId) {
            Resources resources;
            try {
                resources = getPackageManager().getResourcesForApplication(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                resources = null;
            }
            if (resources != null) {
                if (iconId != 0) {
                    return getFullResIcon(resources, iconId);
                }
            }
            return getFullResDefaultActivityIcon();
        }*/
}
