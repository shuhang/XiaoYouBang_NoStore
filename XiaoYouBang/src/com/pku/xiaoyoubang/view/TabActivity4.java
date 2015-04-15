package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.Window;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.AnswerEntity;import com.pku.xiaoyoubang.entity.QuestionEntity;import com.pku.xiaoyoubang.entity.ReplyEntity;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.model.ReplyListAdapter;import com.pku.xiaoyoubang.selfview.XListView;import com.pku.xiaoyoubang.selfview.XListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;import com.umeng.update.UmengDialogButtonListener;import com.umeng.update.UmengUpdateAgent;import com.umeng.update.UmengUpdateListener;import com.umeng.update.UpdateResponse;import com.umeng.update.UpdateStatus;@SuppressLint("HandlerLeak")public class TabActivity4 extends Activity implements IXListViewListener{	private XListView replyListView;	private ReplyListAdapter adapter;		private int scrollPosition = 0;	private int scrollTop = 0;		private Dialog dialog;		private HttpURLConnection connection = null;		private boolean force = false;	private boolean isFirst = true;	private boolean firstRefresh = true;		private String saveQuestionId;	private String saveQuestionTitle;	/**	 *  	 */	private Handler handler;	public static boolean shouldUpdate = false;	protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //Refresh success					storeRefreshTime();										refreshSuccess( ( String ) message.obj );					replyListView.stopRefresh();					if( firstRefresh )					{						firstRefresh = false;						replyListView.updateHeaderHeight( -120 );					}					replyListView.setPullLoadEnable( true );					replyListView.stopLoadMore();					break;				case 1 : //Load more sucess					loadMoreSuccess( ( String ) message.obj );										replyListView.setSelectionFromTop( scrollPosition, scrollTop );					replyListView.stopLoadMore();					break;				case 2 : //load info success					if( dialog != null )					{						dialog.dismiss();					}										break;				case 3 : //get list failed					showError( "加载失败" );					replyListView.stopRefresh();					if( firstRefresh )					{						firstRefresh = false;						replyListView.updateHeaderHeight( -120 );					}					replyListView.setPullLoadEnable( true );					break;				case 4 : //No net - refresh					replyListView.stopRefresh();					if( firstRefresh )					{						firstRefresh = false;						replyListView.updateHeaderHeight( -120 );					}					replyListView.setPullLoadEnable( true );					showError( "网络不可用，请打开网络" );					break;				case 5 : //No net - laod more					replyListView.stopLoadMore();					showError( "网络不可用，请打开网络" );					break;				case 6 : //get info failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "加载失败" );					break;				case 7 : //load more failed					showError( "加载失败" );					replyListView.setSelectionFromTop( scrollPosition, scrollTop );					replyListView.stopLoadMore();					break;				case 8 : //get answer success					if( dialog != null )					{						dialog.dismiss();					}					showAnswer( ( String ) message.obj );					break;				case 9 : //get answer failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "加载失败" );					break;				case 10 : //load info success					if( dialog != null )					{						dialog.dismiss();					}									try 					{						QuestionEntity questionEntity = new QuestionEntity();						Tool.loadQuestionInfoEntity( questionEntity, ( JSONObject ) message.obj );						showToQuestionInfo( questionEntity );					}					catch( Exception ex ) 					{						ex.printStackTrace();					}					break;				case 11 : //get info failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "加载失败" );					break;				case 12 : //get user success					if( dialog != null )					{						dialog.dismiss();					}					showUser( ( UserEntity ) message.obj, 0 );					break;				case 13 : //get user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取用户资料失败" );					break;				case 14 : //get question type success					if( dialog != null )					{						dialog.dismiss();					}					showCommentList( message.arg1 );					break;				case 15 : //get question type failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取评论失败" );					break;				}			}		};				initView();				Information.IsFirst = false;	}		private void initView()	{		setContentView( R.layout.tab_4 );		replyListView = ( XListView ) findViewById( R.id.tab_4_list );				replyListView.setPullLoadEnable( false );		replyListView.setXListViewListener( this );		adapter = new ReplyListAdapter( this, Tool.loadReplyListFromFile( this ) );		replyListView.setAdapter( adapter );		replyListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showRelpyInfo( position - 1 );				}			}		);				SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		replyListView.setRefreshTime( shared.getString( "reply_update_time", "" ) );				replyListView.updateHeaderHeight( 120 );		replyListView.startRefresh();	}		private void showAnswer( String jsonString )	{		Intent intent = new Intent( this, AnswerInfoActivity.class );		intent.putExtra( "type", 4 );		AnswerEntity answer = new AnswerEntity();		try 		{			Tool.loadAnswerEntity( answer, new JSONObject( jsonString ) );		}		catch( Exception e ) {}				intent.putExtra( "answer", answer );		startActivity( intent );	}		private void showToQuestionInfo( QuestionEntity questionEntity )	{		Intent intent = new Intent( this, QuestionInfoActivity.class );		intent.putExtra( "type", 3 );		intent.putExtra( "question", questionEntity );		startActivity( intent );	}		private void showRelpyInfo( final int position )	{		ReplyEntity entity = adapter.getItem( position );		if( entity.isInvite() || entity.isActInvite() )		{			if( Tool.isNetworkConnected( this ) == true )			{				dialog = new Dialog( this, R.style.dialog_progress );				LayoutInflater inflater = LayoutInflater.from( this );  				View view = inflater.inflate( R.layout.dialog_progress, null );				TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );				if( entity.isInvite() )				{					textView.setText( "正在加载问题详情" );				}				else if( entity.isActInvite() )				{					textView.setText( "正在加载活动详情" );				}				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();				layoutParams.alpha = 0.8f;				dialog.getWindow().setAttributes( layoutParams );				dialog.setContentView( view );				dialog.setCancelable( false );				dialog.setOnKeyListener				(					new OnKeyListener()					{						public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 						{							if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )							{								if( connection != null )								{									connection.disconnect();								}								dialog.dismiss();								return true;							}							return false;						}					}				);				dialog.show();								new Thread		    	(		    		new Thread()		    		{		    			public void run()		    			{		    				loadQuestionInfoFromNet( adapter.getItem( position ).getQuestionId() );		    			}		    		}		    	).start();			}			else			{				Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();			}		}		else if( entity.isInviteLogin() )		{			getUser( entity.getInviteUserId() );		}		else		{			if( entity.getType() == 0 )			{				saveQuestionId = entity.getQuestionId();				saveQuestionTitle = entity.getTitle();				if( Tool.isNetworkConnected( this ) == true )				{					dialog = new Dialog( this, R.style.dialog_progress );					LayoutInflater inflater = LayoutInflater.from( this );  					View view = inflater.inflate( R.layout.dialog_progress, null );					TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );					textView.setText( "正在加载" );										WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();					layoutParams.alpha = 0.8f;					dialog.getWindow().setAttributes( layoutParams );					dialog.setContentView( view );					dialog.setCancelable( false );					dialog.setOnKeyListener					(						new OnKeyListener()						{							public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 							{								if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )								{									if( connection != null )									{										connection.disconnect();									}									dialog.dismiss();									return true;								}								return false;							}						}					);					dialog.show();										new Thread			    	(			    		new Thread()			    		{			    			public void run()			    			{			    				loadQuestionType();			    			}			    		}			    	).start();				}				else				{					Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();				}			}			else if( entity.getType() == 5 || entity.getType() == 6 )			{				Intent intent = new Intent( this, CommentListActivity.class );				intent.putExtra( "type", 4 );				if( entity.getType() == 5 )				{					intent.putExtra( "userId", Information.Id );					intent.putExtra( "userName", "我" );				}				else				{					intent.putExtra( "userId", entity.getTitleUserName() );					intent.putExtra( "userName", entity.getCommentUserName() );				}				startActivity( intent );			}			else			{				if( Tool.isNetworkConnected( this ) == true )				{					dialog = new Dialog( this, R.style.dialog_progress );					LayoutInflater inflater = LayoutInflater.from( this );  					View view = inflater.inflate( R.layout.dialog_progress, null );					TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );					textView.setText( "正在加载" );										WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();					layoutParams.alpha = 0.8f;					dialog.getWindow().setAttributes( layoutParams );					dialog.setContentView( view );					dialog.setCancelable( false );					dialog.setOnKeyListener					(						new OnKeyListener()						{							public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 							{								if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )								{									if( connection != null )									{										connection.disconnect();									}									dialog.dismiss();									return true;								}								return false;							}						}					);					dialog.show();										new Thread			    	(			    		new Thread()			    		{			    			public void run()			    			{			    				loadAnswerInfo( adapter.getItem( position ).getAnswerId() );			    			}			    		}			    	).start();				}				else				{					Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();				}			}		}	}		private void showCommentList( final int questionType )	{		Intent intent = new Intent( this, CommentListActivity.class );		intent.putExtra( "type", 1 );		intent.putExtra( "questionId", saveQuestionId );		intent.putExtra( "questionTitle", saveQuestionTitle );		intent.putExtra( "questionType", questionType );		startActivity( intent );	}		private void getUser( final String id )	{		UserEntity user = null;		if( id.equals( Information.Id ) )		{			startActivity( new Intent( this, MyInfoActivity.class ) );			return;		}		else		{			user = MyDatabaseHelper.getInstance( this ).getFriend( id );		}		if( user == null )		{			if( Tool.isNetworkConnected( this ) == true )			{				dialog = new Dialog( this, R.style.dialog_progress );				LayoutInflater inflater = LayoutInflater.from( this );  				View view = inflater.inflate( R.layout.dialog_progress, null );				TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );				textView.setText( "正在获取用户资料" );								WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();				layoutParams.alpha = 0.8f;				dialog.getWindow().setAttributes( layoutParams );				dialog.setContentView( view );				dialog.setCancelable( false );				dialog.setOnKeyListener				(					new OnKeyListener()					{						public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 						{							if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )							{								if( connection != null )								{									connection.disconnect();								}								dialog.dismiss();								return true;							}							return false;						}					}				);				dialog.show();								new Thread		    	(		    		new Thread()		    		{		    			public void run()		    			{		    				doLoadInformation( id );		    			}		    		}		    	).start();			}			else			{				Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();			}		}		else		{			showUser( user, 1 );		}	}		private void doLoadInformation( final String id )	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + id + "?token=" + Information.Token;			JSONObject jsonObject = Tool.doGetWithUrl( urlString );			if( jsonObject == null )			{				handler.sendEmptyMessage( 13 );			}			else			{				UserEntity user = new UserEntity();				user.setId( id );				user.setName( jsonObject.getString( "name" ) );				user.setHeadUrl( jsonObject.getString( "headUrl" ) );				user.setSex( jsonObject.getInt( "sex" ) );				user.setBirthday( jsonObject.getString( "birthyear" ) );				user.setPku( jsonObject.getString( "pku" ) );				user.setNowHome( jsonObject.getString( "base" ) );				user.setOldHome( jsonObject.getString( "hometown" ) );				user.setQq( jsonObject.getString( "qq" ) );				user.setJob1( jsonObject.getString( "company" ) );				user.setJob2( jsonObject.getString( "department" ) );				user.setJob3( jsonObject.getString( "job" ) );				user.setVersion( jsonObject.getInt( "version" ) );				user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				user.setAnswerCount( jsonObject.getInt( "answerCount" ) );				user.setQuestionCount( jsonObject.getInt( "questionCount" ) );				user.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				user.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				user.setIntro( jsonObject.getString( "intro" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				user.setTag( temp );				if( jsonObject.has( "invitedBy" ) )				{					user.setInviteName( jsonObject.getJSONObject( "invitedBy" ).getString( "name" ) );					user.setInviteHeadUrl( jsonObject.getJSONObject( "invitedBy" ).getString( "headUrl" ) );					user.setInviteUserId( jsonObject.getJSONObject( "invitedBy" ).getString( "id" ) );				}				else				{					user.setInviteName( "元老" );					user.setInviteHeadUrl( "" );					user.setInviteUserId( "" );				}								Message message = handler.obtainMessage();				message.what = 12;				message.obj = user;				handler.sendMessage( message );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 13 );		}	}		private void showUser( UserEntity user, int type )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();		bundle.putInt( "type", type );		bundle.putSerializable( "user", user );		intent.putExtras( bundle );		startActivity( intent );	}	/**	 *  Load question info from net	 */	private void loadQuestionInfoFromNet( String questionId )	{		try		{			final String urlString = Information.Server_Url + "/api/question/" + questionId + "?token=" + Information.Token;			JSONObject result = Tool.doGetWithUrl( urlString );			if( result == null )			{				handler.sendEmptyMessage( 11 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 10;					message.obj = result;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 11 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 3 );		}	}		private void loadQuestionType()	{		try		{			final String urlString = Information.Server_Url + "/api/question/" + saveQuestionId + "/type?token=" + Information.Token;			JSONObject result = Tool.doGetWithUrl( urlString );			if( result == null )			{				handler.sendEmptyMessage( 15 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 14;					message.arg1 = result.getInt( "questionType" );					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 15 );				}			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 15 );		}	}		private void loadAnswerInfo( String answerId )	{		HttpURLConnection connection = null;		final String urlString = Information.Server_Url + "/api/answer/" + answerId + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 4000 )				{					Message message = handler.obtainMessage();					message.what = 8;					message.obj = jsonObject.toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 9 );				}			}			else			{				handler.sendEmptyMessage( 9 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 9 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private List< ReplyEntity > getReplyList( String jsonString )	{		List< ReplyEntity > list = new ArrayList< ReplyEntity >();		try		{			JSONArray array = new JSONArray( jsonString );	        final int count = array.length();	        	        for( int i = 0; i < count; i ++ )	        {	        	JSONObject object = array.getJSONObject( i );	        		        	ReplyEntity entity = new ReplyEntity();				entity.setHeadUrl( object.getString( "srcUserHeadUrl" ) );								entity.setReplyUserId( object.getString( "user" ) );				entity.setReplyUserName( object.getString( "srcUserName" ) );				entity.setTime( object.getString( "createTime" ) );								JSONObject message = object.getJSONObject( "msg" );								if( object.getInt( "type" ) == 1 )				{					entity.setInfo( message.getString( "content" ) );					entity.setTitle( message.getString( "title" ) );					entity.setTitleUserName( message.getString( "username" ) );										if( message.has( "qid" ) )					{						entity.setType( 0 );						entity.setQuestionId( message.getString( "qid" ) );					}					else					{						entity.setType( 1 );						entity.setAnswerId( message.getString( "aid" ) );					}				}				else if( object.getInt( "type" ) == 2 )				{					entity.setInvite( true );					entity.setInfo( message.getString( "words" ) );					entity.setTitle( message.getString( "title" ) );					entity.setTitleUserName( message.getString( "questionUser" ) );										entity.setType( 0 );					entity.setQuestionId( message.getString( "qid" ) );				}				else if( object.getInt( "type" ) == 3 )				{					entity.setInviteLogin( true );					entity.setInviteUserId( object.getString( "srcUser" ) );					entity.setInfo( message.getString( "message" ) );				}				else if( object.getInt( "type" ) == 4 )				{					entity.setQuestionType( 1 );					entity.setActInvite( true );					entity.setInfo( message.getString( "words" ) );					entity.setTitle( message.getString( "title" ) );					entity.setTitleUserName( message.getString( "questionUser" ) );										entity.setType( 0 );					entity.setQuestionId( message.getString( "qid" ) );				}				else if( object.getInt( "type" ) == 5 )				{					entity.setType( 5 );					entity.setInfo( message.getString( "content" ) );					entity.setTitleUserName( message.getString( "user" ) );				}				else if( object.getInt( "type" ) == 6 )				{					entity.setType( 6 );					entity.setInfo( message.getString( "content" ) );					entity.setTitleUserName( message.getString( "user" ) );					entity.setCommentUserName( message.getString( "from" ) );				}								list.add( entity );	        }	        	        return list;		}		catch( Exception ex ) 		{			ex.printStackTrace();			return list;		}	}		private void refreshSuccess( String jsonString )	{	    adapter = new ReplyListAdapter( this, getReplyList( jsonString ) );		replyListView.setAdapter( adapter );	}		private void loadMoreSuccess( String jsonString )	{		try		{	        List< ReplyEntity > tempList = new ArrayList< ReplyEntity >( adapter.getList() );	        tempList.addAll( getReplyList( jsonString ) );	        adapter = new ReplyListAdapter( this, tempList );			replyListView.setAdapter( adapter );		}		catch( Exception ex ) {}	}		/**	 * 	 */	private void storeRefreshTime()	{		shouldUpdate = false;				SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );				replyListView.setRefreshTime( shared.getString( "reply_update_time", "" ) );	}		public void onResume() 	{		super.onResume();				MobclickAgent.onResume( this );				if( !isFirst )		{			if( shouldUpdate )			{				replyListView.updateHeaderHeight( 120 );				replyListView.startRefresh();			}		}		else		{			isFirst = false;		}				UmengUpdateAgent.setUpdateAutoPopup( false );		UmengUpdateAgent.setUpdateListener 		(			new UmengUpdateListener() 			{			    public void onUpdateReturned( int updateStatus, UpdateResponse updateInfo ) 			    {			        switch( updateStatus ) 			        {			        case UpdateStatus.Yes:			        	SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );			        	if( shared.getBoolean( "force_update", true ) == true )			        	{			        		if( updateInfo.version.indexOf( "F" ) != -1 )				        	{				        		force = true;				        	}				            UmengUpdateAgent.showUpdateDialog( TabActivity4.this, updateInfo );			        	}			            break;			        }			    }		});		UmengUpdateAgent.update( this );		UmengUpdateAgent.setDialogListener		(			new UmengDialogButtonListener() 			{			    public void onClick( int status ) 			    {			        switch( status ) 			        {			        case UpdateStatus.Update:			            			            break;			        case UpdateStatus.NotNow:			            if( force == true )			            {			            	MyApplication.getInstance().logout();			            }			            else			            {			            	SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );			        		SharedPreferences.Editor editor = shared.edit();			        		editor.putBoolean( "force_update", false );			        		editor.commit();			            }			            break;			        }			    }			}		);	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			Intent intent= new Intent( Intent.ACTION_MAIN );  			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );  			intent.addCategory( Intent.CATEGORY_HOME );			startActivity( intent );  			return true;		}		return false;	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );	}	public void onRefresh() 	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						loadNewReplyListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 4 );		}	}		private void loadNewReplyListFromNet()	{		HttpURLConnection connection = null;		final String urlString = Information.Server_Url + "/api/mq?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );			JSONObject json = new JSONObject();			json.put( "updateTime", "" );			connection.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 8000 )				{					Tool.writeReplyListToFile( jsonObject.getJSONArray( "data" ).toString() );										SharedPreferences.Editor editor = shared.edit();					editor.putString( "reply_update_time", jsonObject.getString( "updateTime" ) );					editor.putString( "tab4_update_count", jsonObject.getString( "updateTime" ) );					editor.commit();										Message message = handler.obtainMessage();					message.what = 0;					message.obj = jsonObject.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	public void onLoadMore() 	{		if( Tool.isNetworkConnected( this ) == true )		{			scrollPosition = replyListView.getFirstVisiblePosition();			View v = replyListView.getChildAt( 0 );	        scrollTop = ( v == null ) ? 0 : v.getTop();			new Thread			(				new Thread()				{					public void run()					{						loadMoreReplyListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 5 );		}	}		private void loadMoreReplyListFromNet()	{		HttpURLConnection connection = null;		final String urlString = Information.Server_Url + "/api/mq?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();				if( adapter.getCount() > 0 )			{				json.put( "before", adapter.getItem( adapter.getCount() - 1 ).getTime() );			}			else			{				json.put( "before", "" );			}			connection.getOutputStream().write( json.toString().getBytes() );						final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 8000 )				{					if( adapter.getCount() == 0 )					{						SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );						SharedPreferences.Editor editor = shared.edit();						editor.putString( "reply_update_time", jsonObject.getString( "updateTime" ) );						editor.putString( "tab4_update_count", jsonObject.getString( "updateTime" ) );						editor.commit();												Message message = handler.obtainMessage();						message.what = 0;						message.obj = jsonObject.getJSONArray( "data" ).toString();						handler.sendMessage( message );					}					else					{						Message message = handler.obtainMessage();						message.what = 1;						message.obj = jsonObject.getJSONArray( "data" ).toString();						handler.sendMessage( message );					}				}				else				{					handler.sendEmptyMessage( 7 );				}			}			else			{				handler.sendEmptyMessage( 7 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 7 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		public void backTo() {}	public void showHead() {}	public void showMe() {}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}}