package com.pku.xiaoyoubang.view;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.AlertDialog;import android.app.Dialog;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.content.SharedPreferences;import android.graphics.drawable.BitmapDrawable;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.Gravity;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.ViewGroup.LayoutParams;import android.view.Window;import android.view.WindowManager;import android.widget.Button;import android.widget.ImageView;import android.widget.PopupWindow;import android.widget.RelativeLayout;import android.widget.TextView;import android.widget.Toast;import com.nostra13.universalimageloader.core.ImageLoader;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class MyInfoActivity extends Activity{	private ImageView registerHead;	private ImageView imageHead;	private Button buttonEdit;		private TextView text1;	private ImageView imageSex;	private TextView text3;	private TextView text4;	private TextView text5;	private TextView text6;	private TextView text7;	private TextView text8;	private TextView text9;	private TextView text10;	private TextView text11;	private TextView textInvite;		private RelativeLayout layout1;	private RelativeLayout layout2;	private RelativeLayout layout3;		private RelativeLayout layoutSelf;	private RelativeLayout layoutComment;	private TextView textComment;	private TextView textCommentCount;	private String comment = "";	private int commentCount = 0;		private Button button1;	private Button button2;	private Handler handler;		private Dialog dialog;	private PopupWindow pop;		private ImageLoader imageLoader;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //update Success					reloadData();					save();					break;				case 6 : //get user success					if( dialog != null )					{						dialog.dismiss();					}					showUser( ( UserEntity ) message.obj, 0 );					break;				case 7 : //get user failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "获取用户资料失败" );					break;				case 8 :					textComment.setText( comment );					textCommentCount.setText( commentCount + " >" );					break;				}			}		};		this.imageLoader = ImageLoader.getInstance();				initView();	}		private void initView()	{		setContentView( R.layout.my_info );				imageHead = ( ImageView ) findViewById( R.id.my_info_info_head );		imageHead.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					showBig();				}			}		);		registerHead = ( ImageView ) findViewById( R.id.my_info_info_layout1_head );		if( !Information.InviteUserHeadUrl.equals( "" ) )		{			imageLoader.displayImage( Information.Server_Url + Information.InviteUserHeadUrl, registerHead, Information.options );		}				buttonEdit = ( Button ) findViewById( R.id.my_info_edit );		buttonEdit.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					edit();				}							}		);				text1 = ( TextView ) findViewById( R.id.my_info_info_name );		text1.setText( Information.Name );		imageSex = ( ImageView ) findViewById( R.id.my_info_info_sex );		if( Information.Sex == 1 )		{			imageSex.setImageResource( R.drawable.male_color );		}		else		{			imageSex.setImageResource( R.drawable.female_color );		}		text3 = ( TextView ) findViewById( R.id.my_info_info_praise_count );			text4 = ( TextView ) findViewById( R.id.my_info_info_birthday );				text5 = ( TextView ) findViewById( R.id.my_info_info_pku );		text5.setText( Tool.getFullPku( Information.PKU_Value ) );		text6 = ( TextView ) findViewById( R.id.my_info_info_job );		text7 = ( TextView ) findViewById( R.id.my_info_info_home );		text8 = ( TextView ) findViewById( R.id.my_info_info_qq );		text9 = ( TextView ) findViewById( R.id.my_info_info_tag );				text10 = ( TextView ) findViewById( R.id.my_info_info_question_count );				text11 = ( TextView ) findViewById( R.id.my_info_info_answer_count );		textInvite = ( TextView ) findViewById( R.id.my_info_info_layout1_name );				layoutSelf = ( RelativeLayout ) findViewById( R.id.my_info_info_layout_self );		layoutSelf.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showIntro();				}			}		);				layoutComment = ( RelativeLayout ) findViewById( R.id.my_info_info_layout_leaveword );		textComment = ( TextView ) findViewById( R.id.my_info_info_layout_comment );		textCommentCount = ( TextView ) findViewById( R.id.my_info_info_layout_comment_count );		textCommentCount.setText( "0 >" );		layoutComment.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showComment();				}			}		);				layout1 = ( RelativeLayout ) findViewById( R.id.my_info_info_layout1 );		layout1.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showRegister();				}			}		);				layout2 = ( RelativeLayout ) findViewById( R.id.my_info_info_layout2 );		layout2.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showUserQuestion( true );				}			}		);				layout3 = ( RelativeLayout ) findViewById( R.id.my_info_info_layout3 );		layout3.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showUserQuestion( false );				}			}		);				button1 = ( Button ) findViewById( R.id.my_info_button_change_password );		button1.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					changePassword();				}			}		);				button2 = ( Button ) findViewById( R.id.my_info_button_logout );		button2.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					logout();				}			}		);				reloadData();	}		private void showRegister()	{		UserEntity user = null;		if( Information.InviteUserName.equals( "元老" ) )		{			return;		}		else		{			user = MyDatabaseHelper.getInstance( this ).getFriend( Information.InviteUserId );		}		if( user == null )		{			if( Tool.isNetworkConnected( this ) == true )			{				dialog = new Dialog( this, R.style.dialog_progress );				LayoutInflater inflater = LayoutInflater.from( this );  				View view = inflater.inflate( R.layout.dialog_progress, null );				TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );				textView.setText( "正在获取用户资料" );								WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();				layoutParams.alpha = 0.8f;				dialog.getWindow().setAttributes( layoutParams );				dialog.setContentView( view );				dialog.setCancelable( false );				dialog.setOnKeyListener				(					new OnKeyListener()					{						public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 						{							if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )							{								dialog.dismiss();								return true;							}							return false;						}					}				);				dialog.show();								new Thread		    	(		    		new Thread()		    		{		    			public void run()		    			{		    				doLoadInformation1();		    			}		    		}		    	).start();			}			else			{				Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();			}		}		else		{			showUser( user, 1 );		}	}		private void doLoadInformation1()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + Information.InviteUserId + "?token=" + Information.Token;						JSONObject jsonObject = Tool.doGetWithUrl( urlString );			if( jsonObject == null )			{				handler.sendEmptyMessage( 7 );			}			else			{				UserEntity user = new UserEntity();				user.setId( Information.InviteUserId );				user.setName( jsonObject.getString( "name" ) );				user.setHeadUrl( jsonObject.getString( "headUrl" ) );				user.setSex( jsonObject.getInt( "sex" ) );				user.setBirthday( jsonObject.getString( "birthyear" ) );				user.setPku( jsonObject.getString( "pku" ) );				user.setNowHome( jsonObject.getString( "base" ) );				user.setOldHome( jsonObject.getString( "hometown" ) );				user.setQq( jsonObject.getString( "qq" ) );				user.setJob1( jsonObject.getString( "company" ) );				user.setJob2( jsonObject.getString( "department" ) );				user.setJob3( jsonObject.getString( "job" ) );				user.setVersion( jsonObject.getInt( "version" ) );				user.setPraiseCount( jsonObject.getInt( "praisedCount" ) );				user.setAnswerCount( jsonObject.getInt( "answerCount" ) );				user.setQuestionCount( jsonObject.getInt( "questionCount" ) );				user.setAnswerMe( jsonObject.getInt( "answerMeCount" ) );				user.setMeAnswer( jsonObject.getInt( "myAnswerCount" ) );				user.setIntro( jsonObject.getString( "intro" ) );				String[] temp = new String[]{ "", "", "", "", "" };				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					temp[ i ] = tags.getString( i );				}				user.setTag( temp );				if( jsonObject.has( "invitedBy" ) )				{					user.setInviteName( jsonObject.getJSONObject( "invitedBy" ).getString( "name" ) );					user.setInviteHeadUrl( jsonObject.getJSONObject( "invitedBy" ).getString( "headUrl" ) );					user.setInviteUserId( jsonObject.getJSONObject( "invitedBy" ).getString( "id" ) );				}				else				{					user.setInviteName( "元老" );					user.setInviteHeadUrl( "" );					user.setInviteUserId( "" );				}								Message message = handler.obtainMessage();				message.what = 6;				message.obj = user;				handler.sendMessage( message );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 7 );		}	}		private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		private void showUser( UserEntity user, int type )	{		Intent intent = new Intent( this, UserInfoActivity.class );		Bundle bundle = new Bundle();		bundle.putInt( "type", type );		bundle.putSerializable( "user", user );		intent.putExtras( bundle );		startActivity( intent );	}		private void showIntro()	{		Intent intent = new Intent( this, UserIntroActivity.class );		intent.putExtra( "name", Information.Name );		intent.putExtra( "value", Information.Intro );		intent.putExtra( "id", Information.Id );		startActivity( intent );	}		private void showComment()	{		Intent intent = new Intent( this, CommentListActivity.class );		intent.putExtra( "type", 4 );		intent.putExtra( "userId", Information.Id );		intent.putExtra( "userName", "我" );		startActivity( intent );	}		private void changePassword()	{		startActivity( new Intent( this, ChangePasswordActivity.class ) );	}		private void logout()	{		AlertDialog.Builder dialog = new AlertDialog.Builder( this );        dialog.setTitle( "退出登录" ).setMessage( "确定要退出登录吗？" )        .setPositiveButton( "确定", new DialogInterface.OnClickListener()         {        	public void onClick( DialogInterface dialog, int which )         	{        		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );        		SharedPreferences.Editor editor = shared.edit();        		editor.putString( "token", "" );        		editor.putBoolean( "showStart", false );        		editor.commit();        		        		Information.InviteUserName = "元老";				Information.InviteUserHeadUrl = "";				Information.InviteUserId = "";        		        		Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() ); 				intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP ); 				startActivity( intent );        	}        }).setNegativeButton( "取消", new DialogInterface.OnClickListener()         {        	public void onClick( DialogInterface dialog, int which )         	{        		dialog.cancel();        	}        }).create().show();	}		private void doLoadInformation()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "?token=" + Information.Token;			JSONObject jsonObject = Tool.doGetWithUrl( urlString );			if( jsonObject != null )			{				Information.Name = jsonObject.getString( "name" );				Information.HeadUrl = jsonObject.getString( "headUrl" );				Information.Sex = jsonObject.getInt( "sex" );				Information.Birthday = jsonObject.getString( "birthyear" );				Information.PKU_Value = jsonObject.getString( "pku" );				Information.Now_Home = jsonObject.getString( "base" );				Information.Old_Home = jsonObject.getString( "hometown" );				Information.QQ = jsonObject.getString( "qq" );				Information.Company = jsonObject.getString( "company" );				Information.Part = jsonObject.getString( "department" );				Information.Job = jsonObject.getString( "job" );				Information.Version = jsonObject.getInt( "version" );				Information.Praise_Count = jsonObject.getInt( "praisedCount" );				Information.Answer_Count = jsonObject.getInt( "answerCount" );				Information.Question_Count = jsonObject.getInt( "questionCount" );				Information.Intro =  jsonObject.getString( "intro" );				if( jsonObject.has( "invitedBy" ) )				{					Information.InviteUserName = jsonObject.getJSONObject( "invitedBy" ).getString( "name" );					Information.InviteUserId = jsonObject.getJSONObject( "invitedBy" ).getString( "id" );					Information.InviteUserHeadUrl = jsonObject.getJSONObject( "invitedBy" ).getString( "headUrl" );				}				else				{					Information.InviteUserName = "元老";					Information.InviteUserHeadUrl = "";					Information.InviteUserId = "";				}				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					Information.Tag[ i ] = tags.getString( i );				}				for( int i = count; i < 5; i ++ )				{					Information.Tag[ i ] = "";				}				handler.sendEmptyMessage( 0 );			}		}		catch( Exception ex ) {}	}		private void refreshUserComments()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "/comments?token=" + Information.Token;						JSONObject json = new JSONObject();			json.put( "after", "" );						JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result == null )			{							}			else			{				if( result.getInt( "result" ) == 4000 )				{					JSONArray array = result.getJSONArray( "comments" );					commentCount = array.length();					if( commentCount > 0 )					{						JSONObject object = array.getJSONObject( 0 );						comment = object.getString( "name" ) + "：" + object.getString( "content" );					}					handler.sendEmptyMessage( 8 );				}			}		}		catch( Exception ex )		{			ex.printStackTrace();		}	}		@SuppressWarnings("deprecation")	private void showBig()	{		View view = LayoutInflater.from( this ).inflate( R.layout.view_head_big_image, null );         // 创建PopupWindow对象         pop = new PopupWindow( view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );        // 需要设置一下此参数，点击外边可消失         pop.setBackgroundDrawable( new BitmapDrawable() );         //设置点击窗口外边窗口消失         pop.setOutsideTouchable( true );         // 设置此参数获得焦点，否则无法点击         pop.setFocusable( true );         pop.showAtLocation( this.getWindow().getDecorView(), Gravity.CENTER, 0, 0 );                ImageView image = ( ImageView ) view.findViewById( R.id.view_head_big_image_id );		imageLoader.displayImage( Information.Server_Url + Information.HeadUrl, image, Information.options );		image.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					pop.dismiss();				}			}		);	}		private void save()	{		UserEntity entity = new UserEntity();		entity.setId( Information.Id );		entity.setName( Information.Name );		entity.setHeadUrl( Information.HeadUrl );		entity.setSex( Information.Sex );		entity.setBirthday( Information.Birthday );		entity.setPku( Information.PKU_Value );		entity.setNowHome( Information.Now_Home );		entity.setOldHome( Information.Old_Home );		entity.setQq( Information.QQ );		entity.setJob1( Information.Company );		entity.setJob2( Information.Part );		entity.setJob3( Information.Job );		entity.setVersion( Information.Version );		entity.setTag( Information.Tag );		entity.setPraiseCount( Information.Praise_Count );		entity.setQuestionCount( Information.Question_Count );		entity.setAnswerCount( Information.Answer_Count );		entity.setInviteHeadUrl( Information.InviteUserHeadUrl );		entity.setInviteName( Information.InviteUserName );		entity.setInviteUserId( Information.InviteUserId );		entity.setIntro( Information.Intro );				MyDatabaseHelper.getInstance( this ).insertUser( entity );	}		private void reloadData()	{		text4.setText( Information.Birthday + "年生人" );		text7.setText( "现居：" + Information.Now_Home + "    家乡：" + Information.Old_Home );				text8.setText( "电邮/QQ/微信：" + Information.QQ );		String tag = "";		for( int i = 0; i < 5; i ++ )		{			if( !Information.Tag[ i ].equals( "" ) ) tag += "[" + Information.Tag[ i ] + "]  ";		}		text9.setText( tag );		text6.setText( Information.Company + "  " + Information.Part + "  " + Information.Job );		text3.setText( "获赞 " + Information.Praise_Count );		text10.setText( Information.Question_Count + ">" );		text11.setText( Information.Answer_Count + ">" );		imageHead.setTag( Information.HeadUrl );		imageLoader.displayImage( Information.Server_Url + Information.HeadUrl, imageHead, Information.options );		textInvite.setText( Information.InviteUserName );				if( !Information.InviteUserHeadUrl.equals( "" ) )		{			imageLoader.displayImage( Information.Server_Url + Information.InviteUserHeadUrl, registerHead, Information.options );		}	}		private void edit()	{		startActivityForResult( new Intent( this, EditInformationActivity.class ), 1011 );	}		private void showUserQuestion( boolean isQuestion )	{		if( isQuestion )		{			Intent intent = new Intent( this, QuestionListActivity.class );			intent.putExtra( "type", 1 );			intent.putExtra( "count", Information.Question_Count );			intent.putExtra( "name", Information.Name );			intent.putExtra( "id", Information.Id );						startActivity( intent );		}		else		{			Intent intent = new Intent( this, AnswerListActivity.class );			intent.putExtra( "count", Information.Answer_Count );			intent.putExtra( "name", Information.Name );			intent.putExtra( "id", Information.Id );						startActivity( intent );		}	}		public void onResume() 	{		super.onResume();				new Thread		(			new Thread()			{				public void run()				{					doLoadInformation();				}			}		).start();				new Thread		(			new Thread()			{				public void run()				{					refreshUserComments();				}			}		).start();				MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 1 )		{			reloadData();		}		else if( resultCode == 5 )		{			comment = data.getStringExtra( "comment" );			commentCount = data.getIntExtra( "commentCount", 0 );			textComment.setText( comment );			textCommentCount.setText( commentCount + " >" );		}	}}