package com.pku.xiaoyoubang.view;import android.annotation.SuppressLint;import android.app.Activity;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.support.v4.app.FragmentActivity;import android.support.v4.app.FragmentPagerAdapter;import android.support.v4.view.ViewPager;import android.support.v4.view.ViewPager.OnPageChangeListener;import android.view.KeyEvent;import android.view.Window;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.model.TabMeViewPagerAdapter;import com.pku.xiaoyoubang.tool.MyApplication;import com.umeng.analytics.MobclickAgent;import com.umeng.update.UmengDialogButtonListener;import com.umeng.update.UmengUpdateAgent;import com.umeng.update.UmengUpdateListener;import com.umeng.update.UpdateResponse;import com.umeng.update.UpdateStatus;import com.viewpagerindicator.IconPagerAdapter;import com.viewpagerindicator.TabPageIndicator;@SuppressLint("HandlerLeak")public class TabActivityMe extends FragmentActivity{	private boolean force = false;	private boolean update = false;	/**	 *  	 */	public static Handler handler;		private TabPageIndicator mIndicator ;	private ViewPager mViewPager ;	private FragmentPagerAdapter mAdapter ;		private int nowIndex = 0;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );		showView();	}		private void showView()	{		setContentView( R.layout.tab_me );				handler = new Handler()		{			public void handleMessage( Message message )			{								switch( message.what )				{				case 0 :					showUpdateAt( message.arg1 );					break;				}			}		};				initView();	}		private void initView()	{		mIndicator = (TabPageIndicator) findViewById( R.id.id_indicator );		mViewPager = (ViewPager) findViewById( R.id.id_pager );		mAdapter = new TabMeViewPagerAdapter( getSupportFragmentManager() );		mViewPager.setAdapter( mAdapter );		mIndicator.setViewPager( mViewPager, 0 );		mViewPager.setOffscreenPageLimit( 4 );				mIndicator.setOnPageChangeListener		(			new OnPageChangeListener()			{				public void onPageScrollStateChanged(int arg0)				{									}				public void onPageScrolled(int arg0, float arg1, int arg2) 				{									}				public void onPageSelected( int index ) 				{					nowIndex = index;					removeUpdateAt( nowIndex );				}			}		);	}	private void showUpdateAt( int index )	{		IconPagerAdapter iconAdapter = (IconPagerAdapter) mViewPager.getAdapter();		iconAdapter.setIconResId( index, R.drawable.point );		mIndicator.notifyDataSetChanged();	}		private void removeUpdateAt( int index )	{		IconPagerAdapter iconAdapter = ( IconPagerAdapter ) mViewPager.getAdapter();		iconAdapter.setIconResId( index, 0 );		mIndicator.notifyDataSetChanged();	}	public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );		if( !update )		{			UmengUpdateAgent.setUpdateAutoPopup( false );			UmengUpdateAgent.setUpdateListener 			(				new UmengUpdateListener() 				{				    public void onUpdateReturned( int updateStatus, UpdateResponse updateInfo ) 				    {				        switch( updateStatus ) 				        {				        case UpdateStatus.Yes:				        	SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );				        	if( shared.getBoolean( "force_update", true ) == true )				        	{				        		if( updateInfo.version.indexOf( "F" ) != -1 )					        	{					        		force = true;					        	}				        		update = true;					            UmengUpdateAgent.showUpdateDialog( TabActivityMe.this, updateInfo );				        	}				            break;				        }				    }				}			);			UmengUpdateAgent.update( this );			UmengUpdateAgent.setDialogListener			(				new UmengDialogButtonListener() 				{				    public void onClick( int status ) 				    {				        switch( status ) 				        {				        case UpdateStatus.Update:     				        	update = true;				            break;				        case UpdateStatus.NotNow:				            if( force == true )				            {				            	MyApplication.getInstance().logout();				            }				            else				            {				            	SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );				        		SharedPreferences.Editor editor = shared.edit();				        		editor.putBoolean( "force_update", false );				        		editor.commit();				            }				            break;				        }				    }				}			);		}	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			Intent intent = new Intent( Intent.ACTION_MAIN );  			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );  			intent.addCategory( Intent.CATEGORY_HOME );			startActivity( intent );  			return true;		}		return false;	}}