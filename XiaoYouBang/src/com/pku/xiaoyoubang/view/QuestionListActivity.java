package com.pku.xiaoyoubang.view;import java.net.HttpURLConnection;import java.util.ArrayList;import java.util.List;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.Button;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.QuestionEntity;import com.pku.xiaoyoubang.model.QuestionListAdapter;import com.pku.xiaoyoubang.selfview.XListView;import com.pku.xiaoyoubang.selfview.XListView.IXListViewListener;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class QuestionListActivity extends Activity implements IXListViewListener{	/**	 *  	 */	private XListView allQuestionListView;	private QuestionListAdapter questionListAdapter;		private int scrollPosition = 0;	private int scrollTop = 0;	public static QuestionEntity selectedEntity;		private Dialog dialog;		private HttpURLConnection connection = null;		private Button buttonBack;	private TextView textTitle;		private int type = 1;	private String name;	private String id;	private int count = 0;	/**	 *  	 */	public static Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				type = getIntent().getIntExtra( "type", 1 );		count = getIntent().getIntExtra( "count", 0 );		name = getIntent().getStringExtra( "name" );		id = getIntent().getStringExtra( "id" );				showView();	}		private void showView()	{		setContentView( R.layout.question_list );				handler = new Handler()		{			public void handleMessage( Message message )			{								switch( message.what )				{				case 0 : //Refresh success					storeRefreshTime();										refreshSuccess( ( String ) message.obj );										allQuestionListView.stopRefresh();					allQuestionListView.updateHeaderHeight( -120 );					allQuestionListView.setPullLoadEnable( true );					break;				case 1 : //Load more sucess					loadMoreSuccess( ( String ) message.obj );										allQuestionListView.setSelectionFromTop( scrollPosition, scrollTop );					allQuestionListView.stopLoadMore();					break;				case 2 : //load info success					if( dialog != null )					{						dialog.dismiss();					}					try 					{						Tool.loadQuestionInfoEntity( selectedEntity, ( JSONObject ) message.obj );					}					catch( Exception ex ) 					{						ex.printStackTrace();					}					selectedEntity.setNew( false );					selectedEntity.setModified( false );					selectedEntity.setUpdateTime( "2115-01-19 15:15:26 +08:00" );					showToQuestionInfo();					break;				case 3 : //get list failed					showError( "加载失败" );					allQuestionListView.stopRefresh();					allQuestionListView.updateHeaderHeight( -120 );					allQuestionListView.setPullLoadEnable( true );					break;				case 4 : //No net - refresh					allQuestionListView.stopRefresh();					allQuestionListView.updateHeaderHeight( -120 );					allQuestionListView.setPullLoadEnable( true );					showError( "网络不可用，请打开网络" );					break;				case 5 : //No net - laod more					allQuestionListView.stopLoadMore();					showError( "网络不可用，请打开网络" );					break;				case 6 : //get info failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "加载失败" );					break;				case 7 : //load more failed					showError( "加载失败" );					allQuestionListView.setSelectionFromTop( scrollPosition, scrollTop );					allQuestionListView.stopLoadMore();					break;				}			}		};				initView();				loadQuestionList();	}		private void initView()	{		allQuestionListView = ( XListView ) findViewById( R.id.question_list_list );		allQuestionListView.setPullLoadEnable( true );		allQuestionListView.setXListViewListener( this );		allQuestionListView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					if( Tool.isFastDoubleClick() )					{						return;					}					selectedEntity = ( QuestionEntity ) questionListAdapter.getItem( position - 1 );					showQuestionInfo();				}			}		);				buttonBack = ( Button ) findViewById( R.id.question_list_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					finish();				}			}		);		textTitle = ( TextView ) findViewById( R.id.question_list_title );		if( type == 1 )		{			textTitle.setText( name + "的提问  " + count );		}		else		{			textTitle.setText( name + "答过的问题  " + count );		}	}	/**	 * 	 */	private void storeRefreshTime()	{		allQuestionListView.setRefreshTime( Tool.getShowTime( Tool.getNowTime() ) );	}	private void loadQuestionList()	{		allQuestionListView.setPullLoadEnable( false );					allQuestionListView.updateHeaderHeight( 120 );		allQuestionListView.startRefresh();	}		private void showQuestionInfo()	{		if( Tool.isNetworkConnected( this ) == true )		{			showDialog();							new Thread			(				new Thread()				{					public void run()					{						loadQuestionInfoFromNet( selectedEntity.getId() );					}				}			).start();		}		else		{			showError( "网络不可用，请打开网络" );		}	}		private void showToQuestionInfo()	{		Intent intent = new Intent( this, QuestionInfoActivity.class );		intent.putExtra( "type", 2 );		startActivityForResult( intent, 1002 );	}	/**	 *  Load new question list from net	 */	private void loadNewQuestionListFromNet()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + id + "/questions?token=" + Information.Token;						JSONObject json = new JSONObject();			json.put( "type", type );			json.put( "size", 10 );			json.put( "before", "" );						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 3 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 0;					message.obj = result.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 3 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 3 );		}	}	/**	 *  Load more question list from net	 */	private void loadMoreQeustionListFromNet()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + id + "/questions?token=" + Information.Token;								JSONObject json = new JSONObject();			json.put( "size", 10 );			json.put( "type", type );			if( questionListAdapter.getCount() > 0 )			{				QuestionEntity entity = ( QuestionEntity ) questionListAdapter.getItem( questionListAdapter.getCount() - 1 );				json.put( "before", entity.getModifyTime() );			}			else			{				json.put( "before", "" );			}						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 7 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 1;					message.obj = result.getJSONArray( "data" ).toString();					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 7 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 7 );		}	}	/**	 *  Load question info from net	 */	private void loadQuestionInfoFromNet( String id )	{		try		{			final String urlString = Information.Server_Url + "/api/question/" + id + "?token=" + Information.Token;			JSONObject result = Tool.doGetWithUrl( urlString );			if( result == null )			{				handler.sendEmptyMessage( 6 );			}			else			{				if( result.getInt( "result" ) == 3000 )				{					Message message = handler.obtainMessage();					message.what = 2;					message.obj = result;					handler.sendMessage( message );				}				else				{					handler.sendEmptyMessage( 6 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 6 );		}	}		private void showDialog()	{		dialog = new Dialog( this, R.style.dialog_progress );		LayoutInflater inflater = LayoutInflater.from( this );  		View view = inflater.inflate( R.layout.dialog_progress, null );		TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );		textView.setText( "正在加载" );				WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();		layoutParams.alpha = 0.8f;		dialog.getWindow().setAttributes( layoutParams );		dialog.setContentView( view );		dialog.setCancelable( false );		dialog.setOnKeyListener		(			new OnKeyListener()			{				public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 				{					if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )					{						if( connection != null )						{							connection.disconnect();						}						dialog.dismiss();						return true;					}					return false;				}			}		);		dialog.show();	}	public void onRefresh() 	{		if( Tool.isNetworkConnected( this ) == true )		{			new Thread			(				new Thread()				{					public void run()					{						loadNewQuestionListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 4 );		}	}	public void onLoadMore() 	{		if( Tool.isNetworkConnected( this ) == true )		{			scrollPosition = allQuestionListView.getFirstVisiblePosition();			View v = allQuestionListView.getChildAt( 0 );	        scrollTop = ( v == null ) ? 0 : v.getTop();			new Thread			(				new Thread()				{					public void run()					{						loadMoreQeustionListFromNet();					}				}			).start();		}		else		{			handler.sendEmptyMessage( 5 );		}	}		public void refreshSuccess( String jsonString )	{		questionListAdapter = new QuestionListAdapter( Tool.loadLocalQuestionListFromJson( jsonString, this ), this, 0 );		allQuestionListView.setAdapter( questionListAdapter );		questionListAdapter.notifyDataSetChanged();	}	public void loadMoreSuccess( String jsonString )	{		List< QuestionEntity > oldList = new ArrayList< QuestionEntity >( questionListAdapter.getList() );		oldList.addAll( Tool.loadLocalQuestionListFromJson( jsonString, this ) );		questionListAdapter = new QuestionListAdapter( oldList, this, 0 );		allQuestionListView.setAdapter( questionListAdapter );		questionListAdapter.notifyDataSetChanged();	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 3 )		{			questionListAdapter.notifyDataSetChanged();		}	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}}