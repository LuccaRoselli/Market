package com.devlucca.ymarket.license;

import java.util.ArrayList;

public class LicenseObject {

	String licen�a;
	Boolean keyprivada;
	String ips;

	public static ArrayList<LicenseObject> licenses = new ArrayList<>();
	
	public LicenseObject(String license, Boolean kp, String ips) {
		this.licen�a = license;
		this.keyprivada = kp;
		this.ips = ips;
		licenses.add(this);
	}
	
	public String getLicense(){
		return this.licen�a;
	}
	
	public Boolean isPrivate(){
		return this.keyprivada;
	}
	
	public String getIPs(){
		return this.ips;
	}
	
	public static ArrayList<LicenseObject> getLicenses(){
		return licenses;
	}

}
