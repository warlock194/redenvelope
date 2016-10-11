package com.android.redenvelope;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RecordListAdapter extends BaseAdapter{
	ArrayList<Record> mArrayList;
	Context mContext;
	
	public void setData(Context context, ArrayList<Record> mLists) {
		mContext = context;
		mArrayList = mLists;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mArrayList.size();
	}

	@Override
	public Object getItem(int index) {
		// TODO Auto-generated method stub
		return mArrayList.get(index);
	}

	@Override
	public long getItemId(int index) {
		// TODO Auto-generated method stub
		return index;
	}

	@Override
	public View getView(int index, View view, ViewGroup group) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (null == view) {
			view = LayoutInflater.from(group.getContext())
					.inflate(R.xml.record_list, group, false);
			viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		Record record = mArrayList.get(index);
		viewHolder.mName.setText(record.getUserName());
		viewHolder.mMoney.setText(mContext.getResources()
				.getString(R.string.record_money, record.getMoney()));
		viewHolder.mTime.setText(record.getReceiveTime());
		
		return view;
	}
	
	class ViewHolder {
		TextView mName;
		TextView mTime;
		TextView mMoney;
		
		public ViewHolder(View view) {
			mName = (TextView) view.findViewById(R.id.textName);
			mTime = (TextView) view.findViewById(R.id.textTime);
			mMoney = (TextView) view.findViewById(R.id.textMoney);
		}
	}

}
