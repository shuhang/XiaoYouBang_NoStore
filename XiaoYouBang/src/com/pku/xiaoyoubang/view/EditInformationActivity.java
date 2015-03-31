package com.pku.xiaoyoubang.view;import java.io.BufferedOutputStream;import java.io.BufferedReader;import java.io.DataOutputStream;import java.io.File;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;import org.json.JSONArray;import org.json.JSONObject;import android.annotation.SuppressLint;import android.app.Activity;import android.app.Dialog;import android.content.ContentResolver;import android.content.ContentValues;import android.content.DialogInterface;import android.content.DialogInterface.OnKeyListener;import android.content.Intent;import android.database.Cursor;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.net.Uri;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.provider.MediaStore;import android.view.KeyEvent;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.ViewGroup;import android.view.ViewGroup.LayoutParams;import android.view.Window;import android.view.WindowManager;import android.widget.Button;import android.widget.EditText;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import com.nostra13.universalimageloader.core.ImageLoader;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;@SuppressLint("HandlerLeak")public class EditInformationActivity extends Activity{	private Handler handler;		private ImageView imageHead;	private TextView textName;	private ImageView imageSex;	private TextView textPKU;	private Button buttonBack;	private Button buttonFinish;		private EditText text1;	private EditText text2;	private EditText text3;	private EditText text4;	private EditText text5;	private EditText text6;	private EditText text7;	private EditText text8;	private EditText text9;	private EditText text10;	private EditText text11;	private EditText text12;		private List< String > edit = new ArrayList< String >( 12 );		private Dialog dialog;		private Uri photoUri = null;	private String chooseUrl = "";	private String imageUrl = "";	private String tempUrl;		private HttpURLConnection connection = null;		private ImageLoader imageLoader;		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			public void handleMessage( Message message )			{				switch( message.what )				{				case 0 : //update success					saveInformation();					if( dialog != null )					{						dialog.dismiss();					}					showError( "修改成功" );					Information.IsMeChanged = true;					back();					break;				case 1 : //update failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "修改失败" );					break;				case 2 : //upload success					startUpdate();					break;				case 3 : //upload failed					if( dialog != null )					{						dialog.dismiss();					}					showError( "修改失败" );					break;				case 4 : //save headUrl success					Information.HeadUrl = tempUrl;					imageHead.setTag( tempUrl );					imageLoader.displayImage( Information.Server_Url + tempUrl, imageHead, Information.options );					saveHeadUrl();					if( dialog != null )					{						dialog.dismiss();					}					showError( "修改成功" );					Information.IsMeChanged = true;					break;				}			}		};				this.imageLoader = ImageLoader.getInstance();				initView();	}	private void initView()	{		setContentView( R.layout.edit_my_info );				imageHead = ( ImageView ) findViewById( R.id.edit_my_info_head );		imageHead.setTag( Information.HeadUrl );		imageLoader.displayImage( Information.Server_Url + Information.HeadUrl, imageHead, Information.options );				textName = ( TextView ) findViewById( R.id.edit_my_info_name );		textName.setText( Information.Name );		imageSex = ( ImageView ) findViewById( R.id.edit_my_info_info_sex );		if( Information.Sex == 1 )		{			imageSex.setImageResource( R.drawable.male_color );		}		else		{			imageSex.setImageResource( R.drawable.female_color );		}		textPKU = ( TextView ) findViewById( R.id.edit_my_info_pku );		textPKU.setText( Tool.getFullPku( Information.PKU_Value ) );				text1 = ( EditText ) findViewById( R.id.edit_my_info_birthday );		text1.setText( Information.Birthday );		text2 = ( EditText ) findViewById( R.id.edit_my_info_now_home );		text2.setText( Information.Now_Home );		text3 = ( EditText ) findViewById( R.id.edit_my_info_old_home );		text3.setText( Information.Old_Home );		text4 = ( EditText ) findViewById( R.id.edit_my_info_qq );		text4.setText( Information.QQ );		text5 = ( EditText ) findViewById( R.id.edit_my_info_company );		text5.setText( Information.Company );		text6 = ( EditText ) findViewById( R.id.edit_my_info_part );		text6.setText( Information.Part );		text7 = ( EditText ) findViewById( R.id.edit_my_info_job );		text7.setText( Information.Job );		text8 = ( EditText ) findViewById( R.id.edit_my_info_tag1 );		text8.setText( Information.Tag[ 0 ] );		text9 = ( EditText ) findViewById( R.id.edit_my_info_tag2 );		text9.setText( Information.Tag[ 1 ] );		text10 = ( EditText ) findViewById( R.id.edit_my_info_tag3 );		text10.setText( Information.Tag[ 2 ] );		text11 = ( EditText ) findViewById( R.id.edit_my_info_tag4 );		text11.setText( Information.Tag[ 3 ] );		text12 = ( EditText ) findViewById( R.id.edit_my_info_tag5 );		text12.setText( Information.Tag[ 4 ] );				buttonBack = ( Button ) findViewById( R.id.edit_my_info_button_back );		buttonBack.setText( "<  " );		buttonBack.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					back();				}			}		);				buttonFinish = ( Button ) findViewById( R.id.edit_my_info_finish );		buttonFinish.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					commitEdit();				}			}		);				imageHead.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					changeHead();				}							}		);	}		@SuppressWarnings("deprecation")	private void changeHead()	{		View view = getLayoutInflater().inflate( R.layout.photo_choose_dialog, null );		dialog = new Dialog( this, R.style.transparentFrameWindowStyle );		dialog.setContentView( view, new LayoutParams( LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT ) );				Button button1 = ( Button ) view.findViewById( R.id.photo_choose_dialog_button1 );		button1.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					chooseFromGallery();					dialog.dismiss();				}			}		);				Button button2 = ( Button ) view.findViewById( R.id.photo_choose_dialog_button2 );		button2.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					chooseFromCamera();					dialog.dismiss();				}			}		);				Button button3 = ( Button ) view.findViewById( R.id.photo_choose_dialog_button3 );		button3.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( Tool.isFastDoubleClick() )					{						return;					}					dialog.dismiss();				}			}		);				Window window = dialog.getWindow();		// 设置显示动画		window.setWindowAnimations( R.style.main_menu_animstyle );		WindowManager.LayoutParams wl = window.getAttributes();		wl.x = 0;		wl.y = getWindowManager().getDefaultDisplay().getHeight();		// 以下这两句是为了保证按钮可以水平满屏		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;		// 设置显示位置		dialog.onWindowAttributesChanged( wl );		// 设置点击外围解散		dialog.setCanceledOnTouchOutside( true );		dialog.show();	}		private void chooseFromCamera()	{		Intent getImageByCamera = new Intent( "android.media.action.IMAGE_CAPTURE" );   		ContentValues value = new ContentValues();		photoUri = this.getContentResolver().insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value );		getImageByCamera.putExtra( android.provider.MediaStore.EXTRA_OUTPUT, photoUri );		startActivityForResult( getImageByCamera, 1 );	}		private void chooseFromGallery()	{		Intent getImage = new Intent( Intent.ACTION_GET_CONTENT ); 		getImage.addCategory( Intent.CATEGORY_OPENABLE ); 		getImage.setType( "image/*" ); 		startActivityForResult( getImage, 0 );	}		private void saveInformation()	{		Information.Birthday = edit.get( 0 );		Information.Now_Home = edit.get( 1 );		Information.Old_Home = edit.get( 2 );		Information.QQ = edit.get( 3 );		Information.Company = edit.get( 4 );		Information.Part = edit.get( 5 );		Information.Job = edit.get( 6 );		Information.Tag[ 0 ] = edit.get( 7 );		Information.Tag[ 1 ] = edit.get( 8 );		Information.Tag[ 2 ] = edit.get( 9 );		Information.Tag[ 3 ] = edit.get( 10 );		Information.Tag[ 4 ] = edit.get( 11 );				UserEntity entity = new UserEntity();		entity.setId( Information.Id );		entity.setName( Information.Name );		entity.setHeadUrl( Information.HeadUrl );		entity.setSex( Information.Sex );		entity.setBirthday( Information.Birthday );		entity.setPku( Information.PKU_Value );		entity.setNowHome( Information.Now_Home );		entity.setOldHome( Information.Old_Home );		entity.setQq( Information.QQ );		entity.setJob1( Information.Company );		entity.setJob2( Information.Part );		entity.setJob3( Information.Job );		entity.setVersion( Information.Version );		entity.setTag( Information.Tag );		entity.setInviteName( Information.InviteUserName );		entity.setInviteHeadUrl( Information.InviteUserHeadUrl );		entity.setInviteUserId( Information.InviteUserId );		entity.setQuestionCount( Information.Question_Count );		entity.setAnswerCount( Information.Answer_Count );		entity.setPraiseCount( Information.Praise_Count );		entity.setIntro( Information.Intro );				MyDatabaseHelper.getInstance( this ).insertUser( entity );	}		private void saveHeadUrl()	{		UserEntity entity = MyDatabaseHelper.getInstance( this ).getUser( Information.Id );		entity.setHeadUrl( Information.HeadUrl );		MyDatabaseHelper.getInstance( this ).insertUser( entity );	}		private void commitEdit()	{		String temp = text1.getText().toString();		if( Tool.judgeTextInput( temp.length(), 4, 4 ) )		{			try			{				int number = Integer.parseInt( temp );				if( number < 1900 )				{					showError( "出生年份输入不正确" );					return;				}			}			catch( Exception ex )			{				showError( "出生年份输入不正确" );				return;			}		}		else		{			showError( "出生年份输入不正确" );			return;		}		edit.add( temp );				temp = text2.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 10, 1 ) )		{			showError( "现居地10个字以内" );			return;		}		edit.add( temp );				temp = text3.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 10, 0 ) )		{			showError( "故乡10个字以内" );			return;		}		edit.add( temp );				temp = text4.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 40, 1 ) )		{			showError( "网络联系方式40个字以内" );			return;		}		edit.add( temp );				temp = text5.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 20, 1 ) )		{			showError( "工作单位输入不正确" );			return;		}		edit.add( temp );				temp = text6.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 10, 0 ) )		{			showError( "所在部门10个字以内" );			return;		}		edit.add( temp );				temp = text7.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 10, 0 ) )		{			showError( "职位10个字以内" );			return;		}		edit.add( temp );				temp = text8.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 6, 0 ) )		{			showError( "标签6个字以内" );			return;		}		edit.add( temp );				temp = text9.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 6, 0 ) )		{			showError( "标签6个字以内" );			return;		}		edit.add( temp );				temp = text10.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 6, 0 ) )		{			showError( "标签6个字以内" );			return;		}		edit.add( temp );				temp = text11.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 6, 0 ) )		{			showError( "标签6个字以内" );			return;		}		edit.add( temp );				temp = text12.getText().toString();		if( !Tool.judgeTextInput( temp.length(), 6, 0 ) )		{			showError( "标签6个字以内" );			return;		}		edit.add( temp );				startChange();	}		private void startChange()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			textView.setText( "正在修改" );						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				doChange();	    			}	    		}	    	).start();		}		else		{			Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();		}	}		private void doChange()	{		try		{			final String urlString = Information.Server_Url + "/api/user";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "birthyear", edit.get( 0 ) );			json.put( "hometown", edit.get( 1 ) );			json.put( "base", edit.get( 2 ) );			json.put( "qq", edit.get( 3 ) );			json.put( "company", edit.get( 4 ) );			json.put( "department", edit.get( 5 ) );			json.put( "job", edit.get( 6 ) );			JSONArray array = new JSONArray();			array.put( edit.get( 7 ) );			array.put( edit.get( 8 ) );			array.put( edit.get( 9 ) );			array.put( edit.get( 10 ) );			array.put( edit.get( 11 ) );			json.put( "tags", array );						JSONObject result = Tool.doPutWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 1 );			}			else			{				if( result.getInt( "result" ) == 1000 )				{					Information.Version = result.getInt( "version" );					handler.sendEmptyMessage( 0 );				}				else				{					handler.sendEmptyMessage( 1 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 1 );		}	}		protected void onActivityResult( int requestCode, int resultCode, Intent data ) 	{		super.onActivityResult( requestCode, resultCode, data );		ContentResolver resolver = getContentResolver(); 		if( requestCode == 0 ) 		{			try 			{//				if( Build.VERSION.SDK_INT == 19 )//				{//					if(DocumentsContract.isDocumentUri(context, data.getData()))//					{//					    String wholeID = DocumentsContract.getDocumentId( data.getData() );//					    String id = wholeID.split(:)[1];//					    String[] column = { MediaStore.Images.Media.DATA };//					    String sel = MediaStore.Images.Media._ID + =?;//					    Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,//					            sel, new String[] { id }, null);//					    int columnIndex = cursor.getColumnIndex(column[0]);//					    if (cursor.moveToFirst()) {//					        filePath = cursor.getString(columnIndex);//					    }//					    cursor.close();//					}else{//					    String[] projection = { MediaStore.Images.Media.DATA };//					    Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);//					    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//					    cursor.moveToFirst();//					    filePath = cursor.getString(column_index);//					}//				}//				else//				{					Cursor cursor = resolver.query( data.getData(), null, null, null, null );            	                if( cursor != null )	                {	                	cursor.moveToFirst();	                	chooseUrl = cursor.getString( 1 );	                	cursor.close();	                		                	checkFile();	                }	                else if( data.getData() != null )	                {	                	chooseUrl = data.getData().getPath();	                	checkFile();	                }//				}			} 			catch( Exception ex ) {}		}		else if( requestCode == 1 )		{            try             {                Cursor cursor = resolver.query( photoUri, null, null, null, null );                            if( cursor != null )                {                	cursor.moveToFirst();                	chooseUrl = cursor.getString( 1 );                	cursor.close();                	                	checkFile();                }            }             catch( Exception ex ) {}		}	}		private void checkFile()	{		try		{			String symbol = chooseUrl.substring( chooseUrl.lastIndexOf( "." ) + 1, chooseUrl.length() );			if( symbol.compareToIgnoreCase( "jpg" ) == 0 || symbol.compareToIgnoreCase( "jpeg" ) == 0 || symbol.compareToIgnoreCase( "png" ) == 0 )			{				FileInputStream stream = new FileInputStream( new File( chooseUrl ) );		    	if( stream.available() > 10000000 )		    	{		    		stream.close();		    		showError( "图片不能超过10M" );		    	}		    	else		    	{		    		stream.close();		    		if( savePhotoToNative() == true )		        	{		    			startUpload();		        	}		    		else		    		{		    			showError( "读取图片失败" );		    		}		    	}			}			else			{				showError( "只支持jpg和png格式图片" );			}		}		catch( Exception ex ) {}	}		private void startUpdate()	{		new Thread    	(    		new Thread()    		{    			public void run()    			{    				doStartUpdate();    			}    		}    	).start();	}		private void doStartUpdate()	{		try		{			final String urlString = Information.Server_Url + "/api/user";						JSONObject json = new JSONObject();			json.put( "token", Information.Token );			json.put( "headUrl", tempUrl );						JSONObject result = Tool.doPutWithUrl( urlString, json );			if( result == null )			{				handler.sendEmptyMessage( 3 );			}			else			{				if( result.getInt( "result" ) == 1000 )				{					Information.Version = result.getInt( "version" );					handler.sendEmptyMessage( 4 );				}				else				{					handler.sendEmptyMessage( 3 );				}			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 3 );		}	}		private void startUpload()	{		if( Tool.isNetworkConnected( this ) == true )		{			dialog = new Dialog( this, R.style.dialog_progress );			LayoutInflater inflater = LayoutInflater.from( this );  			View view = inflater.inflate( R.layout.dialog_progress, null );			TextView textView = ( TextView ) view.findViewById( R.id.dialog_textview );			textView.setText( "正在更新头像" );						WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();			layoutParams.alpha = 0.8f;			dialog.getWindow().setAttributes( layoutParams );			dialog.setContentView( view );			dialog.setCancelable( false );			dialog.setOnKeyListener			(				new OnKeyListener()				{					public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) 					{						if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )						{							if( connection != null )							{								connection.disconnect();							}							dialog.dismiss();							return true;						}						return false;					}				}			);			dialog.show();						new Thread	    	(	    		new Thread()	    		{	    			public void run()	    			{	    				doStartUpload();	    			}	    		}	    	).start();		}		else		{			Toast.makeText( this, "网络不可用，请打开网络", Toast.LENGTH_SHORT ).show();		}	}		private void doStartUpload()	{		final String urlString = Information.Server_Url + "/api/user/upload?token=" + Information.Token;		try		{			URL url = new URL( urlString );			connection = ( HttpURLConnection ) url.openConnection();  			connection.setRequestProperty( "Connection", "keep-alive" );			connection.setRequestMethod( "POST" );			connection.setConnectTimeout( 10000 );			connection.setReadTimeout( 60000 );			connection.setDoOutput( true );			            String symbol = imageUrl.substring( imageUrl.lastIndexOf( "." ) + 1, imageUrl.length() );            if( symbol.compareToIgnoreCase( "jpg" ) == 0 || symbol.compareToIgnoreCase( "jpeg" ) == 0 )            {                symbol = "jpeg";            }			String contentDisposition = "Content-Disposition: form-data; name=\"head\"; filename=\"" + Information.Phone + "." + symbol + "\"";            String contentType = "Content-Type: image/" + symbol;            String BOUNDRY = "----WebKitFormBoundaryabcdefghijklmnop";            connection.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + BOUNDRY );                        DataOutputStream dataOS = new DataOutputStream( connection.getOutputStream());            dataOS.writeBytes( "--" + BOUNDRY + "\r\n" );            dataOS.writeBytes( contentDisposition + "\r\n" );            dataOS.writeBytes( contentType + "\r\n\r\n" );                        FileInputStream fis = new FileInputStream( imageUrl );              byte[] buffer = new byte[ 1024 ];            int count = 0;              while( ( count = fis.read( buffer ) ) != -1 )              {                  dataOS.write( buffer, 0, count );            }            dataOS.writeBytes( "\r\n--" + BOUNDRY + "--\r\n" );                        dataOS.flush();            dataOS.close();            fis.close();			final int responseCode = connection.getResponseCode();			if( responseCode == 200 )			{				BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );				String temp1 = null;				StringBuilder value = new StringBuilder();				while( ( temp1 = reader.readLine() ) != null )				{					value.append( temp1 );				}								JSONObject jsonObject = new JSONObject( value.toString() );				if( jsonObject.getInt( "result" ) == 1000 )				{					tempUrl = jsonObject.getString( "headUrl" );					handler.sendEmptyMessage( 2 );				}				else				{					handler.sendEmptyMessage( 3 );				}			}			else			{				handler.sendEmptyMessage( 3 );			}		}		catch( Exception ex )		{			handler.sendEmptyMessage( 3 );		}		finally		{			if( connection != null )			{				connection.disconnect();			}		}	}		/**	 *  保存用户自己的头像，100像素，上传成功之前	 */	private boolean savePhotoToNative()	{		try		{			Bitmap oldBitmap = BitmapFactory.decodeStream( new FileInputStream( new File( chooseUrl ) ) );			oldBitmap = Tool.roateBitmap( oldBitmap, Tool.getPictureDegree( chooseUrl ) );			Bitmap newBitmap = Tool.zoomBitmap( oldBitmap, 400 );			String symbol = chooseUrl.substring( chooseUrl.lastIndexOf( "." ) + 1, chooseUrl.length() );			File file = new File( Information.Image_Path + "temp." + symbol  );			if( !file.exists() )			{				file.createNewFile();			}			BufferedOutputStream stream = new BufferedOutputStream( new FileOutputStream( file ) );			if( symbol.compareToIgnoreCase( "jpg" ) == 0 || symbol.compareToIgnoreCase( "jpeg" ) == 0 )			{				newBitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream );			}			else			{				newBitmap.compress( Bitmap.CompressFormat.PNG, 100, stream );			}			imageUrl = Information.Image_Path + "temp." + symbol;			stream.flush();			stream.close();			return true;		}		catch( Exception ex )		{			return false;		}	}	private void showError( String text )	{		Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();	}		public boolean onKeyDown( int keyCode, KeyEvent event )	{		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )		{			back();			return true;		}		return false;	}		private void back()	{		setResult( 1 );		finish();	}}