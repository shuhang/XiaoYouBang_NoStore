package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.Window;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.AnswerEntity;import com.pku.xiaoyoubang.entity.QuestionEntity;import com.pku.xiaoyoubang.model.MyAnswerListAdapter;import com.pku.xiaoyoubang.model.MyAnswerListAdapter.MyAnswerListListener;import com.pku.xiaoyoubang.selfview.XListView;import com.pku.xiaoyoubang.selfview.XListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class AnswerListActivity extends Activity implements IXListViewListener, MyAnswerListListener{	/**	 *  	 */	private TextView textTitle;		private XListView answerListView;	private MyAnswerListAdapter adapter;		private int scrollPosition = 0;	private int scrollTop = 0;		private Dialog dialog;		private HttpURLConnection connection = null;		public static AnswerEntity selectedEntity;		private String id;	private String name;	private int count;	/**	 *  	 */	public static Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				id = getIntent().getStringExtra( "id" );		name = getIntent().getStringExtra( "name" );		count = getIntent().getIntExtra( "count", 0 );				showView();	}		private void showView()	{		setContentView( R.layout.answer_list );				handler = new Handler()		{			public void handleMessage( Message message )			{								switch( message.what )				{				case 0 : //Refresh success					storeRefreshTime();										refreshSuccess( ( String ) message.obj );										answerListView.stopRefresh();					answerListView.updateHeaderHeight( -120 );					answerListView.setPullLoadEnable( true );					break;				case 1 : //Load more sucess					loadMoreSuccess( ( String ) message.obj );										answerListView.setSelectionFromTop( scrollPosition, scrollTop );					answerListView.stopLoadMore();					break;				case 2 : //load info success					if( dialog != null )					{						dialog.dismiss();					}									try 					{						QuestionEntity questionEntity = new QuestionEntity();						Tool.loadQuestionInfoEntity( questionEntity, ( JSONObject ) message.obj );						showToQuestionInfo( questionEntity );					}					catch( Exception ex ) 					{						ex.printStackTrace();					}					break;				case 3 : //get list failed					showError( "加载失败" );					answerListView.stopRefresh();					answerListView.updateHeaderHeight( -120 );					answerListView.setPullLoadEnable( true );					break;				case 4 : //No net - refresh					answerListView.stopRefresh();					answerListView.updateHeaderHeight( -120 );					answerListView.setPullLoadEnable( true );					showError( "网络不可用，请打开网络" );					break;				case 5 : //No net - laod more					answerListView.stopLoadMore();					showError( "网络不可用，请打开网络" );					break;				case 6 : //get info failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "加载失败" );					break;				case 7 : //load more failed					showError( "加载失败" );					answerListView.setSelectionFromTop( scrollPosition, scrollTop );					answerListView.stopLoadMore();					break;				}			}		};				initView();	}		private void initView()	{		textTitle = ( TextView ) findViewById( R.id.answer_list_title );		textTitle.setText( name + "的回答  " + count );				answerListView = ( XListView ) findViewById( R.id.answer_list_list );		answerListView.setPullLoadEnable( true );		answerListView.setXListViewListener( this );		answerListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					showAnswerInfo( position - 1 );				}			}		);				adapter = new MyAnswerListAdapter( this, new ArrayList< AnswerEntity >(), 1 );		adapter.setMyListener( this );		answerListView.setAdapter( adapter );				if( adapter.getCount() == 0 )		{			answerListView.setPullLoadEnable( false );		}					answerListView.updateHeaderHeight( 120 );		answerListView.startRefresh();	}	/**	 * 	 */	private void storeRefreshTime()	{		answerListView.setRefreshTime( Tool.getNowTime() );	}	/**	 *  先加载本地的所有缓存	 *  然后开始刷新	 */	private void showAnswerInfo( int position )	{		Intent intent = new Intent( this, AnswerInfoActivity.class );		intent.putExtra( "type", 2 );		selectedEntity = adapter.getList().get( position );				startActivityForResult( intent, 1003 );	}		private void showToQuestionInfo( QuestionEntity questionEntity )	{		Intent intent = new Intent( this, QuestionInfoActivity.class );		intent.putExtra( "type", 3 );		intent.putExtra( "question", questionEntity );		startActivityForResult( intent, 1002 );	}	/**	 *  Load new question list from net	 */	private void loadNewAnswerListFromNet()	{		final String urlString = Information.Server_Url + "/api/user/" + id + "/answers?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "size", 10 );			json.put( "before", "" );			connection.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 4000 )				{					Message message = handler.obtainMessage();					message.what = 0;					message.obj = jsonObject.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	/**	 *  Load more question list from net	 */	private void loadMoreAnswerListFromNet()	{		String urlString = Information.Server_Url + "/api/user/" + id + "/answers?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "size", 10 );						if( adapter.getCount() > 0 )			{				AnswerEntity entity = ( AnswerEntity ) adapter.getItem( adapter.getCount() - 1 );				json.put( "before", entity.getModifyTime() );			}			else			{				json.put( "before", "" );			}			connection.getOutputStream().write( json.toString().getBytes() );						final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 4000 )				{					Message message = handler.obtainMessage();					message.what = 1;					message.obj = jsonObject.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 7 );				}			}			else			{				handler.sendEmptyMessage( 7 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 7 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}	/**	 *  Load question info from net	 */	private void loadQuestionInfoFromNet( String id )	{		final String urlString = Information.Server_Url + "/api/question/" + id + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 2;					message.obj = jsonObject;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 6 );				}			}			else			{								handler.sendEmptyMessage( 6 );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 6 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void showDialog()	{		dialog = new Dialog( this, R.style.dialog_progress );		LayoutInflater inflater = LayoutInflater.from( this );  		View view = inflater.inflate( R.layout.dialog_progress, null );		TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );		textView.setText( "正在加载问题详情" );				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();		layoutParams.alpha = 0.8f;		dialog.getWindow().setAttributes( layoutParams );		dialog.setContentView( view );		dialog.setCancelable( false );		dialog.setOnKeyListener		(			new OnKeyListener()			{				public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 				{					if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )					{						if( connection != null )						{							connection.disconnect();						}						dialog.dismiss();						return true;					}					return false;				}			}		);		dialog.show();	}	public void onRefresh() 	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						loadNewAnswerListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 4 );		}	}	public void onLoadMore() 	{		if( Tool.isNetworkConnected( this ) == true )		{			scrollPosition = answerListView.getFirstVisiblePosition();			View v = answerListView.getChildAt( 0 );	        scrollTop = ( v == null ) ? 0 : v.getTop();			new Thread			(				new Thread()				{					public void run()					{						loadMoreAnswerListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 5 );		}	}		/**	 *  刷新问题成功，并且有更新	 *  1. 先删除本地所有缓存文件	 *  2. 保存更新的到缓存文件	 *  3. 更新内存数据，刷新界面	 */	public void refreshSuccess( String jsonString )	{		adapter = new MyAnswerListAdapter( this, Tool.loadLocalAnswerListFromString( jsonString.toString(), this ), 1 );		adapter.setMyListener( this );		answerListView.setAdapter( adapter );		adapter.notifyDataSetChanged();	}	/**	 *  加载更多问题成功	 *  直接刷新界面	 */	public void loadMoreSuccess( String jsonString )	{		List< AnswerEntity > oldList = new ArrayList< AnswerEntity >( adapter.getList() );		oldList.addAll( Tool.loadLocalAnswerListFromString( jsonString.toString(), this ) );		adapter = new MyAnswerListAdapter( this, oldList, 1 );		adapter.setMyListener( this );		answerListView.setAdapter( adapter );		adapter.notifyDataSetChanged();	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		adapter.notifyDataSetChanged();	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}	public void clickQuestion( final int index ) 	{		if( Tool.isFastDoubleClick() )		{			return;		}		if( Tool.isNetworkConnected( this ) == true )		{			showDialog();						new Thread			(				new Thread()				{					public void run()					{						loadQuestionInfoFromNet( adapter.getList().get( index ).getQuestionId() );					}				}			).start();		}		else		{			showError( "网络不可用，请打开网络" );		}	}}