package cz.slaw.jcr.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import cz.slaw.jcr.R;
import cz.slaw.jcr.beans.PopupItem;

public class PopupListAdapter extends ArrayAdapter<PopupItem> {

	private List<PopupItem> items;
	private int layoutResourceId;
	private Context context;
	private List<Boolean> checkStates = new ArrayList<Boolean>();

	public PopupListAdapter(Context context, int layoutResourceId, List<PopupItem> items) {
		super(context, layoutResourceId, items);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.items = items;
		for (int i = 0; i < this.items.size(); i++) {
			getCheckStates().add(i,this.items.get(i).isChecked());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addAll(Collection<? extends PopupItem> collection) {
		this.items=(List<PopupItem>) collection;
		
		for (int i = 0; i < this.items.size(); i++) {
			getCheckStates().add(i,this.items.get(i).isChecked());
		}
		
		super.addAll(collection);
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(items.size()==0)
			return convertView;
		final ItemHolder holder;
		View row = convertView;
		if(row==null){
			holder = new ItemHolder();
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder.item = items.get(position);
			
			holder.checkBox = (CheckBox)row.findViewById(R.id.popup_list_item_name);
			holder.checkBox.setTag(holder.item);
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					getCheckStates().set(position,isChecked);
				}
			});
			holder.checkBox.setChecked(getCheckStates().get(position));
			setupItem(holder);
			row.setTag(holder);
			row.setEnabled(holder.item.isEnabled());
		}else{
			holder=(ItemHolder) row.getTag();
			holder.item = items.get(position);
			
			holder.checkBox.setTag(holder.item);
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					getCheckStates().set(position,isChecked);
				}
			});
			holder.checkBox.setChecked(getCheckStates().get(position));
			row.setEnabled(holder.item.isEnabled());
			setupItem(holder);
		}

		
		return row;
	}
	
	private void setupItem(ItemHolder holder) {
		if(holder.item!=null)
			holder.checkBox.setText(holder.item.getName());		
	}

	public static class ItemHolder {
		PopupItem item;
		CheckBox checkBox;
	}

	public List<Boolean> getCheckStates() {
		return checkStates;
	}

}