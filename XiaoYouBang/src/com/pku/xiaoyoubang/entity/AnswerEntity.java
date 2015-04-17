package com.pku.xiaoyoubang.entity;import java.io.Serializable;import java.util.ArrayList;import java.util.List;public class AnswerEntity implements Serializable{	/**	 * 	 */	private static final long serialVersionUID = 1L;		private String id;	private String questionId;	private String userId;	private String name;	private String userHeadUrl;	private String answerInfo;	private String company;	private String job;	private String part = "";	private String pku;	private int sex;	private int praiseCount = 0;	private boolean isInvisible;	private boolean hasPraised = true;	private boolean hasImage = false;	private boolean isHasSaved = false;	private String createTime = "";	private String modifyTime = "";	private String editTime = "";	private String questionTitle = "";	private String questionerName = "";	private List< String > invite;	private List< String > praise;	private List< String > imageList = new ArrayList< String >();		/**	 *  0 : answer	 *  1 : act	 */	private int type = 0;		public String getPart() {		return part;	}	public void setPart(String part) {		this.part = part;	}	public String getPku() {		return pku;	}	public void setPku(String pku) {		this.pku = pku;	}	public int getSex() {		return sex;	}	public void setSex(int sex) {		this.sex = sex;	}	public String getCompany() {		return company;	}	public void setCompany(String company) {		this.company = company;	}	public String getJob() {		return job;	}	public void setJob(String job) {		this.job = job;	}	public List<String> getInvite() {		return invite;	}	public void setInvite(List<String> invite) {		this.invite = invite;	}	public List<String> getPraise() {		return praise;	}	public void setPraise(List<String> praise) {		this.praise = praise;	}	private List< CommentEntity > commentList;	private int commentCount;		public String getId() {		return id;	}	public void setId(String id) {		this.id = id;	}	public String getQuestionId() {		return questionId;	}	public void setQuestionId(String questionId) {		this.questionId = questionId;	}	public String getUserId() {		return userId;	}	public void setUserId(String userId) {		this.userId = userId;	}	public String getName() {		return name;	}	public void setName(String name) {		this.name = name;	}	public String getUserHeadUrl() {		return userHeadUrl;	}	public void setUserHeadUrl(String userHeadUrl) {		this.userHeadUrl = userHeadUrl;	}	public String getAnswerInfo() {		return answerInfo;	}	public void setAnswerInfo(String answerInfo) {		this.answerInfo = answerInfo;	}	public List< CommentEntity > getCommentList() {		return commentList;	}	public void setCommentList(List< CommentEntity > commentList) {		this.commentList = commentList;	}	public String getQuestionTitle() {		return questionTitle;	}	public void setQuestionTitle(String questionTitle) {		this.questionTitle = questionTitle;	}	public String getModifyTime() {		return modifyTime;	}	public void setModifyTime(String modifyTime) {		this.modifyTime = modifyTime;	}	public String getCreateTime() {		return createTime;	}	public void setCreateTime(String createTime) {		this.createTime = createTime;	}	public boolean isInvisible() {		return isInvisible;	}	public void setInvisible(boolean isInvisible) {		this.isInvisible = isInvisible;	}	public int getCommentCount() {		return commentCount;	}	public void setCommentCount(int commentCount) {		this.commentCount = commentCount;	}	public boolean isHasPraised() {		return hasPraised;	}	public void setHasPraised(boolean hasPraised) {		this.hasPraised = hasPraised;	}	public int getPraiseCount() {		return praiseCount;	}	public void setPraiseCount(int praiseCount) {		this.praiseCount = praiseCount;	}	public String getQuestionerName() {		return questionerName;	}	public void setQuestionerName(String questionerName) {		this.questionerName = questionerName;	}	public String getEditTime() {		return editTime;	}	public void setEditTime(String editTime) {		this.editTime = editTime;	}	public List< String > getImageList() {		return imageList;	}	public void setImageList(List< String > imageList) {		this.imageList = imageList;	}	public boolean isHasImage() {		return hasImage;	}	public void setHasImage(boolean hasImage) {		this.hasImage = hasImage;	}	/**	 * @return the type	 */	public int getType() {		return type;	}	/**	 * @param type the type to set	 */	public void setType(int type) {		this.type = type;	}	public boolean isHasSaved() {		return isHasSaved;	}	public void setHasSaved(boolean isHasSaved) {		this.isHasSaved = isHasSaved;	}}