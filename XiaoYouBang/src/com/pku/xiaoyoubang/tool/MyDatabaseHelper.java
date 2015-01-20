package com.pku.xiaoyoubang.tool;import java.util.ArrayList;import java.util.List;import android.content.ContentValues;import android.content.Context;import android.database.Cursor;import android.database.sqlite.SQLiteDatabase;import android.database.sqlite.SQLiteOpenHelper;import com.pku.xiaoyoubang.entity.InviteEntity;import com.pku.xiaoyoubang.entity.InviteUserEntity;import com.pku.xiaoyoubang.entity.UserEntity;import com.pku.xiaoyoubang.view.InviteOtherActivity;import com.pku.xiaoyoubang.view.QuestionInfoActivity;public class MyDatabaseHelper extends SQLiteOpenHelper{	private static final String database_name = "pku.db";      private static final String table_user = "user";    private static final String table_question = "question";    private static final String table_friend = "friend";    private static final String table_answer = "answer";    private static final String sqlUser = "create table user(id text primary key, name text, headUrl text, version int," +    								"sex int, birthday text, pku text, nowHome text, oldHome text, qq text," +    								" company text, part text, job text, tag1 text, tag2 text, tag3 text, tag4 text, tag5 text, " +    								"questionCount int, answerCount int, praiseCount int)";    private static final String sqlFriend = "create table friend(id text primary key, name text, headUrl text, version int," +			"sex int, birthday text, pku text, nowHome text, oldHome text, qq text," +			" company text, part text, job text, tag1 text, tag2 text, tag3 text, tag4 text, tag5 text, " +			"questionCount int, answerCount int, praiseCount int, answerMe int, myAnswer int)";    private static final String sqlQuestion = "create table question(id text primary key, hasRead int, modifyTime text, answerModify text, updateTime text)";    private static final String sqlAnswer = "create table answer(id text primary key, modifyTime text)";        private SQLiteDatabase database;        private static MyDatabaseHelper instance = null;        public static MyDatabaseHelper getInstance( Context context )    {    	if( instance == null )    	{    		instance = new MyDatabaseHelper( context );    	}    	return instance;    }        public MyDatabaseHelper( Context context )    {    	super( context, database_name, null, 1 );    }	public void onCreate( SQLiteDatabase db )	{		this.database = db;		database.execSQL( sqlUser );		database.execSQL( sqlQuestion );		database.execSQL( sqlFriend );		database.execSQL( sqlAnswer );	}		public void insertFriend( UserEntity entity )	{          SQLiteDatabase db = getWritableDatabase();        Cursor cursor = db.query( table_friend, null, "id=?", new String[]{ entity.getId() }, null, null, null );        if( cursor.moveToFirst() )        {        	db.delete( table_friend, "id=?", new String[]{ entity.getId() } );        }        cursor.close();                ContentValues values = new ContentValues();        values.put( "id", entity.getId() );        values.put( "name", entity.getName() );        values.put( "version", entity.getVersion() );        values.put( "headUrl", entity.getHeadUrl() );        values.put( "sex", entity.getSex() );        values.put( "birthday", entity.getBirthday() );        values.put( "pku", entity.getPku() );        values.put( "nowHome", entity.getNowHome() );        values.put( "oldHome", entity.getOldHome() );        values.put( "qq", entity.getQq() );        values.put( "company", entity.getJob1() );        values.put( "part", entity.getJob2() );        values.put( "job", entity.getJob3() );        values.put( "tag1", entity.getTags()[ 0 ] );        values.put( "tag2", entity.getTags()[ 1 ] );        values.put( "tag3", entity.getTags()[ 2 ] );        values.put( "tag4", entity.getTags()[ 3 ] );        values.put( "tag5", entity.getTags()[ 4 ] );        values.put( "questionCount", entity.getQuestionCount() );        values.put( "answerCount", entity.getAnswerCount() );        values.put( "praiseCount", entity.getPraiseCount() );        values.put( "answerMe", entity.getAnswerMe() );        values.put( "myAnswer", entity.getMeAnswer() );        db.insert( table_friend, null, values );    }		public String[] searchFriend( String name )	{		 SQLiteDatabase db = getWritableDatabase();	     Cursor cursor = db.query( table_friend, null, "name=?", new String[]{ name }, null, null, null );	     if( cursor.moveToFirst() )	     {	    	 String[] result = new String[]{ "", "" };	    	 result[ 0 ] = cursor.getString( 0 );	    	 result[ 1 ] = cursor.getString( 2 );	    	 cursor.close();	    	 return result;	     }	     cursor.close();	     return null;	}		public List< UserEntity > getFriendList()	{		SQLiteDatabase db = getWritableDatabase();  		Cursor cursor1 = db.query( table_friend, null, null, null, null, null, "name" );		SortCursor cursor = new SortCursor( cursor1, "name" );		int count = cursor.getCount();		List< UserEntity > tempList = new ArrayList< UserEntity >( count );		cursor.moveToFirst();		for( int i = 0; i < count; i ++ )		{			UserEntity entity = new UserEntity();						entity.setId( cursor.getString( 0 ) );			entity.setName( cursor.getString( 1 ) );			entity.setHeadUrl( cursor.getString( 2 ) );			entity.setVersion( cursor.getInt( 3 ) );			entity.setSex( cursor.getInt( 4 ) );			entity.setBirthday( cursor.getString( 5 ) );			entity.setPku( cursor.getString( 6 ) );			entity.setNowHome( cursor.getString( 7 ) );			entity.setOldHome( cursor.getString( 8 ) );			entity.setQq( cursor.getString( 9 ) );			entity.setJob1( cursor.getString( 10 ) );			entity.setJob2( cursor.getString( 11 ) );			entity.setJob3( cursor.getString( 12 ) );			entity.setTag( new String[]{ cursor.getString( 13 ), cursor.getString( 14), 					cursor.getString( 15 ), cursor.getString( 16 ) ,cursor.getString( 17 ) } );			entity.setQuestionCount( cursor.getInt( 18 ) );			entity.setAnswerCount( cursor.getInt( 19 ) );			entity.setPraiseCount( cursor.getInt( 20 ) );			entity.setAnswerMe( cursor.getInt( 21 ) );			entity.setMeAnswer( cursor.getInt( 22 ) );						tempList.add( entity );						cursor.moveToNext();		}				cursor.close();		return tempList;	}		public List< InviteUserEntity > getInviterList()	{		SQLiteDatabase db = getWritableDatabase();  		Cursor cursor = db.query( table_friend, null, null, null, null, null, "name" );		List< InviteUserEntity > tempList = new ArrayList< InviteUserEntity >( cursor.getCount() );		boolean ya = cursor.moveToFirst();		while( ya )		{			if( cursor.getString( 0 ).equals( Information.Id ) )			{				if( cursor.isLast() )				{					break;				}				cursor.moveToNext();				continue;			}			InviteUserEntity entity = new InviteUserEntity();						entity.setId( cursor.getString( 0 ) );			entity.setName( cursor.getString( 1 ) );			entity.setHeadUrl( cursor.getString( 2 ) );			entity.setSex( cursor.getInt( 4 ) );			entity.setPku( cursor.getString( 6 ) );			entity.setCompany( cursor.getString( 10 ) );			entity.setPart( cursor.getString( 11 ) );			entity.setJob( cursor.getString( 12 ) );						if( InviteOtherActivity.answerUser.indexOf( entity.getId() ) != -1 )			{				entity.setHasAnswered( true );			}			else			{				List< InviteEntity > list = QuestionInfoActivity.entity.getMyInviteList();				final int count = list.size();				for( int i = 0; i < count; i ++ )				{					if( list.get( i ).getInviterId().equals( entity.getId() ) )					{						entity.setHasInvited( true );						break;					}				}			}						tempList.add( entity );						if( cursor.isLast() )			{				break;			}						cursor.moveToNext();		}				cursor.close();		return tempList;	}		public UserEntity getFriend( String id )	{		UserEntity entity = new UserEntity();		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_friend, null, "id=?", new String[]{ id }, null, null, null );				if( !cursor.moveToFirst() )		{			return null;		}				entity.setId( id );		entity.setName( cursor.getString( 1 ) );		entity.setHeadUrl( cursor.getString( 2 ) );		entity.setVersion( cursor.getInt( 3 ) );		entity.setSex( cursor.getInt( 4 ) );		entity.setBirthday( cursor.getString( 5 ) );		entity.setPku( cursor.getString( 6 ) );		entity.setNowHome( cursor.getString( 7 ) );		entity.setOldHome( cursor.getString( 8 ) );		entity.setQq( cursor.getString( 9 ) );		entity.setJob1( cursor.getString( 10 ) );		entity.setJob2( cursor.getString( 11 ) );		entity.setJob3( cursor.getString( 12 ) );		entity.setTag( new String[]{ cursor.getString( 13 ), cursor.getString( 14), 				cursor.getString( 15 ), cursor.getString( 16 ) ,cursor.getString( 17 ) } );		entity.setQuestionCount( cursor.getInt( 18 ) );		entity.setAnswerCount( cursor.getInt( 19 ) );		entity.setPraiseCount( cursor.getInt( 20 ) );		entity.setAnswerMe( cursor.getInt( 21 ) );		entity.setMeAnswer( cursor.getInt( 22 ) );				cursor.close();				return entity;	}		 public void close() 	 {  		 if( database != null )		 {			 database.close();		 }	 }  		public void insertUser( UserEntity entity )	{          SQLiteDatabase db = getWritableDatabase();        Cursor cursor = db.query( table_user, null, "id=?", new String[]{ entity.getId() }, null, null, null );        if( cursor.moveToFirst() )        {        	db.delete( table_user, "id=?", new String[]{ entity.getId() } );        }        cursor.close();                ContentValues values = new ContentValues();        values.put( "id", entity.getId() );        values.put( "name", entity.getName() );        values.put( "version", entity.getVersion() );        values.put( "headUrl", entity.getHeadUrl() );        values.put( "sex", entity.getSex() );        values.put( "birthday", entity.getBirthday() );        values.put( "pku", entity.getPku() );        values.put( "nowHome", entity.getNowHome() );        values.put( "oldHome", entity.getOldHome() );        values.put( "qq", entity.getQq() );        values.put( "company", entity.getJob1() );        values.put( "part", entity.getJob2() );        values.put( "job", entity.getJob3() );        values.put( "tag1", entity.getTags()[ 0 ] );        values.put( "tag2", entity.getTags()[ 1 ] );        values.put( "tag3", entity.getTags()[ 2 ] );        values.put( "tag4", entity.getTags()[ 3 ] );        values.put( "tag5", entity.getTags()[ 4 ] );        values.put( "questionCount", entity.getQuestionCount() );        values.put( "answerCount", entity.getAnswerCount() );        values.put( "praiseCount", entity.getPraiseCount() );        db.insert( table_user, null, values );    }		public boolean judgeQuestionHasRead( String id )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_question, null, "id=?", new String[]{ id }, null, null, null );	    if( !cursor.moveToFirst() )	    {	    	cursor.close();	        	        return false;	    }	    else	    {	    	if( cursor.getInt( 1 ) == 0 )	    	{	    		cursor.close();	    		return false;	    	}	    	else	    	{	    		cursor.close();	    		return true;	    	}	    }	}		public String getQuestionModifyTime( String id )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_question, null, "id=?", new String[]{ id }, null, null, null );		if( cursor.moveToFirst() )		{			String value = cursor.getString( 2 );			cursor.close();			return value;		}		cursor.close();		return "";	}		public String getAnswerModifyTime( String id )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_question, null, "id=?", new String[]{ id }, null, null, null );		if( cursor.moveToFirst() )		{			String value = cursor.getString( 3 );			cursor.close();			return value;		}		cursor.close();		return "";	}		public String getQuestionUpdateTime( String id )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_question, null, "id=?", new String[]{ id }, null, null, null );		if( cursor.moveToFirst() )		{			String value = cursor.getString( 4 );			cursor.close();			return value;		}		cursor.close();		return "";	}	//	public void updateAnswer( String id, String modifyTime )//	{//		SQLiteDatabase db = getWritableDatabase();//		ContentValues values = new ContentValues();//		values.put( "answerModify", modifyTime );//		db.update( table_question, values, "id=?", new String[]{ id } );//	}		public void updateQuestion1( String id, String modifyTime )	{		SQLiteDatabase db = getWritableDatabase();		ContentValues values = new ContentValues();		values.put( "modifyTime", modifyTime );		db.update( table_question, values, "id=?", new String[]{ id } );	}		public void updateQuestion2( String id, String updateTime )	{		SQLiteDatabase db = getWritableDatabase();		ContentValues values = new ContentValues();		values.put( "updateTime", updateTime );		db.update( table_question, values, "id=?", new String[]{ id } );	}		public void insertAnswer( String id, String modifyTime )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_answer, null, "id=?", new String[]{ id }, null, null, null );	    if( !cursor.moveToFirst() )	    {	    	cursor.close();	    				ContentValues values = new ContentValues();		    values.put( "id", id );		    values.put( "modifyTime", modifyTime );		    db.insert( table_answer, null, values );	    }	}		public void updateMyAnswer( String id, String modifyTime )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_answer, null, "id=?", new String[]{ id }, null, null, null );		if( cursor.moveToFirst() )		{			cursor.close();			ContentValues values = new ContentValues();			values.put( "modifyTime", modifyTime );			db.update( table_answer, values, "id=?", new String[]{ id } );		}	}		public String getMyAnswer( String id )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_answer, null, "id=?", new String[]{ id }, null, null, null );		if( cursor.moveToFirst() )		{			String value = cursor.getString( 1 );			cursor.close();			return value;		}		cursor.close();		return "";	}		public void insertOtherQuestion( String id, String modifyTime, String updateTime )	{		SQLiteDatabase db = getWritableDatabase();		Cursor cursor = db.query( table_question, null, "id=?", new String[]{ id }, null, null, null );	    if( !cursor.moveToFirst() )	    {	    	cursor.close();	    				ContentValues values = new ContentValues();		    values.put( "id", id );		    values.put( "hasRead", 1 );		    values.put( "modifyTime", modifyTime );		    values.put( "answerModify", "" );		    values.put( "updateTime", updateTime );		    db.insert( table_question, null, values );	    }	    else	    {	    	updateQuestion1( id, modifyTime );	    }	}		public void insertMyQuestion( String id, String modifyTime )	{		SQLiteDatabase db = getWritableDatabase();		ContentValues values = new ContentValues();	    values.put( "id", id );	    values.put( "hasRead", 1 );	    values.put( "modifyTime", modifyTime );	    values.put( "answerModify", modifyTime );	    values.put( "updateTime", modifyTime );	    db.insert( table_question, null, values );	}	//	public void setQuestionHasRead( String id, String modifyTime )//	{//		SQLiteDatabase db = getWritableDatabase();//		ContentValues values = new ContentValues();//		values.put( "hasRead", 1 );//		values.put( "modifyTime", modifyTime );//		db.update( table_question, values, "id=?", new String[]{ id } );//	}		public boolean judgeUserExist( String id )	{		SQLiteDatabase db = getWritableDatabase();		boolean symbol = false;		Cursor cursor = db.query( table_user, null, "id=?", new String[]{ id }, null, null, null );		symbol = cursor.moveToFirst();		cursor.close();		return symbol;	}		public UserEntity getUser( String id )	{		UserEntity entity = new UserEntity();		SQLiteDatabase db = getWritableDatabase();				Cursor cursor = db.query( table_user, null, "id=?", new String[]{ id }, null, null, null );		cursor.moveToFirst();				entity.setId( id );		entity.setName( cursor.getString( 1 ) );		entity.setHeadUrl( cursor.getString( 2 ) );		entity.setVersion( cursor.getInt( 3 ) );		entity.setSex( cursor.getInt( 4 ) );		entity.setBirthday( cursor.getString( 5 ) );		entity.setPku( cursor.getString( 6 ) );		entity.setNowHome( cursor.getString( 7 ) );		entity.setOldHome( cursor.getString( 8 ) );		entity.setQq( cursor.getString( 9 ) );		entity.setJob1( cursor.getString( 10 ) );		entity.setJob2( cursor.getString( 11 ) );		entity.setJob3( cursor.getString( 12 ) );		entity.setTag( new String[]{ cursor.getString( 13 ), cursor.getString( 14), 				cursor.getString( 15 ), cursor.getString( 16 ) ,cursor.getString( 17 ) } );		entity.setQuestionCount( cursor.getInt( 18 ) );		entity.setAnswerCount( cursor.getInt( 19 ) );		entity.setPraiseCount( cursor.getInt( 20 ) );				cursor.close();		return entity;	}		public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) 	{//		if( newVersion == 2 )//		{//			Log.e( "ss", "ya" + newVersion );//			db.execSQL( "delete from user" );//			db.execSQL( "delete from question" );//			db.execSQL( "delete from friend" );//			db.execSQL( "ALTER TABLE question ADD COLUMN answerModify text default \"\"" );//		}	}}