package com.mop.friendflare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


/**
 * Provides functionality for the HereMe menu 
 * 
 * @author paulin
 */
public class MenuAdapter {
	public static final String TAG = "MenuAdapter";

	public static final int REFRESH = 1;
	public static final int CLEAR = 2;
	public static final int EXIT = 3;
	private static Menu menu;

	public static void conditionMenu(Menu menu) {

		MenuAdapter.menu = menu;
		rebuildMenu();
	}
	
	public static void rebuildMenu() {
		menu.clear();
		MenuItem tempMI;
	    tempMI= menu.add(0, REFRESH, 0, "Refresh");

	    tempMI= menu.add(1, CLEAR, 0, "Clear");

	    
//	    tempMI= menu.add(2, EXIT, 0, "Exit");
//	    tempMI.setIcon(android.R.drawable.ic_menu_info_details);
	    
	    
	}
	
	public static void setLoggedIn(boolean loggedin) {
		rebuildMenu();
	}
	
	/* Handles item selections */
	public static boolean onOptionsItemSelected(MenuItem item, Activity activity) {		
		Intent i;
	    switch (item.getItemId()) {
//	    case CLEAR:	    	
//	    	//System.runFinalizersOnExit(true);
//	    	android.os.Process.killProcess(android.os.Process.myPid());
//	        return true;	        
//	    case REFRESH:	    	
//	    	//System.runFinalizersOnExit(true);
//	    	android.os.Process.killProcess(android.os.Process.myPid());
//	        return true;	        

	        
	    case EXIT:	    	
	    	//System.runFinalizersOnExit(true);
	    	android.os.Process.killProcess(android.os.Process.myPid());
	        return true;	        
	    }
	    return false;
	}
	
	public static void setMenuBackground(Activity activity){
		activity.getLayoutInflater().setFactory( new MenuAdapter.MenuFactory(activity));
	}	

	
	static class MenuFactory implements Factory {
			
		private Activity activity;
		public MenuFactory(Activity activity) {
			super();
			this.activity = activity;
		}
		
		@Override
		public View onCreateView ( String name, Context context, AttributeSet attrs ) {
			
			if ( name.equalsIgnoreCase( "com.android.internal.view.menu.IconMenuItemView" ) ) {
				
				try { // Ask our inflater to create the view
					LayoutInflater f = activity.getLayoutInflater();
					final View view = f.createView( name, null, attrs );
					// Kind of apply our own background
					new Handler().post( new Runnable() {
						public void run () {
							view.setBackgroundResource( R.drawable.menu_background_color);							
						}
					} );
					return view;
				}
				catch ( InflateException e ) {
				}
				catch ( ClassNotFoundException e ) {

				}
			}
			return null;
		}		
	}	
}
