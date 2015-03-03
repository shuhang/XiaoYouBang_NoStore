package com.pku.xiaoyoubang.service;import java.io.BufferedReader;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import org.json.JSONObject;import android.app.Activity;import android.app.Service;import android.content.Intent;import android.content.SharedPreferences;import android.os.Binder;import android.os.IBinder;import android.os.Message;import com.pku.xiaoyoubang.MainActivity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.Tool;import com.umeng.message.UmengRegistrar;public class MyService extends Service{	/**	 *  Fetch My Question Count	 */	public void startTab2()	{		new Thread		(			new Thread()			{				public void run()				{					doGetMyQuestionUpdate();				}			}		).start();	}	/**	 *  	 */	private void doGetMyQuestionUpdate()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "/questions/update?token=" + Information.Token;			SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );			JSONObject json = new JSONObject();			json.put( "updateTime", shared.getString( "tab2_update_count", "" ) );			JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result != null )			{				if( result.getInt( "result" ) == 4000 )				{					int change = result.getInt( "changes" );					if( change > 0 )					{						if( change > 9 ) change = 9;						Message message = MainActivity.handler.obtainMessage();						message.what = 2;						message.arg1 = change;						MainActivity.handler.sendMessage( message );					}				}			}		}		catch( Exception ex ) {}	}		public void startTab3()	{		new Thread		(			new Thread()			{				public void run()				{					doGetAnswerUpdate();				}			}		).start();	}		/**	 *  	 */	private void doGetAnswerUpdate()	{		try		{			final String urlString = Information.Server_Url + "/api/user/" + Information.Id + "/answers/update?token=" + Information.Token;			SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );			JSONObject json = new JSONObject();			json.put( "updateTime", shared.getString( "tab3_update_count", "" ) );			JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result != null )			{				if( result.getInt( "result" ) == 4000 )				{					int change = result.getInt( "changes" );					if( change > 0 )					{						if( change > 9 ) change = 9;						Message message = MainActivity.handler.obtainMessage();						message.what = 3;						message.arg1 = change;						MainActivity.handler.sendMessage( message );					}				}			}		}		catch( Exception ex ) {}	}		public void startTab4()	{		new Thread		(			new Thread()			{				public void run()				{					doGetReplyUpdate();				}			}		).start();	}		/**	 *  	 */	private void doGetReplyUpdate()	{		try		{			final String urlString = Information.Server_Url + "/api/mq/update?token=" + Information.Token;			SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );			JSONObject json = new JSONObject();			json.put( "updateTime", shared.getString( "tab4_update_count", "" ) );			JSONObject result = Tool.doPostWithUrl( urlString, json );			if( result != null )			{				if( result.getInt( "result" ) == 4000 )				{					int change = result.getInt( "changes" );					if( change > 0 )					{						if( change > 9 ) change = 9;						Message message = MainActivity.handler.obtainMessage();						message.what = 4;						message.arg1 = change;						MainActivity.handler.sendMessage( message );					}				}			}		}		catch( Exception ex ) {}	}	/**	 * 	 */	public void onCreate()	{		updateDeviceToken();	}		private void updateDeviceToken()	{		SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );		if( !shared.getBoolean( "device_token_update", false ) )		{			new Thread			(				new Thread()				{					public void run()					{						SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );						while( !shared.getBoolean( "device_token_update", false ) )						{							try							{								Thread.sleep( 10000 );							}							catch( Exception ex ) {}							startUpdateDeviceToken();						}					}				}			).start();		}	}		private void startUpdateDeviceToken()	{		String urlString = Information.Server_Url + "/api/user/" + Information.Id + "/device?token=" + Information.Token;		String device_token = UmengRegistrar.getRegistrationId( this );		if( device_token != null && !device_token.equals( "" ) )		{			urlString += "&device=" + device_token;			try			{				URL url = new URL( urlString );				HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();  				connection.setRequestProperty( "Connection", "keep-alive" );				connection.setRequestMethod( "GET" );				connection.setConnectTimeout( 10000 );				connection.setReadTimeout( 30000 );				connection.connect();				final int responseCode = connection.getResponseCode();				if( responseCode == 200 )				{					BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );					String temp1 = null;					StringBuilder value = new StringBuilder();					while( ( temp1 = reader.readLine() ) != null )					{						value.append( temp1 );					}					JSONObject jsonObject = new JSONObject( value.toString() );					if( jsonObject.getInt( "result" ) == 1000 )					{						SharedPreferences shared = getSharedPreferences( "whole2", Activity.MODE_PRIVATE );						SharedPreferences.Editor editor = shared.edit();						editor.putBoolean( "device_token_update", true );						editor.commit();					}				}			}			catch( Exception ex ) 			{				ex.printStackTrace();			}		}	}	//	private void showNoti()//	{//        NotificationManager manager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );//        //        Intent intent = new Intent();//        if( Tool.isBackground( this ) )//        {//	        intent = new Intent( this, MainActivity.class );//        }//        //        NotificationCompat.Builder builder = new NotificationCompat.Builder( this );//        builder.setContentTitle( "测试标题" )//        .setContentText( "测试内容" )//        .setContentIntent( PendingIntent.getActivity( this, 1, intent, Notification.FLAG_AUTO_CANCEL ) )//        .setTicker( "测试通知来啦" )//        .setPriority( Notification.PRIORITY_DEFAULT )//        .setAutoCancel( true )//        .setOngoing( false )//        .setDefaults( Notification.DEFAULT_VIBRATE )//        .setSmallIcon( R.drawable.umeng_update_close_bg_tap );//        //        manager.notify( 0, builder.build() );//	}	/**	 * 	 */	public IBinder onBind( Intent intent ) 	{		return new MyService.MyBinder();	}	/**	 * 	 */	public int onStartCommand( Intent intent, int flags, int startId )	{		return START_STICKY;	}	/**	 *  Binder	 * @author shuhang	 */	public class MyBinder extends Binder	{		public MyService getService()		{			return MyService.this;		}	}}