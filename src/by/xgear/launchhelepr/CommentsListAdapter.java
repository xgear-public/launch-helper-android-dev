package by.xgear.launchhelepr;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsListAdapter extends BaseAdapter {
	
	List<ResolveInfo> mAppResolveInfoList;
	
	public CommentsListAdapter(List<ResolveInfo> mComments) {
		super();
		this.mAppResolveInfoList = mComments;
	}

	@Override
	public int getCount() {
		return mAppResolveInfoList.size();
	}

	@Override
	public ResolveInfo getItem(int position) {
		return mAppResolveInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Context context = parent.getContext();
		if(convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue_comments, null, false);
			holder = new ViewHolder();
			holder.mAppIcon = (ImageView) convertView.findViewById(R.id.app_icon);
			holder.mAppName = (TextView) convertView.findViewById(R.id.app_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ResolveInfo resolveInfo = mAppResolveInfoList.get(position);

		ApplicationInfo info = resolveInfo.activityInfo.applicationInfo;
		Drawable icon = info.loadIcon(context.getPackageManager());
		String appName = info.loadLabel(context.getPackageManager()).toString();
		if(icon != null)
			holder.mAppIcon.setImageDrawable(icon);
		if(!TextUtils.isEmpty(appName))
			holder.mAppName.setText(appName);
		return convertView;
	}
	
	static class ViewHolder {
		ImageView mAppIcon;
		TextView mAppName;
	}

	public void appendComments(List<ResolveInfo> commentsToAdd) {
		mAppResolveInfoList.addAll(commentsToAdd);
	}
	
	public void appendComment(ResolveInfo commentToAdd) {
		mAppResolveInfoList.add(commentToAdd);
	}
	
	public void setComments(List<ResolveInfo> comments) {
		mAppResolveInfoList.clear();
		mAppResolveInfoList.addAll(comments);
	}

	public void clear() {
		mAppResolveInfoList.clear();
	}
	
}
