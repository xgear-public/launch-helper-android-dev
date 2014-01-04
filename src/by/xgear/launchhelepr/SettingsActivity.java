package by.xgear.launchhelepr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
	private static final String DB_PATH = "data";
	private DB mDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mPath = new File(getCacheDir()/*getFilesDir()*/, DB_PATH);
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
				Drawable icon = getAppIcon(rs);
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
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				saveWidgetData(new AppDataHolder(
						((BitmapDrawable) icon).getBitmap(), appName, mAppWidgetId));
				loadWidgetData(mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}
	
	private static final String IMG = "img", NAME = "name";
	
	private void saveWidgetData(AppDataHolder dataHolder) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		dataHolder.getIcon().compress(Bitmap.CompressFormat.PNG, 100, stream);
        mDb.put(bytes(IMG+dataHolder.getWidgetId()), stream.toByteArray());
        mDb.put(bytes(NAME+dataHolder.getWidgetId()), bytes(dataHolder.getName()));
	}
	
	private AppDataHolder loadWidgetData(int appWidgetId) {
		byte[] appName = mDb.get(bytes(NAME+appWidgetId));
		String appNameStr = null;
		if(appName != null && appName.length > 0 )
			appNameStr = new String(appName);
		byte[] icon = mDb.get(bytes(IMG+appWidgetId));
		Bitmap appIcon = null;
		if(icon != null && icon.length > 0 ) {
			InputStream is = new ByteArrayInputStream(icon);
			appIcon = BitmapFactory.decodeStream(is);
		}
		if(appNameStr != null && appIcon != null) {
			return new AppDataHolder(appIcon, appNameStr, appWidgetId);
		} else
			return null;
	}
	
	@Override
	protected void onDestroy() {        
		mDb.close();
		DB.destroy(mPath);
		super.onDestroy();
	}

	public static byte[] bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);   
        ret[2] = (byte) ((a >> 8) & 0xFF);   
        ret[1] = (byte) ((a >> 16) & 0xFF);   
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }
    
    public static int byteArrayToInt(byte[] b) {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
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

    private Drawable getAppIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }
	
	static class AppDataHolder{
		private Bitmap icon;
		private String name;
		private int widgetId;
		public AppDataHolder(Bitmap icon, String name, int widgetId) {
			super();
			this.icon = icon;
			this.name = name;
			this.widgetId = widgetId;
		}
		public Bitmap getIcon() {
			return icon;
		}
		public void setIcon(Bitmap icon) {
			this.icon = icon;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getWidgetId() {
			return widgetId;
		}
		public void setWidgetId(int widgetId) {
			this.widgetId = widgetId;
		}
	}
}
