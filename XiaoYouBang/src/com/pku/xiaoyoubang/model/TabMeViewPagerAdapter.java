package com.pku.xiaoyoubang.model;import android.support.v4.app.Fragment;import android.support.v4.app.FragmentManager;import android.support.v4.app.FragmentPagerAdapter;import com.pku.xiaoyoubang.selfview.MyAnswerFragment;import com.pku.xiaoyoubang.selfview.MyMessageFragment;import com.pku.xiaoyoubang.selfview.MyQuestionFragment;import com.pku.xiaoyoubang.selfview.MySaveAnswerFragment;import com.viewpagerindicator.IconPagerAdapter;public class TabMeViewPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter{	public static final String[] TITLES = new String[] { "问过", "答过", "消息", "收藏" };	private int[] icons = { 0, 0, 0, 0 };		private MyQuestionFragment fragment1 = null;	private MyAnswerFragment fragment2 = null;	private MyMessageFragment fragment3 = null;	private MySaveAnswerFragment fragment4 = null;	public TabMeViewPagerAdapter(FragmentManager fm)	{		super( fm );	}		public Fragment getItem( int index )	{		if( index == 0 )		{			if( fragment1 == null )			{				fragment1 = new MyQuestionFragment();			}			return fragment1;		}		else if( index == 1 )		{			if( fragment2 == null )			{				fragment2 = new MyAnswerFragment();			}			return fragment2;		}		else if( index == 2 )		{			if( fragment3 == null )			{				fragment3 = new MyMessageFragment();			}			return fragment3;		}		else if( index == 3 )		{			if( fragment4 == null )			{				fragment4 = new MySaveAnswerFragment();			}			return fragment4;		}		return null;	}	public CharSequence getPageTitle( int position )	{		return TITLES[ position % TITLES.length ];	}	public int getCount()	{		return TITLES.length;	}	public int getIconResId( int index ) 	{		return icons[ index ];	}		public void setIconResId( int index, int value )	{		icons[ index ] = value;	}}