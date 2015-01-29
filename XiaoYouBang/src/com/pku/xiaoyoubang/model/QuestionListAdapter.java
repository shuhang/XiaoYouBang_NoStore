package com.pku.xiaoyoubang.model;import java.util.List;import android.content.Context;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.BaseAdapter;import android.widget.ImageView;import android.widget.TextView;import com.nostra13.universalimageloader.core.ImageLoader;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.QuestionEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.Tool;public class QuestionListAdapter extends BaseAdapter{	private List< QuestionEntity > list;	private Context context;	private ImageLoader imageLoader;		public QuestionListAdapter( List< QuestionEntity > list, Context context )	{		this.list = list;		this.context = context;		this.imageLoader = ImageLoader.getInstance();	}		public void addItemTop( QuestionEntity entity )	{		list.add( 0, entity );	}		public void addItemBottom( QuestionEntity entity )	{		list.add( entity );	}		public List< QuestionEntity > getList()	{		return list;	}		public int getCount() 	{		return list.size();	}	public Object getItem( int position ) 	{		return list.get( position );	}	public long getItemId( int position ) 	{		return position;	}		public View getView( int position, View convertView, ViewGroup parent ) 	{		ViewHolder viewHolder;		if( convertView == null )		{			convertView = LayoutInflater.from( context ).inflate( R.layout.question_list_item, null );			viewHolder = new ViewHolder();			convertView.setTag( viewHolder );						viewHolder.userHeadImage = ( ImageView ) convertView.findViewById( R.id.question_list_item_head );			viewHolder.pointUp = ( ImageView ) convertView.findViewById( R.id.question_list_item_point_up );			viewHolder.textUserName = ( TextView ) convertView.findViewById( R.id.question_list_item_name );			viewHolder.textAnswerCount = ( TextView ) convertView.findViewById( R.id.question_list_item_answer_count );			viewHolder.textAnswerSymbol = ( TextView ) convertView.findViewById( R.id.question_list_item_temp_text1 );			viewHolder.textPraiseCount = ( TextView ) convertView.findViewById( R.id.question_list_item_praise_count );			viewHolder.textQuestion = ( TextView ) convertView.findViewById( R.id.question_list_item_question );			viewHolder.textTime = ( TextView ) convertView.findViewById( R.id.question_list_item_time );			viewHolder.textInviteMe = ( TextView ) convertView.findViewById( R.id.question_list_item_invite_me );			viewHolder.textMyInvite = ( TextView ) convertView.findViewById( R.id.question_list_item_my_invite );			viewHolder.textSymbol = ( TextView ) convertView.findViewById( R.id.question_list_item_symbol );		}		else		{			viewHolder = ( ViewHolder ) convertView.getTag();		}		QuestionEntity entity = list.get( position );		String url = entity.getUserHeadUrl();		if( entity.isInvisible() )		{			url = "";			viewHolder.textUserName.setText( "匿名用户" );		}		else		{			viewHolder.textUserName.setText( entity.getUserName() );		}		imageLoader.displayImage( Information.Server_Url + url, viewHolder.userHeadImage, Information.options );				viewHolder.textAnswerCount.setText( "" + entity.getAnswerCount() );		viewHolder.textPraiseCount.setText( "" + entity.getPraiseCount() );		viewHolder.textQuestion.setText( entity.getQuestionTitle() );		viewHolder.textTime.setText( "" );			if( entity.isNew() )		{			viewHolder.pointUp.setVisibility( View.VISIBLE );		}		else		{			viewHolder.pointUp.setVisibility( View.INVISIBLE );		}				if( entity.isUpdate() )		{			viewHolder.textAnswerCount.setTextColor( context.getResources().getColor( R.color.text_red ) );			viewHolder.textAnswerSymbol.setTextColor( context.getResources().getColor( R.color.text_red ) );		}		else		{			viewHolder.textAnswerCount.setTextColor( context.getResources().getColor( R.color.gray ) );			viewHolder.textAnswerSymbol.setTextColor( context.getResources().getColor( R.color.gray ) );		}				int count = entity.getInviteMeList().size();		if( count == 0 )		{			viewHolder.textInviteMe.setVisibility( View.GONE );		}		else		{			viewHolder.textInviteMe.setVisibility( View.VISIBLE );			String value = entity.getInviteMeList().get( 0 ).getName();			if( count > 1 ) value += "、" + entity.getInviteMeList().get( 1 ).getName();			if( count > 2 ) value += "等" + count + "人";			viewHolder.textInviteMe.setText( value + " 邀请你回答" );		}				count = entity.getMyInviteList().size();		if( count == 0 )		{			viewHolder.textMyInvite.setVisibility( View.GONE );		}		else		{			viewHolder.textMyInvite.setVisibility( View.VISIBLE );			String value = entity.getMyInviteList().get( 0 ).getName();			if( count > 1 ) value += "、" + entity.getMyInviteList().get( 1 ).getName();			if( count > 2 ) value += "等" + count + "人";			viewHolder.textMyInvite.setText( "你邀请 " + value + " 回答" );		}				viewHolder.textSymbol.setText( Tool.judgeTimeSymbol( entity.getModifyTime() ) );				return convertView;	}		static class ViewHolder	{		ImageView userHeadImage;		ImageView pointUp;		TextView textUserName;		TextView textAnswerCount;		TextView textAnswerSymbol;		TextView textPraiseCount;		TextView textQuestion;		TextView textTime;		TextView textInviteMe;		TextView textMyInvite;		TextView textSymbol;	}}