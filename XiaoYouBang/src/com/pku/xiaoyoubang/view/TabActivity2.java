package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.Window;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.QuestionEntity;import com.pku.xiaoyoubang.model.QuestionListAdapter;import com.pku.xiaoyoubang.selfview.XListView;import com.pku.xiaoyoubang.selfview.XListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class TabActivity2 extends Activity implements IXListViewListener{	/**	 *  	 */	private XListView allQuestionListView;	private QuestionListAdapter questionListAdapter;		private int scrollPosition = 0;	private int scrollTop = 0;	public static QuestionEntity selectedEntity;		private Dialog dialog;		private HttpURLConnection connection = null;	/**	 *  	 */	public static Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				showView();	}		private void showView()	{		setContentView( R.layout.tab_2 );				handler = new Handler()		{			public void handleMessage( Message message )			{								switch( message.what )				{				case 0 : //Refresh success					storeRefreshTime();										refreshSuccess( ( String ) message.obj );										allQuestionListView.stopRefresh();					allQuestionListView.updateHeaderHeight( -120 );					allQuestionListView.setPullLoadEnable( true );					break;				case 1 : //Load more sucess					loadMoreSuccess( ( String ) message.obj );										allQuestionListView.setSelectionFromTop( scrollPosition, scrollTop );					allQuestionListView.stopLoadMore();					break;				case 2 : //load info success					if( dialog != null )					{						dialog.dismiss();					}					try 					{						Tool.loadQuestionInfoEntity( selectedEntity, ( JSONObject ) message.obj );						selectedEntity.setNew( false );						selectedEntity.setUpdate( false );						MyDatabaseHelper.getInstance( TabActivity2.this ).setQuestionHasRead( selectedEntity.getId(), selectedEntity.getModifyTime() );						showToQuestionInfo();					}					catch( Exception ex ) 					{						ex.printStackTrace();					}					break;				case 3 : //get list failed					showError( "加载失败" );					allQuestionListView.stopRefresh();					allQuestionListView.updateHeaderHeight( -120 );					allQuestionListView.setPullLoadEnable( true );					break;				case 4 : //No net - refresh					allQuestionListView.stopRefresh();					allQuestionListView.updateHeaderHeight( -120 );					allQuestionListView.setPullLoadEnable( true );					showError( "网络不可用，请打开网络" );					break;				case 5 : //No net - laod more					allQuestionListView.stopLoadMore();					showError( "网络不可用，请打开网络" );					break;				case 6 : //get info failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "加载失败" );					break;				case 7 : //load more failed					showError( "加载失败" );					allQuestionListView.setSelectionFromTop( scrollPosition, scrollTop );					allQuestionListView.stopLoadMore();					break;				}			}		};				initView();				loadQuestionList();	}		private void initView()	{		allQuestionListView = ( XListView ) findViewById( R.id.tab_2_list );		allQuestionListView.setPullLoadEnable( true );		allQuestionListView.setXListViewListener( this );		allQuestionListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					selectedEntity = ( QuestionEntity ) questionListAdapter.getItem( position - 1 );					showQuestionInfo();				}			}		);				SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		allQuestionListView.setRefreshTime( shared.getString( "my_question_update_time", "" ) );	}	/**	 * 	 */	private void storeRefreshTime()	{		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		SharedPreferences.Editor editor = shared.edit();		editor.putString( "my_question_update_time", Tool.getNowTime() );		editor.commit();				allQuestionListView.setRefreshTime( shared.getString( "my_question_update_time", "" ) );	}	/**	 *  刚开始进入问题界面	 *  先加载本地的所有缓存	 *  然后开始刷新	 */	private void loadQuestionList()	{		questionListAdapter = new QuestionListAdapter( Tool.loadLocalQuestionListFromFile( this, 1 ), this );		allQuestionListView.setAdapter( questionListAdapter );				if( questionListAdapter.getCount() == 0 )		{			allQuestionListView.setPullLoadEnable( false );		}					allQuestionListView.updateHeaderHeight( 120 );		allQuestionListView.startRefresh();	}		private void showQuestionInfo()	{		if( Tool.isNetworkConnected( this ) == true )		{			showDialog();						new Thread			(				new Thread()				{					public void run()					{						loadQuestionInfoFromNet( selectedEntity.getId() );					}				}			).start();		}		else		{			showError( "网络不可用，请打开网络" );		}	}		private void showToQuestionInfo()	{		Intent intent = new Intent( this, QuestionInfoActivity.class );		intent.putExtra( "type", 1 );		startActivityForResult( intent, 1002 );	}	/**	 *  Load new question list from net	 */	private void loadNewQuestionListFromNet()	{		final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "/questions?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "type", 1 );			json.put( "size", 10 );			json.put( "before", "" );			connection.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					Tool.writeQuestionListToFile( jsonObject.getJSONArray( "data" ).toString(), 1 );					Message message = handler.obtainMessage();					message.what = 0;					message.obj = jsonObject.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	/**	 *  Load more question list from net	 */	private void loadMoreQeustionListFromNet()	{		String urlString = Information.Server_Url + "/api/user/" + Information.Id + "/questions?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "type", 1 );			json.put( "size", 10 );						if( questionListAdapter.getCount() > 0 )			{				QuestionEntity entity = ( QuestionEntity ) questionListAdapter.getItem( questionListAdapter.getCount() - 1 );				json.put( "before", entity.getModifyTime() );			}			else			{				json.put( "before", "" );			}			connection.getOutputStream().write( json.toString().getBytes() );						final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 1;					message.obj = jsonObject.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 7 );				}			}			else			{				handler.sendEmptyMessage( 7 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 7 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	/**	 *  Load question info from net	 */	private void loadQuestionInfoFromNet( String id )	{		final String urlString = Information.Server_Url + "/api/question/" + id + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 2;					message.obj = jsonObject;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 6 );				}			}			else			{								handler.sendEmptyMessage( 6 );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 6 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void showDialog()	{		dialog = new Dialog( this, R.style.dialog_progress );		LayoutInflater inflater = LayoutInflater.from( this );  		View view = inflater.inflate( R.layout.dialog_progress, null );		TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );		textView.setText( "正在加载" );				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();		layoutParams.alpha = 0.8f;		dialog.getWindow().setAttributes( layoutParams );		dialog.setContentView( view );		dialog.setCancelable( false );		dialog.setOnKeyListener		(			new OnKeyListener()			{				public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 				{					if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )					{						if( connection != null )						{							connection.disconnect();						}						dialog.dismiss();						return true;					}					return false;				}			}		);		dialog.show();	}	public void onRefresh() 	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						loadNewQuestionListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 4 );		}	}	public void onLoadMore() 	{		if( Tool.isNetworkConnected( this ) == true )		{			scrollPosition = allQuestionListView.getFirstVisiblePosition();			View v = allQuestionListView.getChildAt( 0 );	        scrollTop = ( v == null ) ? 0 : v.getTop();			new Thread			(				new Thread()				{					public void run()					{						loadMoreQeustionListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 5 );		}	}		/**	 *  刷新问题成功，并且有更新	 *  1. 先删除本地所有缓存文件	 *  2. 保存更新的到缓存文件	 *  3. 更新内存数据，刷新界面	 */	public void refreshSuccess( String jsonString )	{		questionListAdapter = new QuestionListAdapter( Tool.loadLocalQuestionListFromJson( jsonString, this ), this );		allQuestionListView.setAdapter( questionListAdapter );		//questionListAdapter.notifyDataSetChanged();	}	/**	 *  加载更多问题成功	 *  直接刷新界面	 */	public void loadMoreSuccess( String jsonString )	{		List< QuestionEntity > oldList = new ArrayList< QuestionEntity >( questionListAdapter.getList() );		oldList.addAll( Tool.loadLocalQuestionListFromJson( jsonString, this ) );		questionListAdapter = new QuestionListAdapter( oldList, this );		allQuestionListView.setAdapter( questionListAdapter );		questionListAdapter.notifyDataSetChanged();	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 2 )		{			QuestionEntity entity = ( QuestionEntity ) data.getSerializableExtra( "question" );			MyDatabaseHelper.getInstance( this ).insertMyQuestion( entity.getId(), entity.getModifyTime() );			questionListAdapter.addItemTop( entity );			questionListAdapter.notifyDataSetChanged();		}		else if( resultCode == 3 )		{			questionListAdapter.notifyDataSetChanged();		}	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			Intent intent= new Intent( Intent.ACTION_MAIN );  			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );  			intent.addCategory( Intent.CATEGORY_HOME );			startActivity( intent );  			return true;		}		return false;	}}