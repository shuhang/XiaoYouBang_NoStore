package com.pku.xiaoyoubang;

import java.io.File;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.pku.xiaoyoubang.entity.UserEntity;
import com.pku.xiaoyoubang.tool.Information;
import com.pku.xiaoyoubang.tool.MyApplication;
import com.pku.xiaoyoubang.tool.MyDatabaseHelper;
import com.pku.xiaoyoubang.view.StartActivity;
import com.pku.xiaoyoubang.view.TabActivity1;
import com.pku.xiaoyoubang.view.TabActivity2;
import com.pku.xiaoyoubang.view.TabActivity3;
import com.pku.xiaoyoubang.view.TabActivity4;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity
{
	private TabHost tabHost;
	
	private RadioGroup radioGroup;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private RadioButton radioButton4;
	
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		UmengUpdateAgent.update( this );
		MobclickAgent.updateOnlineConfig( this );
		
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		MyApplication.getInstance().addActivity( this );
		
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
        tabHost.setCurrentTab( 0 );
        tabHost.setSelected( false ); 
        
        radioButton1 = ( RadioButton ) findViewById( R.id.radio_button1 );
        radioButton2 = ( RadioButton ) findViewById( R.id.radio_button2 );
        radioButton3 = ( RadioButton ) findViewById( R.id.radio_button3 );
        radioButton4 = ( RadioButton ) findViewById( R.id.radio_button4 );

        radioGroup = ( RadioGroup ) findViewById( R.id.main_radio );
        radioGroup.setOnCheckedChangeListener
	    (
	    	new OnCheckedChangeListener() 
	    	{
	    		public void onCheckedChanged( RadioGroup group, int checkedId ) 
	    		{
	    			switch( checkedId ) 
	    			{
	    			case R.id.radio_button1:
	    				tabHost.setCurrentTabByTag( "tab1" );
	    				changeTextColor( Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK );
	    				break;
	    			case R.id.radio_button2:
	    				tabHost.setCurrentTabByTag( "tab2" );
	    				changeTextColor( Color.BLACK, Color.WHITE, Color.BLACK, Color.BLACK );    				
						break;
					case R.id.radio_button3:
						tabHost.setCurrentTabByTag( "tab3" );
						changeTextColor( Color.BLACK, Color.BLACK, Color.WHITE, Color.BLACK );						
						break;
					case R.id.radio_button4:
						tabHost.setCurrentTabByTag( "tab4" );
						changeTextColor( Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE );						
	    				break;
					default:
						break;
	    			}
	    		}
	    	}
	    );
    }
	
	private void changeTextColor( int color1, int color2, int color3, int color4 )
	{
		radioButton1.setTextColor( color1 );
		radioButton2.setTextColor( color2 );
		radioButton3.setTextColor( color3 );
		radioButton4.setTextColor( color4 );
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
	
	public void onResume() 
	{
		super.onResume();
		MobclickAgent.onResume( this );
	}
	
	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPause( this );
	}
}
