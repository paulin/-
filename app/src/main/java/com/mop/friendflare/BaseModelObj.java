package com.mop.friendflare;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class BaseModelObj implements Serializable{
	
	public static final String DEFAULT_STRING_VALUE = "";
	
	private static final long serialVersionUID = 1L;
	public static final BigDecimal CURRENCY_DIVIDER = new BigDecimal(100);
	
	//Database Column Names
	public static final String ROWID = "_id";
	
	private boolean isPersistant;
	private boolean isChanged;
	private boolean isLoaded;
	
	protected Long id;
	
	
	
	/**
	 * Returns true if the value has changed
	 * Also sets the isChanged flag
	 * @param newObj
	 * @param currentObj
	 * @return	True if the value has changed
	 */
	protected boolean isChanged(Object newObj, Object currentObj){
		
		if(currentObj == null || !currentObj.equals(newObj)){
			setChanged(true);
			return true;
		}
		return false;
	}
	
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
		setPersistant(true);
	}
	
	public boolean isPersistant() {
		return isPersistant;
	}
	protected void setPersistant(boolean isPersistant) {
		this.isPersistant = isPersistant;
	}
	public boolean isChanged() {
		return isChanged;
	}
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
	public boolean isLoaded() {
		return isLoaded;
	}
	protected void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}


	/**
	 * Returns the converted long as a date object
	 * If it is null, then it returns a new Date object (current time)
	 * @param dateValue
	 * @return a Date Object
	 */
	protected Date convertLongToDate(Long dateValue){
		if(dateValue > 0 && dateValue != null){
			return new Date(dateValue);
		}
		return new Date();
	}
	
	protected Boolean convertIntToBoolean(Integer boolValue){
		if(boolValue != null){
			if(boolValue.intValue() == 1) return true;
		}
		return false;
	}
	
	
	public static BigDecimal convertLongToBigDecimal(Long value){
		if(value != null){
			BigDecimal myVal = new BigDecimal(value);
			return myVal.divide(CURRENCY_DIVIDER,2, RoundingMode.HALF_DOWN);
		}else{
			return new BigDecimal(0);
		}
	}
	
	public static Long convertBigDecimalToLong(BigDecimal value){
		if(value == null){
			return 0l;
		}else{
			BigDecimal myVal = value.multiply(CURRENCY_DIVIDER);
			return myVal.longValue();
		}
	}
	
	/**
	 * Loads the object's values from the cursor
	 * @param cursor
	 */
	void loadFromCursor(Cursor cursor){
		id = cursor.getLong(cursor.getColumnIndexOrThrow(ROWID));
		if(id != null){
			isPersistant = true;
		}
		
		buildFromCursor(cursor);
	}
	
	abstract void buildFromCursor(Cursor cursor);
	
	
	/**
	 * Returns a contentValues map containing the values to be saved in the model object
	 * @return
	 */
	abstract ContentValues getContentValues();
	
}
