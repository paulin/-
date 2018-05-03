package com.mop.friendflare;

import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

/**
 * Used to display the multiple feed lists
 * 
 * @author paulin
 */
public class MultiListAdapter extends BaseAdapter {
	
	public static final String TAG = "MultiListAdapter";
	public final ArrayList<Object> items = new ArrayList();
	
	public MultiListAdapter() {
		super();
	}

	/**
	 * Clears out everything
	 */
	public void clear() {
		items.clear();
	}
	
	/**
	 * Adds a new view to the adapter
	 * @param view
	 */
	public void addView(View view) {
		items.add(view);
	}
	
	/**
	 * Add a new Adapter to this adapter
	 * @param adapter
	 */
	public void addAdapter(SingleObjectListHolder adapter) {
		items.add(adapter);
	}

	public void redraw() {
		this.notifyDataSetChanged();
	}
	
	public Object getItem(int position) {
		//Walk through the array and for any adapter go into it 
		int positionPointer = 0;
		for (Object item : this.items) {
			if(item instanceof View) {
				if(positionPointer == position) {
					return item;
				} else {
					positionPointer += 1;					
				}
			} else if (item instanceof Adapter) {
				Adapter tempAdapter = (Adapter)item;

				if(tempAdapter.getCount() > 0) {				
					if(positionPointer + tempAdapter.getCount() <= position) {
						//Not looking in this adapter, move on
						positionPointer += tempAdapter.getCount();
					} else {
						//Looking in this adapter
						return tempAdapter.getItem(position - positionPointer);
					}
				} else {
					Log.w(TAG, "Empty Adapter in the FeedListAdapter [" + item.getClass().getName() + "]");					
				}
				
			} else {
				Log.e(TAG, "Unknown object type found in the FeedListAdapter [" + item.getClass().getName() + "]");
			}
		}
		return null;
	}

	public int getCount() {
		int total = 0;
		for (Object item : this.items) {
			if(item instanceof View) {
				total += 1;
			} else if (item instanceof Adapter) {
				Adapter tempAdapter = (Adapter)item;
				total += tempAdapter.getCount();				
			} else {
				Log.e(TAG, "Unknown object type found in the FeedListAdapter [" + item.getClass().getName() + "]");
			}
		}		
		return total;
	}

	public int getViewTypeCount() {
		if(items.size() < 1) return 1; 
		return items.size();
	}

	/**
	 * This is how the correct view is returned to the correct adapter.  Under the hood android knows
	 * what type of view should go where and it is recycled.  These values must match up to how the adapters
	 * and views are put into this class.
	 */
	public int getItemViewType(int position) {
		return AdapterView.ITEM_VIEW_TYPE_IGNORE;
				
//		int positionPointer = 0;
//		int itemCounter = 0;
//		for (Object item : this.items) {
//			//Log.v(TAG, "MATT: getItemViewType position [" + position + "] item [" + item.getClass().getName() + "] positionPoint [" + positionPointer + "]" );
//			if(item instanceof View) {
//				if(positionPointer == position) {
//					return AdapterView.ITEM_VIEW_TYPE_IGNORE;
//				} else {
//					positionPointer += 1;					
//				}
//			} else if (item instanceof Adapter) {
//				Adapter tempAdapter = (Adapter)item;
//				if(tempAdapter.getCount() > 0) {
//					if(positionPointer + tempAdapter.getCount() <= position) {
//						//Not looking in this adapter, move on
//						positionPointer += tempAdapter.getCount();
//					} else {
//						return itemCounter;
//					}					
//				} else {
//					Log.w(TAG, "Empty Adapter in the FeedListAdapter [" + item.getClass().getName() + "]");					
//				}
//			} else {
//				Log.e(TAG, "Unknown object type found in the FeedListAdapter [" + item.getClass().getName() + "]");
//			}
//			itemCounter++;
//		}
//		
//		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {
		Object tempObj = getItem(position);
		if(tempObj instanceof View) {
			return ((View)tempObj).isEnabled(); 
		}
		
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int positionPointer = 0;
		for (Object item : this.items) {
//			Log.v(TAG, "MATT: getItemViewType position [" + position + "] item [" + item.getClass().getName() + "] positionPoint [" + positionPointer + "]" );
			if(item instanceof View) {
				if(positionPointer == position) {
					return (View)item; 
				} else {
					positionPointer += 1;					
				}
			} else if (item instanceof Adapter) {
				Adapter tempAdapter = (Adapter)item;
				if(tempAdapter.getCount() > 0) {
					if(positionPointer + tempAdapter.getCount() <= position) {
						//Not looking in this adapter, move on
						positionPointer += tempAdapter.getCount();
					} else {
						//Looking in this adapter
						return tempAdapter.getView(position - positionPointer, convertView, parent);
					}					
				} else {
					Log.w(TAG, "Empty Adapter in the FeedListAdapter [" + item.getClass().getName() + "]");					
				}
			} else {
				Log.e(TAG, "Unknown object type found in the FeedListAdapter [" + item.getClass().getName() + "]");
			}
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}