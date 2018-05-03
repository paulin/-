package com.mop.friendflare;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

/**
 * Special Holder to be used with a list.  This holder should only ever have 1 type of object in it
 * 
 * @author paulin
 *
 */
public abstract class SingleObjectListHolder extends BaseAdapter {

	public static final String TAG = "SingleObjectListHolder";
	
	private ArrayAdapter<Object> listItems;
    private LayoutInflater inflater;	
    private Context context;
    private int rowLayout;
    private boolean reuseViews = true;
    
	public SingleObjectListHolder( 
			Context context, 
			LayoutInflater mInflater, 
			int rowLayout) {
		super();
		this.rowLayout = rowLayout;
		this.context = context;
		this.inflater = mInflater;
		listItems = new ArrayAdapter<Object>(context, rowLayout);		
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	
	public boolean isReuseViews() {
		return reuseViews;
	}

	public void setReuseViews(boolean reuseViews) {
		this.reuseViews = reuseViews;
	}

	/**
	 * Utility method for creating the view
	 * @return
	 */
	public View createRowView() {
		return getInflater().inflate(getRowLayout(), null);		
	}
	
	public ArrayAdapter<Object> getListItems() {
		return listItems;
	}

	public LayoutInflater getInflater() {
		return inflater;
	}

	public Context getContext() {
		return context;
	}

	public int getRowLayout() {
		return rowLayout;
	}

	public void addListItems(List tempListItems) {
		if(tempListItems != null) {
			Iterator gIter = tempListItems.iterator();
			while(gIter.hasNext()) {
				this.addListItem(gIter.next());
			}			
		} else {
			Log.w(TAG, "Null ListItem collection attepted to be added to the SingleObjectListHolder");
		}
	}
	
	public void addListItem(Object listItem) {
		listItems.add(listItem);
	}
		
	public void removeListItem(Object listItem) {
		listItems.remove(listItem);
	}
	
	
	public void redraw() {
		this.notifyDataSetChanged();
	}
		
	/**
	 * Clears out all of the Feeds from the Holder
	 */
	public void clear() {
		this.listItems.clear();
	}
	
	@Override
	public int getCount() {
		return listItems.getCount();
	}

	@Override
	public Object getItem(int index) {
		return listItems.getItem(index);
	}

	@Override
	public long getItemId(int position) {
		return listItems.getItemId(position);
	}

	@Override
	public int getItemViewType(int position) {
		if(reuseViews) {
			return 1;	
		} else {
			// Don't let ListView try to reuse the views.
			return AdapterView.ITEM_VIEW_TYPE_IGNORE;			
		}
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return listItems.isEmpty();
	}
}
