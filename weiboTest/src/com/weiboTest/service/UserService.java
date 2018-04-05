package com.weiboTest.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import com.weiboTest.bean.WeiboBean;

public class UserService {
	private final String USERS;
	private LinkedList<WeiboBean> newestWeibos;
	public UserService(String USERS) {
		this.USERS = USERS;
		newestWeibos = new LinkedList<>();
	}
	
	public boolean isValidUsername(String username) {
		if(username == null || username.length() > 16 || username.length() == 0)
			return false;
		
		if(isExistUsername(username))
			return false;
		else
			return true;
	}
	
	public boolean isExistUsername(String username) {
		String[] list = new File(USERS).list();
		for(String tmpUserName : list) {
			if(tmpUserName.equals(username))
				return true;
		}
		return false;
	}
	
	public void createUserData(String email,String username,String password) throws IOException {
		File userhome = new File(USERS + username);
		userhome.mkdir();
		BufferedWriter writer = new BufferedWriter(new FileWriter(userhome.toString() + "/profile"));
		writer.write(email + '\n' + password);
		writer.close();
	}
	
	public boolean checkIn(String username,String password) throws IOException {
		//这样子读文件也不是线程安全的，因为可能有人同时在进行注册
		for(String _username : new File(USERS).list()) {
			if(_username.equals(username)) {
				BufferedReader reader = new BufferedReader(new FileReader(USERS + _username + "/profile"));
				reader.readLine();
				String _password = reader.readLine();
				if(_password.equals(password)) {
					reader.close();
					return true;
				}
				reader.close();
			}
		}
		return false;
	}
	
	private class TxtFileNameFilter implements FilenameFilter{
		
		@Override
		public boolean accept(File dir,String name){
			return name.endsWith(".txt");
		}
	}
	
	private class reverseDateComparator implements Comparator<Date>{
		@Override
		public int compare(Date d1,Date d2){
			return -d1.compareTo(d2);
		}
	}
	
	public List<WeiboBean> getWeibos(String username) throws IOException{
		File dir = new File(USERS + username);
		TreeMap<Date,String>messageMap = new TreeMap<>(new reverseDateComparator());
		List<WeiboBean>list = new ArrayList<>();
		for(String filename: dir.list(new TxtFileNameFilter())){
			Date date = new Date(Long.parseLong(filename.substring(0,filename.indexOf(".txt"))));
			BufferedReader reader = new BufferedReader(new FileReader(USERS + username + "/" + filename));
			StringBuilder message = new StringBuilder();
			String tmp;
			while((tmp = reader.readLine()) != null){
				message.append(tmp);
				message.append('\n');
			}
			messageMap.put(date, message.toString());
			reader.close();
		}
		for(Date date : messageMap.keySet()) {
			list.add(new WeiboBean(username,date,messageMap.get(date)));
		}
		return list;
	}
	
	public void addWeibo(WeiboBean weibo) throws IOException {
		String username = weibo.getUsername();
		String date = String.valueOf(weibo.getDate().getTime());
		String message = weibo.getMessage();
		newestWeibos.add(weibo);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(USERS + username + "/" + date + ".txt"));
		writer.write(message);
		writer.close();
	}
	
	public void deleteWeibo(WeiboBean weibo) {
		String username = weibo.getUsername();
		newestWeibos.remove(weibo);
		File file = new File(USERS + username + "/" + weibo.getDate().getTime() + ".txt");
		file.delete();
	}

	public LinkedList<WeiboBean> getNewestWeibos() {
		return newestWeibos;
	}

}
