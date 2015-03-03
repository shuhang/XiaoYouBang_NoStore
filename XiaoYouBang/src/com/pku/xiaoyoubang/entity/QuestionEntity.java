package com.pku.xiaoyoubang.entity;import java.io.Serializable;import java.util.ArrayList;import java.util.List;public class QuestionEntity implements Serializable{	/**	 * 	 */	private static final long serialVersionUID = 1L;		private String id;	private String userId;	private String userHeadUrl;	private String userName;	private String createTime;	private String modifyTime;	private String updateTime;	private String changeTime = "";	private String company;	private String job;	private int sex;	private boolean hasAnswered = false;	private boolean isNew;	private boolean isModified;	private boolean isUpdated;	private boolean isInvisible;	private boolean hasImage = false;	private String PKU;	private int answerCount;	private int praiseCount;	private int commentCount = 0;	private String questionTitle;	private String questionInfo;	private AnswerEntity myAnswer;	private List< InviteEntity > inviteMeList;	private List< InviteEntity > myInviteList;	private List< AnswerEntity > answerList;	private List< CommentEntity > commentList = new ArrayList< CommentEntity >();	private List< String > imageList = new ArrayList< String >();		public boolean isNew() {		return isNew;	}	public void setNew(boolean isNew) {		this.isNew = isNew;	}	public int getSex() {		return sex;	}	public void setSex(int sex) {		this.sex = sex;	}	public String getPKU() {		return PKU;	}	public void setPKU(String pKU) {		PKU = pKU;	}		public List<AnswerEntity> getAnswerList() {		return answerList;	}	public void setAnswerList(List<AnswerEntity> answerList) {		this.answerList = answerList;	}	public List<CommentEntity> getCommentList() {		return commentList;	}	public void setCommentList(List<CommentEntity> commentList) {		this.commentList = commentList;	}	public String getId() 	{		return id;	}	public void setId(String id) 	{		this.id = id;	}	public String getUserId()	{		return userId;	}	public void setUserId(String userId) 	{		this.userId = userId;	}	public String getUserHeadUrl() 	{		return userHeadUrl;	}	public void setUserHeadUrl(String userHeadUrl) 	{		this.userHeadUrl = userHeadUrl;	}	public String getUserName() 	{		return userName;	}	public void setUserName(String userName) 	{		this.userName = userName;	}	public int getAnswerCount() 	{		return answerCount;	}	public void setAnswerCount(int answerCount) 	{		this.answerCount = answerCount;	}	public int getPraiseCount() 	{		return praiseCount;	}	public void setPraiseCount(int praiseCount) 	{		this.praiseCount = praiseCount;	}	public String getQuestionTitle() 	{		return questionTitle;	}	public void setQuestionTitle(String questionTitle) 	{		this.questionTitle = questionTitle;	}	public String getQuestionInfo()	{		return questionInfo;	}	public void setQuestionInfo(String questionInfo) 	{		this.questionInfo = questionInfo;	}	public String getCreateTime() 	{		return createTime;	}	public void setCreateTime(String createTime) 	{		this.createTime = createTime;	}	public String getCompany() 	{		return company;	}	public void setCompany(String company) 	{		this.company = company;	}	public String getJob() 	{		return job;	}	public void setJob(String job) 	{		this.job = job;	}	/**	 * @return the hasAnswered	 */	public boolean isHasAnswered() {		return hasAnswered;	}	/**	 * @param hasAnswered the hasAnswered to set	 */	public void setHasAnswered(boolean hasAnswered) {		this.hasAnswered = hasAnswered;	}	public List< InviteEntity > getInviteMeList() {		return inviteMeList;	}	public void setInviteMeList(List< InviteEntity > inviteMeList) {		this.inviteMeList = inviteMeList;	}	public List< InviteEntity > getMyInviteList() {		return myInviteList;	}	public void setMyInviteList(List< InviteEntity > myInviteList) {		this.myInviteList = myInviteList;	}	public boolean isInvisible() {		return isInvisible;	}	public void setInvisible(boolean isInvisible) {		this.isInvisible = isInvisible;	}	public String getModifyTime() {		return modifyTime;	}	public void setModifyTime(String modifyTime) {		this.modifyTime = modifyTime;	}	public String getUpdateTime() {		return updateTime;	}	public void setUpdateTime(String updateTime) {		this.updateTime = updateTime;	}	public AnswerEntity getMyAnswer() {		return myAnswer;	}	public void setMyAnswer(AnswerEntity myAnswer) {		this.myAnswer = myAnswer;	}	public int getCommentCount() {		return commentCount;	}	public void setCommentCount(int commentCount) {		this.commentCount = commentCount;	}	public boolean isModified() {		return isModified;	}	public void setModified(boolean isModified) {		this.isModified = isModified;	}	public boolean isUpdated() {		return isUpdated;	}	public void setUpdated(boolean isUpdated) {		this.isUpdated = isUpdated;	}	public String getChangeTime() {		return changeTime;	}	public void setChangeTime(String changeTime) {		this.changeTime = changeTime;	}	public List< String > getImageList() {		return imageList;	}	public void setImageList(List< String > imageList) {		this.imageList = imageList;	}	public boolean isHasImage() {		return hasImage;	}	public void setHasImage(boolean hasImage) {		this.hasImage = hasImage;	}}