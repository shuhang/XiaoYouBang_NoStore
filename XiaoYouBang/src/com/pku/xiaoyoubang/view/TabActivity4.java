package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.text.Collator;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.HashMap;import java.util.List;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.View;import android.view.Window;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.model.FriendListAdapter;import com.pku.xiaoyoubang.selfview.FriendListView;import com.pku.xiaoyoubang.selfview.FriendListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class TabActivity4 extends Activity implements IXListViewListener{	private FriendListView friendListView;	private FriendListAdapter adapter;	private Handler handler;		private HttpURLConnection connection1 = null;	private HttpURLConnection connection2 = null;		private boolean isChanged = false;		private HashMap< String, Boolean > map = new HashMap< String, Boolean >();		@SuppressWarnings("rawtypes")	private Comparator cmp = Collator.getInstance( java.util.Locale.CHINA );		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			@SuppressWarnings("unchecked")			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //					friendListView.stopRefresh();					friendListView.updateHeaderHeight( -120 );										addNewFriend( ( ArrayList< UserEntity > ) ( message.obj ) );					break;				case 1 : //					friendListView.stopRefresh();					friendListView.updateHeaderHeight( -120 );					showError( "加载失败" );					break;				case 2 : //No net - refresh					friendListView.stopRefresh();					friendListView.updateHeaderHeight( -120 );					showError( "网络不可用，请打开网络" );					break;				case 3 : //Update my info succss					friendListView.updateHeader( adapter.getCount() );					save();					break;				}			}		};				initView();				loadFriendList();				Information.IsFirst = false;	}		private void initView()	{		setContentView( R.layout.tab_4 );		friendListView = ( FriendListView ) findViewById( R.id.tab_4_list );				friendListView.setPullLoadEnable( false );		friendListView.setXListViewListener( this );		List< UserEntity > tempList = MyDatabaseHelper.getInstance( this ).getFriendList();		//Collections.sort( tempList, comparator );		adapter = new FriendListAdapter( this, tempList );		friendListView.setAdapter( adapter );		friendListView.updateHeader( tempList.size() );		friendListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					showUserAtIndex( position - 2 );				}			}		);				SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		friendListView.setRefreshTime( shared.getString( "friend_update_time", "" ) );	}		public Comparator< UserEntity > comparator = new Comparator< UserEntity >()	{        	    @SuppressWarnings("unchecked")		public int compare( UserEntity entity1, UserEntity entity2 ) 	    {              	        return cmp.compare( entity1.getName(), entity2.getName() );         	    }     	};		private void save()	{		UserEntity entity = new UserEntity();		entity.setId( Information.Id );		entity.setName( Information.Name );		entity.setHeadUrl( Information.HeadUrl );		entity.setSex( Information.Sex );		entity.setBirthday( Information.Birthday );		entity.setPku( Information.PKU_Value );		entity.setNowHome( Information.Now_Home );		entity.setOldHome( Information.Old_Home );		entity.setQq( Information.QQ );		entity.setJob1( Information.Company );		entity.setJob2( Information.Part );		entity.setJob3( Information.Job );		entity.setVersion( Information.Version );		entity.setTag( Information.Tag );		entity.setPraiseCount( Information.Praise_Count );		entity.setQuestionCount( Information.Question_Count );		entity.setAnswerCount( Information.Answer_Count );				MyDatabaseHelper.getInstance( this ).insertUser( entity );	}	private void addNewFriend( ArrayList< UserEntity > list )	{		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		friendListView.setRefreshTime( shared.getString( "friend_update_time", "" ) );		if( isChanged )		{			final int count1 = list.size();			for( int i = 0; i < count1; i ++ )			{				map.put( list.get( i ).getId(), true );			}						List< UserEntity > tempList = new ArrayList< UserEntity >( adapter.getList() );			final int count = tempList.size();			for( int i = count - 1; i >= 0; i -- )			{				if( map.containsKey( tempList.get( i ).getId() ) )				{					tempList.remove( i );				}			}						tempList.addAll( 0, list );						Collections.sort( tempList, comparator );						adapter = new FriendListAdapter( this, tempList );			friendListView.setAdapter( adapter );			adapter.notifyDataSetChanged();			friendListView.updateHeader( adapter.getCount() );						isChanged = false;			map.clear();		}	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		private void loadFriendList()	{		friendListView.updateHeaderHeight( 120 );		friendListView.startRefresh();	}		private void doLoadFriendList()	{		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		String updateTime = shared.getString( "friend_update_time", "" );		final String urlString = Information.Server_Url + "/api/users";		try		{			URL url = new URL( urlString );			connection1 = ( HttpURLConnection ) url.openConnection();  			connection1.setRequestProperty( "Connection", "keep-alive" );			connection1.setRequestProperty( "Content-Type", "application/json" );			connection1.setRequestMethod( "POST" );			connection1.setConnectTimeout( 10000 );			connection1.setReadTimeout( 30000 );			connection1.setDoOutput( true );			connection1.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "after", updateTime );			connection1.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection1.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection1.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject1 = new JSONObject( value.toString() );				if( jsonObject1.getInt( "result" ) == 8000 )				{					updateTime = jsonObject1.getString( "updateTime" );					SharedPreferences.Editor editor = shared.edit();					editor.putString( "friend_update_time", updateTime );					editor.commit();					JSONArray array = jsonObject1.getJSONArray( "data" );					int count = array.length();					if( count > 0 )					{						isChanged = true;					}					List< UserEntity > list = new ArrayList< UserEntity >( count );					for( int i = 0; i < count; i ++ )					{						JSONObject jsonObject = array.getJSONObject( i );						UserEntity user = new UserEntity();												user.setId( jsonObject.getString( "id" ) );						user.setName( jsonObject.getString( "name" ) );						user.setHeadUrl( jsonObject.getString( "headUrl" ) );						user.setSex( jsonObject.getInt( "sex" ) );						user.setBirthday( jsonObject.getString( "birthyear" ) );						user.setPku( jsonObject.getString( "pku" ) );						user.setNowHome( jsonObject.getString( "base" ) );						user.setOldHome( jsonObject.getString( "hometown" ) );						user.setQq( jsonObject.getString( "qq" ) );						user.setJob1( jsonObject.getString( "company" ) );						user.setJob2( jsonObject.getString( "department" ) );						user.setJob3( jsonObject.getString( "job" ) );						user.setVersion( jsonObject.getInt( "version" ) );						user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );						user.setAnswerCount( jsonObject.getInt( "answerCount" ) );						user.setQuestionCount( jsonObject.getInt( "questionCount" ) );						String[] temp = new String[]{ "", "", "", "", "" };						JSONArray tags = jsonObject.getJSONArray( "tags" );						final int count1 = tags.length();						for( int j = 0; j < count1; j ++ )						{							temp[ j ] = tags.getString( j );						}						user.setTag( temp );												MyDatabaseHelper.getInstance( this ).insertFriend( user );												list.add( user );					}										Message message = new Message();					message.what = 0;					message.obj = list;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 1 );				}			}			else			{				handler.sendEmptyMessage( 1 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection1 != null )			{				connection1.disconnect();			}		}	}		private void showUserAtIndex( int position )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();				bundle.putInt( "type", 1 );		bundle.putSerializable( "user", adapter.getList().get( position ) );				intent.putExtras( bundle );		startActivity( intent );	}		private void doLoadInformation()	{		final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "?token=" + Information.Token;		try		{			HttpURLConnection.setFollowRedirects( false );						URL url = new URL( urlString );			connection2 = ( HttpURLConnection ) url.openConnection();  			connection2.setRequestProperty( "Connection", "keep-alive" );			connection2.setRequestMethod( "GET" );			connection2.setConnectTimeout( 10000 );			connection2.setReadTimeout( 30000 );			connection2.connect();						final int responseCode = connection2.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection2.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				Information.Name = jsonObject.getString( "name" );				Information.HeadUrl = jsonObject.getString( "headUrl" );				Information.Sex = jsonObject.getInt( "sex" );				Information.Birthday = jsonObject.getString( "birthyear" );				Information.PKU_Value = jsonObject.getString( "pku" );				Information.Now_Home = jsonObject.getString( "base" );				Information.Old_Home = jsonObject.getString( "hometown" );				Information.QQ = jsonObject.getString( "qq" );				Information.Company = jsonObject.getString( "company" );				Information.Part = jsonObject.getString( "department" );				Information.Job = jsonObject.getString( "job" );				Information.Version = jsonObject.getInt( "version" );				Information.Praise_Count = jsonObject.getInt( "praisedCount" );				Information.Answer_Count = jsonObject.getInt( "answerCount" );				Information.Question_Count = jsonObject.getInt( "questionCount" );				Information.Intro =  jsonObject.getString( "intro" );				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					Information.Tag[ i ] = tags.getString( i );				}				for( int i = count; i < 5; i ++ )				{					Information.Tag[ i ] = "";				}				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();		}		finally		{			if( connection2 != null )			{				connection2.disconnect();			}		}	}		public void onResume() 	{		super.onResume();				new Thread		(			new Thread()			{				public void run()				{					doLoadInformation();				}			}		).start();				MobclickAgent.onResume( this );				friendListView.updateHeader( adapter.getCount() );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			Intent intent= new Intent( Intent.ACTION_MAIN );  			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );  			intent.addCategory( Intent.CATEGORY_HOME );			startActivity( intent );  			return true;		}		return false;	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		friendListView.updateHeader( adapter.getCount() );		adapter.notifyDataSetChanged();	}	public void onRefresh() 	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						doLoadFriendList();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 2 );		}	}	public void onLoadMore() {}	public void backTo() {}	public void showHead() {}	public void showMe() 	{		startActivity( new Intent( this, MyInfoActivity.class ) );	}}