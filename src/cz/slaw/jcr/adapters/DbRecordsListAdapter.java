package cz.slaw.jcr.adapters;

import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.slaw.jcr.R;
import cz.slaw.jcr.domain.DbRecord;

public class DbRecordsListAdapter extends ArrayAdapter<DbRecord> {

	private final List<DbRecord> items;
	private final int layoutResourceId;
	private final LayoutInflater inflater;
	private SparseBooleanArray mSelectedItemsIds =  new SparseBooleanArray();;

	public DbRecordsListAdapter(Context context, int layoutResourceId, List<DbRecord> items) {
		super(context, layoutResourceId, items);
		this.layoutResourceId = layoutResourceId;
		this.items = items;
		this.inflater = LayoutInflater.from(context);
	}

	public static class RecordHolder {
		DbRecord record;
		LinearLayout layout;
		TextView name;
		TextView duration;
		TextView date;
		ImageView directionImg;
		ImageView playButton;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(items.size()==0)
			return convertView;
		final RecordHolder holder;
		View row = convertView;
		
		if(row==null){
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new RecordHolder();
			holder.record = items.get(position);
			
			holder.layout = (LinearLayout) row.findViewById(R.id.record_detail_show);
			holder.layout.setTag(holder.record);
			
			holder.directionImg=(ImageView)row.findViewById(R.id.list_record_direction);
			
			holder.playButton=(ImageView)row.findViewById(R.id.list_record_play);
			holder.playButton.setTag(holder.record);
			
			holder.date = (TextView)row.findViewById(R.id.list_record_date);
			holder.duration = (TextView)row.findViewById(R.id.list_record_duration);
			holder.name = (TextView)row.findViewById(R.id.list_record_name);

			setupItem(holder);
			row.setTag(holder);
			
		}else{
			holder=(RecordHolder) row.getTag();
			holder.record=items.get(position);
			
			holder.layout = (LinearLayout) row.findViewById(R.id.record_detail_show);
			
			holder.directionImg=(ImageView)row.findViewById(R.id.list_record_direction);
			
			holder.playButton=(ImageView)row.findViewById(R.id.list_record_play);
			holder.playButton.setTag(holder.record);
			
			holder.date = (TextView)row.findViewById(R.id.list_record_date);
			holder.duration = (TextView)row.findViewById(R.id.list_record_duration);
			holder.name = (TextView)row.findViewById(R.id.list_record_name);
			setupItem(holder);
		}

		return row;
	}
	
	public boolean isSelected(int position){
		return mSelectedItemsIds.get(position);
	}
	
	public void removeSelection() {
		mSelectedItemsIds= new SparseBooleanArray();
		notifyDataSetChanged();
	}
 
	public void selectView(int position, boolean value) {
		if(value){
			mSelectedItemsIds.put(position, value);
		}else{
			mSelectedItemsIds.delete(position);
		}
		notifyDataSetChanged();
	}
 
	public int getSelectedCount() {
		return mSelectedItemsIds.size();
	}
 
	public SparseBooleanArray getSelectedIds() {
		return mSelectedItemsIds;
	}
	
	private void setupItem(RecordHolder holder) {
		holder.layout.setTag(holder.record);
		if(holder.record.getIncoming()){
			holder.directionImg.setImageResource(R.drawable.ic_in_icon);
		}else{
			holder.directionImg.setImageResource(R.drawable.ic_out_icon);
		}
		holder.name.setText(holder.record.getContactName()!=null?holder.record.getContactName():(holder.record.getContactNumber()!=null?holder.record.getContactNumber():holder.record.getName()));
		if(holder.record.getStart()!=null){
			CharSequence date = DateUtils.getRelativeDateTimeString(getContext(), holder.record.getStart().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
			holder.date.setText(date);
		}
		holder.duration.setText(DateUtils.formatElapsedTime(holder.record.getDuration()));
	}

}