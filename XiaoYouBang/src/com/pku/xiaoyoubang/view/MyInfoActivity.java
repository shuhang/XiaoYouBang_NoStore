package com.pku.xiaoyoubang.view;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.AlertDialog;import android.content.DialogInterface;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.View;import android.view.View.OnClickListener;import android.view.Window;import android.widget.Button;import android.widget.ImageView;import android.widget.RelativeLayout;import android.widget.TextView;import com.nostra13.universalimageloader.core.ImageLoader;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.analytics.MobclickAgent;@SuppressLint("HandlerLeak")public class MyInfoActivity extends Activity{	private ImageView registerHead;	private ImageView imageHead;	private Button buttonEdit;		private TextView text1;	private ImageView imageSex;	private TextView text3;	private TextView text4;	private TextView text5;	private TextView text6;	private TextView text7;	private TextView text8;	private TextView text9;	private TextView text10;	private TextView text11;	private TextView textIntro;		private RelativeLayout layout2;	private RelativeLayout layout3;		private Button button1;	private Button button2;	private Handler handler;		private ImageLoader imageLoader;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //update Success					reloadData();					save();					break;				}			}		};		this.imageLoader = ImageLoader.getInstance();				initView();	}		private void initView()	{		setContentView( R.layout.my_info );				imageHead = ( ImageView ) findViewById( R.id.my_info_info_head );		registerHead = ( ImageView ) findViewById( R.id.my_info_info_layout1_head );		registerHead.setVisibility( View.GONE );				buttonEdit = ( Button ) findViewById( R.id.my_info_edit );		buttonEdit.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					edit();				}							}		);				text1 = ( TextView ) findViewById( R.id.my_info_info_name );		text1.setText( Information.Name );		imageSex = ( ImageView ) findViewById( R.id.my_info_info_sex );		if( Information.Sex == 1 )		{			imageSex.setImageResource( R.drawable.male_color );		}		else		{			imageSex.setImageResource( R.drawable.female_color );		}		text3 = ( TextView ) findViewById( R.id.my_info_info_praise_count );			text4 = ( TextView ) findViewById( R.id.my_info_info_birthday );				text5 = ( TextView ) findViewById( R.id.my_info_info_pku );		text5.setText( Tool.getFullPku( Information.PKU_Value ) );		text6 = ( TextView ) findViewById( R.id.my_info_info_job );		text7 = ( TextView ) findViewById( R.id.my_info_info_home );		text8 = ( TextView ) findViewById( R.id.my_info_info_qq );		text9 = ( TextView ) findViewById( R.id.my_info_info_tag );				text10 = ( TextView ) findViewById( R.id.my_info_info_question_count );				text11 = ( TextView ) findViewById( R.id.my_info_info_answer_count );				textIntro = ( TextView ) findViewById( R.id.my_info_intro );		textIntro.setText( "自我介绍  >" );		textIntro.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showIntro();				}			}		);				layout2 = ( RelativeLayout ) findViewById( R.id.my_info_info_layout2 );		layout2.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showUserQuestion( true );				}			}		);				layout3 = ( RelativeLayout ) findViewById( R.id.my_info_info_layout3 );		layout3.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					showUserQuestion( false );				}			}		);				button1 = ( Button ) findViewById( R.id.my_info_button_change_password );		button1.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					changePassword();				}			}		);				button2 = ( Button ) findViewById( R.id.my_info_button_logout );		button2.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					logout();				}			}		);				reloadData();	}		private void showIntro()	{		Intent intent = new Intent( this, UserIntroActivity.class );		intent.putExtra( "name", Information.Name );		intent.putExtra( "value", Information.Intro );		startActivity( intent );	}		private void changePassword()	{		startActivity( new Intent( this, ChangePasswordActivity.class ) );	}		private void logout()	{		AlertDialog.Builder dialog = new AlertDialog.Builder( this );        dialog.setTitle( "退出登录" ).setMessage( "确定要退出登录吗？" )        .setPositiveButton( "确定", new DialogInterface.OnClickListener()         {        	public void onClick( DialogInterface dialog, int which )         	{        		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );        		SharedPreferences.Editor editor = shared.edit();        		editor.putString( "token", "" );        		editor.putBoolean( "showStart", false );        		editor.commit();        		        		Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() ); 				intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP ); 				startActivity( intent );        	}        }).setNegativeButton( "取消", new DialogInterface.OnClickListener()         {        	public void onClick( DialogInterface dialog, int which )         	{        		dialog.cancel();        	}        }).create().show();	}		private void doLoadInformation()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "?token=" + Information.Token;			JSONObject jsonObject = Tool.doGetWithUrl( urlString );			if( jsonObject != null )			{				Information.Name = jsonObject.getString( "name" );				Information.HeadUrl = jsonObject.getString( "headUrl" );				Information.Sex = jsonObject.getInt( "sex" );				Information.Birthday = jsonObject.getString( "birthyear" );				Information.PKU_Value = jsonObject.getString( "pku" );				Information.Now_Home = jsonObject.getString( "base" );				Information.Old_Home = jsonObject.getString( "hometown" );				Information.QQ = jsonObject.getString( "qq" );				Information.Company = jsonObject.getString( "company" );				Information.Part = jsonObject.getString( "department" );				Information.Job = jsonObject.getString( "job" );				Information.Version = jsonObject.getInt( "version" );				Information.Praise_Count = jsonObject.getInt( "praisedCount" );				Information.Answer_Count = jsonObject.getInt( "answerCount" );				Information.Question_Count = jsonObject.getInt( "questionCount" );				Information.Intro =  jsonObject.getString( "intro" );				JSONArray tags = jsonObject.getJSONArray( "tags" );				final int count = tags.length();				for( int i = 0; i < count; i ++ )				{					Information.Tag[ i ] = tags.getString( i );				}				for( int i = count; i < 5; i ++ )				{					Information.Tag[ i ] = "";				}				handler.sendEmptyMessage( 0 );			}		}		catch( Exception ex ) {}	}		private void save()	{		UserEntity entity = new UserEntity();		entity.setId( Information.Id );		entity.setName( Information.Name );		entity.setHeadUrl( Information.HeadUrl );		entity.setSex( Information.Sex );		entity.setBirthday( Information.Birthday );		entity.setPku( Information.PKU_Value );		entity.setNowHome( Information.Now_Home );		entity.setOldHome( Information.Old_Home );		entity.setQq( Information.QQ );		entity.setJob1( Information.Company );		entity.setJob2( Information.Part );		entity.setJob3( Information.Job );		entity.setVersion( Information.Version );		entity.setTag( Information.Tag );		entity.setPraiseCount( Information.Praise_Count );		entity.setQuestionCount( Information.Question_Count );		entity.setAnswerCount( Information.Answer_Count );				MyDatabaseHelper.getInstance( this ).insertUser( entity );	}		private void reloadData()	{		text4.setText( Information.Birthday + "年生人" );		text7.setText( "现居：" + Information.Now_Home + "    家乡：" + Information.Old_Home );				text8.setText( "电邮/QQ/微信：" + Information.QQ );		String tag = "";		for( int i = 0; i < 5; i ++ )		{			if( !Information.Tag[ i ].equals( "" ) ) tag += "[" + Information.Tag[ i ] + "]  ";		}		text9.setText( tag );		text6.setText( Information.Company + "  " + Information.Part + "  " + Information.Job );		text3.setText( "获赞 " + Information.Praise_Count );		text10.setText( Information.Question_Count + ">" );		text11.setText( Information.Answer_Count + ">" );		imageHead.setTag( Information.HeadUrl );		imageLoader.displayImage( Information.Server_Url + Information.HeadUrl, imageHead, Information.options );	}		private void edit()	{		startActivityForResult( new Intent( this, EditInformationActivity.class ), 1011 );	}		private void showUserQuestion( boolean isQuestion )	{		if( isQuestion )		{			Intent intent = new Intent( this, QuestionListActivity.class );			intent.putExtra( "type", 1 );			intent.putExtra( "count", Information.Question_Count );			intent.putExtra( "name", Information.Name );			intent.putExtra( "id", Information.Id );						startActivity( intent );		}		else		{			Intent intent = new Intent( this, AnswerListActivity.class );			intent.putExtra( "count", Information.Answer_Count );			intent.putExtra( "name", Information.Name );			intent.putExtra( "id", Information.Id );						startActivity( intent );		}	}		public void onResume() 	{		super.onResume();				new Thread		(			new Thread()			{				public void run()				{					doLoadInformation();				}			}		).start();				MobclickAgent.onResume( this );	}		public void onPause()	{		super.onPause();		MobclickAgent.onPause( this );	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		if( resultCode == 1 )		{			reloadData();		}	}}