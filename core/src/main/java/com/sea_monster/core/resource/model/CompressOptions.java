package com.sea_monster.core.resource.model;

public class CompressOptions {

	private int thumbWidth;
	private int thumbHeight;
	private int srcWidth;
	private int srcHeight;
	private int orientation;
	private boolean isCrop;

	public CompressOptions(int thumbWidth, int thumbHeight, int srcWidth, int srcHeight, int orientation) {
		this(thumbWidth, thumbHeight, srcWidth, srcHeight, orientation, true);
	}

	public CompressOptions(int thumbWidth, int thumbHeight, int srcWidth, int srcHeight, int orientation, Boolean isCrop) {
		this.thumbHeight = thumbHeight;
		this.thumbWidth = thumbWidth;
		this.srcWidth = srcWidth;
		this.srcHeight = srcHeight;
		this.orientation = orientation;
		this.isCrop = isCrop;
	}

	public boolean isCrop() {
		return isCrop;
	}

	public void setCrop(boolean isCrop) {
		this.isCrop = isCrop;
	}

	public int getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(int thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public int getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(int thumbHeight) {
		this.thumbHeight = thumbHeight;
	}

	public int getSrcWidth() {
		return srcWidth;
	}

	public void setSrcWidth(int srcWidth) {
		this.srcWidth = srcWidth;
	}

	public int getSrcHeight() {
		return srcHeight;
	}

	public void setSrcHeight(int srcHeight) {
		this.srcHeight = srcHeight;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public void compareOffset() {
		String path;

	}

}
