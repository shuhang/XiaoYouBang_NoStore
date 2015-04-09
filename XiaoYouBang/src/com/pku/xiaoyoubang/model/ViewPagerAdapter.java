package com.pku.xiaoyoubang.model;import android.support.v4.app.Fragment;import android.support.v4.app.FragmentManager;import android.support.v4.app.FragmentPagerAdapter;import com.pku.xiaoyoubang.selfview.AllActFragment;import com.pku.xiaoyoubang.selfview.AllQuestionFragment;public class ViewPagerAdapter extends FragmentPagerAdapter{	public static final String[] TITLES = new String[] { "问题", "活动" };	private AllQuestionFragment questionFragment = null;	private AllActFragment actFragment = null;	public ViewPagerAdapter(FragmentManager fm)	{		super( fm );	}		public Fragment getItem( int index )	{		if( index == 0 )		{			if( questionFragment == null )			{				questionFragment = new AllQuestionFragment();			}			return questionFragment;		}		else if( index == 1 )		{			if( actFragment == null )			{				actFragment = new AllActFragment();			}			return actFragment;		}		return null;	}	public CharSequence getPageTitle( int position )	{		return TITLES[ position % TITLES.length ];	}	public int getCount()	{		return TITLES.length;	}}