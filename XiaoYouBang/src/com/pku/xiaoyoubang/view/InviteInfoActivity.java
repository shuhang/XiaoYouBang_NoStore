package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.Button;import android.widget.ImageView;import android.widget.ListView;import android.widget.TextView;import android.widget.Toast;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.InviteEntity;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.model.InviteListAdapter1;import com.pku.xiaoyoubang.model.InviteListAdapter1.InviteListListener1;import com.pku.xiaoyoubang.model.InviteListAdapter2;import com.pku.xiaoyoubang.model.InviteListAdapter2.InviteListListener2;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class InviteInfoActivity extends Activity implements InviteListListener1, InviteListListener2{	private Button buttonBack;		private TextView textTitle;	private TextView text1;	private TextView text2;	private ListView listView;	private ImageView imageCheck;		private List< InviteEntity > list;		private InviteListAdapter1 adapter1;	private InviteListAdapter2 adapter2;		private String chooseId;		private Handler handler;		private Dialog dialog;	private HttpURLConnection connection = null;		@SuppressWarnings("unchecked")	protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				switch( message.what )				{				case 6 : //get user success					if( dialog != null )					{						dialog.dismiss();					}					showUser( ( UserEntity ) message.obj, 0 );					break;				case 7 : //get user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取用户资料失败" );					break;				}			}		};				ArrayList<?> temp = getIntent().getExtras().getParcelableArrayList( "list" );		list = ( ArrayList< InviteEntity > ) temp.get( 0 );				initView( getIntent().getBooleanExtra( "isInviteMe", true ), getIntent().getBooleanExtra( "hasAnswered", false ) );	}		private void initView( boolean isInviteMe, boolean hasAnswered )	{		setContentView( R.layout.invite_info );		buttonBack = ( Button ) findViewById( R.id.invite_info_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					back();				}			}		);		textTitle = ( TextView ) findViewById( R.id.invite_info_title );		text1 = ( TextView ) findViewById( R.id.invite_info_text1 );		text2 = ( TextView ) findViewById( R.id.invite_info_text2 );		listView = ( ListView ) findViewById( R.id.invite_info_list );		imageCheck = ( ImageView ) findViewById( R.id.invite_info_check );				if( isInviteMe )		{			textTitle.setText( "受邀详情" );			text1.setText( "受邀 " + list.size() );			if( hasAnswered )			{				imageCheck.setVisibility( View.VISIBLE );				text2.setText( "已作答" );			}			adapter1 = new InviteListAdapter1( this, list );			adapter1.setMyListener( this );			listView.setAdapter( adapter1 );		}		else		{			textTitle.setText( "邀请详情" );			text1.setText( "邀请 " + list.size() );			adapter2 = new InviteListAdapter2( this, list );			adapter2.setMyListener( this );			listView.setAdapter( adapter2 );		}	}		private void back()	{		finish();	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}	public void clickUser( int index ) 	{		chooseId = list.get( index ).getInviterId();		getUser();	}		private void getUser()	{		UserEntity user = MyDatabaseHelper.getInstance( this ).getFriend( chooseId );		if( user == null )		{			if( Tool.isNetworkConnected( this ) == true )			{				dialog = new Dialog( this, R.style.dialog_progress );				LayoutInflater inflater = LayoutInflater.from( this );  				View view = inflater.inflate( R.layout.dialog_progress, null );				TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );				textView.setText( "正在获取用户资料" );								WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();				layoutParams.alpha = 0.8f;				dialog.getWindow().setAttributes( layoutParams );				dialog.setContentView( view );				dialog.setCancelable( false );				dialog.setOnKeyListener				(					new OnKeyListener()					{						public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 						{							if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )							{								if( connection != null )								{									connection.disconnect();								}								dialog.dismiss();								return true;							}							return false;						}					}				);				dialog.show();								new Thread		    	(		    		new Thread()		    		{		    			public void run()		    			{		    				doLoadInformation();		    			}		    		}		    	).start();			}			else			{				Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();			}		}		else		{			showUser( user, 1 );		}	}		private void doLoadInformation()	{		final String urlString = Information.Server_Url + "/api/user/" + chooseId + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				UserEntity user = new UserEntity();				user.setId( chooseId );				user.setName( jsonObject.getString( "name" ) );				user.setHeadUrl( jsonObject.getString( "headUrl" ) );				user.setSex( jsonObject.getInt( "sex" ) );				user.setBirthday( jsonObject.getString( "birthyear" ) );				user.setPku( jsonObject.getString( "pku" ) );				user.setNowHome( jsonObject.getString( "base" ) );				user.setOldHome( jsonObject.getString( "hometown" ) );				user.setQq( jsonObject.getString( "qq" ) );				user.setJob1( jsonObject.getString( "company" ) );				user.setJob2( jsonObject.getString( "department" ) );				user.setJob3( jsonObject.getString( "job" ) );				user.setVersion( jsonObject.getInt( "version" ) );				user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				user.setAnswerCount( jsonObject.getInt( "answerCount" ) );				user.setQuestionCount( jsonObject.getInt( "questionCount" ) );				user.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				user.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				user.setTag( temp );								Message message = handler.obtainMessage();				message.what = 6;				message.obj = user;				handler.sendMessage( message );			}			else			{				handler.sendEmptyMessage( 7 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 7 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		private void showUser( UserEntity user, int type )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();		bundle.putInt( "type", type );		bundle.putSerializable( "user", user );		intent.putExtras( bundle );		startActivity( intent );	}	public void clickUser_( int index ) 	{		chooseId = list.get( index ).getInviterId();		getUser();	}}