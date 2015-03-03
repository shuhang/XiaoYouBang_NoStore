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
import android.app.AlertDialog;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.pku.xiaoyoubang.R;
import com.pku.xiaoyoubang.entity.AnswerEntity;
import com.pku.xiaoyoubang.entity.CommentEntity;
import com.pku.xiaoyoubang.tool.Information;
import com.pku.xiaoyoubang.tool.MyApplication;
import com.pku.xiaoyoubang.tool.MyDatabaseHelper;
import com.pku.xiaoyoubang.tool.Tool;
import com.umeng.analytics.MobclickAgent;

public class AddAnswerActivity extends Activity
{
	private Button buttonBack;
	private Button buttonFinish;
	private Button buttonPicture;
	
	private TextView textTitle;
	private TextView textQuestion;
	private EditText textInfo;
	private CheckBox box;
	
	private RelativeLayout layout;
	
	private Dialog dialog;
	private HttpURLConnection connection = null;
	
	private String questionId;
	private String answerId;
	private String info;
	private String questionTitle;
	
	private ArrayList< String > pictureListBig = new ArrayList< String >();
	private ArrayList< String > pictureListSmall = new ArrayList< String >();
	
	private ArrayList< String > pictureListBigNew = new ArrayList< String >();
	private ArrayList< String > pictureListSmallNew = new ArrayList< String >();
	
	private Handler handler;
	private String time;
	
	private int type;
	private int index;
	private TextView textView;
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
		
		if( getIntent().getIntExtra( "type", 0 ) == 0 )
		{
			type = 0;
			questionId = getIntent().getStringExtra( "questionId" );
			questionTitle = getIntent().getStringExtra( "questionTitle" );
		}
		else
		{
			type = 1;
			questionTitle = getIntent().getStringExtra( "questionTitle" );
		}
		
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
					addAnswerSuccess();
					break;
				case 1 : //add failed
					if( dialog != null )
					{
						dialog.dismiss();
					}
					showError( "添加回答失败" );
					break;
				case 2 : //add success
					if( dialog != null )
					{
						dialog.dismiss();
					}
					editAnswerSuccess();
					break;
				case 3 : //add failed
					if( dialog != null )
					{
						dialog.dismiss();
					}
					showError( "编辑回答失败" );
					break;
				case 4 : //upload success
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
		setContentView( R.layout.add_answer );
		
		textTitle = ( TextView ) findViewById( R.id.add_answer_title );
		if( type == 0 )
		{
			textTitle.setText( "添加回答" );
		}
		else
		{
			textTitle.setText( "编辑回答" );
		}
		
		textQuestion = ( TextView ) findViewById( R.id.add_answer_question );
		textQuestion.setText( "问题：" + questionTitle );
		
		textInfo = ( EditText ) findViewById( R.id.add_answer_input );
		textInfo.setHint( "写下你的答案、建议、参考…" );
		
		layout = ( RelativeLayout ) findViewById( R.id.add_answer_layout2 );
		
		box = ( CheckBox ) findViewById( R.id.add_answer_checkbox );
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
		
		buttonBack = ( Button ) findViewById( R.id.add_answer_button_back );
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
		
		buttonFinish = ( Button ) findViewById( R.id.add_answer_button_finish );
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
		
		buttonPicture = ( Button ) findViewById( R.id.add_answer_picture );
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
		
		if( type == 1 )
		{
			textInfo.setText( AnswerInfoActivity.entity.getAnswerInfo() );
			LayoutParams params = new LayoutParams( LayoutParams.MATCH_PARENT, 0 );
			params.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
			layout.setLayoutParams( params );
			
			final int count = AnswerInfoActivity.entity.getImageList().size();
			for( int i = 0; i < count; i ++ )
			{
				pictureList.add( AnswerInfoActivity.entity.getImageList().get( i ) );
				pictureListBig.add( AnswerInfoActivity.entity.getImageList().get( i ).replaceAll( "_small", "" ) );
				pictureListSmall.add( AnswerInfoActivity.entity.getImageList().get( i ) );
			}
			
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
	
	private void judgeAdd()
	{
		if( type == 0 )
		{
			if( pictureListBig.size() == index )
			{
				textView.setText( "正在添加回答" );
			}
			else
			{
				textView.setText( "正在上传第" + ( index + 1 ) + "张照片" );
			}
		}
		else
		{
			if( pictureListBigNew.size() == index )
			{
				textView.setText( "正在添加回答" );
			}
			else
			{
				textView.setText( "正在上传第" + ( index + 1 + pictureListSmall.size() ) + "张照片" );
			}
		}
		new Thread
    	(
    		new Thread()
    		{
    			public void run()
    			{
    				if( type == 0 )
    				{
	    				if( pictureListBig.size() == index )
	    				{
	    					doAddAnswer();
	    				}
	    				else
	    				{
	    					doUploadImage();
	    				}
    				}
    				else
    				{
    					if( pictureListBigNew.size() == index )
	    				{
	    					doEdit();
	    				}
	    				else
	    				{
	    					doUploadImage();
	    				}
    				}
    			}
    		}
    	).start();
	}
	
	private void addPicture()
	{
		Intent intent = new Intent( this, AddPictureActivity.class );
		intent.putStringArrayListExtra( "pictureListBig", pictureListBig );
		intent.putStringArrayListExtra( "pictureListSmall", pictureListSmall );
		if( type == 1 )
		{
			intent.putStringArrayListExtra( "pictureListBigNew", pictureListBigNew );
			intent.putStringArrayListExtra( "pictureListSmallNew", pictureListSmallNew );			
		}
		intent.putExtra( "type", type );
		startActivityForResult( intent, 0 );
	}
	
	private void judgeInput()
	{
		info = textInfo.getText().toString();
		if( info.length() == 0 )
		{
			showError( "请输入回答" );
			return;
		}
		
		index = 0;
		pictureList.clear();
		
		if( type == 0 )
		{	
			startAdd();
		}
		else
		{
			final int count = AnswerInfoActivity.entity.getImageList().size();
			for( int i = 0; i < count; i ++ )
			{
				pictureList.add( AnswerInfoActivity.entity.getImageList().get( i ) );
			}
			startEdit();
		}
	}
	
	private void startEdit()
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
	
	private void doEdit()
	{
		try
		{
			final String urlString = Information.Server_Url + "/api/answer/" + AnswerInfoActivity.entity.getId();
			JSONObject json = new JSONObject();
			json.put( "token", Information.Token );
			json.put( "content", info );
			JSONArray array = new JSONArray();
			for( String temp : pictureList )
			{
				array.put( temp );
			}
			json.put( "images", array );
			JSONObject result = Tool.doPutWithUrl( urlString, json );
			if( result == null )
			{
				handler.sendEmptyMessage( 3 );
			}
			else
			{
				if( result.getInt( "result" ) == 4000 )
				{
					time = result.getString( "editTime" );
					handler.sendEmptyMessage( 2 );
				}
				else
				{
					handler.sendEmptyMessage( 3 );
				}
			}
		}
		catch( Exception ex )
		{
			handler.sendEmptyMessage( 3 );
		}
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
	
	private void doAddAnswer()
	{
		try
		{
			final String urlString = Information.Server_Url + "/api/answer";
			
			JSONObject json = new JSONObject();
			json.put( "token", Information.Token );
			json.put( "questionId", questionId );
			json.put( "answer", info );
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
				if( result.getInt( "result" ) == 4000 )
				{
					time = result.getString( "modifyTime" );					
					answerId = result.getString( "id" );
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
			
			String fileName = "";
			if( type == 0 )
			{
				fileName = pictureListBig.get( index );
			}
			else
	        {
	            fileName = pictureListBigNew.get( index );
	        }
            String symbol = fileName.substring( fileName.lastIndexOf( "." ) + 1, fileName.length() );
            if( symbol.compareToIgnoreCase( "jpg" ) == 0 || symbol.compareToIgnoreCase( "jpeg" ) == 0 )
            {
                symbol = "jpeg";
            }
            else
            {
            	symbol = "png";
            }
			String contentDisposition = "Content-Disposition: form-data; name=\"head\"; filename=\"" + fileName + "\"";
            String contentType = "Content-Type: image/" + symbol;
            String BOUNDRY = "----WebKitFormBoundaryabcdefghijklmnop";
            connection.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + BOUNDRY );
            
            DataOutputStream dataOS = new DataOutputStream( connection.getOutputStream());
            dataOS.writeBytes( "--" + BOUNDRY + "\r\n" );
            dataOS.writeBytes( contentDisposition + "\r\n" );
            dataOS.writeBytes( contentType + "\r\n\r\n" );

            InputStream is = new FileInputStream( new File( fileName ) );

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
					message.what = 4;
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
	
	private void showExitDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder( this );
        dialog.setTitle( "编辑回答" ).setMessage( "确定要放弃编辑吗？" )
        .setPositiveButton( "确定", new DialogInterface.OnClickListener() 
        {
        	public void onClick( DialogInterface dialog, int which ) 
        	{
        		Tool.deleteAllTempImage();
        		finish();
        	}
        }).setNegativeButton( "取消", new DialogInterface.OnClickListener() 
        {
        	public void onClick( DialogInterface dialog, int which ) 
        	{
        		dialog.cancel();
        	}
        }).create().show();
	}
	
	private void addAnswerSuccess()
	{
		AnswerEntity entity = new AnswerEntity();
		entity.setAnswerInfo( info );
		entity.setCommentList( new ArrayList< CommentEntity >() );
		entity.setCompany( Information.Company );
		entity.setId( answerId );
		entity.setInvite( new ArrayList< String >() );
		entity.setJob( Information.Job );
		entity.setName( Information.Name );
		entity.setPart( Information.Part );
		entity.setPku( Information.PKU_Value );
		entity.setPraise( new ArrayList< String >() );
		entity.setPraiseCount( 0 );
		entity.setQuestionId( questionId );
		entity.setSex( Information.Sex );
		entity.setCreateTime( time );
		entity.setModifyTime( "" );
		entity.setUserHeadUrl( Information.HeadUrl );
		entity.setUserId( Information.Id );
		entity.setInvisible( box.isChecked() );
		entity.setHasPraised( false );
		entity.setImageList( pictureList );
		
		if( entity.isInvisible() )
		{
			entity.setUserId( "" );
		}
		
		MyDatabaseHelper.getInstance( this ).updateQuestion1( QuestionInfoActivity.entity.getId(), time );
		MyDatabaseHelper.getInstance( this ).updateQuestion2( QuestionInfoActivity.entity.getId(), time );
		
		Tool.deleteAllTempImage();
		
		Intent intent = getIntent();
		intent.putExtra( "answer", entity );
		setResult( 2, intent );
		finish();
	}
	
	private void editAnswerSuccess()
	{
		Tool.deleteAllTempImage();
		
		AnswerInfoActivity.entity.setAnswerInfo( info );
		AnswerInfoActivity.entity.setModifyTime( "" );
		AnswerInfoActivity.entity.setEditTime( time );
		AnswerInfoActivity.entity.setImageList( pictureList );
		
		MyDatabaseHelper.getInstance( this ).updateMyAnswer( AnswerInfoActivity.entity.getId(), time );
		MyDatabaseHelper.getInstance( this ).updateQuestion2( AnswerInfoActivity.entity.getQuestionId(), time );
		
		Intent intent = getIntent();
		setResult( 2, intent );
		finish();
	}
	
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
		{
			back();
			return true;
		}
		return false;
	}
	
	private void showError( String text )
	{
		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();
	}
	
	private void back()
	{
		if( type == 0 )
		{
			Tool.deleteAllTempImage();
			finish();
		}
		else
		{
			showExitDialog();
		}
	}
	
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) 
	{
		super.onActivityResult( requestCode, resultCode, data );
		if( resultCode == 1 )
		{
			if( type == 0 )
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
			else
			{
				pictureListSmallNew = data.getStringArrayListExtra( "pictureListSmallNew" );
				pictureListBigNew = data.getStringArrayListExtra( "pictureListBigNew" );
				if( pictureListSmallNew.size() + pictureListSmall.size() > 0 )
				{
					buttonPicture.setText( "" + ( pictureListSmallNew.size() + pictureListSmall.size() ) );
				}
				else
				{
					buttonPicture.setText( "" );
				}
			}
		}
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