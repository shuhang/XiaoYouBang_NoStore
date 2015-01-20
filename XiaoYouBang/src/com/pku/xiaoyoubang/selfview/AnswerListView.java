/** * @file XListView.java * @package me.maxwin.view * @create Mar 18, 2012 6:28:41 PM * @author Maxwin * @description An ListView support (a) Pull down to refresh, (b) Pull up to load more. * 		Implement IXListViewListener, and see stopRefresh() / stopLoadMore(). */package com.pku.xiaoyoubang.selfview;import android.content.Context;import android.util.AttributeSet;import android.view.LayoutInflater;import android.view.MotionEvent;import android.view.View;import android.view.ViewTreeObserver.OnGlobalLayoutListener;import android.view.animation.DecelerateInterpolator;import android.widget.AbsListView;import android.widget.AbsListView.OnScrollListener;import android.widget.Button;import android.widget.ImageView;import android.widget.ListAdapter;import android.widget.ListView;import android.widget.RelativeLayout;import android.widget.Scroller;import android.widget.TextView;import com.nostra13.universalimageloader.core.DisplayImageOptions;import com.nostra13.universalimageloader.core.ImageLoader;import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;import com.pku.xiaoyoubang.R;import com.pku.xiaoyoubang.tool.Information;import com.pku.xiaoyoubang.tool.Tool;import com.pku.xiaoyoubang.view.QuestionInfoActivity;public class AnswerListView extends ListView implements OnScrollListener {	private float mLastY = -1; // save event y	private Scroller mScroller; // used for scroll back	private OnScrollListener mScrollListener; // user's scroll listener	// the interface to trigger refresh and load more.	private IXListViewListener mListViewListener;	private Context context;	// -- header view	private XListViewHeader mHeaderView;	// header view content, use it to calculate the Header's height. And hide it	// when disable pull refresh.	private RelativeLayout mHeaderViewContent;	private TextView mHeaderTimeView;	private int mHeaderViewHeight; // header view's height	private boolean mEnablePullRefresh = true;	private boolean mPullRefreshing = false; // is refreashing.	// -- footer view	private XListViewFooter mFooterView;	private boolean mEnablePullLoad;	private boolean mPullLoading;	private boolean mIsFooterReady = false;	// total list items, used to detect is at the bottom of listview.	private int mTotalItemCount;	// for mScroller, scroll back from header or footer.	private int mScrollBack;	private final static int SCROLLBACK_HEADER = 0;	private final static int SCROLLBACK_FOOTER = 1;	private final static int SCROLL_DURATION = 400; // scroll back duration	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px														// at bottom, trigger														// load more.	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull		private View viewHeader;	private ImageView headImage;	private TextView textName;	private ImageView imageSex;	private TextView textPKU;	private TextView textCompany;	private TextView textJob;	private TextView textTime;	private TextView textQuestionTitle;	private TextView textQuestionInfo;	private TextView textInviteMe;	private TextView textInviteSymbol;	private TextView textMyInvite;	private TextView textMyInviteSymbol;	private TextView textCommentInfo;	private TextView textCommentCount;	private TextView textAnswerCount;	private Button buttonAddAnswer;	private Button buttonInvite;	private RelativeLayout layoutUser;	private RelativeLayout layoutInviteMe;	private RelativeLayout layoutMyInvite;	private RelativeLayout layoutComment;		private ImageLoader imageLoader;	private DisplayImageOptions options;	/**	 * @param context	 */	public AnswerListView(Context context) {		super(context);		initWithContext(context);	}	public AnswerListView(Context context, AttributeSet attrs) {		super(context, attrs);		initWithContext(context);	}	public AnswerListView(Context context, AttributeSet attrs, int defStyle) {		super(context, attrs, defStyle);		initWithContext(context);	}	private void initWithContext(Context context) 	{		this.context = context;				ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder( context ).build();		ImageLoader.getInstance().init( config );				this.options = new DisplayImageOptions.Builder()		.showImageOnLoading( R.drawable.head )		.showImageForEmptyUri( R.drawable.head )		.showImageOnFail( R.drawable.head )		.cacheInMemory( true )		.cacheOnDisk( true )		.build();		this.imageLoader = ImageLoader.getInstance();				mScroller = new Scroller(context, new DecelerateInterpolator());		// XListView need the scroll event, and it will dispatch the event to		// user's listener (as a proxy).		super.setOnScrollListener(this);		// init header view		mHeaderView = new XListViewHeader(context);		mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);		mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.xlistview_header_time);		addHeaderView(mHeaderView);				/**		 * 		 */		viewHeader = LayoutInflater.from( context ).inflate( R.layout.question_info_answer_list_header, null );		addHeaderView( viewHeader );				headImage = ( ImageView ) viewHeader.findViewById( R.id.question_info_answer_list_header_head );		if( !QuestionInfoActivity.entity.isInvisible() )		{			headImage.setTag( QuestionInfoActivity.entity.getUserHeadUrl() );			imageLoader.displayImage( Information.Server_Url + QuestionInfoActivity.entity.getUserHeadUrl(), headImage, options );		}		textName = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_name );		if( QuestionInfoActivity.entity.isInvisible() )		{			textName.setText( "匿名用户" );		}		else		{			textName.setText( QuestionInfoActivity.entity.getUserName() );		}		imageSex = ( ImageView ) viewHeader.findViewById( R.id.question_info_answer_list_header_sex );		textPKU = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_pku );		if( !QuestionInfoActivity.entity.isInvisible() )		{			if( QuestionInfoActivity.entity.getSex() == 1 )			{				imageSex.setImageResource( R.drawable.male_color );			}			else			{				imageSex.setImageResource( R.drawable.female_color );			}						textPKU.setText( QuestionInfoActivity.entity.getPKU() );		}		textCompany = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_company );		textJob = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_job );				textTime = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_time );		textTime.setText( Tool.getShowTime( QuestionInfoActivity.entity.getCreateTime() ) );		textQuestionTitle = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_question_title );		textQuestionTitle.setText( QuestionInfoActivity.entity.getQuestionTitle() );		textQuestionInfo = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_question_info );		textQuestionInfo.setText( QuestionInfoActivity.entity.getQuestionInfo() );		textInviteMe = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_invite_me );				textInviteSymbol = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_invite_me_symbol );		textInviteSymbol.setText( ">" );		textMyInvite = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_my_invite );				textMyInviteSymbol = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_my_invite_symbol );		textMyInviteSymbol.setText( ">" );		textCommentInfo = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_comment_info );				textCommentCount = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_comment_count );			textAnswerCount = ( TextView ) viewHeader.findViewById( R.id.question_info_answer_list_header_answer_count );			buttonAddAnswer = ( Button ) viewHeader.findViewById( R.id.question_info_answer_list_header_button_add );		buttonAddAnswer.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.addAnswer();				}			}		);				buttonInvite = ( Button ) viewHeader.findViewById( R.id.question_info_answer_list_header_button_invite );		buttonInvite.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.invite();				}			}		);				layoutInviteMe = ( RelativeLayout ) viewHeader.findViewById( R.id.question_info_answer_list_header_invite_me_layout );		layoutInviteMe.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.showInvite( true );				}			}		);		layoutMyInvite = ( RelativeLayout ) viewHeader.findViewById( R.id.question_info_answer_list_header_my_invite_layout );		layoutMyInvite.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.showInvite( false );				}			}		);		layoutUser = ( RelativeLayout ) viewHeader.findViewById( R.id.question_info_answer_list_header_layout1 );		layoutUser.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.showUserInfo();				}			}		);				layoutComment = ( RelativeLayout ) viewHeader.findViewById( R.id.question_info_answer_list_header_layout2 );		layoutComment.setOnClickListener		(			new OnClickListener()			{				public void onClick( View view )				{					mListViewListener.showCommentList();				}			}		);		mFooterView = new XListViewFooter(context);		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener		(			new OnGlobalLayoutListener()			{				@SuppressWarnings("deprecation")				public void onGlobalLayout()				{					mHeaderViewHeight = mHeaderViewContent.getHeight();					getViewTreeObserver().removeGlobalOnLayoutListener(this);				}			}		);	}		public void updateHeader()	{		if( !QuestionInfoActivity.entity.isInvisible() )		{			String value = QuestionInfoActivity.entity.getCompany();			if( value.length() > 8 )			{				value = value.substring( 0, 8 ) + "...";			}			textCompany.setText( value );			value = QuestionInfoActivity.entity.getJob();			if( value.length() > 5 )			{				value = value.substring( 0, 5 ) + "...";			}			textJob.setText( value );		}		int count = QuestionInfoActivity.entity.getInviteMeList().size();		if( count > 0 )		{			textInviteMe.setVisibility( View.VISIBLE );			String value = QuestionInfoActivity.entity.getInviteMeList().get( 0 ).getName();			if( count > 1 ) value += "、" + QuestionInfoActivity.entity.getInviteMeList().get( 1 ).getName();			if( count > 2 ) value += "等" + count + "人";			textInviteMe.setText( value + " 邀请你回答" );		}		else		{			textInviteMe.setVisibility( View.GONE );		}		count = QuestionInfoActivity.entity.getMyInviteList().size();		if( count > 0 )		{			textMyInvite.setVisibility( View.VISIBLE );			String value = QuestionInfoActivity.entity.getMyInviteList().get( 0 ).getName();			if( count > 1 ) value += "、" + QuestionInfoActivity.entity.getMyInviteList().get( 1 ).getName();			if( count > 2 ) value += "等" + count + "人";			textMyInvite.setText( "你邀请 " + value + " 回答" );		}		else		{			textMyInvite.setVisibility( View.GONE );		}		if( QuestionInfoActivity.entity.getCommentList().size() > 0 )		{			String comment = QuestionInfoActivity.entity.getCommentList().get( 0 ).getUserName() + "：" + QuestionInfoActivity.entity.getCommentList().get( 0 ).getCommentInfo();			if( comment.length() > 14 )			{				comment = comment.substring( 0, 14 ) + "...";			}			textCommentInfo.setText( comment );		}		if( QuestionInfoActivity.entity.getCommentList().size() > 0 )		{			textCommentCount.setText( "评论" + QuestionInfoActivity.entity.getCommentList().size() + " >" );		}		else		{			textCommentCount.setText( "+ 添加评论" );		}		textAnswerCount.setText( "回答 " + QuestionInfoActivity.entity.getAnswerCount() );		if( QuestionInfoActivity.entity.getInviteMeList().size() == 0 )		{			layoutInviteMe.setVisibility( View.GONE );		}		else		{			layoutInviteMe.setVisibility( View.VISIBLE );		}		if( QuestionInfoActivity.entity.getMyInviteList().size() == 0 )		{			layoutMyInvite.setVisibility( View.GONE );		}		else		{			layoutMyInvite.setVisibility( View.VISIBLE );		}				if( QuestionInfoActivity.entity.isHasAnswered() )		{			buttonAddAnswer.setBackgroundColor( context.getResources().getColor( R.color.heavy_gray ) );			buttonAddAnswer.setTextColor( context.getResources().getColor( R.color.gray ) );			buttonAddAnswer.setEnabled( false );		}	}	public void setAdapter(ListAdapter adapter)	{		if (mIsFooterReady == false)		{			mIsFooterReady = true;			addFooterView(mFooterView);		}		super.setAdapter(adapter);	}	/**	 * enable or disable pull down refresh feature.	 * 	 * @param enable	 */	public void setPullRefreshEnable(boolean enable) {		mEnablePullRefresh = enable;		if (!mEnablePullRefresh) { // disable, hide the content			mHeaderViewContent.setVisibility(View.INVISIBLE);		} else {			mHeaderViewContent.setVisibility(View.VISIBLE);		}	}	/**	 * enable or disable pull up load more feature.	 * 	 * @param enable	 */	public void setPullLoadEnable(boolean enable) {		mEnablePullLoad = enable;		if (!mEnablePullLoad) {			mFooterView.hide();			mFooterView.setOnClickListener(null);		} else {			mPullLoading = false;			mFooterView.show();			mFooterView.setState(XListViewFooter.STATE_NORMAL);			// both "pull up" and "click" will invoke load more.			mFooterView.setOnClickListener(new OnClickListener() {				@Override				public void onClick(View v) {					startLoadMore();				}			});		}	}	/**	 * stop refresh, reset header view.	 */	public void stopRefresh() {		if (mPullRefreshing == true) {			mPullRefreshing = false;			resetHeaderHeight();		}	}	/**	 * stop load more, reset footer view.	 */	public void stopLoadMore() {		if (mPullLoading == true) {			mPullLoading = false;			mFooterView.setState(XListViewFooter.STATE_NORMAL);		}	}	/**	 * set last refresh time	 * 	 * @param time	 */	public void setRefreshTime(String time) {		mHeaderTimeView.setText(time);	}	private void invokeOnScrolling() {		if (mScrollListener instanceof OnXScrollListener) {			OnXScrollListener l = (OnXScrollListener) mScrollListener;			l.onXScrolling(this);		}	}	public void updateHeaderHeight(float delta) {		mHeaderView.setVisiableHeight((int) delta				+ mHeaderView.getVisiableHeight());		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {				mHeaderView.setState(XListViewHeader.STATE_READY);			} else {				mHeaderView.setState(XListViewHeader.STATE_NORMAL);			}		}		setSelection(0); // scroll to top each time	}	/**	 * reset header view's height.	 */	private void resetHeaderHeight() {		int height = mHeaderView.getVisiableHeight();		if (height == 0) // not visible.			return;		// refreshing and header isn't shown fully. do nothing.		if (mPullRefreshing && height <= mHeaderViewHeight) {			return;		}		int finalHeight = 0; // default: scroll back to dismiss header.		// is refreshing, just scroll back to show all the header.		if (mPullRefreshing && height > mHeaderViewHeight) {			finalHeight = mHeaderViewHeight;		}		mScrollBack = SCROLLBACK_HEADER;		mScroller.startScroll(0, height, 0, finalHeight - height,				SCROLL_DURATION);		// trigger computeScroll		invalidate();	}	private void updateFooterHeight(float delta) {		int height = mFooterView.getBottomMargin() + (int) delta;		if (mEnablePullLoad && !mPullLoading) {			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load													// more.				mFooterView.setState(XListViewFooter.STATE_READY);			} else {				mFooterView.setState(XListViewFooter.STATE_NORMAL);			}		}		mFooterView.setBottomMargin(height);		// setSelection(mTotalItemCount - 1); // scroll to bottom	}	private void resetFooterHeight() {		int bottomMargin = mFooterView.getBottomMargin();		if (bottomMargin > 0) {			mScrollBack = SCROLLBACK_FOOTER;			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,					SCROLL_DURATION);			invalidate();		}	}	private void startLoadMore() {		mPullLoading = true;		mFooterView.setState(XListViewFooter.STATE_LOADING);		if (mListViewListener != null) {			mListViewListener.onLoadMore();		}	}	@Override	public boolean onTouchEvent(MotionEvent ev) {		if (mLastY == -1) {			mLastY = ev.getRawY();		}		switch (ev.getAction()) {		case MotionEvent.ACTION_DOWN:			mLastY = ev.getRawY();			break;		case MotionEvent.ACTION_MOVE:			final float deltaY = ev.getRawY() - mLastY;			mLastY = ev.getRawY();			System.out.println("数据监测：" + getFirstVisiblePosition() + "---->"					+ getLastVisiblePosition());			if (getFirstVisiblePosition() == 0					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {				// the first item is showing, header has shown or pull down.				updateHeaderHeight(deltaY / OFFSET_RADIO);				invokeOnScrolling();			} else if (getLastVisiblePosition() == mTotalItemCount - 1					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {				// last item, already pulled up or want to pull up.				updateFooterHeight(-deltaY / OFFSET_RADIO);			}			break;		default:			mLastY = -1; // reset			if (getFirstVisiblePosition() == 0) {				// invoke refresh				if (mEnablePullRefresh						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {					mPullRefreshing = true;					mHeaderView.setState(XListViewHeader.STATE_REFRESHING);					if (mListViewListener != null) {						mListViewListener.onRefresh();					}				}				resetHeaderHeight();			}			if (getLastVisiblePosition() == mTotalItemCount - 1) {				// invoke load more.				if (mEnablePullLoad						&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {					startLoadMore();				}				resetFooterHeight();			}			break;		}		return super.onTouchEvent(ev);	}	@Override	public void computeScroll() {		if (mScroller.computeScrollOffset()) {			if (mScrollBack == SCROLLBACK_HEADER) {				mHeaderView.setVisiableHeight(mScroller.getCurrY());			} else {				mFooterView.setBottomMargin(mScroller.getCurrY());			}			postInvalidate();			invokeOnScrolling();		}		super.computeScroll();	}	@Override	public void setOnScrollListener(OnScrollListener l) {		mScrollListener = l;	}	@Override	public void onScrollStateChanged(AbsListView view, int scrollState) {		if (mScrollListener != null) {			mScrollListener.onScrollStateChanged(view, scrollState);		}	}	@Override	public void onScroll(AbsListView view, int firstVisibleItem,			int visibleItemCount, int totalItemCount) {		// send to user's listener		mTotalItemCount = totalItemCount;		if (mScrollListener != null) {			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,					totalItemCount);		}	}	public void setXListViewListener(IXListViewListener l) {		mListViewListener = l;	}	/**	 * you can listen ListView.OnScrollListener or this one. it will invoke	 * onXScrolling when header/footer scroll back.	 */	public interface OnXScrollListener extends OnScrollListener {		public void onXScrolling(View view);	}	/**	 * implements this interface to get refresh/load more event.	 */	public interface IXListViewListener 	{		public void onRefresh();		public void onLoadMore();				public void addAnswer();		public void invite();		public void showInvite( boolean symbol );		public void showUserInfo();		public void showCommentList();	}}