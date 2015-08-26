package com.wedcel.dragexpandgrid.model;


/**
 *
 * 类: DargChildInfo <p>
 * 描述: 子item显示 <p>
 * 作者: wedcel wedcel@gmail.com<p>
 * 时间: 2015年8月25日 下午5:24:04 <p>
 */
public class DargChildInfo {

	private int id;
	private String name;


	public DargChildInfo() {
		// TODO Auto-generated constructor stub
	}


	public DargChildInfo(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}



}
