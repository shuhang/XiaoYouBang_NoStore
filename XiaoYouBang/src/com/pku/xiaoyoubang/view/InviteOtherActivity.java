package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.text.Editable;import android.text.TextWatcher;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.view.WindowManager;import android.widget.Button;import android.widget.EditText;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import com.nostra13.universalimageloader.core.DisplayImageOptions;import com.nostra13.universalimageloader.core.ImageLoader;import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.InviteEntity;import com.pku.xiaoyoubang.entity.InviteUserEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class InviteOtherActivity extends Activity{	private Button buttonBack;	private Button buttonCommit;		private ImageView imageHead;	private ImageView imageCheck;	private EditText textInput1;	private EditText textInput2;	private TextView textSymbol;		private String name;	private String reason;	private String id;	private String questionId;	private String headUrl;	private String time;		public static List< String > answerUser;		private Dialog dialog;	private HttpURLConnection connection = null;		private Handler handler;		private ImageLoader imageLoader;	private DisplayImageOptions options;		private boolean noJudge = false;		@SuppressWarnings("unchecked")	protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				if( dialog != null )				{					dialog.dismiss();				}				switch( message.what )				{				case 0 : //refresh success					inviteSuccess();					break;				case 1 : //load more success					showError( "邀请失败" );					break;				}			}		};				questionId = getIntent().getExtras().getString( "id" );		ArrayList<?> temp = getIntent().getExtras().getParcelableArrayList( "list" );		answerUser = ( ArrayList< String > ) temp.get( 0 );				ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder( this ).build();		ImageLoader.getInstance().init( config );				this.options = new DisplayImageOptions.Builder()		.showImageOnLoading( R.drawable.head )		.showImageForEmptyUri( R.drawable.head )		.showImageOnFail( R.drawable.head )		.cacheInMemory( true )		.cacheOnDisk( true )		.build();		this.imageLoader = ImageLoader.getInstance();				initView();	}		private void initView()	{		setContentView( R.layout.invite_other );				imageHead = ( ImageView ) findViewById( R.id.invite_other_head );		imageHead.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					showInviteList();				}			}		);				imageCheck = ( ImageView ) findViewById( R.id.invite_other_check );		textInput1 = ( EditText ) findViewById( R.id.invite_other_input_name );		textInput2 = ( EditText ) findViewById( R.id.inviter_other_input_reason );		textInput2.setHint( "本邀请只有被邀请人可以看到，请写下你的邀请理由..." );		textSymbol = ( TextView ) findViewById( R.id.invite_other_symbol );				buttonBack = ( Button ) findViewById( R.id.invite_other_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					back();				}			}		);				buttonCommit = ( Button ) findViewById( R.id.invite_other_button_finish );		buttonCommit.setVisibility( View.INVISIBLE );		buttonCommit.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					judgeInput();				}			}		);				textInput1.addTextChangedListener		(			new TextWatcher()			{				public void beforeTextChanged(CharSequence s, int start,int count, int after) {}				public void onTextChanged(CharSequence s, int start,int before, int count) {}				public void afterTextChanged(Editable s) 				{					name = textInput1.getText().toString();					judgeName();				}			}		);	}		private void showInviteList()	{		startActivityForResult( new Intent( this, ChooseInviteActivity.class ), 1015 );	}		private void inviteSuccess()	{		MyDatabaseHelper.getInstance( this ).updateQuestion( QuestionInfoActivity.entity.getId(), time );				Intent intent = getIntent();		intent.putExtra( "id", id );		intent.putExtra( "headUrl", headUrl );		intent.putExtra( "name", name );		intent.putExtra( "reason", reason );		intent.putExtra( "time", time );				setResult( 6, intent );		finish();	}		private void judgeName()	{		if( !noJudge )		{			String[] result = MyDatabaseHelper.getInstance( this ).searchFriend( name );			if( result == null || result[ 0 ].equals( Information.Id ) )			{				imageCheck.setImageResource( R.drawable.no );				buttonCommit.setVisibility( View.INVISIBLE );				imageHead.setImageResource( R.drawable.head );				textSymbol.setText( "" );			}			else			{				id = result[ 0 ];				imageLoader.displayImage( Information.Server_Url + result[ 1 ], imageHead, options );								List< InviteEntity > list = QuestionInfoActivity.entity.getMyInviteList();				final int count = list.size();				for( int i = 0; i < count; i ++ )				{					if( list.get( i ).getInviterId().equals( result[ 0 ] ) )					{						textSymbol.setText( "该用户已被邀请" );							return;					}				}				if( answerUser.indexOf( id ) != -1 )				{					textSymbol.setText( "该用户已作答" );						return;				}				id = result[ 0 ];				headUrl = result[ 1 ];				buttonCommit.setVisibility( View.VISIBLE );				textSymbol.setText( "" );				imageCheck.setImageResource( R.drawable.yes );			}		}		else		{			noJudge = false;		}	}		private void judgeInput()	{		reason = textInput2.getText().toString();		if( reason.length() == 0 )		{			showError( "请输入邀请理由" );			return;		}				startInvite();	}		private void startInvite()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			textView.setText( "正在提交" );						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				doInvite();	    			}	    		}	    	).start();		}		else		{			Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();		}	}		private void doInvite()	{		final String urlString = Information.Server_Url + "/api/question/" + questionId + "/invite";		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestProperty( "Content-Type", "application/json" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.setDoOutput( true );			connection.setDoInput( true );						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "target", id );			json.put( "words", reason );			connection.getOutputStream().write( json.toString().getBytes() );			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 3000 )				{					time = jsonObject.getString( "time" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 1 );				}			}			else			{				handler.sendEmptyMessage( 1 );			}		}		catch( Exception ex )		{			ex.printStackTrace();			handler.sendEmptyMessage( 1 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 1 )		{			InviteUserEntity entity = ( InviteUserEntity ) data.getSerializableExtra( "user" );						noJudge = true;			textInput1.setText( entity.getName() );			id = entity.getId();			headUrl = entity.getHeadUrl();			imageLoader.displayImage( Information.Server_Url + headUrl, imageHead, options );			buttonCommit.setVisibility( View.VISIBLE );			textSymbol.setText( "" );			imageCheck.setImageResource( R.drawable.yes );		}	}		private void back()	{		finish();	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}}