package com.pku.xiaoyoubang.view;import java.text.Collator;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.List;import android.annotation.SuppressLint;import android.app.Activity;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.View;import android.view.Window;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.ListView;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.InviteUserEntity;import com.pku.xiaoyoubang.model.ChooseInviteListAdapter;import com.pku.xiaoyoubang.tool.MyApplication;import com.pku.xiaoyoubang.tool.MyDatabaseHelper;import com.pku.xiaoyoubang.tool.Tool;@SuppressLint("HandlerLeak")public class ChooseInviteActivity extends Activity{	private ListView listView;	private ChooseInviteListAdapter adapter;	private List< InviteUserEntity > allList = new ArrayList< InviteUserEntity >();	private Handler handler;		@SuppressWarnings("rawtypes")	private Comparator cmp = Collator.getInstance( java.util.Locale.CHINA );		protected void onCreate( Bundle savedInstanceState )	{		super.onCreate( savedInstanceState );		requestWindowFeature( Window.FEATURE_NO_TITLE );		MyApplication.getInstance().addActivity( this );				handler = new Handler()		{			@SuppressWarnings("unchecked")			public void handleMessage( Message message )			{				if( message.what == 0 )				{					adapter = new ChooseInviteListAdapter( ChooseInviteActivity.this, ( ArrayList< InviteUserEntity > ) message.obj );					listView.setAdapter( adapter );					adapter.notifyDataSetChanged();				}			}		};				initView();				loadFriend();	}	private void initView()	{		setContentView( R.layout.choose_invite );				listView = ( ListView ) findViewById( R.id.choose_invite_list );		adapter = new ChooseInviteListAdapter( this, allList );		listView.setAdapter( adapter );				listView.setOnItemClickListener		(			new OnItemClickListener()			{				public void onItemClick( AdapterView<?> parent, View view, int position, long id )				{					if( Tool.isFastDoubleClick() )					{						return;					}					judgeIndex( position );				}			}		);	}	public void judgeIndex( int position )	{		InviteUserEntity entity = adapter.getItem( position );		if( !entity.isHasAnswered() && !entity.isHasInvited() && !entity.isHasJoined() )		{			getIntent().putExtra( "user", entity );			setResult( 1, getIntent() );			finish();		}	}		private void loadFriend()	{		new Thread		(			new Thread()			{				public void run()				{					doLoadFriend();				}			}		).start();	}		public Comparator< InviteUserEntity > comparator = new Comparator< InviteUserEntity >()	{        		@SuppressWarnings("unchecked")		public int compare( InviteUserEntity entity1, InviteUserEntity entity2 ) 		{              			return cmp.compare( entity1.getName(), entity2.getName() );         		}     	};		private void doLoadFriend()	{		Message message = handler.obtainMessage();		message.what = 0;		List< InviteUserEntity > tempList = MyDatabaseHelper.getInstance( this ).getInviterList();		Collections.sort( tempList, comparator );		message.obj = tempList;		handler.sendMessage( message );	}}