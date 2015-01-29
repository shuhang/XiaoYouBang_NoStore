package com.pku.xiaoyoubang.view;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pku.xiaoyoubang.R;
import com.pku.xiaoyoubang.entity.AnswerEntity;
import com.pku.xiaoyoubang.entity.CommentEntity;
import com.pku.xiaoyoubang.tool.Information;
import com.pku.xiaoyoubang.tool.MyApplication;
import com.pku.xiaoyoubang.tool.MyDatabaseHelper;
import com.pku.xiaoyoubang.tool.Tool;
import com.umeng.analytics.MobclickAgent;

public class AddAnswerActivity extends Activity
{
	private Button buttonBack;
	private Button buttonFinish;
	
	private TextView textTitle;
	private TextView textQuestion;
	private EditText textInfo;
	private CheckBox box;
	
	private RelativeLayout layout;
	
	private Dialog dialog;
	private HttpURLConnection connection = null;
	
	private String questionId;
	private String answerId;
	private String info;
	private String questionTitle;
	
	private Handler handler;
	private String time;
	
	private int type;
	
	@SuppressLint("HandlerLeak")
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		MyApplication.getInstance().addActivity( this );
		
		if( getIntent().getIntExtra( "type", 0 ) == 0 )
		{
			type = 0;
			questionId = getIntent().getStringExtra( "questionId" );
			questionTitle = getIntent().getStringExtra( "questionTitle" );
		}
		else
		{
			type = 1;
			questionTitle = getIntent().getStringExtra( "questionTitle" );
		}
		
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
					addAnswerSuccess();
					break;
				case 1 : //add failed					
					showError( "添加回答失败" );
					break;
				case 2 : //add success
					editAnswerSuccess();
					break;
				case 3 : //add failed					
					showError( "编辑回答失败" );
					break;
				}
			}
		};
		
		initView();
	}
	
	private void initView()
	{
		setContentView( R.layout.add_answer );
		
		textTitle = ( TextView ) findViewById( R.id.add_answer_title );
		if( type == 0 )
		{
			textTitle.setText( "添加回答" );
		}
		else
		{
			textTitle.setText( "编辑回答" );
		}
		
		textQuestion = ( TextView ) findViewById( R.id.add_answer_question );
		textQuestion.setText( "问题：" + questionTitle );
		
		textInfo = ( EditText ) findViewById( R.id.add_answer_input );
		textInfo.setHint( "写下你的答案、建议、参考…" );
		
		layout = ( RelativeLayout ) findViewById( R.id.add_answer_layout2 );
		
		box = ( CheckBox ) findViewById( R.id.add_answer_checkbox );
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
		
		buttonBack = ( Button ) findViewById( R.id.add_answer_button_back );
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
		
		buttonFinish = ( Button ) findViewById( R.id.add_answer_button_finish );
		buttonFinish.setOnClickListener
		(
			new OnClickListener()
			{
				public void onClick( View view )
				{
					if( Tool.isFastDoubleClick() )
					{
						return;
					}
					judgeInput();
				}
			}
		);
		
		if( type == 1 )
		{
			textInfo.setText( AnswerInfoActivity.entity.getAnswerInfo() );
			layout.setVisibility( View.GONE );
		}
	}
	
	private void judgeInput()
	{
		info = textInfo.getText().toString();
		if( info.length() == 0 )
		{
			showError( "请输入回答" );
			return;
		}
		
		if( type == 0 )
		{
			startAdd();
		}
		else
		{
			startEdit();
		}
	}
	
	private void startEdit()
	{
		if( Tool.isNetworkConnected( this ) == true )
		{
			dialog = new Dialog( this, R.style.dialog_progress );
			LayoutInflater inflater = LayoutInflater.from( this );  
			View view = inflater.inflate( R.layout.dialog_progress, null );
			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );
			textView.setText( "正在提交" );
			
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
	    				doEdit();
	    			}
	    		}
	    	).start();
		}
		else
		{
			showError( "网络不可用，请打开网络" );
		}
	}
	
	private void doEdit()
	{
		final String urlString = Information.Server_Url + "/api/answer/" + AnswerInfoActivity.entity.getId();
		try
		{
			URL url = new URL( urlString );
			connection = ( HttpURLConnection ) url.openConnection();  
			connection.setRequestProperty( "Connection", "keep-alive" );
			connection.setRequestProperty( "Content-Type", "application/json" );
			connection.setRequestMethod( "PUT" );
			connection.setConnectTimeout( 10000 );
			connection.setReadTimeout( 30000 );
			connection.setDoOutput( true );
			connection.setDoInput( true );
			
			JSONObject json = new JSONObject();
			json.put( "token", Information.Token );
			json.put( "content", info );
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
				if( jsonObject.getInt( "result" ) == 4000 )
				{
					time = jsonObject.getString( "editTime" );
					handler.sendEmptyMessage( 2 );
				}
				else
				{
					handler.sendEmptyMessage( 3 );
				}
			}
			else
			{
				handler.sendEmptyMessage( 3 );
			}
		}
		catch( Exception ex )
		{
			handler.sendEmptyMessage( 3 );
		}
		finally
		{
			if( connection != null )
			{
				connection.disconnect();
			}
		}
	}
	
	private void startAdd()
	{
		if( Tool.isNetworkConnected( this ) == true )
		{
			dialog = new Dialog( this, R.style.dialog_progress );
			LayoutInflater inflater = LayoutInflater.from( this );  
			View view = inflater.inflate( R.layout.dialog_progress, null );
			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );
			textView.setText( "正在添加回答" );
			
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
	    				doAdd();
	    			}
	    		}
	    	).start();
		}
		else
		{
			showError( "网络不可用，请打开网络" );
		}
	}
	
	private void doAdd()
	{
		final String urlString = Information.Server_Url + "/api/answer";
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
			json.put( "questionId", questionId );
			json.put( "answer", info );
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
				if( jsonObject.getInt( "result" ) == 4000 )
				{
					time = jsonObject.getString( "modifyTime" );					
					answerId = jsonObject.getString( "id" );
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
	
	private void showExitDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder( this );
        dialog.setTitle( "编辑回答" ).setMessage( "确定要放弃编辑吗？" )
        .setPositiveButton( "确定", new DialogInterface.OnClickListener() 
        {
        	public void onClick( DialogInterface dialog, int which ) 
        	{
        		finish();
        	}
        }).setNegativeButton( "取消", new DialogInterface.OnClickListener() 
        {
        	public void onClick( DialogInterface dialog, int which ) 
        	{
        		dialog.cancel();
        	}
        }).create().show();
	}
	
	private void addAnswerSuccess()
	{
		AnswerEntity entity = new AnswerEntity();
		entity.setAnswerInfo( info );
		entity.setCommentList( new ArrayList< CommentEntity >() );
		entity.setCompany( Information.Company );
		entity.setId( answerId );
		entity.setInvite( new ArrayList< String >() );
		entity.setJob( Information.Job );
		entity.setName( Information.Name );
		entity.setPart( Information.Part );
		entity.setPku( Information.PKU_Value );
		entity.setPraise( new ArrayList< String >() );
		entity.setPraiseCount( 0 );
		entity.setQuestionId( questionId );
		entity.setSex( Information.Sex );
		entity.setCreateTime( time );
		entity.setModifyTime( "" );
		entity.setUserHeadUrl( Information.HeadUrl );
		entity.setUserId( Information.Id );
		entity.setInvisible( box.isChecked() );
		entity.setHasPraised( false );
		
		if( entity.isInvisible() )
		{
			entity.setUserId( "" );
		}
		
		MyDatabaseHelper.getInstance( this ).updateQuestion1( QuestionInfoActivity.entity.getId(), time );
		MyDatabaseHelper.getInstance( this ).updateQuestion2( QuestionInfoActivity.entity.getId(), time );
		
		Intent intent = getIntent();
		intent.putExtra( "answer", entity );
		setResult( 2, intent );
		finish();
	}
	
	private void editAnswerSuccess()
	{
		AnswerInfoActivity.entity.setAnswerInfo( info );
		AnswerInfoActivity.entity.setModifyTime( "" );
		AnswerInfoActivity.entity.setEditTime( time );
		
		MyDatabaseHelper.getInstance( this ).updateMyAnswer( AnswerInfoActivity.entity.getId(), time );
		MyDatabaseHelper.getInstance( this ).updateQuestion2( AnswerInfoActivity.entity.getQuestionId(), time );
		
		Intent intent = getIntent();
		setResult( 2, intent );
		finish();
	}
	
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
		{
			if( type == 0 )
			{
				finish();
			}
			else
			{
				showExitDialog();
			}
			return true;
		}
		return false;
	}
	
	private void showError( String text )
	{
		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();
	}
	
	private void back()
	{
		if( type == 0 )
		{
			finish();
		}
		else
		{
			showExitDialog();
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