/** * @file XListView.java * @package me.maxwin.view * @create Mar 18, 2012 6:28:41 PM * @author Maxwin * @description An ListView support (a) Pull down to refresh, (b) Pull up to load more. * 		Implement IXListViewListener, and see stopRefresh() / stopLoadMore(). */package com.pku.xiaoyoubang.selfview;import android.content.Context;import android.util.AttributeSet;import android.view.LayoutInflater;import android.view.MotionEvent;import android.view.View;import android.view.ViewTreeObserver.OnGlobalLayoutListener;import android.view.animation.DecelerateInterpolator;import android.widget.AbsListView;import android.widget.AbsListView.OnScrollListener;import android.widget.ImageView;import android.widget.ListAdapter;import android.widget.ListView;import android.widget.RelativeLayout;import android.widget.Scroller;import android.widget.TextView;import com.nostra13.universalimageloader.core.DisplayImageOptions;import com.nostra13.universalimageloader.core.ImageLoader;import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.Tool;import com.pku.xiaoyoubang.view.AnswerInfoActivity;public class CommentListView extends ListView implements OnScrollListener {	private float mLastY = -1; // save event y	private Scroller mScroller; // used for scroll back	private OnScrollListener mScrollListener; // user's scroll listener	// the interface to trigger refresh and load more.	private IXListViewListener mListViewListener;	// -- header view	private XListViewHeader mHeaderView;	// header view content, use it to calculate the Header's height. And hide it	// when disable pull refresh.	private RelativeLayout mHeaderViewContent;	private TextView mHeaderTimeView;	private int mHeaderViewHeight; // header view's height	private boolean mEnablePullRefresh = true;	private boolean mPullRefreshing = false; // is refreashing.	// -- footer view	private XListViewFooter mFooterView;	private boolean mEnablePullLoad;	private boolean mPullLoading;	private boolean mIsFooterReady = false;	// total list items, used to detect is at the bottom of listview.	private int mTotalItemCount;	// for mScroller, scroll back from header or footer.	private int mScrollBack;	private final static int SCROLLBACK_HEADER = 0;	private final static int SCROLLBACK_FOOTER = 1;	private final static int SCROLL_DURATION = 400; // scroll back duration	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px														// at bottom, trigger														// load more.	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull		private View viewHeader;	private ImageView imageHead;	private TextView textQuestion;	private TextView textName;	private ImageView imageSex;	private TextView textPku;	private TextView textTime;	private TextView textCompany;	private TextView textPart;	private TextView textJob;	private TextView textInvite;	private TextView textAnswerInfo;	private TextView textPraiseCount;	private TextView textPraiseUser;	private TextView textCommentCount;	private RelativeLayout layoutHead;		private ImageLoader imageLoader;	private DisplayImageOptions options;	/**	 * @param context	 */	public CommentListView(Context context)	{		super(context);		initWithContext(context);	}	public CommentListView(Context context, AttributeSet attrs)	{		super(context, attrs);		initWithContext(context);	}	public CommentListView(Context context, AttributeSet attrs, int defStyle)	{		super(context, attrs, defStyle);		initWithContext(context);	}	private void initWithContext(Context context) 	{		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder( context ).build();		ImageLoader.getInstance().init( config );				this.options = new DisplayImageOptions.Builder()		.showImageOnLoading( R.drawable.head )		.showImageForEmptyUri( R.drawable.head )		.showImageOnFail( R.drawable.head )		.cacheInMemory( true )		.cacheOnDisk( true )		.build();		this.imageLoader = ImageLoader.getInstance();				mScroller = new Scroller(context, new DecelerateInterpolator());		// XListView need the scroll event, and it will dispatch the event to		// user's listener (as a proxy).		super.setOnScrollListener(this);		// init header view		mHeaderView = new XListViewHeader(context);		mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);		mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.xlistview_header_time);		addHeaderView(mHeaderView);				/**		 * 		 */		viewHeader = LayoutInflater.from( context ).inflate( R.layout.answer_info_list_header, null );		addHeaderView( viewHeader );				imageHead = ( ImageView ) viewHeader.findViewById( R.id.answer_info_list_header_head );		if( !AnswerInfoActivity.entity.isInvisible() )		{			imageHead.setTag( AnswerInfoActivity.entity.getUserHeadUrl() );			imageLoader.displayImage( Information.Server_Url + AnswerInfoActivity.entity.getUserHeadUrl(), imageHead, options );		}		textQuestion = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_question );		textQuestion.setText( "问题：" + AnswerInfoActivity.entity.getQuestionTitle() );		textQuestion.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.backTo();				}			}		);				textName = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_name);		imageSex = ( ImageView ) viewHeader.findViewById( R.id.answer_info_list_header_sex );		textPku = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_pku );		if( !AnswerInfoActivity.entity.isInvisible() )		{			textName.setText( AnswerInfoActivity.entity.getName() );			textPku.setText( AnswerInfoActivity.entity.getPku() );					if( AnswerInfoActivity.entity.getSex() == 1 )			{				imageSex.setImageResource(R.drawable.male_color );			}			else			{				imageSex.setImageResource(R.drawable.female_color );			}		}		else		{			textName.setText( "匿名用户" );		}		textTime = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_time );		textTime.setText( Tool.getShowTime( AnswerInfoActivity.entity.getCreateTime() ) );		textCompany = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_company );		textPart = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_part );		textJob = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_job );		textInvite = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_invite );		textAnswerInfo = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_answer );		textAnswerInfo.setText( AnswerInfoActivity.entity.getAnswerInfo() );		textPraiseCount = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_praise_count );		textPraiseUser = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_praise_user );		textCommentCount = ( TextView ) viewHeader.findViewById( R.id.answer_info_list_header_comment_count );					textAnswerInfo.setOnTouchListener		(			new OnTouchListener()			{				public boolean onTouch( View v, MotionEvent event ) 				{					int[] location = new int[2];					textAnswerInfo.getLocationOnScreen( location );				    int y = location[ 1 ];					mListViewListener.showCopy( y, ( int ) textAnswerInfo.getHeight() );					return false;				}			}		);				layoutHead = ( RelativeLayout ) viewHeader.findViewById( R.id.answer_info_list_header_layout1 );		layoutHead.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.showHead();				}							}		);				mFooterView = new XListViewFooter(context);		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener		(			new OnGlobalLayoutListener() 			{				@SuppressWarnings("deprecation")				public void onGlobalLayout()				{					mHeaderViewHeight = mHeaderViewContent.getHeight();					getViewTreeObserver().removeGlobalOnLayoutListener(this);				}			}		);	}	public void updateHeader()	{		if( !AnswerInfoActivity.entity.isInvisible() )		{			String value = AnswerInfoActivity.entity.getCompany();			if( value.length() > 8 )			{				value = value.substring( 0, 8 ) + "...";			}			textCompany.setText( value );			value = AnswerInfoActivity.entity.getPart();			if( value.length() > 5 )			{				value = value.substring( 0, 5 ) + "...";			}			textPart.setText( value );			value = AnswerInfoActivity.entity.getJob();			if( value.length() > 5 )			{				value = value.substring( 0, 5 ) + "...";			}			textJob.setText( value );		}		final int count = AnswerInfoActivity.entity.getInvite().size();		if( count == 0 )		{			textInvite.setVisibility( View.GONE );		}		else		{			textInvite.setVisibility( View.VISIBLE );			String value = AnswerInfoActivity.entity.getInvite().get( 0 );			final int count1 = AnswerInfoActivity.entity.getInvite().size();			for( int i = 1; i < count1; i ++ )			{				value += "、" + AnswerInfoActivity.entity.getInvite().get( i );			}			textInvite.setText( "应 " + value + " 邀请作答" );		}		textPraiseCount.setText( "赞 " + AnswerInfoActivity.entity.getPraise().size() );		final int count1 = AnswerInfoActivity.entity.getPraise().size();		StringBuilder value = new StringBuilder();		for( int i = 0; i < count1; i ++ )		{			if( i > 0 ) value.append( "、" );			value.append( AnswerInfoActivity.entity.getPraise().get( i ) );		} 		textPraiseUser.setText( value.toString() );		textCommentCount.setText( "评论 " + AnswerInfoActivity.entity.getCommentList().size() );	}		public void startRefresh()	{		mPullRefreshing = true;		mHeaderView.setState(XListViewHeader.STATE_REFRESHING);		if( mListViewListener != null )		{			mListViewListener.onRefresh();		}	}		public void setAdapter(ListAdapter adapter)	{		if (mIsFooterReady == false) 		{			mIsFooterReady = true;			addFooterView(mFooterView);		}		super.setAdapter(adapter);	}	/**	 * enable or disable pull down refresh feature.	 * 	 * @param enable	 */	public void setPullRefreshEnable(boolean enable) {		mEnablePullRefresh = enable;		if (!mEnablePullRefresh) { // disable, hide the content			mHeaderViewContent.setVisibility(View.INVISIBLE);		} else {			mHeaderViewContent.setVisibility(View.VISIBLE);		}	}	/**	 * enable or disable pull up load more feature.	 * 	 * @param enable	 */	public void setPullLoadEnable(boolean enable) {		mEnablePullLoad = enable;		if (!mEnablePullLoad) {			mFooterView.hide();			mFooterView.setOnClickListener(null);		} else {			mPullLoading = false;			mFooterView.show();			mFooterView.setState(XListViewFooter.STATE_NORMAL);			// both "pull up" and "click" will invoke load more.			mFooterView.setOnClickListener(new OnClickListener() {				@Override				public void onClick(View v) {					startLoadMore();				}			});		}	}	/**	 * stop refresh, reset header view.	 */	public void stopRefresh() {		if (mPullRefreshing == true) {			mPullRefreshing = false;			resetHeaderHeight();		}	}	/**	 * stop load more, reset footer view.	 */	public void stopLoadMore() {		if (mPullLoading == true) {			mPullLoading = false;			mFooterView.setState(XListViewFooter.STATE_NORMAL);		}	}	/**	 * set last refresh time	 * 	 * @param time	 */	public void setRefreshTime(String time) {		mHeaderTimeView.setText(time);	}	private void invokeOnScrolling() {		if (mScrollListener instanceof OnXScrollListener) {			OnXScrollListener l = (OnXScrollListener) mScrollListener;			l.onXScrolling(this);		}	}	public void updateHeaderHeight(float delta) {		mHeaderView.setVisiableHeight((int) delta				+ mHeaderView.getVisiableHeight());		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {				mHeaderView.setState(XListViewHeader.STATE_READY);			} else {				mHeaderView.setState(XListViewHeader.STATE_NORMAL);			}		}		setSelection(0); // scroll to top each time	}	/**	 * reset header view's height.	 */	private void resetHeaderHeight() {		int height = mHeaderView.getVisiableHeight();		if (height == 0) // not visible.			return;		// refreshing and header isn't shown fully. do nothing.		if (mPullRefreshing && height <= mHeaderViewHeight) {			return;		}		int finalHeight = 0; // default: scroll back to dismiss header.		// is refreshing, just scroll back to show all the header.		if (mPullRefreshing && height > mHeaderViewHeight) {			finalHeight = mHeaderViewHeight;		}		mScrollBack = SCROLLBACK_HEADER;		mScroller.startScroll(0, height, 0, finalHeight - height,				SCROLL_DURATION);		// trigger computeScroll		invalidate();	}	private void updateFooterHeight(float delta) {		int height = mFooterView.getBottomMargin() + (int) delta;		if (mEnablePullLoad && !mPullLoading) {			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load													// more.				mFooterView.setState(XListViewFooter.STATE_READY);			} else {				mFooterView.setState(XListViewFooter.STATE_NORMAL);			}		}		mFooterView.setBottomMargin(height);		// setSelection(mTotalItemCount - 1); // scroll to bottom	}	private void resetFooterHeight() {		int bottomMargin = mFooterView.getBottomMargin();		if (bottomMargin > 0) {			mScrollBack = SCROLLBACK_FOOTER;			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,					SCROLL_DURATION);			invalidate();		}	}	private void startLoadMore() {		mPullLoading = true;		mFooterView.setState(XListViewFooter.STATE_LOADING);		if (mListViewListener != null) {			mListViewListener.onLoadMore();		}	}	@Override	public boolean onTouchEvent(MotionEvent ev) {		if (mLastY == -1) {			mLastY = ev.getRawY();		}		switch (ev.getAction()) {		case MotionEvent.ACTION_DOWN:			mLastY = ev.getRawY();			break;		case MotionEvent.ACTION_MOVE:			final float deltaY = ev.getRawY() - mLastY;			mLastY = ev.getRawY();			System.out.println("数据监测：" + getFirstVisiblePosition() + "---->"					+ getLastVisiblePosition());			if (getFirstVisiblePosition() == 0					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {				// the first item is showing, header has shown or pull down.				updateHeaderHeight(deltaY / OFFSET_RADIO);				invokeOnScrolling();			} else if (getLastVisiblePosition() == mTotalItemCount - 1					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {				// last item, already pulled up or want to pull up.				updateFooterHeight(-deltaY / OFFSET_RADIO);			}			break;		default:			mLastY = -1; // reset			if (getFirstVisiblePosition() == 0) {				// invoke refresh				if (mEnablePullRefresh						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {					mPullRefreshing = true;					mHeaderView.setState(XListViewHeader.STATE_REFRESHING);					if (mListViewListener != null) {						mListViewListener.onRefresh();					}				}				resetHeaderHeight();			}			if (getLastVisiblePosition() == mTotalItemCount - 1) {				// invoke load more.				if (mEnablePullLoad						&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {					startLoadMore();				}				resetFooterHeight();			}			break;		}		return super.onTouchEvent(ev);	}	@Override	public void computeScroll() {		if (mScroller.computeScrollOffset()) {			if (mScrollBack == SCROLLBACK_HEADER) {				mHeaderView.setVisiableHeight(mScroller.getCurrY());			} else {				mFooterView.setBottomMargin(mScroller.getCurrY());			}			postInvalidate();			invokeOnScrolling();		}		super.computeScroll();	}	@Override	public void setOnScrollListener(OnScrollListener l) {		mScrollListener = l;	}	@Override	public void onScrollStateChanged(AbsListView view, int scrollState) {		if (mScrollListener != null) {			mScrollListener.onScrollStateChanged(view, scrollState);		}	}	@Override	public void onScroll(AbsListView view, int firstVisibleItem,			int visibleItemCount, int totalItemCount) {		// send to user's listener		mTotalItemCount = totalItemCount;		if (mScrollListener != null) {			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,					totalItemCount);		}	}	public void setXListViewListener(IXListViewListener l) {		mListViewListener = l;	}	/**	 * you can listen ListView.OnScrollListener or this one. it will invoke	 * onXScrolling when header/footer scroll back.	 */	public interface OnXScrollListener extends OnScrollListener {		public void onXScrolling(View view);	}	/**	 * implements this interface to get refresh/load more event.	 */	public interface IXListViewListener 	{		public void onRefresh();		public void onLoadMore();				public void backTo();		public void showHead();		public void showCopy( int x, int y );	}}