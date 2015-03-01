package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.LinkedHashMap;import java.util.List;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.Context;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.text.ClipboardManager;import android.util.DisplayMetrics;import android.view.Gravity;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.Button;import android.widget.ImageView;import android.widget.TableLayout;import android.widget.TableRow;import android.widget.TextView;import android.widget.Toast;import com.nostra13.universalimageloader.core.ImageLoader;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.AnswerEntity;import com.pku.xiaoyoubang.entity.CommentEntity;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.model.CommentListAdapter;import com.pku.xiaoyoubang.model.CommentListAdapter.CommentListener;import com.pku.xiaoyoubang.selfview.CommentListView;import com.pku.xiaoyoubang.selfview.CommentListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressWarnings("deprecation")@SuppressLint("HandlerLeak")public class AnswerInfoActivity extends Activity implements IXListViewListener, CommentListener, OnClickListener{	/**	 *  Main	 */	private Button buttonBack;	private TextView textTitle;	private Button buttonPraise;	private Button buttonComment;		private TableLayout tableImage;	private LinkedHashMap< ImageView, Integer > mapImage = new LinkedHashMap< ImageView, Integer >();		private Dialog dialog;	/**	 *  List	 */	private CommentListView commentListView;	private CommentListAdapter adapter;		private int scrollPosition = 0;	private int scrollTop = 0;		private boolean hasEdit = false;	/**	 *  Data	 */	public static AnswerEntity entity;	private int commentClickIndex;		private HttpURLConnection connection = null;		private Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				int type = getIntent().getIntExtra( "type", 0 );		if( type == 0 )		{			entity = QuestionInfoActivity.entity.getAnswerList().get( getIntent().getIntExtra( "index", 0 ) );			entity.setQuestionTitle( QuestionInfoActivity.entity.getQuestionTitle() );		}		else if( type == 1 )		{			entity = TabActivity3.selectedEntity;		}		else if( type == 2 )		{			entity = AnswerListActivity.selectedEntity;		}		else if( type == 3 )		{			entity = QuestionInfoActivity.entity.getMyAnswer();		}		else if( type == 4 )		{			entity = ( AnswerEntity ) getIntent().getSerializableExtra( "answer" );		}		handler = new Handler()		{			@SuppressWarnings("unchecked")			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //refresh success									ArrayList< CommentEntity > newList = ( ArrayList< CommentEntity > ) message.obj;					if( newList.size() > 0 )					{						refreshCommentListUp( newList );					}					if( entity.isHasPraised() )					{						buttonPraise.setBackgroundColor( AnswerInfoActivity.this.getResources().getColor( R.color.bg_red ) );					}					commentListView.updateHeader();					updateTable();					commentListView.stopRefresh();					commentListView.updateHeaderHeight( -120 );					commentListView.setRefreshTime( Tool.getShowTime( Tool.getNowTime() ) );					commentListView.setPullLoadEnable( true );					break;				case 1 : //refresh failed					showError( "刷新失败" );					break;				case 2 : //load more success					break;				case 3 : //get list failed					showError( "加载失败" );					commentListView.stopRefresh();					commentListView.updateHeaderHeight( -120 );					commentListView.setPullLoadEnable( true );					commentListView.stopLoadMore();					break;				case 4 : //No net - refresh					commentListView.stopRefresh();					commentListView.updateHeaderHeight( -120 );					commentListView.setPullLoadEnable( true );					showError( "网络不可用，请打开网络" );					break;				case 5 : //No net - laod more					commentListView.stopLoadMore();					showError( "网络不可用，请打开网络" );					break;				case 6 : //get user success					if( dialog != null )					{						dialog.dismiss();					}					showUser( ( UserEntity ) message.obj, 0 );					break;				case 7 : //get user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取用户资料失败" );					break;				case 8 : //praise success					if( dialog != null )					{						dialog.dismiss();					}							entity.setPraiseCount( entity.getPraiseCount() + 1 );					entity.setHasPraised( true );					entity.getPraise().add( Information.Name );					commentListView.updateHeader();					updateTable();					buttonPraise.setBackgroundColor( AnswerInfoActivity.this.getResources().getColor( R.color.bg_red ) );					break;				case 9 : //praise failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "点赞失败" );					break;				}			}		};				initView();	}		private void initView()	{		setContentView( R.layout.answer_info );		commentListView = ( CommentListView ) findViewById( R.id.answer_info_list );		commentListView.setPullLoadEnable( true );		commentListView.setXListViewListener( this );		adapter = new CommentListAdapter( this, new ArrayList< CommentEntity >() );		adapter.setMyListener( this );		commentListView.setAdapter( adapter );		commentListView.updateHeader();		commentListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					if( position >= 2 )					{						clickCommentAtIndex( position - 2 );					}				}			}		);				textTitle = ( TextView ) findViewById( R.id.answer_info_title );		if( entity.isInvisible() )		{			textTitle.setText( "匿名用户的回答" );		}		else		{			textTitle.setText( entity.getName() + "的回答" );		}						buttonBack = ( Button ) findViewById( R.id.answer_info_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					back();				}							}		);				buttonPraise = ( Button ) findViewById( R.id.answer_info_button_praise );		if( entity.getUserId().equals( Information.Id ) && !entity.isInvisible() )		{			buttonPraise.setText( "编辑回答" );		}		else		{			if( entity.isHasPraised() || entity.getUserId().equals( Information.Id ) )			{				buttonPraise.setBackgroundColor( this.getResources().getColor( R.color.bg_red ) );			}		}		buttonPraise.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					praise();				}							}		);		buttonComment = ( Button ) findViewById( R.id.answer_info_button_comment );		buttonComment.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					comment();				}							}		);				commentListView.setPullLoadEnable( false );		commentListView.startRefresh();				tableImage = ( TableLayout ) findViewById( R.id.answer_info_list_header_table );		updateTable();	}		private void updateTable()	{		int sum = 0;		final int count = entity.getImageList().size();		if( count == 0 )		{			tableImage.setVisibility( View.INVISIBLE );		}		else		{			tableImage.removeAllViews();			for( int i = 0; i < count / 3 + 1; i ++ )			{				if( sum == count ) break;				TableRow row = ( TableRow ) LayoutInflater.from( this ).inflate( R.layout.add_picture_row, null );				tableImage.addView( row );					ImageView image1 = ( ImageView ) row.findViewById( R.id.add_picture_row1 );				mapImage.put( image1, sum );				image1.setOnClickListener( this );				ImageLoader.getInstance().displayImage( Information.Server_Url + entity.getImageList().get( sum ), image1, Information.options_image );				sum ++;				if( sum == count ) break;								ImageView image2 = ( ImageView ) row.findViewById( R.id.add_picture_row2 );				mapImage.put( image2, sum );				image2.setOnClickListener( this );				ImageLoader.getInstance().displayImage( Information.Server_Url + entity.getImageList().get( sum ), image2, Information.options_image );				sum ++;				if( sum == count ) break;								ImageView image3 = ( ImageView ) row.findViewById( R.id.add_picture_row3 );				mapImage.put( image3, sum );				image3.setOnClickListener( this );				ImageLoader.getInstance().displayImage( Information.Server_Url + entity.getImageList().get( sum ), image3, Information.options_image );				sum ++;				if( sum == count ) break;			}		}	}		private void back()	{				//MyDatabaseHelper.getInstance( this ).updateMyAnswer( entity.getId(), entity.getModifyTime() );		if( hasEdit )		{			Intent intent = getIntent();			intent.putExtra( "id", entity.getId() );			intent.putExtra( "answer", entity.getAnswerInfo() );			setResult( 8, intent );			finish();		}		else		{			entity.setModifyTime( "" );			setResult( 5 );			finish();		}	}		private void praise()	{		if( entity.getUserId().equals( Information.Id ) )		{			Intent intent = new Intent( this, AddAnswerActivity.class );			intent.putExtra( "type", 1 );			intent.putExtra( "value", entity.getAnswerInfo() );			intent.putExtra( "id", entity.getId() );			intent.putExtra( "questionTitle", entity.getQuestionTitle() );			startActivityForResult( intent, 1005 );		}		else		{			if( entity.isHasPraised() )			{				Toast.makeText( this, "已经点过赞了亲", Toast.LENGTH_SHORT ).show();			}			else			{				startPraise();			}		}	}		private void startPraise()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			textView.setText( "正在点赞" );						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				doPraise();	    			}	    		}	    	).start();		}		else		{			Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();		}	}		private void doPraise()	{		final String urlString = Information.Server_Url + "/api/answer/" + entity.getId() + "/praise";		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			connection.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 6000 )				{					MyDatabaseHelper.getInstance( this ).updateQuestion2( entity.getQuestionId(), jsonObject.getString( "modifyTime" ) );					MyDatabaseHelper.getInstance( this ).updateMyAnswer( entity.getId(), jsonObject.getString( "modifyTime" ) );					handler.sendEmptyMessage( 8 );				}				else				{					handler.sendEmptyMessage( 9 );				}			}			else			{				handler.sendEmptyMessage( 9 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 9 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void comment()	{		Intent intent = new Intent( this, AddCommentActivity.class );		intent.putExtra( "type", 1 );		intent.putExtra( "answerId", entity.getId() );		intent.putExtra( "questionId", entity.getQuestionId() );		startActivityForResult( intent, 1007 );	}		private void clickCommentAtIndex( int position )	{		commentClickIndex = position;				dialog = new Dialog( this, R.style.my_dialog );		LayoutInflater inflater = LayoutInflater.from( this );  		View view = inflater.inflate( R.layout.comment_pop_view, null );		Button buttonReply = ( Button ) view.findViewById( R.id.comment_pop_view_button_reply );		buttonReply.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					reply();				}							}		);		Button buttonCopy = ( Button ) view.findViewById( R.id.comment_pop_view_button_copy );		buttonCopy.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					copy();				}							}		);				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();		layoutParams.alpha = 1.0f;		dialog.getWindow().setAttributes( layoutParams );		dialog.setContentView( view );		dialog.setCancelable( true );		dialog.show();	}		private void reply()	{		dialog.dismiss();				Intent intent = new Intent( this, AddCommentActivity.class );		intent.putExtra( "replyId", ( ( CommentEntity ) adapter.getItem( commentClickIndex ) ).getUserId() );		intent.putExtra( "name", ( ( CommentEntity ) adapter.getItem( commentClickIndex ) ).getUserName() );		intent.putExtra( "questionId", entity.getQuestionId() );		intent.putExtra( "answerId", entity.getId() );		intent.putExtra( "type", 3 );		startActivityForResult( intent, 1010 );	}		private void copy()	{		ClipboardManager manager =( ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );		manager.setText( ( ( CommentEntity ) adapter.getItem( commentClickIndex ) ).getCommentInfo() );				dialog.dismiss();		Toast.makeText( this, "文字已复制", Toast.LENGTH_SHORT ).show();	}		private void copyAnswer()	{		ClipboardManager manager =( ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );		manager.setText( entity.getAnswerInfo() );				dialog.dismiss();		Toast.makeText( this, "文字已复制", Toast.LENGTH_SHORT ).show();	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}	public void onRefresh()	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						refresh();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 4 );		}	}		private void refresh()	{		final String urlString = Information.Server_Url + "/api/answer/" + entity.getId() + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 4000 )				{					entity.setCreateTime( jsonObject.getString( "createTime" ) );					entity.setModifyTime( jsonObject.getString( "modifyTime" ) );					entity.setUserHeadUrl( jsonObject.getString( "headUrl" ) );					entity.setJob( jsonObject.getString( "job" ) );					entity.setCompany( jsonObject.getString( "company" ) );					if( jsonObject.has( "editTime" ) )					{						entity.setEditTime( jsonObject.getString( "editTime" ) );					}										JSONArray praise = jsonObject.getJSONArray( "praiseUserList" );					int tempCount = praise.length();					List< String > tempList = new ArrayList< String >();					for( int i = 0; i < tempCount; i ++ )					{						tempList.add( praise.getString( i ) );					}					entity.setPraise( tempList );										entity.setHasPraised( jsonObject.getBoolean( "praised" ) );										JSONArray array = jsonObject.getJSONArray( "comments" );					int count = array.length();					List< CommentEntity > list = new ArrayList< CommentEntity >( count );					for( int i = 0; i < count; i ++ )					{						JSONObject object = array.getJSONObject( i );						CommentEntity temp = new CommentEntity();						temp.setQuestionId( entity.getQuestionId() );						temp.setCommentInfo( object.getString( "content" ) );						temp.setTime( object.getString( "time" ) );						if( object.has( "replyId" ) )						{							temp.setReplyId( object.getString( "replyId" ) );						}						if( object.has( "replyName" ) )						{							temp.setReplyName( object.getString( "replyName" ) );						}						temp.setUserHeadUrl( object.getString( "headUrl" ) );						temp.setUserId( object.getString( "userId" ) );						temp.setUserName( object.getString( "name" ) );												list.add( temp );					}					Message message = handler.obtainMessage();					message.what = 0;					message.obj = list;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	//	private void refresh()//	{//		final String urlString = Information.Server_Url + "/api/answer/" + entity.getId() + "/update?token=" + Information.Token;//		try//		{//			URL url = new URL( urlString );//			connection = ( HttpURLConnection ) url.openConnection();  //			connection.setRequestProperty( "Connection", "keep-alive" );//			connection.setRequestProperty( "Content-Type", "application/json" );//			connection.setRequestMethod( "POST" );//			connection.setConnectTimeout( 10000 );//			connection.setReadTimeout( 30000 );//			connection.setDoOutput( true );//			connection.setDoInput( true );//			//			JSONObject json = new JSONObject();//			json.put( "after", "2015-01-01 10:43:26 +08:00" );//			connection.getOutputStream().write( json.toString().getBytes() );////			final int responseCode = connection.getResponseCode();//			if( responseCode == 200 )//			{//				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );//				String temp1 = null;//				StringBuilder value = new StringBuilder();//				while( ( temp1 = reader.readLine() ) != null )//				{//					value.append( temp1 );//				}////				JSONObject jsonObject = new JSONObject( value.toString() );//				if( jsonObject.getInt( "result" ) == 4000 )//				{//					entity.setModifyTime( jsonObject.getString( "modifyTime" ) );//					entity.setUserHeadUrl( jsonObject.getString( "headUrl" ) );//					entity.setJob( jsonObject.getString( "job" ) );//					entity.setCompany( jsonObject.getString( "company" ) );//					//					JSONArray array = jsonObject.getJSONArray( "comments" );//					int count = array.length();//					List< CommentEntity > list = new ArrayList< CommentEntity >( count );//					for( int i = 0; i < count; i ++ )//					{//						JSONObject object = array.getJSONObject( i );//						CommentEntity temp = new CommentEntity();//						temp.setQuestionId( QuestionInfoActivity.entity.getId() );//						temp.setCommentInfo( object.getString( "content" ) );//						temp.setTime( object.getString( "time" ) );//						if( object.has( "replyId" ) )//						{//							temp.setReplyId( object.getString( "replyId" ) );//						}//						if( object.has( "replyName" ) )//						{//							temp.setReplyName( object.getString( "replyName" ) );//						}//						temp.setUserHeadUrl( object.getString( "headUrl" ) );//						temp.setUserId( object.getString( "userId" ) );//						temp.setUserName( object.getString( "name" ) );//						//						list.add( temp );//					}//					Message message = handler.obtainMessage();//					message.what = 0;//					message.obj = list;//					handler.sendMessage( message );//				}//				else//				{//					handler.sendEmptyMessage( 3 );//				}//			}//			else//			{//				handler.sendEmptyMessage( 3 );//			}//		}//		catch( Exception ex )//		{//			ex.printStackTrace();//			handler.sendEmptyMessage( 3 );//		}//		finally//		{//			if( connection != null )//			{//				connection.disconnect();//			}//		}//	}		private void refreshCommentListUp( ArrayList< CommentEntity > newCommentList )	{		entity.setCommentList( newCommentList );		entity.setCommentCount( entity.getCommentList().size() );				List< CommentEntity > commentList = new ArrayList< CommentEntity >();		int count = entity.getCommentList().size();		if( count > 0 )		{			if( count > 20 ) count = 20;			commentList = new ArrayList< CommentEntity >( entity.getCommentList().subList( 0, count ) );		}				adapter = new CommentListAdapter( this, commentList );					adapter.setMyListener( this );		commentListView.setAdapter( adapter );		adapter.notifyDataSetChanged();	}	public void onLoadMore()	{		scrollPosition = commentListView.getFirstVisiblePosition();		View v = commentListView.getChildAt( 0 );        scrollTop = ( v == null ) ? 0 : v.getTop();                List< CommentEntity > commentList = new ArrayList< CommentEntity >();		int count = entity.getCommentList().size() - adapter.getCount();		if( count > 0 )		{			if( count > 20 ) count = 20;			commentList = new ArrayList< CommentEntity >( entity.getCommentList().subList( adapter.getCount(), adapter.getCount() + count ) );						adapter = new CommentListAdapter( this, commentList );						adapter.setMyListener( this );			commentListView.setAdapter( adapter );			adapter.notifyDataSetChanged();		}				commentListView.setSelectionFromTop( scrollPosition, scrollTop );		commentListView.stopLoadMore();	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}	public void backTo() 	{		back();	}		public void showCopy( int x, int y )	{		if( Tool.isFastDoubleClick() )		{			return;		}				dialog = new Dialog( this, R.style.my_dialog );		LayoutInflater inflater = LayoutInflater.from( this );  		View view = inflater.inflate( R.layout.answer_pop_view, null );		Button buttonCopy = ( Button ) view.findViewById( R.id.answer_pop_view_button );		buttonCopy.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					copyAnswer();				}							}		);				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();		layoutParams.alpha = 1.0f;		DisplayMetrics dm = new DisplayMetrics();		getWindowManager().getDefaultDisplay().getMetrics(dm);		layoutParams.y = ( int )(  x + y / 2 - dm.heightPixels / 2 );		dialog.getWindow().setAttributes( layoutParams );		dialog.getWindow().setGravity( Gravity.CENTER );		dialog.setContentView( view );		dialog.setCancelable( true );		dialog.show();	}	public void showHead() 	{		if( Tool.isFastDoubleClick() )		{			return;		}				if( entity.isInvisible() )		{			showError( "此乃匿名用户" );		}		else		{			getUser( entity.getUserId() );		}	}		public void getUser( final String id )	{		UserEntity user = null;		if( id.equals( Information.Id ) )		{			startActivity( new Intent( this, MyInfoActivity.class ) );			return;		}		else		{			user = MyDatabaseHelper.getInstance( this ).getFriend( id );		}		if( user == null )		{			if( Tool.isNetworkConnected( this ) == true )			{				dialog = new Dialog( this, R.style.dialog_progress );				LayoutInflater inflater = LayoutInflater.from( this );  				View view = inflater.inflate( R.layout.dialog_progress, null );				TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );				textView.setText( "正在获取用户资料" );								WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();				layoutParams.alpha = 0.8f;				dialog.getWindow().setAttributes( layoutParams );				dialog.setContentView( view );				dialog.setCancelable( false );				dialog.setOnKeyListener				(					new OnKeyListener()					{						public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 						{							if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )							{								if( connection != null )								{									connection.disconnect();								}								dialog.dismiss();								return true;							}							return false;						}					}				);				dialog.show();								new Thread		    	(		    		new Thread()		    		{		    			public void run()		    			{		    				doLoadInformation( id );		    			}		    		}		    	).start();			}			else			{				Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();			}		}		else		{			showUser( user, 1 );		}	}		private void showUser( UserEntity user, int type )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();		bundle.putInt( "type", type );		bundle.putSerializable( "user", user );		intent.putExtras( bundle );		startActivity( intent );	}		private void doLoadInformation( final String id )	{		final String urlString = Information.Server_Url + "/api/user/" + id + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				UserEntity user = new UserEntity();				user.setId( id );				user.setName( jsonObject.getString( "name" ) );				user.setHeadUrl( jsonObject.getString( "headUrl" ) );				user.setSex( jsonObject.getInt( "sex" ) );				user.setBirthday( jsonObject.getString( "birthyear" ) );				user.setPku( jsonObject.getString( "pku" ) );				user.setNowHome( jsonObject.getString( "base" ) );				user.setOldHome( jsonObject.getString( "hometown" ) );				user.setQq( jsonObject.getString( "qq" ) );				user.setJob1( jsonObject.getString( "company" ) );				user.setJob2( jsonObject.getString( "department" ) );				user.setJob3( jsonObject.getString( "job" ) );				user.setVersion( jsonObject.getInt( "version" ) );				user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				user.setAnswerCount( jsonObject.getInt( "answerCount" ) );				user.setQuestionCount( jsonObject.getInt( "questionCount" ) );				user.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				user.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				user.setIntro( jsonObject.getString( "intro" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				user.setTag( temp );								Message message = handler.obtainMessage();				message.what = 6;				message.obj = user;				handler.sendMessage( message );			}			else			{				handler.sendEmptyMessage( 7 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 7 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 2 )		{			hasEdit = true;			commentListView.updateHeader();			updateTable();		}		else if( resultCode == 3 )		{			CommentEntity commentEntity = ( CommentEntity ) data.getSerializableExtra( "comment" );					MyDatabaseHelper.getInstance( this ).updateQuestion2( entity.getQuestionId(), commentEntity.getTime() );			MyDatabaseHelper.getInstance( this ).updateMyAnswer( entity.getId(), commentEntity.getTime() );			entity.getCommentList().add( 0, commentEntity );			entity.setCommentCount( entity.getCommentList().size() );			commentListView.updateHeader();			updateTable();			List< CommentEntity > list = new ArrayList< CommentEntity >( adapter.getList() );			list.add( 0, commentEntity );			adapter = new CommentListAdapter( this, list );			adapter.setMyListener( this );			commentListView.setAdapter( adapter );			adapter.notifyDataSetChanged();		}	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			back();			return true;		}		return false;	}	public void showCommentHead( int index ) 	{		getUser( entity.getCommentList().get( index ).getUserId() );	}		public void onClick( View v ) 	{		Intent intent = new Intent( this, ViewPictureActivity.class );		intent.putExtra( "index", mapImage.get( ( ImageView ) v ) );		ArrayList< String > pictureList = new ArrayList< String >( entity.getImageList().size() );		for( String temp : entity.getImageList() )		{			pictureList.add( Information.Server_Url + temp.replaceFirst( "_small", "" ) );		}		intent.putStringArrayListExtra( "pictureListBig", pictureList );		startActivity( intent );	}}