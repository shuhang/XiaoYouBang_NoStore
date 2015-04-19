package com.pku.xiaoyoubang.view;import java.net.HttpURLConnection;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.AlertDialog;import android.app.Dialog;import android.content.DialogInterface;import android.content.SharedPreferences;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.Button;import android.widget.EditText;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.CommentEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class AddCommentActivity extends Activity{	private Button buttonBack;	private Button buttonFinish;		private EditText textInput;	private TextView textTitle;		private String commentTime;	private String commentId;	private String commentInfo;	/**	 *  0 : question	 *  1 : answer	 *  2 : comment	 *  3 :	 *  4 : add act	 *  5 : edit act	 *  6 : add user comment	 *  7 : edit leave word	 */	private int type;	private String questionId;	private String answerId;	private String replyId = "";	private String replyName = "";		private CommentEntity myAct;		/**	 *  type : 6	 */	private String userId;	private String userName;	/**	 *  type : 7	 */	private String leaveWord;		private Dialog dialog;	private HttpURLConnection connection = null;		private Handler handler;		public static int noticeCount1 = 0; //question_comment	public static int noticeCount2 = 0; //answer_comment	public static int noticeCount3 = 0; //reply_comment		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				type = getIntent().getIntExtra( "type", 0 );		if( type == 0 )		{			questionId = getIntent().getStringExtra( "questionId" );		}		else if( type == 1 )		{			answerId = getIntent().getStringExtra( "answerId" );			questionId = getIntent().getStringExtra( "questionId" );		}		else if( type == 2 )		{			replyId = getIntent().getStringExtra( "replyId" );			replyName = getIntent().getStringExtra( "name" );			questionId = getIntent().getStringExtra( "questionId" );		}		else if( type == 3 )		{			replyId = getIntent().getStringExtra( "replyId" );			replyName = getIntent().getStringExtra( "name" );			answerId = getIntent().getStringExtra( "answerId" );			questionId = getIntent().getStringExtra( "questionId" );		}		else if( type == 4 )		{			questionId = getIntent().getStringExtra( "questionId" );		}		else if( type == 5 )		{			questionId = getIntent().getStringExtra( "questionId" );			myAct = ( CommentEntity ) getIntent().getSerializableExtra( "myAct" );		}		else if( type == 6 )		{			userId = getIntent().getStringExtra( "userId" );			userName = getIntent().getStringExtra( "userName" );			replyId = getIntent().getStringExtra( "replyId" );			replyName = getIntent().getStringExtra( "replyName" );		}		else if( type == 7 )		{			leaveWord = getIntent().getStringExtra( "leaveWord" );		}				handler = new Handler()		{			public void handleMessage( Message message )			{					if( dialog != null )				{					dialog.dismiss();				}				switch( message.what )				{				case 0 : //add success					addSuccess();					break;				case 1 : //add comment failed										showError( "添加评论失败" );					Tool.setHttpTag( buttonFinish.getId(), false );					break;				case 2 : //add act failed					showError( "添加活动报名失败" );					Tool.setHttpTag( buttonFinish.getId(), false );					break;				case 3 : //edit act failed					showError( "修改活动报名失败" );					Tool.setHttpTag( buttonFinish.getId(), false );					break;				case 4 : //add user comment failed					showError( "发布失败" );					Tool.setHttpTag( buttonFinish.getId(), false );					break;				}			}		};				initView();	}		private void initView()	{		setContentView( R.layout.add_comment );				buttonBack = ( Button ) findViewById( R.id.add_comment_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					back();				}			}		);				buttonFinish = ( Button ) findViewById( R.id.add_comment_button_finish );		Tool.setHttpTag( buttonFinish.getId(), false );		buttonFinish.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					if( Tool.getHttpTag( buttonFinish.getId() ) )					{						return;					}					Tool.setHttpTag( buttonFinish.getId(), true );					judgeInput();				}			}		);				textTitle = ( TextView ) findViewById( R.id.add_comment_title );		textInput = ( EditText ) findViewById( R.id.add_comment_input );		if( type == 0 || type == 1 )		{			textInput.setHint( "请写下你的评论…" );		}		else if( type == 2 || type == 3 )		{			textInput.setHint( "回复" + replyName + "的评论…" );		}		else if( type == 4 || type == 5 )		{			textTitle.setText( "活动报名" );			if( type == 4 )			{				textInput.setHint( "请写下你的报名人数或者说明…" );			}			else			{				textInput.setText( myAct.getCommentInfo() );			}		}		else if( type == 6 )		{			textTitle.setText( "留言板" );			if( replyId.equals( "" ) )			{				textInput.setHint( "请写下你给" + userName + "的留言…" );			}			else			{				textInput.setHint( "回复" + replyName + "的留言…" );			}		}		else if( type == 7 )		{			textTitle.setText( "主人寄语" );			textInput.setText( leaveWord );			textInput.setHint( "请在你的留言板上，跟大家说句话吧" );		}	}		private void addSuccess()	{		if( type == 7 )		{			Intent intent = getIntent();			intent.putExtra( "leaveWord", commentInfo );			setResult( 12, intent );			finish();		}		else		{			CommentEntity entity = new CommentEntity();			entity.setId( commentId );			entity.setTime( commentTime );			entity.setCommentInfo( commentInfo );			entity.setUserHeadUrl( Information.HeadUrl );			entity.setUserId( Information.Id );			entity.setUserName( Information.Name );			entity.setQuestionId( questionId );			entity.setReplyId( "" );			entity.setReplyName( "" );			if( type == 1 )			{				entity.setAnswerId( answerId );			}			else if( type == 2 )			{				entity.setReplyName( replyName );			}			else if( type == 3 )			{				entity.setReplyName( replyName );				entity.setAnswerId( answerId );			}			else if( type == 6 )			{				entity.setReplyName( replyName );			}						if( type == 4 || type == 5 )			{				entity.setType( 1 );								if( type == 4 )				{					SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );					SharedPreferences.Editor editor = shared.edit();					editor.putString( "all_act_time", commentTime );					editor.commit();				}			}			else			{				entity.setType( 0 );			}						Intent intent = getIntent();			intent.putExtra( "comment", entity );			if( type == 5 )			{				myAct.setCommentInfo( commentInfo );				intent.putExtra( "myAct", myAct );				setResult( 11, intent );			}			else			{				setResult( 3, intent );			}			finish();		}	}		private void judgeInput()	{		commentInfo = textInput.getText().toString();		if( commentInfo.equals( "" ) )		{			if( type == 4 || type == 5 )			{				showError( "报名内容不能为空" );			}			else if( type == 7 )			{				showError( "寄语不能为空" );			}			else			{				showError( "评论不能为空" );			}			return;		}				startAdd();	}		private void startAdd()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			if( type == 4 )			{				textView.setText( "正在添加报名" );			}			else if( type == 5 )			{				textView.setText( "正在修改报名" );			}			else if( type == 6 )			{				textView.setText( "正在添加留言" );			}			else if( type == 7 )			{				textView.setText( "正在修改寄语" );			}			else			{				textView.setText( "正在添加评论" );			}						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				if( type == 0 || type == 2 )	    				{	    					doAddToQuestion( replyId );	    				}	    				else if( type == 1 || type == 3 )	    				{	    					doAddToAnswer( replyId );	    				}	    				else if( type == 4 )	    				{	    					doAddAct();	    				}	    				else if( type == 5 )	    				{	    					doEditAct();	    				}	    				else if( type == 6 )	    				{	    					doAddUserComment();	    				}	    				else if( type == 7 )	    				{	    					doEditLeaveWord();	    				}	    			}	    		}	    	).start();		}		else		{			showError( "网络不可用，请打开网络" );		}	}		private void doAddToQuestion( final String replyId )	{		try		{			final String urlString = Information.Server_Url + "/api/question/" + questionId + "/comment";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			json.put( "commentType", 0 );			if( !replyId.equals( "" ) )			{				json.put( "replyId", replyId );			}						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 1 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					commentTime = result.getString( "time" );					commentId = result.getString( "id" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 1 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 1 );		}	}		private void doAddToAnswer( final String replyId )	{		try		{			final String urlString = Information.Server_Url + "/api/answer/" + answerId + "/comment";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			json.put( "commentType", 0 );			if( !replyId.equals( "" ) )			{				json.put( "replyId", replyId );			}						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 1 );			}			else			{				if( result.getInt( "result" ) == 4000 )				{					commentTime = result.getString( "time" );					commentId = result.getString( "id" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 1 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 1 );		}	}		private void doAddAct()	{		try		{			final String urlString = Information.Server_Url + "/api/question/" + questionId + "/comment";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			json.put( "commentType", 1 );						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 2 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					commentTime = result.getString( "time" );					commentId = result.getString( "id" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 2 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 2 );		}	}		private void doAddUserComment()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + userId + "/comment";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			if( !replyId.equals( "" ) )			{				json.put( "replyId", replyId );			}						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 4 );			}			else			{				if( result.getInt( "result" ) == 4000 )				{					commentTime = result.getString( "time" );					commentId = result.getString( "id" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 4 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 4 );		}	}		private void doEditAct()	{		try		{			final String urlString = Information.Server_Url + "/api/question/" + questionId + "/comment/edit";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "content", commentInfo );			json.put( "commentId", myAct.getId() );			json.put( "commentType", 0 );			JSONObject result = Tool.doPutWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 3 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 3 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 3 );		}	}		private void doEditLeaveWord()	{		try		{			final String urlString = Information.Server_Url + "/api/user/leaveword";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "leaveWord", commentInfo );			JSONObject result = Tool.doPutWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 4 );			}			else			{				if( result.getInt( "result" ) == 1000 )				{					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 4 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 4 );		}	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			back();			return true;		}		return false;	}		private void back()	{		if( textInput.getText().toString().equals( "" ) )		{			setResult( 0 );    		finish();		}		else		{			AlertDialog.Builder dialog = new AlertDialog.Builder( this );	        dialog.setTitle( "返回提示" ).setMessage( "确定返回吗？" )	        .setPositiveButton( "确定", new DialogInterface.OnClickListener() 	        {	        	public void onClick( DialogInterface dialog, int which ) 	        	{	        		setResult( 0 );	        		finish();	        	}	        }).setNegativeButton( "取消", new DialogInterface.OnClickListener() 	        {	        	public void onClick( DialogInterface dialog, int which ) 	        	{	        		dialog.cancel();	        	}	        }).create().show();		}	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}}