package com.pku.xiaoyoubang.view;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pku.xiaoyoubang.R;
import com.pku.xiaoyoubang.entity.AnswerEntity;
import com.pku.xiaoyoubang.entity.CommentEntity;
import com.pku.xiaoyoubang.entity.InviteEntity;
import com.pku.xiaoyoubang.entity.QuestionEntity;
import com.pku.xiaoyoubang.tool.Information;
import com.pku.xiaoyoubang.tool.MyApplication;
import com.pku.xiaoyoubang.tool.Tool;
import com.umeng.analytics.MobclickAgent;

public class AddQuestionActivity extends Activity
{
	private Button buttonBack;
	private Button buttonFinish;
	
	private EditText textTitle;
	private EditText textInfo;
	private CheckBox box;
	
	private Dialog dialog;
	private HttpURLConnection connection = null;
	
	private Handler handler;
	
	private String id;
	private String title;
	private String info;
	private String time;
	
	@SuppressLint("HandlerLeak")
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		MyApplication.getInstance().addActivity( this );
		
		handler = new Handler()
		{
			public void handleMessage( Message message )
			{	
				if( dialog != null )
				{
					dialog.dismiss();
				}
				switch( message.what )
				{
				case 0 : //add success
					addQuestionSuccess();
					break;
				case 1 : //add failed					
					showError( "添加提问失败" );
					break;
				}
			}
		};
		
		initView();
	}
	
	private void initView()
	{
		setContentView( R.layout.add_question );
		
		textTitle = ( EditText ) findViewById( R.id.add_question_input1 );
		textTitle.setHint( "请简要描述你的问题..." );
		textInfo = ( EditText ) findViewById( R.id.add_question_input2 );
		textInfo.setHint( "请补充描述相关的背景、想法、要求等..." );
		
		box = ( CheckBox ) findViewById( R.id.add_question_checkbox );
		box.setOnCheckedChangeListener
		(
			new OnCheckedChangeListener()
			{
				public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) 
				{
					box.setChecked( isChecked );
					if( isChecked )
					{
						box.setBackgroundResource( R.drawable.check_yes );
					}
					else
					{
						box.setBackgroundResource( R.drawable.check_no );
					}
				}
			}
		);
		
		buttonBack = ( Button ) findViewById( R.id.add_question_button_back );
		buttonBack.setText( "<  " );
		buttonBack.setOnClickListener
		(
			new OnClickListener()
			{
				public void onClick( View view )
				{
					back();
				}
			}
		);
		
		buttonFinish = ( Button ) findViewById( R.id.add_question_button_finish );
		buttonFinish.setOnClickListener
		(
			new OnClickListener()
			{
				public void onClick( View view )
				{
					judgeInput();
				}
			}
		);
	}
	
	private void addQuestionSuccess()
	{
		QuestionEntity entity = new QuestionEntity();
		entity.setId( id );
		entity.setQuestionInfo( info );
		entity.setQuestionTitle( title );
		entity.setAnswerCount( 0 );
		entity.setAnswerList( new ArrayList< AnswerEntity >() );
		entity.setCommentList( new ArrayList< CommentEntity >() );
		entity.setCompany( Information.Company );
		entity.setCreateTime( time );
		entity.setHasAnswered( false );
		entity.setInviteMeList( new ArrayList< InviteEntity >() );
		entity.setJob( Information.Job );
		entity.setMyInviteList( new ArrayList< InviteEntity >() );
		entity.setNew( false );
		entity.setPKU( Information.PKU_Value );
		entity.setPraiseCount( 0 );
		entity.setSex( Information.Sex );
		entity.setUpdate( false );
		entity.setModifyTime( time );
		entity.setUserHeadUrl( Information.HeadUrl );
		entity.setUserId( Information.Id );
		entity.setUserName( Information.Name );
		entity.setNew( false );
		entity.setInvisible( box.isChecked() );
		
		Tool.writeQuestionInfoToFile( entity.getId(), entity );
		
		Intent intent = getIntent();
		intent.putExtra( "question", entity );
		setResult( 2, intent );
		finish();
	}
	
	private void judgeInput()
	{
		title = textTitle.getText().toString();
		if( title.length() == 0 )
		{
			showError( "标题不能为空" );
			return;
		}
		if( !title.substring( title.length() - 1, title.length() ).equals( "？" ) )
		{
			if( title.length() == 30 )
			{
				showError( "问题包含问号不能超过30个字" );
				return;
			}
			title += "？";
		}
		info = textInfo.getText().toString();
		startAdd( title, info );
	}
	
	private void startAdd( final String title, final String info )
	{
		if( Tool.isNetworkConnected( this ) == true )
		{
			dialog = new Dialog( this, R.style.dialog_progress );
			LayoutInflater inflater = LayoutInflater.from( this );  
			View view = inflater.inflate( R.layout.dialog_progress, null );
			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );
			textView.setText( "正在添加提问" );
			
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
							if( connection != null )
							{
								connection.disconnect();
							}
							dialog.dismiss();
							return true;
						}
						return false;
					}
				}
			);
			dialog.show();
			
			new Thread
	    	(
	    		new Thread()
	    		{
	    			public void run()
	    			{
	    				doAdd( title, info );
	    			}
	    		}
	    	).start();
		}
		else
		{
			showError( "网络不可用，请打开网络" );
		}
	}
	
	private void doAdd( final String title, final String info )
	{
		final String urlString = Information.Server_Url + "/api/question";
		try
		{
			URL url = new URL( urlString );
			connection = ( HttpURLConnection ) url.openConnection();  
			connection.setRequestProperty( "Connection", "keep-alive" );
			connection.setRequestProperty( "Content-Type", "application/json" );
			connection.setRequestMethod( "POST" );
			connection.setConnectTimeout( 10000 );
			connection.setReadTimeout( 30000 );
			connection.setDoOutput( true );
			connection.setDoInput( true );
			
			JSONObject json = new JSONObject();
			json.put( "token", Information.Token );
			json.put( "title", title );
			json.put( "info", info );
			json.put( "invisible", box.isChecked() );
			connection.getOutputStream().write( json.toString().getBytes() );			

			final int responseCode = connection.getResponseCode();
			if( responseCode == 200 )
			{
				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
				String temp1 = null;
				StringBuilder value = new StringBuilder();
				while( ( temp1 = reader.readLine() ) != null )
				{
					value.append( temp1 );
				}

				JSONObject jsonObject = new JSONObject( value.toString() );
				if( jsonObject.getInt( "result" ) == 3000 )
				{
					id = jsonObject.getString( "id" );
					time = jsonObject.getString( "modifyTime" );
					handler.sendEmptyMessage( 0 );
				}
				else
				{
					handler.sendEmptyMessage( 1 );
				}
			}
			else
			{
				handler.sendEmptyMessage( 1 );
			}
		}
		catch( Exception ex )
		{
			handler.sendEmptyMessage( 1 );
		}
		finally
		{
			if( connection != null )
			{
				connection.disconnect();
			}
		}
	}
	
	private void showError( String text )
	{
		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();
	}
	
	private void back()
	{
		setResult( 1 );
		finish();
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
