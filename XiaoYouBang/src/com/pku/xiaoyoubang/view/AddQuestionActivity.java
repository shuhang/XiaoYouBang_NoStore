package com.pku.xiaoyoubang.view;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pku.xiaoyoubang.R;
import com.pku.xiaoyoubang.entity.AnswerEntity;
import com.pku.xiaoyoubang.entity.CommentEntity;
import com.pku.xiaoyoubang.entity.InviteEntity;
import com.pku.xiaoyoubang.entity.QuestionEntity;
import com.pku.xiaoyoubang.tool.Information;
import com.pku.xiaoyoubang.tool.MyApplication;
import com.pku.xiaoyoubang.tool.Tool;
import com.umeng.analytics.MobclickAgent;

public class AddQuestionActivity extends Activity
{
	private Button buttonBack;
	private Button buttonFinish;
	private Button buttonPicture;
	
	private TextView textView;
	
	private EditText textTitle;
	private EditText textInfo;
	private CheckBox box;
	
	private Dialog dialog;
	private HttpURLConnection connection = null;
	
	private Handler handler;
	
	private String id;
	private String title;
	private String info;
	private String time;
	
	private ArrayList< String > pictureListBig = new ArrayList< String >();
	private ArrayList< String > pictureListSmall = new ArrayList< String >();
	
	private int index;
	private ArrayList< String > pictureList = new ArrayList< String >();
	@SuppressLint("HandlerLeak")
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		MyApplication.getInstance().addActivity( this );
		
		File file1 = new File( Information.Temp_Image_Path );
    	if( !file1.exists() )
    		file1.mkdirs();
		
		handler = new Handler()
		{
			public void handleMessage( Message message )
			{					
				switch( message.what )
				{
				case 0 : //add success
					if( dialog != null )
					{
						dialog.dismiss();
					}
					addQuestionSuccess();
					break;
				case 1 : //add failed			
					if( dialog != null )
					{
						dialog.dismiss();
					}
					showError( "发布失败" );
					break;
				case 2 : //upload success
					index ++;
					pictureList.add( ( String ) message.obj );
					judgeAdd();
					break;
				}
			}
		};
		
		initView();
	}
	
	private void initView()
	{
		setContentView( R.layout.add_question );
		
		textTitle = ( EditText ) findViewById( R.id.add_question_input1 );
		textTitle.setHint( "请简要描述你的问题，至少包含一个问号" );
		textInfo = ( EditText ) findViewById( R.id.add_question_input2 );
		textInfo.setHint( "请补充描述相关的背景、想法、要求等…" );
		
		box = ( CheckBox ) findViewById( R.id.add_question_checkbox );
		box.setEnabled( false );
		box.setOnCheckedChangeListener
		(
			new OnCheckedChangeListener()
			{
				public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) 
				{
					box.setChecked( isChecked );
					if( isChecked )
					{
						box.setBackgroundResource( R.drawable.check_yes );
					}
					else
					{
						box.setBackgroundResource( R.drawable.check_no );
					}
				}
			}
		);
		
		buttonBack = ( Button ) findViewById( R.id.add_question_button_back );
		buttonBack.setText( "<  " );
		buttonBack.setOnClickListener
		(
			new OnClickListener()
			{
				public void onClick( View view )
				{
					if( Tool.isFastDoubleClick() )
					{
						return;
					}
					back();
				}
			}
		);
		
		buttonFinish = ( Button ) findViewById( R.id.add_question_button_finish );
		buttonFinish.setOnClickListener
		(
			new OnClickListener()
			{
				public void onClick( View view )
				{
					if( Tool.isFastDoubleClick() )
					{
						return;
					}
					judgeInput();
				}
			}
		);
		
		buttonPicture = ( Button ) findViewById( R.id.add_question_picture );
		buttonPicture.setOnClickListener
		(
			new OnClickListener()
			{
				public void onClick( View view )
				{
					if( Tool.isFastDoubleClick() )
					{
						return;
					}
					addPicture();
				}
			}
		);
		
		textTitle.setOnFocusChangeListener
		(
			new OnFocusChangeListener()
			{
				public void onFocusChange( View v, boolean hasFocus )
				{
					if( !hasFocus )
					{
						String title = textTitle.getText().toString();
						int length = title.length();
						if( length > 0 )
						{
							if( title.indexOf( "?" ) == -1 && title.indexOf( "？" ) == -1 )
							{
								if( length < 36 ) title += "？";
								else
								{
									title = title.substring( 0, title.length() - 1 ) + "？";
								}
							}
						}
						textTitle.setText( title );
					}
				}
			}
		);
	}
	
	private void addPicture()
	{
		Intent intent = new Intent( this, AddPictureActivity.class );
		intent.putStringArrayListExtra( "pictureListBig", pictureListBig );
		intent.putStringArrayListExtra( "pictureListSmall", pictureListSmall );
		startActivityForResult( intent, 0 );
	}
	
	private void addQuestionSuccess()
	{
		QuestionEntity entity = new QuestionEntity();
		entity.setId( id );
		entity.setQuestionInfo( info );
		entity.setQuestionTitle( title );
		entity.setAnswerCount( 0 );
		entity.setAnswerList( new ArrayList< AnswerEntity >() );
		entity.setCommentList( new ArrayList< CommentEntity >() );
		entity.setCompany( Information.Company );
		entity.setCreateTime( time );
		entity.setHasAnswered( false );
		entity.setInviteMeList( new ArrayList< InviteEntity >() );
		entity.setJob( Information.Job );
		entity.setMyInviteList( new ArrayList< InviteEntity >() );
		entity.setNew( false );
		entity.setPKU( Information.PKU_Value );
		entity.setPraiseCount( 0 );
		entity.setSex( Information.Sex );
		entity.setModified( false );
		entity.setModifyTime( time );
		entity.setUpdateTime( time );
		entity.setUserHeadUrl( Information.HeadUrl );
		entity.setUserId( Information.Id );
		entity.setUserName( Information.Name );
		entity.setNew( false );
		entity.setInvisible( box.isChecked() );
		entity.setImageList( pictureListSmall );
		
		Tool.deleteAllTempImage();
		
		Intent intent = getIntent();
		intent.putExtra( "question", entity );
		setResult( 2, intent );
		finish();
	}
	
	private void judgeInput()
	{
		title = textTitle.getText().toString();
		if( title.length() == 0 )
		{
			showError( "标题不能为空" );
			return;
		}
		else if( title.indexOf( "?" ) == -1 && title.indexOf( "？" ) == -1 )
		{
			if( title.length() < 36 ) title += "？";
			else
			{
				title = title.substring( 0, title.length() - 1 ) + "？";
			}
			showError( "标题必须包含问号且不能超过36字" );
			return;
		}
		
		info = textInfo.getText().toString();
		if( info.length() == 0 )
		{
			showError( "为了他人能更好地回答你的问题，请用力填写描述" );
			return;
		}
		
		index = 0;
		pictureList.clear();
		startAdd();
	}
	
	private void startAdd()
	{
		if( Tool.isNetworkConnected( this ) == true )
		{
			dialog = new Dialog( this, R.style.dialog_progress );
			LayoutInflater inflater = LayoutInflater.from( this );  
			View view = inflater.inflate( R.layout.dialog_progress, null );
			textView = ( TextView ) view.findViewById( R.id.dialog_textview );		
			WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
			layoutParams.alpha = 0.8f;
			dialog.getWindow().setAttributes( layoutParams );
			dialog.setContentView( view );
			dialog.setCancelable( false );
			dialog.setOnKeyListener
			(
				new OnKeyListener()
				{
					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 
					{
						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
						{					
							if( connection != null )
							{
								connection.disconnect();
							}
							dialog.dismiss();
							return true;
						}
						return false;
					}
				}
			);
			dialog.show();
			
			judgeAdd();
		}
		else
		{
			showError( "网络不可用，请打开网络" );
		}
	}
	
	private void judgeAdd()
	{
		if( pictureListBig.size() == index )
		{
			textView.setText( "正在添加提问" );
		}
		else
		{
			textView.setText( "正在上传第" + ( index + 1 ) + "张照片" );
		}
		new Thread
    	(
    		new Thread()
    		{
    			public void run()
    			{
    				if( pictureListBig.size() == index )
    				{
    					doAddQuestion();
    				}
    				else
    				{
    					doUploadImage();
    				}
    			}
    		}
    	).start();
	}
	
	private void doUploadImage()
	{
		final String urlString = Information.Server_Url + "/api/image?token=" + Information.Token;
		try
		{
			URL url = new URL( urlString );
			connection = ( HttpURLConnection ) url.openConnection();  
			connection.setRequestProperty( "Connection", "keep-alive" );
			connection.setRequestMethod( "POST" );
			connection.setConnectTimeout( 10000 );
			connection.setReadTimeout( 60000 );
			connection.setDoOutput( true );
			
            String symbol = pictureListBig.get( index ).substring( pictureListBig.get( index ).lastIndexOf( "." ) + 1, pictureListBig.get( index ).length() );
            if( symbol.compareToIgnoreCase( "jpg" ) == 0 || symbol.compareToIgnoreCase( "jpeg" ) == 0 )
            {
                symbol = "jpeg";
            }
            else
            {
            	symbol = "png";
            }
			String contentDisposition = "Content-Disposition: form-data; name=\"head\"; filename=\"" + pictureListBig.get( index ) + "\"";
            String contentType = "Content-Type: image/" + symbol;
            String BOUNDRY = "----WebKitFormBoundaryabcdefghijklmnop";
            connection.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + BOUNDRY );
            
            DataOutputStream dataOS = new DataOutputStream( connection.getOutputStream());
            dataOS.writeBytes( "--" + BOUNDRY + "\r\n" );
            dataOS.writeBytes( contentDisposition + "\r\n" );
            dataOS.writeBytes( contentType + "\r\n\r\n" );
            
			InputStream is = new FileInputStream( new File( pictureListBig.get( index ) ) );
            byte[] buffer = new byte[ 1024 ];
            int count = 0;  
            while( ( count = is.read( buffer ) ) != -1 )  
            {  
                dataOS.write( buffer, 0, count );
            }
            dataOS.writeBytes( "\r\n--" + BOUNDRY + "--\r\n" );
            
            dataOS.flush();
            dataOS.close();
            is.close();
            
			final int responseCode = connection.getResponseCode();
			if( responseCode == 200 )
			{
				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
				String temp1 = null;
				StringBuilder value = new StringBuilder();
				while( ( temp1 = reader.readLine() ) != null )
				{
					value.append( temp1 );
				}
				
				JSONObject jsonObject = new JSONObject( value.toString() );
				if( jsonObject.getInt( "result" ) == 9000 )
				{
					Message message = handler.obtainMessage();
					message.what = 2;
					message.obj = jsonObject.getString( "url" );
					handler.sendMessage( message );
				}
				else
				{
					handler.sendEmptyMessage( 1 );
				}
			}
			else
			{
				handler.sendEmptyMessage( 1 );
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			handler.sendEmptyMessage( 1 );
		}
		finally
		{
			if( connection != null )
			{
				connection.disconnect();
			}
		}
	}
	
	private void doAddQuestion()
	{
		try
		{
			final String urlString = Information.Server_Url + "/api/question";
			
			JSONObject json = new JSONObject();
			json.put( "token", Information.Token );
			json.put( "title", title );
			json.put( "info", info );
			json.put( "invisible", box.isChecked() );
			
			JSONArray array = new JSONArray();
			for( String temp : pictureList )
			{
				array.put( temp );
			}
			json.put( "images", array );
			
			JSONObject result = Tool.doPostWithUrl( urlString, json );
			if( result == null )
			{
				handler.sendEmptyMessage( 1 );
			}
			else
			{
				if( result.getInt( "result" ) == 3000 )
				{
					id = result.getString( "id" );
					time = result.getString( "modifyTime" );
					handler.sendEmptyMessage( 0 );
				}
				else
				{
					handler.sendEmptyMessage( 1 );
				}
			}
		}
		catch( Exception ex )
		{
			handler.sendEmptyMessage( 1 );
		}
	}
	
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) 
	{
		super.onActivityResult( requestCode, resultCode, data );
		if( resultCode == 1 )
		{
			pictureListSmall = data.getStringArrayListExtra( "pictureListSmall" );
			pictureListBig = data.getStringArrayListExtra( "pictureListBig" );
			if( pictureListSmall.size() > 0 )
			{
				buttonPicture.setText( "" + pictureListSmall.size() );
			}
			else
			{
				buttonPicture.setText( "" );
			}
		}
	}
	
	private void showError( String text )
	{
		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();
	}
	
	private void back()
	{
		Tool.deleteAllTempImage();
		setResult( 1 );
		finish();
	}
	
	public void onResume() 
	{
		super.onResume();
		MobclickAgent.onResume( this );
	}
	
	public void onPause()
	{
		super.onPause();
		MobclickAgent.onPause( this );
	}
}
