package com.pku.xiaoyoubang.view;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.DialogInterface;import android.content.Intent;import android.content.DialogInterface.OnKeyListener;import android.graphics.drawable.BitmapDrawable;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.Gravity;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.WindowManager;import android.view.View.OnClickListener;import android.view.ViewGroup.LayoutParams;import android.view.Window;import android.widget.Button;import android.widget.ImageView;import android.widget.PopupWindow;import android.widget.RelativeLayout;import android.widget.TextView;import android.widget.Toast;import com.nostra13.universalimageloader.core.ImageLoader;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class UserInfoActivity extends Activity{	private Button buttonBack;		private ImageView headImage;	private TextView textName;	private TextView textPraise;	private ImageView imageSex;	private TextView textBirthday;	private TextView textPku;	private TextView textJob;	private TextView textQQ;	private TextView textHome;	private TextView textTag;	private TextView textAnswerMe;	private TextView textMyAnswer;	private TextView textSymbol;	private ImageView inviteUser;	private TextView textInvite;	private TextView textQuestionCount;	private TextView textAnswerCount;	private TextView textIntro;		private RelativeLayout layout1;	private RelativeLayout layout2;	private RelativeLayout layout3;		private PopupWindow pop;	private Dialog dialog;		private UserEntity entity;	private int type = 0;	private String userId = "";		private ImageLoader imageLoader;		private HttpURLConnection connection = null;		private Handler handler;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //refresh success					MyDatabaseHelper.getInstance( UserInfoActivity.this ).insertFriend( entity );					update();					break;				case 6 : //get user success					if( dialog != null )					{						dialog.dismiss();					}					showUser( ( UserEntity ) message.obj, 0 );					break;				case 7 : //get user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取用户资料失败" );					break;				}			}		};				entity = ( UserEntity ) getIntent().getExtras().getSerializable( "user" );		type = getIntent().getExtras().getInt( "type", 0 );		this.imageLoader = ImageLoader.getInstance();				initView();				if( type == 1 )		{			startLoad();		}		else		{			MyDatabaseHelper.getInstance( this ).insertFriend( entity );		}	}		private void initView()	{		setContentView( R.layout.friend_info );				buttonBack = ( Button ) findViewById( R.id.friend_info_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					back();				}			}		);				headImage = ( ImageView ) findViewById( R.id.friend_info_head );		headImage.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					showBig();				}			}		);				inviteUser = ( ImageView ) findViewById( R.id.friend_info_layout1_head );		if( !entity.getInviteHeadUrl().equals( "" ) )		{			imageLoader.displayImage( Information.Server_Url + entity.getInviteHeadUrl(), inviteUser, Information.options );		}		textName = ( TextView ) findViewById( R.id.friend_info_name );		textName.setText( entity.getName() );				textPraise = ( TextView ) findViewById( R.id.friend_info_praise_count );		imageSex = ( ImageView ) findViewById( R.id.friend_info_sex );		if( entity.getSex() == 1 )		{			imageSex.setBackgroundResource( R.drawable.male_color );		}		else		{			imageSex.setBackgroundResource( R.drawable.female_color );		}				textBirthday = ( TextView ) findViewById( R.id.friend_info_birthday );		textBirthday.setText( entity.getBirthday() + "年生人" );				textPku = ( TextView ) findViewById( R.id.friend_info_pku );		textPku.setText( Tool.getFullPku( entity.getPku() ) );				textJob = ( TextView ) findViewById( R.id.friend_info_job );		textJob.setText( entity.getJob1() + "  " + entity.getJob2() + "  " + entity.getJob3() );				textHome = ( TextView ) findViewById( R.id.friend_info_home );		textHome.setText( "现居：" + entity.getNowHome() + "    家乡：" + entity.getOldHome() );				textQQ = ( TextView ) findViewById( R.id.friend_info_qq );		textQQ.setText( "电邮/QQ/微信：" + entity.getQq() );				textTag = ( TextView ) findViewById( R.id.friend_info_tag  );			textAnswerMe = ( TextView ) findViewById( R.id.friend_info_other_answer_me );				textMyAnswer = ( TextView ) findViewById( R.id.friend_info_me_answer_other );				textSymbol = ( TextView ) findViewById( R.id.friend_info_layout1_symbol );		textInvite = ( TextView ) findViewById( R.id.friend_info_layout1_name );		textSymbol.setText( "注册邀请人" );		textInvite.setText( entity.getInviteName() );				textQuestionCount = ( TextView ) findViewById( R.id.friend_info_question_count );				textAnswerCount = ( TextView ) findViewById( R.id.friend_info_answer_count );				textIntro = ( TextView ) findViewById( R.id.friend_info_intro );		textIntro.setText( "自我介绍  >" );		textIntro.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showIntro();				}			}		);				layout1 = ( RelativeLayout ) findViewById( R.id.friend_info_layout1 );		layout1.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showRegister();				}			}		);				layout2 = ( RelativeLayout ) findViewById( R.id.friend_info_layout2 );		layout2.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showUserQuestion( true );				}			}		);				layout3 = ( RelativeLayout ) findViewById( R.id.friend_info_layout3 );		layout3.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showUserQuestion( false );				}			}		);				update();	}		@SuppressWarnings("deprecation")	private void showBig()	{		View view = LayoutInflater.from( this ).inflate( R.layout.view_head_big_image, null );         // 创建PopupWindow对象         pop = new PopupWindow( view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );        // 需要设置一下此参数，点击外边可消失         pop.setBackgroundDrawable( new BitmapDrawable() );         //设置点击窗口外边窗口消失         pop.setOutsideTouchable( true );         // 设置此参数获得焦点，否则无法点击         pop.setFocusable( true );         pop.showAtLocation( this.getWindow().getDecorView(), Gravity.CENTER, 0, 0 );                ImageView image = ( ImageView ) view.findViewById( R.id.view_head_big_image_id );		imageLoader.displayImage( Information.Server_Url + entity.getHeadUrl(), image, Information.options );		image.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					pop.dismiss();				}			}		);	}		private void showIntro()	{		Intent intent = new Intent( this, UserIntroActivity.class );		intent.putExtra( "name", entity.getName() );		intent.putExtra( "value", entity.getIntro() );		intent.putExtra( "id", entity.getId() );		startActivity( intent );	}		private void update()	{		headImage.setTag( entity.getHeadUrl() );		imageLoader.displayImage( Information.Server_Url + entity.getHeadUrl(), headImage, Information.options );				textPraise.setText( "获赞 "  + entity.getPraiseCount() );				String tag = "";		for( int i = 0; i < entity.getTags().length; i ++ )		{			if( !entity.getTags()[ i ].equals( "" ) )			{				tag += "[" + entity.getTags()[ i ] + "]  ";			}		}		textTag.setText( tag );				textJob.setText( entity.getJob1() + "  " + entity.getJob2() + "  " + entity.getJob3() );		textHome.setText( "现居：" + entity.getNowHome() + "    家乡：" + entity.getOldHome() );		textQQ.setText( "电邮/QQ/微信：" + entity.getQq() );				textAnswerMe.setText( "他答过我 " + entity.getAnswerMe() );		textMyAnswer.setText( "我答过他 " + entity.getMeAnswer() );				textQuestionCount.setText( entity.getQuestionCount() + " >" );		textAnswerCount.setText( entity.getAnswerCount() + " >" );		textInvite.setText( entity.getInviteName() );	}		private void startLoad()	{		new Thread		(			new Thread()			{				public void run()				{					doLoadInformation();				}			}		).start();	}		private void doLoadInformation()	{		final String urlString = Information.Server_Url + "/api/user/" + entity.getId() + "?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "GET" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 30000 );			connection.connect();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}				JSONObject jsonObject = new JSONObject( value.toString() );				entity.setHeadUrl( jsonObject.getString( "headUrl" ) );				entity.setNowHome( jsonObject.getString( "base" ) );				entity.setOldHome( jsonObject.getString( "hometown" ) );				entity.setQq( jsonObject.getString( "qq" ) );				entity.setJob1( jsonObject.getString( "company" ) );				entity.setJob2( jsonObject.getString( "department" ) );				entity.setJob3( jsonObject.getString( "job" ) );				entity.setVersion( jsonObject.getInt( "version" ) );				entity.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				entity.setAnswerCount( jsonObject.getInt( "answerCount" ) );				entity.setQuestionCount( jsonObject.getInt( "questionCount" ) );				entity.setIntro( jsonObject.getString( "intro" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				entity.setTag( temp );				entity.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				entity.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				if( jsonObject.has( "invitedBy" ) )				{					entity.setInviteName( jsonObject.getJSONObject( "invitedBy" ).getString( "name" ) );					entity.setInviteUserId( jsonObject.getJSONObject( "invitedBy" ).getString( "id" ) );					entity.setInviteHeadUrl( jsonObject.getJSONObject( "invitedBy" ).getString( "headUrl" ) );				}				else				{					entity.setInviteName( "元老" );					entity.setInviteHeadUrl( "" );					entity.setInviteUserId( "" );				}				handler.sendEmptyMessage( 0 );			}		}		catch( Exception ex )		{			ex.printStackTrace();		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		private void showUserQuestion( boolean isQuestion )	{				if( isQuestion )		{			Intent intent = new Intent( this, QuestionListActivity.class );			intent.putExtra( "type", 1 );			intent.putExtra( "count", entity.getQuestionCount() );			intent.putExtra( "name", entity.getName() );			intent.putExtra( "id", entity.getId() );						startActivity( intent );		}		else		{			Intent intent = new Intent( this, AnswerListActivity.class );			intent.putExtra( "count", entity.getAnswerCount() );			intent.putExtra( "name", entity.getName() );			intent.putExtra( "id", entity.getId() );						startActivity( intent );		}			}		private void back()	{		finish();	}		private void showRegister()	{		UserEntity user = null;		if( entity.getInviteName().equals( "元老" ) )		{			return;		}		else		{			userId = entity.getInviteUserId();			user = MyDatabaseHelper.getInstance( this ).getFriend( userId );		}		if( user == null )		{			if( Tool.isNetworkConnected( this ) == true )			{				dialog = new Dialog( this, R.style.dialog_progress );				LayoutInflater inflater = LayoutInflater.from( this );  				View view = inflater.inflate( R.layout.dialog_progress, null );				TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );				textView.setText( "正在获取用户资料" );								WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();				layoutParams.alpha = 0.8f;				dialog.getWindow().setAttributes( layoutParams );				dialog.setContentView( view );				dialog.setCancelable( false );				dialog.setOnKeyListener				(					new OnKeyListener()					{						public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 						{							if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )							{								if( connection != null )								{									connection.disconnect();								}								dialog.dismiss();								return true;							}							return false;						}					}				);				dialog.show();								new Thread		    	(		    		new Thread()		    		{		    			public void run()		    			{		    				doLoadInformation1();		    			}		    		}		    	).start();			}			else			{				Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();			}		}		else		{			showUser( user, 1 );		}	}		private void doLoadInformation1()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + userId + "?token=" + Information.Token;						JSONObject jsonObject = Tool.doGetWithUrl( urlString );			if( jsonObject == null )			{				handler.sendEmptyMessage( 7 );			}			else			{				UserEntity user = new UserEntity();				user.setId( userId );				user.setName( jsonObject.getString( "name" ) );				user.setHeadUrl( jsonObject.getString( "headUrl" ) );				user.setSex( jsonObject.getInt( "sex" ) );				user.setBirthday( jsonObject.getString( "birthyear" ) );				user.setPku( jsonObject.getString( "pku" ) );				user.setNowHome( jsonObject.getString( "base" ) );				user.setOldHome( jsonObject.getString( "hometown" ) );				user.setQq( jsonObject.getString( "qq" ) );				user.setJob1( jsonObject.getString( "company" ) );				user.setJob2( jsonObject.getString( "department" ) );				user.setJob3( jsonObject.getString( "job" ) );				user.setVersion( jsonObject.getInt( "version" ) );				user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				user.setAnswerCount( jsonObject.getInt( "answerCount" ) );				user.setQuestionCount( jsonObject.getInt( "questionCount" ) );				user.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				user.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				user.setIntro( jsonObject.getString( "intro" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				user.setTag( temp );				if( jsonObject.has( "invitedBy" ) )				{					user.setInviteName( jsonObject.getJSONObject( "invitedBy" ).getString( "name" ) );					user.setInviteHeadUrl( jsonObject.getJSONObject( "invitedBy" ).getString( "headUrl" ) );					user.setInviteUserId( jsonObject.getJSONObject( "invitedBy" ).getString( "id" ) );				}				else				{					user.setInviteName( "元老" );					user.setInviteHeadUrl( "" );					user.setInviteUserId( "" );				}								Message message = handler.obtainMessage();				message.what = 6;				message.obj = user;				handler.sendMessage( message );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 7 );		}	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		private void showUser( UserEntity user, int type )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();		bundle.putInt( "type", type );		bundle.putSerializable( "user", user );		intent.putExtras( bundle );		startActivity( intent );	}		public void onResume() 	{		super.onResume();		MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}}