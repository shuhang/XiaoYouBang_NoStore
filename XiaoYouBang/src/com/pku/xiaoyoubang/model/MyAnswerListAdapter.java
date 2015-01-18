package com.pku.xiaoyoubang.model;import java.util.List;import android.content.Context;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.ViewGroup;import android.widget.BaseAdapter;import android.widget.ImageView;import android.widget.TextView;import com.nostra13.universalimageloader.core.DisplayImageOptions;import com.nostra13.universalimageloader.core.ImageLoader;import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.entity.AnswerEntity;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.Tool;public class MyAnswerListAdapter extends BaseAdapter{	private Context context;	private List< AnswerEntity > list;	private ImageLoader imageLoader;	private DisplayImageOptions options;	private MyAnswerListListener listener;		public MyAnswerListAdapter( Context context, List< AnswerEntity > list )	{		this.context = context;		this.list = list;		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder( context ).build();		ImageLoader.getInstance().init( config );				this.options = new DisplayImageOptions.Builder()		.showImageOnLoading( R.drawable.head )		.showImageForEmptyUri( R.drawable.head )		.showImageOnFail( R.drawable.head )		.cacheInMemory( true )		.cacheOnDisk( true )		.build();		this.imageLoader = ImageLoader.getInstance();	}		public List< AnswerEntity > getList()	{		return list;	} 		public int getCount() 	{		return list.size();	}		public void addItemTop( AnswerEntity entity )	{		list.add( 0, entity );	}	public Object getItem( int position ) 	{		return list.get( position );	}	public long getItemId( int position ) 	{		return position;	}	public View getView( final int position, View convertView, ViewGroup parent )	{		ViewHolder viewHolder;		if( convertView == null )		{			convertView = LayoutInflater.from( context ).inflate( R.layout.my_answer_list_item, null );			viewHolder = new ViewHolder();						viewHolder.headImage = ( ImageView ) convertView.findViewById( R.id.my_answer_list_item_head );			viewHolder.textName = ( TextView ) convertView.findViewById( R.id.my_answer_list_item_name );			viewHolder.textCommentCount = ( TextView ) convertView.findViewById( R.id.my_answer_list_item_comment_count );			viewHolder.textPraiseCount = ( TextView ) convertView.findViewById( R.id.my_answer_list_item_praise_count );			viewHolder.textAnswerInfo = ( TextView ) convertView.findViewById( R.id.my_answer_list_item_answer );			viewHolder.textQuestion = ( TextView ) convertView.findViewById( R.id.my_answer_list_item_question );			viewHolder.textTime = ( TextView ) convertView.findViewById( R.id.my_answer_list_item_time );						convertView.setTag( viewHolder );		}		else		{			viewHolder = ( ViewHolder ) convertView.getTag(); 		}				AnswerEntity entity = list.get( position );				viewHolder.textQuestion.setText( entity.getQuestionerName() + "：" + entity.getQuestionTitle() );		viewHolder.textQuestion.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					if( listener != null )					{						listener.clickQuestion( position );					}				}			}		);		viewHolder.textTime.setText( Tool.getShowTime( entity.getCreateTime() ) );				String url = entity.getUserHeadUrl();		if( entity.isInvisible() )		{			url = "";			viewHolder.textName.setText( "匿名用户" );		}		else		{			viewHolder.textName.setText( entity.getName() );		}		imageLoader.displayImage( Information.Server_Url + url, viewHolder.headImage, options );		viewHolder.textCommentCount.setText( entity.getCommentCount() + "" );		viewHolder.textAnswerInfo.setText( entity.getAnswerInfo() );		viewHolder.textPraiseCount.setText( entity.getPraiseCount() + "" );				return convertView;	}		static class ViewHolder	{		ImageView headImage;		TextView textName;		TextView textCommentCount;		TextView textPraiseCount;		TextView textAnswerInfo;		TextView textTime;		TextView textQuestion;	}		public void setMyListener( MyAnswerListListener listener )	{		this.listener = listener;	}		public interface MyAnswerListListener	{		public void clickQuestion( int index );	}}