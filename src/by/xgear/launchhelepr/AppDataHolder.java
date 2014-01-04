package by.xgear.launchhelepr;

import android.graphics.Bitmap;

class AppDataHolder{
	private Bitmap icon;
	private String name;
	private String packageName;
	private int widgetId;
	
	public AppDataHolder(Bitmap icon, String name, String packageName, int widgetId) {
		super();
		this.icon = icon;
		this.name = name;
		this.packageName = packageName;
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
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}