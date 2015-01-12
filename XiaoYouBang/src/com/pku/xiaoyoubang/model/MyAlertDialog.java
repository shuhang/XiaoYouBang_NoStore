package com.pku.xiaoyoubang.model;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.pku.xiaoyoubang.R;

public class MyAlertDialog 
{
	/**
	 * 
	 * @param context
	 * @param title
	 * @param items
	 * @param exit
	 * @param alertDo
	 * @return
	 */
	public static Dialog showAlert( final Context context, final String title, final List< String > items, String exit, final OnAlertSelectId alertDo )
	{
		final Dialog dialog = new Dialog( context, R.style.MMTheme_DataSheet );
		LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		LinearLayout layout = ( LinearLayout ) inflater.inflate( R.layout.alert_dialog_menu_layout, null );
		
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth( cFullFillWidth );
		
		final ListView list = ( ListView ) layout.findViewById( R.id.content_list );
		AlertAdapter adapter = new AlertAdapter( context, title, items, exit, "取消" );
		list.setAdapter( adapter );
		list.setDividerHeight( 0 );

		list.setOnItemClickListener
		(
			new OnItemClickListener() 
			{
				public void onItemClick( AdapterView< ? > parent, View view, int position, long id ) 
				{
					if( !( title == null || title.equals( "" ) ) && position - 1 >= 0 ) 
					{
						alertDo.onClick( position - 1 );
						dialog.dismiss();
						list.requestFocus();
					} 
					else 
					{
						alertDo.onClick( position );
						dialog.dismiss();
						list.requestFocus();
					}
				}
			}
		);

		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = Gravity.BOTTOM;
		dialog.onWindowAttributesChanged( lp );
		dialog.setCanceledOnTouchOutside( true );
		dialog.setContentView( layout );
		dialog.show();
		return dialog;
	}
	/**
	 * 
	 * @author shuhang
	 *
	 */
	public interface OnAlertSelectId 
	{
		void onClick( int whichButton );
	}
}
