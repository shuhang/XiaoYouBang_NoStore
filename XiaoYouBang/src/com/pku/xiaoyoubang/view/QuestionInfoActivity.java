package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.Context;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.text.ClipboardManager;import android.util.DisplayMetrics;import android.view.Gravity;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.Button;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.AnswerEntity;import com.pku.xiaoyoubang.entity.CommentEntity;import com.pku.xiaoyoubang.entity.InviteEntity;import com.pku.xiaoyoubang.entity.QuestionEntity;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.model.AnswerListAdapter;import com.pku.xiaoyoubang.selfview.AnswerListView;import com.pku.xiaoyoubang.selfview.AnswerListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressWarnings("deprecation")@SuppressLint("HandlerLeak")public class QuestionInfoActivity extends Activity implements IXListViewListener{	/**	 *  Main	 */	private Button buttonBack;	private TextView textTitle;	/** 	 *  List	 */	private AnswerListAdapter adapter;	private AnswerListView answerListView;		private int scrollPosition = 0;	private int scrollTop = 0;	private int type;	//private HashMap< String, Boolean > map = new HashMap< String, Boolean >();	/**	 *  Data	 */	public static QuestionEntity entity;		private Dialog dialog;	private HttpURLConnection connection = null;		private Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				type = getIntent().getIntExtra( "type", 0 );		if( type == 0 )		{			entity = TabActivity1.selectedEntity;		}		else if( type == 1 )		{			entity = TabActivity2.selectedEntity;			}		else if( type == 2 )		{			entity = QuestionListActivity.selectedEntity;		}		else if( type == 3 )		{			entity = ( QuestionEntity ) getIntent().getSerializableExtra( "question" );		}				handler = new Handler()		{			@SuppressWarnings("unchecked")			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //refresh success					if( message.arg1 == 1 )					{						refreshAnswerList();						storeRefreshTime();						answerListView.updateHeader();					}					answerListView.stopRefresh();					break;				case 1 : //load more success					if( message.arg1 == 1 )					{						refreshAnswerList();						answerListView.setSelectionFromTop( scrollPosition, scrollTop );					}					answerListView.stopLoadMore();					break;				case 3 : //get list failed					showError( "加载失败" );					answerListView.stopRefresh();					answerListView.updateHeaderHeight( -120 );					answerListView.setPullLoadEnable( true );					answerListView.stopLoadMore();					break;				case 4 : //No net - refresh					answerListView.stopRefresh();					answerListView.updateHeaderHeight( -120 );					answerListView.setPullLoadEnable( true );					showError( "网络不可用，请打开网络" );					break;				case 5 : //No net - laod more					answerListView.stopLoadMore();					showError( "网络不可用，请打开网络" );					break;				case 6 : //get user success					if( dialog != null )					{						dialog.dismiss();					}					showUser( ( UserEntity ) message.obj, 0 );					break;				case 7 : //get user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取用户资料失败" );					break;				case 8 : //get answer user success					if( dialog != null )					{						dialog.dismiss();					}					showInvite( ( ArrayList< String > ) message.obj );					break;				case 9 : //get answer user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取回答用户失败" );					break;				}			}		};				initView();	}		private void initView()	{		setContentView( R.layout.question_info );				buttonBack = ( Button ) findViewById( R.id.question_info_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					back();				}			}		);		textTitle = ( TextView ) findViewById( R.id.question_info_title );		if( entity.isInvisible() )		{			textTitle.setText( "匿名用户的提问" );		}		else		{			textTitle.setText( entity.getUserName() + "的提问" );		}		answerListView = ( AnswerListView ) findViewById( R.id.question_info_list );		answerListView.setPullLoadEnable( true );		answerListView.setXListViewListener( this );		answerListView.setRefreshTime( entity.getUpdateTime().substring( 0, 19 ) );		adapter = new AnswerListAdapter( this, entity.getAnswerList(), entity.getAnswerCount(), type );		answerListView.setAdapter( adapter );		answerListView.updateHeader();		answerListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					showAnswerInfo( position - 2 );				}			}		);	}		/**	 * 	 */	private void storeRefreshTime()	{		answerListView.setRefreshTime( entity.getUpdateTime().substring( 0, 19 ) );	}		private void loadAnswerList()	{		List< AnswerEntity > tempList = new ArrayList< AnswerEntity >( entity.getAnswerList() );		adapter = new AnswerListAdapter( QuestionInfoActivity.this, tempList, entity.getAnswerCount(), type );					answerListView.setAdapter( adapter );	}		private void refreshAnswerList()	{		loadAnswerList();		adapter.notifyDataSetChanged();	}		private void back()	{		MyDatabaseHelper.getInstance( this ).updateQuestion1( entity.getId(), entity.getModifyTime() );		MyDatabaseHelper.getInstance( this ).updateQuestion2( entity.getId(), entity.getUpdateTime() );		setResult( 3 );		finish();	}		public void addAnswer()	{		if( entity.isHasAnswered() )		{			Intent intent = new Intent( this, AnswerInfoActivity.class );			intent.putExtra( "type", 3 );			startActivityForResult( intent, 1003 );		}		else		{			Intent intent = new Intent( this, AddAnswerActivity.class );			intent.putExtra( "type", 0 );			intent.putExtra( "questionId", entity.getId() );			intent.putExtra( "questionTitle", entity.getQuestionTitle() );			startActivityForResult( intent, 1005 );		}	}		@SuppressWarnings({ "unchecked", "rawtypes" })	public void showInvite( List< String > user )	{		Intent intent = new Intent( this, InviteOtherActivity.class );		ArrayList list1 = new ArrayList();		Bundle bundle = new Bundle();		list1.add( user );		bundle.putParcelableArrayList( "list", list1 );		bundle.putString( "id", entity.getId() );		intent.putExtras(bundle);		startActivityForResult( intent, 1017 );	}		public void invite()	{		startLoadAnswerUser();	}		private void doLoadAnswerUser()	{		final String urlString = Information.Server_Url + "/api/question/" + entity.getId() + "/answerusers?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					JSONArray array = jsonObject.getJSONArray( "userids" );					final int count = array.length();					List< String > users = new ArrayList< String >( count );					for( int i = 0; i < count; i ++ )					{						users.add( array.getString( i ) );					}										Message message = handler.obtainMessage();					message.what = 8;					message.obj = users;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 9 );				}			}			else			{				handler.sendEmptyMessage( 9 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 9 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void startLoadAnswerUser()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			textView.setText( "正在获取回答用户列表" );						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				doLoadAnswerUser();	    			}	    		}	    	).start();		}		else		{			Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();		}	}		@SuppressWarnings({ "unchecked", "rawtypes" })	public void showInvite( boolean isInviteMe )	{		Intent intent = new Intent( this, InviteInfoActivity.class );		intent.putExtra( "isInviteMe", isInviteMe );		intent.putExtra( "hasAnswered", entity.isHasAnswered() );				ArrayList list1 = new ArrayList();			Bundle bundle = new Bundle();		if( isInviteMe )		{			list1.add( entity.getInviteMeList() );		}		else		{			list1.add( entity.getMyInviteList() );		}		bundle.putParcelableArrayList( "list", list1 );		intent.putExtras(bundle);		startActivity( intent );	}		public void showUserInfo()	{		if( entity.isInvisible() )		{			showError( "此乃匿名用户" );		}		else		{			UserEntity user = null;			if( entity.getUserId().equals( Information.Id ) )			{				startActivity( new Intent( this, MyInfoActivity.class ) );				return;			}			else			{				user = MyDatabaseHelper.getInstance( this ).getFriend( entity.getUserId() );			}			if( user == null )			{				if( Tool.isNetworkConnected( this ) == true )				{					dialog = new Dialog( this, R.style.dialog_progress );					LayoutInflater inflater = LayoutInflater.from( this );  					View view = inflater.inflate( R.layout.dialog_progress, null );					TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );					textView.setText( "正在获取用户资料" );										WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();					layoutParams.alpha = 0.8f;					dialog.getWindow().setAttributes( layoutParams );					dialog.setContentView( view );					dialog.setCancelable( false );					dialog.setOnKeyListener					(						new OnKeyListener()						{							public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 							{								if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )								{									if( connection != null )									{										connection.disconnect();									}									dialog.dismiss();									return true;								}								return false;							}						}					);					dialog.show();										new Thread			    	(			    		new Thread()			    		{			    			public void run()			    			{			    				doLoadInformation();			    			}			    		}			    	).start();				}				else				{					Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();				}			}			else			{				showUser( user, 1 );			}		}	}		private void doLoadInformation()	{		final String urlString = Information.Server_Url + "/api/user/" + entity.getUserId() + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				UserEntity user = new UserEntity();				user.setId( entity.getUserId() );				user.setName( jsonObject.getString( "name" ) );				user.setHeadUrl( jsonObject.getString( "headUrl" ) );				user.setSex( jsonObject.getInt( "sex" ) );				user.setBirthday( jsonObject.getString( "birthyear" ) );				user.setPku( jsonObject.getString( "pku" ) );				user.setNowHome( jsonObject.getString( "base" ) );				user.setOldHome( jsonObject.getString( "hometown" ) );				user.setQq( jsonObject.getString( "qq" ) );				user.setJob1( jsonObject.getString( "company" ) );				user.setJob2( jsonObject.getString( "department" ) );				user.setJob3( jsonObject.getString( "job" ) );				user.setVersion( jsonObject.getInt( "version" ) );				user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				user.setAnswerCount( jsonObject.getInt( "answerCount" ) );				user.setQuestionCount( jsonObject.getInt( "questionCount" ) );				user.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				user.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				user.setIntro( jsonObject.getString( "intro" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				user.setTag( temp );								Message message = handler.obtainMessage();				message.what = 6;				message.obj = user;				handler.sendMessage( message );			}			else			{				handler.sendEmptyMessage( 7 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 7 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void showUser( UserEntity user, int type )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();		bundle.putInt( "type", type );		bundle.putSerializable( "user", user );		intent.putExtras( bundle );		startActivity( intent );	}		public void showCommentList()	{		if( entity.getCommentList().size() > 0 )		{			Intent intent = new Intent( this, CommentListActivity.class );			intent.putExtra( "questionTitle", entity.getQuestionTitle() );			startActivityForResult( intent, 1006 );		}		else		{			Intent intent = new Intent( this, AddCommentActivity.class );			intent.putExtra( "id", entity.getId() );			intent.putExtra( "type", 0 );			startActivityForResult( intent, 1010 );		}	}		private void showAnswerInfo( int position )	{		entity.getAnswerList().get( position ).setModifyTime( "" );				Intent intent = new Intent( this, AnswerInfoActivity.class );		intent.putExtra( "type", 0 );		intent.putExtra( "index", position );		startActivityForResult( intent, 1003 );	}		public void onRefresh() 	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						refresh();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 4 );		}	}		private void refresh()	{		final String urlString = Information.Server_Url + "/api/question/" + entity.getId() + "/update?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "after", "" );			connection.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{//					if( jsonObject.getBoolean( "hasChange" ) == true )//					{						entity.setUpdateTime( jsonObject.getString( "updateTime" ) );												entity.setModifyTime( jsonObject.getString( "modifyTime" ) );//						MyDatabaseHelper.getInstance( this ).updateQuestion1( entity.getId(), entity.getModifyTime() );//						MyDatabaseHelper.getInstance( this ).updateQuestion2( entity.getId(), entity.getUpdateTime() );												entity.setUserHeadUrl( jsonObject.getString( "headUrl" ) );						entity.setJob( jsonObject.getString( "job" ) );						entity.setCompany( jsonObject.getString( "company" ) );						entity.setPraiseCount( jsonObject.getInt( "praiseCount" ) );						entity.setAnswerList( Tool.loadAnswerList( jsonObject.getJSONArray( "answers" ) ) );						Message message = handler.obtainMessage();						message.what = 0;						message.arg1 = 1;						handler.sendMessage( message );//					}//					else//					{//						Message message = handler.obtainMessage();//						message.what = 0;//						message.arg1 = 0;//						handler.sendMessage( message );//					}				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	public void onLoadMore() 	{		scrollPosition = answerListView.getFirstVisiblePosition();		View v = answerListView.getChildAt( 0 );        scrollTop = ( v == null ) ? 0 : v.getTop();        		new Thread		(			new Thread()			{				public void run()				{					loadMore();				}			}		).start();	}		private void loadMore()	{		String urlString = Information.Server_Url + "/api/question/" + entity.getId() + "/answers?token=" + Information.Token;				try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "size", 10 );						if( entity.getAnswerList().size() > 0 )			{				json.put( "before", entity.getAnswerList().get( entity.getAnswerList().size() - 1 ).getCreateTime() );			}			else			{				json.put( "before", "" );			}			connection.getOutputStream().write( json.toString().getBytes() );						final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 4000 )				{					Message message = handler.obtainMessage();					message.what = 1;					if( jsonObject.getJSONArray( "data" ).length() > 0 )					{						message.arg1 = 1;						entity.getAnswerList().addAll( Tool.loadAnswerList( jsonObject.getJSONArray( "data" ) ) );					}					else					{						message.arg1 = 0;					}					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 2 )		{			AnswerEntity answerEntity = ( AnswerEntity ) data.getSerializableExtra( "answer" );			answerEntity.setQuestionTitle( entity.getQuestionTitle() );			answerEntity.setInvite( new ArrayList< String >() );			int count = entity.getInviteMeList().size();			for( int i = 0; i < count; i ++ )			{				answerEntity.getInvite().add( entity.getInviteMeList().get( i ).getName() );			}						if( !answerEntity.isInvisible() )			{				entity.setHasAnswered( true );				entity.setMyAnswer( answerEntity );			}			entity.getAnswerList().add( 0, answerEntity );			entity.setAnswerCount( entity.getAnswerCount() + 1 );						answerListView.updateHeader();			refreshAnswerList();		}		else if( resultCode == 3 )		{			CommentEntity commentEntity = ( CommentEntity ) data.getSerializableExtra( "comment" );			entity.getCommentList().add( 0, commentEntity );			answerListView.updateHeader();		}		else if( resultCode == 4 )		{			answerListView.updateHeader();		}		else if( resultCode == 6 )		{			InviteEntity temp = new InviteEntity();			temp.setHasAnswered( false );			temp.setHeadUrl( data.getStringExtra( "headUrl" ) );			temp.setInviteInfo( data.getStringExtra( "reason" ) );			temp.setInviterId( data.getStringExtra( "id" ) );			temp.setName( data.getStringExtra( "name" ) );			temp.setTime( data.getStringExtra( "time" ) );						entity.getMyInviteList().add( temp );			answerListView.updateHeader();		}		else if( resultCode == 8 )		{			String id = data.getStringExtra( "id" );			final int count = adapter.getCount();			for( int i = 0; i < count; i ++ )			{				if( adapter.getItem( i ).getId().equals( id ) )				{					adapter.getItem( i ).setAnswerInfo( data.getStringExtra( "answer" ) );					adapter.notifyDataSetChanged();					break;				}			}		}		else 		{			adapter.notifyDataSetChanged();		}	}	//	private void removeSameAnswer( List< AnswerEntity > tempList )//	{//		map.clear();//		int temp = tempList.size();//		for( int i = 0; i < temp; i ++ )//		{//			map.put( tempList.get( i ).getId(), true );//		}//		int count = entity.getAnswerList().size();//		for( int i = count - 1; i >= 0; i -- )//		{//			if( map.get( entity.getAnswerList().get( i ).getId() ) != null )//			{//				entity.getAnswerList().remove( i );//			}//		}//	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			back();			return true;		}		return false;	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		private void copyQuestion()	{		ClipboardManager manager =( ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );		manager.setText( "问题：" + entity.getQuestionTitle() + "\n描述：" + entity.getQuestionInfo() );				dialog.dismiss();		Toast.makeText( this, "文字已复制", Toast.LENGTH_SHORT ).show();	}	public void showCopy( int x, int y )	{		dialog = new Dialog( this, R.style.my_dialog );		LayoutInflater inflater = LayoutInflater.from( this );  		View view = inflater.inflate( R.layout.answer_pop_view, null );		Button buttonCopy = ( Button ) view.findViewById( R.id.answer_pop_view_button );		buttonCopy.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					copyQuestion();				}							}		);				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();		layoutParams.alpha = 1.0f;		DisplayMetrics dm = new DisplayMetrics();		getWindowManager().getDefaultDisplay().getMetrics(dm);		layoutParams.y = ( int )(  x + y / 2 - dm.heightPixels / 2 );		dialog.getWindow().setAttributes( layoutParams );		dialog.getWindow().setGravity( Gravity.CENTER );		dialog.setContentView( view );		dialog.setCancelable( true );		dialog.show();	}}