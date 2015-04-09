package com.pku.xiaoyoubang;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pku.xiaoyoubang.entity.UserEntity;
import com.pku.xiaoyoubang.model.MyAlertDialog;
import com.pku.xiaoyoubang.service.MyService;
import com.pku.xiaoyoubang.tool.Information;
import com.pku.xiaoyoubang.tool.MyApplication;
import com.pku.xiaoyoubang.tool.MyDatabaseHelper;
import com.pku.xiaoyoubang.view.StartActivity;
import com.pku.xiaoyoubang.view.TabActivity1;
import com.pku.xiaoyoubang.view.TabActivity2;
import com.pku.xiaoyoubang.view.TabActivity3;
import com.pku.xiaoyoubang.view.TabActivity4;
import com.pku.xiaoyoubang.view.TabActivity5;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements OnCheckedChangeListener
{
	private TabHost tabHost;
	
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private RadioButton radioButton4;
	private RadioButton radioButton5;
	
	private static TextView textNumber2;
	private static TextView textNumber3;
	private static TextView textNumber4;
	
	private Dialog dialog;
	private static int nowState = 0;
	
	private boolean force = false;
	
	//Service
	public static MyService myService = null;
	private ServiceConnection connection = new ServiceConnection()
	{
		public void onServiceConnected( ComponentName name, IBinder service )
		{
			MyService.MyBinder myBinder = ( MyService.MyBinder ) service;
			myService = myBinder.getService();

			myService.startTab2();
			myService.startTab3();
			myService.startTab4();
		}
		public void onServiceDisconnected( ComponentName name ) {}
	};
	
	public static Handler handler = new Handler()
	{
		public void handleMessage( Message message )
		{
			switch( message.what )
			{
			case 2 :
				showNumber2( message.arg1 );
				break;
			case 3 :
				showNumber3( message.arg1 );
				break;
			case 4 :
				showNumber4( message.arg1 );
				break;
			}
		}
	};
	
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		nowState = 0;
		
		PushAgent mPushAgent = PushAgent.getInstance( this );
		mPushAgent.enable();

		UmengUpdateAgent.setUpdateOnlyWifi( false );
		
		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );
		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean( "force_update", true );
		editor.commit();
		
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		MyApplication.getInstance().addActivity( this );
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
		.Builder( getApplicationContext() )
		.discCacheSize( 50 * 1024 * 1024 )//
		.discCacheFileCount( 100 )//缓存一百张图片
		.build();
		ImageLoader.getInstance().init( config );
		
		Information.options = new DisplayImageOptions.Builder()
		.showImageOnLoading( R.drawable.head_hidden )
		.showImageForEmptyUri( R.drawable.head_hidden )
		.showImageOnFail( R.drawable.head_hidden )
		.cacheInMemory( true )
		.cacheOnDisk( true )
		.build();
		
		Information.options_image_big = new DisplayImageOptions.Builder()
		.cacheInMemory( false )
		.cacheOnDisk( true )
		.bitmapConfig( Bitmap.Config.RGB_565 )
		.build();
		
		Information.options_image_small = new DisplayImageOptions.Builder()
		.showImageOnLoading( R.drawable.image_download )
		.showImageForEmptyUri( R.drawable.image_download )
		.showImageOnFail( R.drawable.image_download )
		.cacheInMemory( false )
		.cacheOnDisk( true )
		.bitmapConfig( Bitmap.Config.RGB_565 )
		.build();
		
		File file1 = new File( Information.Image_Path );
    	if( !file1.exists() )
    		file1.mkdirs();
    	
    	File file2 = new File( Information.File_Path );
    	if( !file2.exists() )
    		file2.mkdirs();
		
		judgeLoginStatus();
	}
	
	
	private void judgeLoginStatus()
	{
		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );
		Information.Token = shared.getString( "token", "" );
		if( Information.Token.equals( "" ) )
		{
			SharedPreferences.Editor editor = shared.edit();
			editor.putBoolean( "device_token_update", false );
			editor.commit();
			startActivityForResult( new Intent( this, StartActivity.class ), 1001 );
		}
		else
		{
			loadInformation();
			initView();
		}
	}
	
	private void loadInformation()
	{
		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );
		Information.Id = shared.getString( "id", "" );
		UserEntity entity = MyDatabaseHelper.getInstance( this ).getUser( Information.Id );
		
		Information.Name = entity.getName();
		Information.HeadUrl = entity.getHeadUrl();
		Information.Sex = entity.getSex();
		Information.Birthday = entity.getBirthday();
		Information.PKU_Value = entity.getPku();
		Information.Now_Home = entity.getNowHome();
		Information.Old_Home = entity.getOldHome();
		Information.QQ = entity.getQq();
		Information.Company = entity.getJob1();
		Information.Part = entity.getJob2();
		Information.Job = entity.getJob3();
		Information.Version = entity.getVersion();
		Information.Tag = entity.getTags();
		Information.Praise_Count = entity.getPraiseCount();
		Information.Question_Count = entity.getQuestionCount();
		Information.Answer_Count = entity.getAnswerCount();
		Information.InviteUserName = entity.getInviteName();
		Information.InviteUserHeadUrl = entity.getInviteHeadUrl();
		Information.InviteUserId = entity.getInviteUserId();
	}
	
	private void initView() 
    {
    	setContentView( R.layout.main_page );
    	
    	tabHost = getTabHost();
    	tabHost.setup();
        tabHost.addTab( tabHost.newTabSpec( "tab1" ).setContent( new Intent( this, TabActivity1.class ) )
        		.setIndicator( "tab1" ) );
        tabHost.addTab( tabHost.newTabSpec( "tab2" ).setContent( new Intent( this, TabActivity2.class ) )
        		.setIndicator( "tab2" ) );
        tabHost.addTab( tabHost.newTabSpec( "tab3" ).setContent( new Intent( this, TabActivity3.class ) )
        		.setIndicator( "tab3" ) );
        tabHost.addTab( tabHost.newTabSpec( "tab4" ).setContent( new Intent( this, TabActivity4.class ) )
        		.setIndicator( "tab4" ) );
        tabHost.addTab( tabHost.newTabSpec( "tab5" ).setContent( new Intent( this, TabActivity5.class ) )
        		.setIndicator( "tab5" ) );
        tabHost.setCurrentTab( 0 );
        tabHost.setSelected( false ); 
        
        radioButton1 = ( RadioButton ) findViewById( R.id.main_tab_1 );
        radioButton1.setOnCheckedChangeListener( this );
        radioButton2 = ( RadioButton ) findViewById( R.id.main_tab_2 );
        radioButton2.setOnCheckedChangeListener( this );
        radioButton3 = ( RadioButton ) findViewById( R.id.main_tab_3 );
        radioButton3.setOnCheckedChangeListener( this );
        radioButton4 = ( RadioButton ) findViewById( R.id.main_tab_4 );
        radioButton4.setOnCheckedChangeListener( this );
        radioButton5 = ( RadioButton ) findViewById( R.id.main_tab_5 );
        radioButton5.setOnCheckedChangeListener( this );
        
        textNumber2 = ( TextView ) findViewById( R.id.main_tab_2_number );
        textNumber3 = ( TextView ) findViewById( R.id.main_tab_3_number );
        textNumber4 = ( TextView ) findViewById( R.id.main_tab_4_number );
        
        Intent intent = new Intent( this, MyService.class );
		bindService( intent, connection, Context.BIND_AUTO_CREATE );
    }
	
	public static void showNumber2( int number )
	{
		if( nowState != 1 )
		{
			TabActivity2.shouldUpdate = true;
			textNumber2.setVisibility( View.VISIBLE );
			textNumber2.setText( "" + number );
		}
	}
	
	public void clearNumber2()
	{
		textNumber2.setVisibility( View.INVISIBLE );
	}
	
	public static void showNumber3( int number )
	{
		if( nowState != 2 )
		{
			TabActivity3.shouldUpdate = true;
			textNumber3.setVisibility( View.VISIBLE );
			textNumber3.setText( "" + number );
		}
	}
	
	public void clearNumber3()
	{
		textNumber3.setVisibility( View.INVISIBLE );
	}
	
	public static void showNumber4( int number )
	{
		if( nowState != 3 )
		{
			TabActivity4.shouldUpdate = true;
			textNumber4.setVisibility( View.VISIBLE );
			textNumber4.setText( "" + number );
		}
	}
	
	public void clearNumber4()
	{
		textNumber4.setVisibility( View.INVISIBLE );
	}
	
	private void changeTextColor( int color1, int color2, int color3, int color4, int color5 )
	{
		radioButton1.setTextColor( color1 );
		radioButton2.setTextColor( color2 );
		radioButton3.setTextColor( color3 );
		radioButton4.setTextColor( color4 );
		radioButton5.setTextColor( color5 );
	}
	
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) 
	{
		super.onActivityResult( requestCode, resultCode, data );
		if( resultCode == 0 )
		{
			initView();
		}
		else
		{
			finish();
		}
	}
	
	/**
	 * 退出
	 */
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) 
//	{
//		if( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
//		{
//			Log.e( "qqq", "wwww" );
//			Intent intent = new Intent( Intent.ACTION_MAIN );  
//			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );  
//			intent.addCategory( Intent.CATEGORY_HOME );
//			startActivity( intent );  
//		}
//		return super.dispatchKeyEvent(event);
//	}
	
	public void onResume() 
	{
		super.onResume();
		MobclickAgent.onResume( this );

		if( myService != null )
		{
			myService.startTab2();
			myService.startTab3();
			myService.startTab4();
		}
	}
	
	public static void startMessage()
	{
		if( myService != null )
		{
			myService.startTab2();
			myService.startTab3();
			myService.startTab4();
		}
	}
	
	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPause( this );
	}
	
	public boolean onPrepareOptionsMenu( Menu menu )
	{
		super.onPrepareOptionsMenu( menu );
		List< String > list = new ArrayList< String >();
		list.add( "检查更新" );
		list.add( "退出程序" );
		MyAlertDialog.showAlert
		( 
			MainActivity.this, "系统", list, null, new MyAlertDialog.OnAlertSelectId()
			{
				public void onClick( int whichButton ) 
				{						
					switch( whichButton )
					{
					case 0 :
						checkUpdate();
						break;
					case 1 :
						MyApplication.getInstance().logout();
						break;
					}
				}
			}
		);
		return false;
	}
	
	private void checkUpdate()
	{
		dialog = new Dialog( this, R.style.dialog_progress );
		LayoutInflater inflater = LayoutInflater.from( this );  
		View view = inflater.inflate( R.layout.dialog_progress, null );
		TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );
		textView.setText( "正在检查更新" );
		
		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.alpha = 0.8f;
		dialog.getWindow().setAttributes( layoutParams );
		dialog.setContentView( view );
		dialog.setCancelable( false );
		dialog.setOnKeyListener
		(
			new OnKeyListener()
			{
				public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 
				{
					if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
					{
						dialog.dismiss();
						return true;
					}
					return false;
				}
			}
		);
		dialog.show();
		
		UmengUpdateAgent.setUpdateAutoPopup( false );
		UmengUpdateAgent.forceUpdate( this );
		UmengUpdateAgent.setUpdateListener 
		(
			new UmengUpdateListener() 
			{
			    public void onUpdateReturned( int updateStatus, UpdateResponse updateInfo ) 
			    {
			    	if( dialog != null )
			    	{
			    		dialog.dismiss();
			    	}
			        switch( updateStatus ) 
			        {
			        case UpdateStatus.Yes:
			        	if( updateInfo.version.indexOf( "F" ) != -1 )
				        {
				        	force = true;
				        }
				        UmengUpdateAgent.showUpdateDialog( MainActivity.this, updateInfo );
			            break;
			        case UpdateStatus.No:
			            Toast.makeText( MainActivity.this, "已经是最新版本", Toast.LENGTH_SHORT ).show();
			            break;
			        case UpdateStatus.Timeout:
			        	Toast.makeText( MainActivity.this, "检查更新超时", Toast.LENGTH_SHORT ).show();
			            break;
			        }
			    }
			}
		);
		UmengUpdateAgent.setDialogListener
		(
			new UmengDialogButtonListener() 
			{
			    public void onClick( int status ) 
			    {
			        switch( status ) 
			        {
			        case UpdateStatus.Update:			            
			            break;
			        case UpdateStatus.NotNow:
			            if( force == true )
			            {
			            	MyApplication.getInstance().logout();
			            }
			            else
			            {
			            	SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );
			        		SharedPreferences.Editor editor = shared.edit();
			        		editor.putBoolean( "force_update", false );
			        		editor.commit();
			            }
			            break;
			        }
			    }
			}
		);
	}
	
	private void switchState( int state ) 
	{
        if( nowState == state )
        {
            return;
        }
 
        nowState = state;
        radioButton1.setChecked( false );
        radioButton2.setChecked( false );
        radioButton3.setChecked( false );
        radioButton4.setChecked( false );
        radioButton5.setChecked( false );
 
        switch( nowState ) 
        {
            case 0:
            	radioButton1.setChecked( true );
            	tabHost.setCurrentTabByTag( "tab1" );
				changeTextColor( Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK );
                break;
            case 1:
            	clearNumber2();
            	radioButton2.setChecked( true );
				tabHost.setCurrentTabByTag( "tab2" );
				changeTextColor( Color.BLACK, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK );
                break;
            case 2:
            	clearNumber3();
            	radioButton3.setChecked( true );
            	tabHost.setCurrentTabByTag( "tab3" );
				changeTextColor( Color.BLACK, Color.BLACK, Color.WHITE, Color.BLACK, Color.BLACK );
                break;
            case 3:
            	clearNumber4();
            	radioButton4.setChecked( true );
            	tabHost.setCurrentTabByTag( "tab4" );
				changeTextColor( Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE, Color.BLACK );
                break;
            case 4:
            	radioButton5.setChecked( true );
            	tabHost.setCurrentTabByTag( "tab5" );
				changeTextColor( Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE );
                break;
            default:
                break;
        }
    }

	public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
	{
        if( isChecked ) 
        {
            switch( buttonView.getId() ) 
            {
            case R.id.main_tab_1:
                switchState( 0 );
                break;
            case R.id.main_tab_2:
                switchState( 1 );
                break;
            case R.id.main_tab_3:
                switchState( 2 );
                break;
            case R.id.main_tab_4:
                switchState( 3 );
                break; 
            case R.id.main_tab_5:
                switchState( 4 );
                break; 
            default:
                break;
            }
        }
    }
}
