package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.Button;import android.widget.EditText;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.CommentEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class AddCommentActivity extends Activity{	private Button buttonBack;	private Button buttonFinish;		private EditText textInput;		private String commentTime;	private String commentId;	private String commentInfo;	/**	 *  0 : question	 *  1 : answer	 *  2 : comment	 */	private int type;	private String questionId;	private String answerId;	private String replyId = "";	private String replyName = "";		private Dialog dialog;	private HttpURLConnection connection = null;		private Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				type = getIntent().getIntExtra( "type", 0 );		if( type == 0 )		{			questionId = getIntent().getStringExtra( "id" );		}		else if( type == 1 )		{			answerId = getIntent().getStringExtra( "id" );			questionId = getIntent().getStringExtra( "questionId" );		}		else if( type == 2 )		{			replyId = getIntent().getStringExtra( "replyId" );			replyName = getIntent().getStringExtra( "name" );			questionId = QuestionInfoActivity.entity.getId();		}		else if( type == 3 )		{			replyId = getIntent().getStringExtra( "replyId" );			replyName = getIntent().getStringExtra( "name" );			answerId = AnswerInfoActivity.entity.getId();			questionId = getIntent().getStringExtra( "id" );		}				handler = new Handler()		{			public void handleMessage( Message message )			{					if( dialog != null )				{					dialog.dismiss();				}				switch( message.what )				{				case 0 : //add success					addSuccess();					break;				case 1 : //add failed										showError( "添加评论失败" );					break;				}			}		};				initView();	}		private void initView()	{		setContentView( R.layout.add_comment );				buttonBack = ( Button ) findViewById( R.id.add_comment_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					back();				}			}		);				buttonFinish = ( Button ) findViewById( R.id.add_comment_button_finish );		buttonFinish.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					judgeInput();				}			}		);				textInput = ( EditText ) findViewById( R.id.add_comment_input );		if( type == 0 || type == 1 )		{			textInput.setHint( "请写下你的评论…" );		}		else		{			textInput.setHint( "回复" + replyName + "的评论…" );		}	}		private void addSuccess()	{		CommentEntity entity = new CommentEntity();		entity.setId( commentId );		entity.setTime( commentTime );		entity.setCommentInfo( commentInfo );		entity.setUserHeadUrl( Information.HeadUrl );		entity.setUserId( Information.Id );		entity.setUserName( Information.Name );		entity.setQuestionId( questionId );		entity.setReplyId( "" );		entity.setReplyName( "" );		if( type == 1 )		{			entity.setAnswerId( answerId );		}		else if( type == 2 )		{			entity.setReplyName( replyName );		}		else if( type == 3 )		{			entity.setReplyName( replyName );			entity.setAnswerId( answerId );		}				Intent intent = getIntent();		intent.putExtra( "comment", entity );		setResult( 3, intent );		finish();	}		private void judgeInput()	{		commentInfo = textInput.getText().toString();		if( commentInfo.equals( "" ) )		{			showError( "评论不能为空" );			return;		}				startAdd();	}		private void startAdd()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			textView.setText( "正在添加评论" );						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				if( type == 0 || type == 2 )	    				{	    					doAddToQuestion( replyId );	    				}	    				else	    				{	    					doAddToAnswer( replyId );	    				}	    			}	    		}	    	).start();		}		else		{			showError( "网络不可用，请打开网络" );		}	}		private void doAddToQuestion( final String replyId )	{		final String urlString = Information.Server_Url + "/api/question/" + questionId + "/comment";		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			if( !replyId.equals( "" ) )			{				json.put( "replyId", replyId );			}			connection.getOutputStream().write( json.toString().getBytes() );						final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					commentTime = jsonObject.getString( "time" );					commentId = jsonObject.getString( "id" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 1 );				}			}			else			{				handler.sendEmptyMessage( 1 );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 1 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void doAddToAnswer( final String replyId )	{		final String urlString = Information.Server_Url + "/api/answer/" + answerId + "/comment";		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			if( !replyId.equals( "" ) )			{				json.put( "replyId", replyId );			}			connection.getOutputStream().write( json.toString().getBytes() );						final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 4000 )				{					commentTime = jsonObject.getString( "time" );					commentId = jsonObject.getString( "id" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 1 );				}			}			else			{				handler.sendEmptyMessage( 1 );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 1 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		private void back()	{		setResult( 0 );		finish();	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}}