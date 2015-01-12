package com.pku.xiaoyoubang.model;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pku.xiaoyoubang.R;

public class AlertAdapter extends BaseAdapter 
{
	public static final int TYPE_BUTTON = 0;
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_EXIT = 2;
	public static final int TYPE_CANCEL = 3;
	private List< String > items;
	private int[] types;
	private boolean isTitle = false;
	private Context context;

	public AlertAdapter( Context context, String title, List< String > items, String exit, String cancel ) 
	{
		this.items = items;
		this.types = new int[ items.size() + 3 ];
		this.context = context;
		
		if( title != null && !title.equals( "" ) ) 
		{
			types[ 0 ] = TYPE_TITLE;
			this.isTitle = true;
			this.items.add( 0, title );
		}

		if( exit != null && !exit.equals( "" ) ) 
		{
			types[ this.items.size() ] = TYPE_EXIT;
			this.items.add( exit );
		}

		if( cancel != null && !cancel.equals( "" ) ) 
		{
			types[ this.items.size() ] = TYPE_CANCEL;
			this.items.add( cancel );
		}
	}

	public int getCount() 
	{
		return items.size();
	}

	public Object getItem( int position )  
	{
		return items.get( position );
	}

	public long getItemId( int position ) 
	{
		return 0;
	}

	public boolean isEnabled( int position ) 
	{
		if( position == 0 && isTitle ) 
		{
			return false;
		} 
		else 
		{
			return super.isEnabled( position );
		}
	}

	public View getView( int position, View convertView, ViewGroup parent ) 
	{
		final String textString = ( String ) getItem( position );
		ViewHolder holder;
		int type = types[ position ];
		
		if( convertView == null || ( ( ViewHolder ) convertView.getTag() ).type != type ) 
		{
			holder = new ViewHolder();
			
			if( type == TYPE_CANCEL ) 
			{
				convertView = View.inflate( context, R.layout.alert_dialog_menu_list_layout_cancel, null );
			} 
			else if( type == TYPE_BUTTON ) 
			{
				convertView = View.inflate( context, R.layout.alert_dialog_menu_list_layout, null );
			} 
			else if( type == TYPE_TITLE ) 
			{
				convertView = View.inflate( context, R.layout.alert_dialog_menu_list_layout_title, null );
			} 
			else if ( type == TYPE_EXIT ) 
			{
				convertView = View.inflate( context, R.layout.alert_dialog_menu_list_layout_special, null );
			}

			holder.text = ( TextView ) convertView.findViewById( R.id.popup_text );
			holder.type = type;

			convertView.setTag( holder );
		} 
		else 
		{
			holder = ( ViewHolder ) convertView.getTag();
		}

		holder.text.setText( textString );
		return convertView;
	}

	static class ViewHolder 
	{
		TextView text;
		int type;
	}
}
