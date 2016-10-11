package com.android.redenvelope;

import java.util.ArrayList;

import com.android.redenvelope.database.RedEnvelopeDBHelper;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


public class RedEnvelopeRecord extends Activity{
	private TextView mRecordAllMoney;
	private TextView mRecordAutoCount;
	private TextView mSuccessCount;
	private TextView mRecordTip;
	private TextView mAveageTime;
	private ListView mRecordList;
	private Context mContext;
	private ArrayList<Record> mArrayList;
	private View clearBtnView;
	public static final int MENU_ID_DELETE = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.record);
//		setActivityContentView(R.layout.record);
//		ensureLayout();
//		initActionBar();
//		setTitle2(getResources().getString(R.string.record_name));
		mContext = this;
		
		mRecordAllMoney = (TextView) findViewById(R.id.recordAllMoney);
//		mRecordAutoCount = (TextView) findViewById(R.id.recordAutoCount);
		mSuccessCount = (TextView) findViewById(R.id.successCount);
		mAveageTime = (TextView) findViewById(R.id.averageUsedTime);
		mRecordTip = (TextView) findViewById(R.id.record_tips);
		mRecordList = (ListView) findViewById(R.id.recordList);
		
		int count = Config.getConfig(mContext).getAutoRobCount();
//		mRecordAutoCount.setText(mContext.getResources()
//				.getString(R.string.record_rab_count, count));
		loadData();
		initFooterBarButton();
	}

	private void initFooterBarButton() {
		/*if(mFooterBarButton == null){
			mFooterBarButton = new FooterBarButton(this);
			mFooterBarButton.addItem(MENU_ID_DELETE, this.getResources().getText(R.string.clear_data), null);
			mFooterBarButton.setTextColor(this.getResources().getColor(R.color.record_clear_data_text));
			mFooterBarButton.setBackgroundColor(R.color.record_bottom_bg_color);
			mFooterBarButton.setOnFooterItemClick(new FooterBarType.OnFooterItemClick() {
				@Override
				public void onFooterItemClick(View view, int id) {
					if(id == MENU_ID_DELETE){
//						new DeleteConfirmDialog();
						clearAllData();
					}
				}});
			mFooterBarButton.updateItems();
		}*/
	/*	if (clearBtnView == null) {
			clearBtnView = getLayoutInflater().inflate(R.layout.clear_btn,null);
			clearBtnView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					clearAllData();
				}
			});
		}
		getFooterBarImpl().addView(clearBtnView);
		getFooterBarImpl().setVisibility(View.VISIBLE);*/
	}
	
	private void loadData() {
		// TODO Auto-generated method stub
		mRecordTip.setText(R.string.data_loading_msg);
		new AsyncRecordTask().execute();
	}
	
	private final class AsyncRecordTask extends AsyncTask<Void, integer, ArrayList<Record>> {

		@Override
		protected ArrayList<Record> doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			ArrayList<Record> mList = new ArrayList<Record>();
			RedEnvelopeDBHelper dbHelper = new RedEnvelopeDBHelper(mContext);
			mList = dbHelper.queryRecord();
			dbHelper.closeDB();
			return mList;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Record> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result.isEmpty()) {
				mRecordTip.setText(R.string.data_tips_msg);
			} else {
				mRecordTip.setVisibility(View.GONE);
			}
			mArrayList = result;
			RecordListAdapter adapter = new RecordListAdapter();
			adapter.setData(mContext, result);
			mRecordList.setAdapter(adapter);
			initUI(result);
		}

		private void initUI(ArrayList<Record> result) {
			// TODO Auto-generated method stub
			ArrayList<Record> arrayList = result;
			int sucCount;
			float allMoney = 0.00f;
			long aveageTime = 0; //ms
			if (null != arrayList && !arrayList.isEmpty()) {
				sucCount = arrayList.size();
				for (int i=0; i<sucCount; i++) {
					allMoney += arrayList.get(i).getMoney();
					aveageTime += arrayList.get(i).getUsedTime();
				}
				mRecordAllMoney.setText(mContext.getResources()
						.getString(R.string.record_all_money));
				mSuccessCount.setText(mContext.getResources()
						.getString(R.string.record_sucess_time, sucCount));
				if (0 != sucCount)
					mAveageTime.setText(mContext.getResources()
							.getString(R.string.record_average_time, aveageTime/sucCount/1000f));
			} else {
				mRecordAllMoney.setText("0.00");
				mSuccessCount.setText(mContext.getResources()
						.getString(R.string.record_sucess_time, 0));
				mAveageTime.setText(mContext.getResources()
						.getString(R.string.record_average_time, 0f));
			}
		}
	}

	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = this.getActionBar();
		if (null != actionBar) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
		}
		setTitle(R.string.record_name);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else if (item.getItemId() == Menu.FIRST) {
			clearAllData();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		 addOptionsMenuItems(menu);
	     return true;
	}

	private void addOptionsMenuItems(Menu menu) {
		// TODO Auto-generated method stub
//		menu.add(Menu.NONE, Menu.FIRST, 0, R.string.clear_data);
	}
	
	private void clearAllData() {
		RedEnvelopeDBHelper dbHelper = new RedEnvelopeDBHelper(mContext);
		dbHelper.deleteRecord(null, null);
		dbHelper.closeDB();
		Config.getConfig(mContext).clearAutoRobcount();
		
		reflashUI();
	}

	private void reflashUI() {
		// TODO Auto-generated method stub
		int count = Config.getConfig(mContext).getAutoRobCount();
//		mRecordAutoCount.setText(mContext.getResources()
//				.getString(R.string.record_rab_count, count));
		loadData();
	}
}
