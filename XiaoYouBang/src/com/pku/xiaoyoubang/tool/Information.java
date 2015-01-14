package com.pku.xiaoyoubang.tool;

import android.annotation.SuppressLint;

public class Information 
{
	public static String Server_Url = "http://118.186.214.42:5000";
	
	@SuppressLint("SdCardPath")
	public static String Image_Path = "/data/data/com.pku.xiaoyoubang/head_image/";
	@SuppressLint("SdCardPath")
	public static String File_Path = "/data/data/com.pku.xiaoyoubang/question/";
	/**
	 *  User Information
	 */
	public static String Key1;
	public static String Key2;
	
	public static boolean IsMeChanged = false;
	public static boolean IsFirst = true;
	
	public static String Id = "";
	public static String Token = "";
	public static String Name = "";
	public static String Password;
	public static String Phone;
	public static String HeadUrl = "";
	public static int Sex = 1;
	public static String Birthday = "";
	public static int PKU_Index = 0;
	public static String PKU_Value = "北大所在院系";
	public static String Now_Home = "";
	public static String Old_Home = "";
	public static String QQ = "";
	public static String Company = "";
	public static String Part = "";
	public static String Job = "";
	public static String Tag[] = { "", "", "", "", "" };
	public static int Praise_Count = 0;
	public static int Question_Count = 0;
	public static int Answer_Count = 0;
	public static int Version = 0;
	
	public static String[] PKU1 = 
		{ 
			"北京国际数学研究中心",
			"城市规划与设计学院",
			"城市与环境学院",
			"地球与空间科学学院",
			"对外汉语教育学院",
			"法学院",
			"分子医学研究所",
			"歌剧研究院",
			"工学院",
			"光华管理学院",
			"国际法学院",
			"国际关系学院",
			"国家发展研究院/CCER",
			"核科学与技术研究院",
			"化学生物学与生物技术学院",
			"化学与分子工程学院",
			"环境科学与工程学院",
			"环境与能源学院",
			"汇丰商学院", 
			"计算机科学技术研究所",
			"建筑与景观设计学院",
			"教育学院",
			"经济学院",
			"考古文博学院",
			"科维理天文研究所",
			"历史学系",
			"马克思主义学院",
			"前沿交叉学科研究院",
			"人口研究所",
			"人文社会科学学院",
			"软件与微电子学院",
			"社会学系",
			"生命科学学院",
			"数学科学学院",
			"体育教研部",
			"外国语学院",
			"物理学院",
			"先进技术研究院",
			"心理学系",
			"新材料学院",
			"新闻与传播学院",
			"信息工程学院",
			"信息管理系",
			"信息科学技术学院",
			"医学部",
			"艺术学院",
			"元培学院",
			"哲学系",
			"政府管理学院",
			"中国社会科学调查中心",
			"中国语言文学系"
		};
	public static String[] PKU2 = 
		{ 
			"国际数学",
			"城市规划",
			"城市环境",
			"地球空间",
			"对外汉语",
			"法学",
			"分子医学",
			"歌剧研究",
			"工学",
			"光华",
			"国际法学",
			"国际关系",
			"国发院",
			"核科学技术",
			"化学生物",
			"化学",
			"环境工程",
			"环境能源",
			"汇丰商学",
			"计科所",
			"建筑景观",
			"教育",
			"经济",
			"考古",
			"科维理天文",
			"历史",
			"马克思主义",
			"前沿交叉",
			"人口所",
			"人文社科",
			"软件微电子",
			"社会",
			"生命科学",
			"数学",
			"体育教研",
			"外语",
			"物理",
			"先进技术",
			"心理",
			"新材料",
			"新闻传播",
			"信息工程",
			"信息管理",
			"信息科学",
			"医学部",
			"艺术",
			"元培",
			"哲学",
			"政府管理",
			"社科调查",
			"中文"
		};
}
