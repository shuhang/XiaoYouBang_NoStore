package com.pku.xiaoyoubang.tool;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

public class MemoryCache 
{
	private Map< String, Bitmap > cache = Collections.synchronizedMap( new LinkedHashMap< String, Bitmap >( 10, 1.5f, true ) );
	
	private long totalSize = 0;
	private long limitSize = 1 * 1024 * 1024;
	
	public MemoryCache()
	{
		setLimitSize( Runtime.getRuntime().maxMemory() / 10 );
	}
	
	public void clear()
	{
		cache.clear();
	}
	
	private void setLimitSize( long limitSize )
	{
		this.limitSize = limitSize;
	}
	
	public Bitmap getBitmap( String key )
	{
		try
		{
			if( !cache.containsKey( key ) )
			{
				return null;
			}
			return cache.get( key );
		}
		catch( Exception ex )
		{
			return null;
		}
	}
	
	public void putBitmap( String key, Bitmap bitmap )
	{
		try
		{
			if( cache.containsKey( key ) )
			{
				totalSize -= getSize( cache.get( key ) );
			}
			cache.put( key, bitmap );
			totalSize += getSize( bitmap );
			controllSize();
		}
		catch( Exception ex ) {};
	}
	
	private void controllSize()
	{
		if( totalSize > limitSize )
		{
			Iterator< Entry< String, Bitmap > > iterator = cache.entrySet().iterator();
			while( iterator.hasNext() )
			{
				Entry< String, Bitmap > entry = iterator.next();
				totalSize -= getSize( entry.getValue() );
				iterator.remove();
				if( totalSize <= limitSize )
				{
					break;
				}
			}
		}
	}
	
	private long getSize( Bitmap bitmap )
	{
		if( bitmap == null )
		{
			return 0;
		}
		return bitmap.getRowBytes() * bitmap.getHeight();
	}
}
