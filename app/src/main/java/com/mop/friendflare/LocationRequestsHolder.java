package com.mop.friendflare;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


/**
 * Holds a list of Games to be displayed
 * 
 * @author paulin
 *
 */
public class LocationRequestsHolder extends SingleObjectListHolder {

	
	private ArrayAdapter<LocationRequest> requests;
    private Activity activity;
	public LocationRequestsHolder(Context context, 
			int rowViewResourceId, 
			LayoutInflater mInflater, 
			Activity activity) {
		super(context, mInflater, rowViewResourceId);
		this.activity = activity;
		requests = new ArrayAdapter<LocationRequest>(context, rowViewResourceId);
	}
	
	protected Activity getActivity() {
		return this.activity;
	}
	

	public void addRequests(ArrayList<LocationRequest> tempRequests) {
		Iterator<LocationRequest> gIter = tempRequests.iterator();
		while(gIter.hasNext()) {
			this.addLocationRequests(gIter.next());
		}
	}

	public ArrayAdapter<LocationRequest> getRequests() {
		return requests;
	}

	public void setCategories(ArrayAdapter<LocationRequest> games) {
		this.requests = games;
	}

	public void addLocationRequests(LocationRequest game) {
		requests.add(game);
	}
	
	public void redraw() {
		//mInflater.inflate(this.rowViewResourceId, null).invalidate();
		this.requests.notifyDataSetChanged();
	}
		
	/**
	 * Clears out all of the GameItems from the Holder
	 */
	public void clear() {
		this.requests.clear();		
	}
	
	@Override
	public int getCount() {
		return requests.getCount();
	}

	@Override
	public Object getItem(int index) {
		return requests.getItem(index);
	}

	@Override
	public long getItemId(int position) {
		return requests.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LocationRequest request = (LocationRequest)getItem(position);
        ViewHolder holder = null;
		
		if (null == convertView) {			
			convertView = this.createRowView();
            holder = new ViewHolder();            
			
            holder.requestFromName = (TextView) convertView.findViewById(com.mop.friendflare.R.id.locationrequest_fromname);
            holder.requestMessage = (TextView) convertView.findViewById(com.mop.friendflare.R.id.locationrequest_message);
            holder.requestDate = (TextView) convertView.findViewById(com.mop.friendflare.R.id.locationrequest_date);
            holder.requestTime = (TextView) convertView.findViewById(com.mop.friendflare.R.id.locationrequest_time);
            holder.requestState = (TextView) convertView.findViewById(com.mop.friendflare.R.id.locationrequest_state);
 
            convertView.setTag(holder);

		} else {
            holder = (ViewHolder)convertView.getTag();
		}
		
				
		holder.requestFromName.setText(request.getWhoRequested());		
		holder.requestMessage.setText(request.getReqNote());		
		holder.requestDate.setText(LocationRequest.DATE_FORMAT.format(request.getLocReqDate()));		
		holder.requestTime.setText(LocationRequest.TIME_FORMAT.format(request.getLocReqDate()));		
		holder.requestState.setText(request.getLocationState().name());		
		//Sweet grey/white candy striping
		if((position % 2) == 0) {
			convertView.setBackgroundDrawable(
					activity.getResources().getDrawable(R.drawable.listitem_even_background));
		} else {
			convertView.setBackgroundDrawable(
					activity.getResources().getDrawable(R.drawable.listitem_odd_background));			
		}
		
		
        //convertView.setOnClickListener(new LocationRequestsRowListener(position, this, this.getActivity()));
		return convertView;
	}	
	
	/**
	 * Convience Holder to prevent from having to find the view everytime it redraws
	 * @author paulin
	 *
	 */
    public static class ViewHolder {
		public TextView requestMessage;
		public TextView requestFromName;
		public TextView requestTime;
		public TextView requestDate;
		public TextView requestState;
    }	
}
