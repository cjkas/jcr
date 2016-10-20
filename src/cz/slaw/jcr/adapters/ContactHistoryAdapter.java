package cz.slaw.jcr.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.slaw.jcr.R;
import cz.slaw.jcr.domain.DbRecord;

public class ContactHistoryAdapter extends ArrayAdapter<DbRecord> {

	private List<DbRecord> items;
	private int layoutResourceId;
	private Context context;

	public ContactHistoryAdapter(Context context, int layoutResourceId, List<DbRecord> items) {
		super(context, layoutResourceId, items);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(items.size()==0)
			return convertView;
		View row = convertView;
		final RecordHolder holder;
		if(row==null){
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new RecordHolder();
			holder.record = items.get(position);

			holder.layout = (LinearLayout) row.findViewById(R.id.recordh_detail_show);
			holder.directionImg=(ImageView)row.findViewById(R.id.listh_record_direction);
			holder.date = (TextView)row.findViewById(R.id.listh_record_date);
			holder.duration = (TextView)row.findViewById(R.id.listh_record_duration);
			holder.name = (TextView)row.findViewById(R.id.listh_record_name);

			setupItem(holder);
			row.setTag(holder);
			
		}else{
			holder=(RecordHolder) row.getTag();
			holder.record=items.get(position);
			
			holder.layout = (LinearLayout) row.findViewById(R.id.recordh_detail_show);
			holder.directionImg=(ImageView)row.findViewById(R.id.listh_record_direction);
			holder.date = (TextView)row.findViewById(R.id.listh_record_date);
			holder.duration = (TextView)row.findViewById(R.id.listh_record_duration);
			holder.name = (TextView)row.findViewById(R.id.listh_record_name);
			
			setupItem(holder);
		}

		return row;
	}
	
	private void setupItem(RecordHolder holder) {
		holder.layout.setTag(holder.record);
		holder.name.setText(holder.record.getContactName()!=null?holder.record.getContactName():(holder.record.getContactNumber()!=null?holder.record.getContactNumber():holder.record.getName()));
		if(holder.record.getStart()!=null){
			CharSequence date = DateUtils.getRelativeDateTimeString(getContext(), holder.record.getStart().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
			holder.date.setText(date);
		}
		holder.duration.setText(DateUtils.formatElapsedTime(holder.record.getDuration()));
		
		if(holder.record.getIncoming()){
			holder.directionImg.setImageResource(R.drawable.ic_in_icon);
		}else{
			holder.directionImg.setImageResource(R.drawable.ic_out_icon);
		}
	}

	public static class RecordHolder {
		DbRecord record;
		LinearLayout layout;
		TextView name;
		TextView duration;
		TextView date;
		ImageView directionImg;
	}
}