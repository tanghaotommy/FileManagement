package main;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class CMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CFrame c = new CFrame(300,400);
		c.setVisible(true);
	}

}

class CFrame extends JFrame{
	private Parameter parameter = new Parameter();
	private ServerSocket ss = null;
	private HashMap<String, Socket> map = new HashMap<String,Socket>();
	private HashMap<String, HashMap<String,HashMap<String, Socket>>> userDownloadingFile = new HashMap<String, HashMap<String,HashMap<String, Socket>>>();
	//private HashMap<String, Socket> threadMap = new HashMap<String,Socket>();
	private int count = 0;
	private String downloadFilePath;
	CSendFileData c;
	
	CFrame(int arg0,int arg1){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(arg0, arg1);
		setLocation(200, 300);
		
		try {
			ss = new ServerSocket(parameter.port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CThreadAccept c = new CThreadAccept(ss);
		c.start();
	}
	class CThreadAccept extends Thread{
		private boolean runFlag = true;
		private ServerSocket ss;
		
		CThreadAccept(ServerSocket arg0){
			ss = arg0;
		}
		public void run(){
			while(runFlag){
				try {
					Socket s = ss.accept();
					System.out.println("Socket connected!" + s.getInetAddress());
					count++;
					//map.put(new String(count + ""), s);
					CThreadInput c = new CThreadInput(s);
					c.start();
					//CThreadInput c = new CThreadInput(s.getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	class CThreadOutput extends Thread{
		private Socket out;
		private String content;
		CThreadOutput(Socket arg0,String arg1){
			out = arg0;
			content = arg1;
		}
		public void run(){
			CMessage m = new CMessage(content);
			while(m.sendNext(out) < 0);
		}
	}

	class CThreadInput extends Thread{
		private Socket in;
		private boolean runFlag;
		private Parameter parameter = new Parameter();
		
		CThreadInput(Socket arg0){
			in = arg0;
			runFlag = true;
		}
		public void run(){
			while(runFlag){
				int loadsize = parameter.loadsize;
				String content = "";
				String current = null;
				try {
					do{						
						byte[] b =new byte[loadsize];			
						in.getInputStream().read(b);
					    current = new String(b);
						content += current.substring(1);
					}while(current.substring(0,1).equals("1"));				
					content.trim();
					System.out.println("Accept: " + content);
					String[] s = content.split(parameter.endOfInput);
					//System.out.println(s[0]);
					ContentClassify(s[0],in,this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					runFlag = false;
					e.printStackTrace();
				}
			}
		}
		public void setrunFlag(boolean flag){
			System.out.println("Set RunFlag :" + flag);
			runFlag = flag;
		}
	}
	boolean ReLogin(Socket s){
		Set<Entry<String, Socket>> sets = map.entrySet();  
        for(Map.Entry<String, Socket> entry : sets) {  
        	if (entry.getValue().getInetAddress().toString().equals(s.getInetAddress().toString())){
        		return true;
        	}
        }
        return false;
	}
	void NewClient(String content,Socket s){
		System.out.println(content + "123");
		if(!ReLogin(s)){
			HashMap<String,HashMap<String, Socket>> downloadingFile = new HashMap<String,HashMap<String, Socket>>();
			userDownloadingFile.put(content, downloadingFile);
			map.put(content, s);
			//UpdateUserList();
			UpdateFileList(s,content);
		}else
		{
			CThreadOutput c = new CThreadOutput(s,"ReLogin" + parameter.headerAndBodySplit + parameter.endOfInput);
			c.start();
		}
		//System.out.println(content);
	}
	void UpdateFileList(Socket s,String userName){
		CMysql database = new CMysql(parameter.mysqlUrl,parameter.mysqlUser,parameter.mysqlPassword,parameter.mysqlDriver);
		String path = database.FindUserFileDirectory(userName);
		String content = "";
		System.out.println(path);
		if(!path.equals("")){
			File f = new File(path);
			System.out.println(path);
			for(File file : f.listFiles()){
				content += file.getName() + parameter.fileAndSizeSplit + file.length() + parameter.contentSplit;
			}
			if(!content.equals("")) content = content.substring(0, content.length()-1);
			content = "UpdateFileList" + parameter.headerAndBodySplit + content + parameter.endOfInput;
			CThreadOutput c;
			c = new CThreadOutput(s,content);
			c.start();
		}
	}
	void UpdateUserList(){
		Set<Entry<String, Socket>> sets = map.entrySet();  
		String sendContentToAll = "";
        for(Map.Entry<String, Socket> entry : sets) {  
            //System.out.print(entry.getKey() + ", ");  
            // System.out.println(entry.getValue());  
        	//System.out.println(entry.getKey() + ":");
			sendContentToAll = sendContentToAll + entry.getKey() + parameter.contentSplit;
		}
		if(!sendContentToAll.equals(""))sendContentToAll = sendContentToAll.substring(0, sendContentToAll.length()-1);
		sendContentToAll = "UpdateList" + parameter.headerAndBodySplit + sendContentToAll + parameter.endOfInput;
		System.out.println(sendContentToAll);
		for(Map.Entry<String, Socket> entry : sets) {  
			Socket ss = entry.getValue();
			CThreadOutput c = new CThreadOutput(ss,sendContentToAll);
			c.start();
		}
	}
	void SendMessage(String content,Socket from){
		String[] s = content.split(parameter.contentSplit);
		String targetUser = s[0];
		String fromUser = "";
		
		Set<Entry<String, Socket>> sets = map.entrySet();  
		for(Map.Entry<String, Socket> entry : sets) {  
			if(from == entry.getValue()){
				fromUser = entry.getKey();
			}
		}
		
		String sendContent = "AcceptMessage" + parameter.headerAndBodySplit + fromUser + " said: " + s[1] + parameter.endOfInput;
		Socket ss = map.get(targetUser);
		System.out.println(sendContent);
		CThreadOutput c = new CThreadOutput(ss,sendContent);
		c.start();
	}
	void LogoutUser(Socket s){
		Set<Entry<String, Socket>> sets = map.entrySet();  
        for(Map.Entry<String, Socket> entry : sets) {  
        	if(entry.getValue() == s){
        		map.remove(entry.getKey());
        		userDownloadingFile.remove(entry.getKey());
        		System.out.println(entry.getKey()+ "log out!");
        		UpdateUserList();
        		break;
        	}
		}
		System.out.println(map.size());
	}
	
	HashMap<String, Socket> FindThreadMapByUserNameAndFileName(String userName,String fileName){
		HashMap<String, HashMap<String,Socket>> downloadingFile  = userDownloadingFile.get(userName);
		HashMap<String, Socket> threadMap = downloadingFile.get(fileName);
		return threadMap;
	}
	
	void NewDownloadSocket(String content,Socket s){
		String[] st = content.split(";");
		String threadName = st[0];
		long position = Long.parseLong(st[1]);
		long size = Long.parseLong(st[2]);
		String fileName = st[3];
		String userName = FindUserNameByIP(s.getInetAddress().toString());
		
		HashMap<String, Socket> threadMap = FindThreadMapByUserNameAndFileName(userName,fileName);
		
		System.out.println("Thread " + st[0] + "is added!" + position + ";" + size);
		threadMap.put(threadName, s);
		//CThreadInput c2 = new CThreadInput(s);
		//c2.start();
		System.out.println("threadMap size:" + threadMap.size());
		
		if(c == null){
			c = new CSendFileData();
		}			
		c.setPosition(Integer.parseInt(threadName), position);
		c.setSize(Integer.parseInt(threadName), size);
		if(threadMap.size() == parameter.threadNumber){
			System.out.println("Begin download!" + downloadFilePath);
			c.setFilePath(downloadFilePath);
			c.setFileName(fileName);
			c.setUserName(userName);
			c.start();
			downloadFilePath = "";
		}
	}
	
	String FindUserNameByIP(String ip){
		Set<Entry<String, Socket>> sets = map.entrySet();  
		System.out.println(map.size());
        for(Map.Entry<String, Socket> entry : sets) {  
        	if(entry.getValue().getInetAddress().toString().equals(ip)){
        		return entry.getKey();
        	}
		}
        return "UserNotFound!";
	}
	String FindUserNameBySocket(Socket s){
		Set<Entry<String, Socket>> sets = map.entrySet();  
		System.out.println(map.size());
        for(Map.Entry<String, Socket> entry : sets) {  
        	if(entry.getValue() == s){
        		return entry.getKey();
        	}
		}
        return "UserNotFound!";
	}
	
	void ReadyForDownload(String content,Socket s){
		String userName = FindUserNameBySocket(s);
		HashMap<String, HashMap<String, Socket>> downloadingFile = userDownloadingFile.get(userName);
		HashMap<String, Socket> downloadingThread = new HashMap<String, Socket>();
		downloadingFile.put(content, downloadingThread);
		System.out.println("Add file task to: " + userName);
		downloadingThread = FindThreadMapByUserNameAndFileName(userName,content);
		System.out.println(downloadingThread.size());
		
		CMysql database = new CMysql(parameter.mysqlUrl,parameter.mysqlUser,parameter.mysqlPassword,parameter.mysqlDriver);
		if(userName.equals("UserNotFound!")){
			System.out.println("User Not Found!");
		}else{
			String path = database.FindUserFileDirectory(userName);
			downloadFilePath = path + "\\" + content;
		}
	}
	
	void CloseThread(String fileName, Socket s){
		c = null;
		String userName = FindUserNameByIP(s.getInetAddress().toString());
		HashMap<String, Socket> threadMap = FindThreadMapByUserNameAndFileName(userName, fileName);
		
		Set<Entry<String, Socket>> sets = threadMap.entrySet();  
		System.out.println("Close threadMap size: " + threadMap.size());
        for(Map.Entry<String, Socket> entry : sets) {  
        	if(entry.getValue() == s){
        		threadMap.remove(entry.getKey());
        		System.out.println("thread " + entry.getKey() + "closed!");
        		break;
        	}	
        }
        if(!s.isClosed())
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        if(threadMap.size() == 0){
        	HashMap<String,HashMap<String, Socket>> downloadingFile = userDownloadingFile.get(userName);
        	downloadingFile.remove(fileName);
        	downloadFilePath = "";
        }
	}
	
	void ContentClassify(String content,Socket s,CThreadInput t){
		String[] st = content.split(parameter.headerAndBodySplit);
		String header = st[0];
		if(header.equals("Login")) NewClient(st[1],s);
		else if(header.equals("SendMessage")) SendMessage(st[1],s);
		else if(header.equals("Logout")) LogoutUser(s);
		else if(header.equals("ReadyAcceptFile")) NewDownloadSocket(st[1],s);
		else if(header.equals("Download")) ReadyForDownload(st[1],s);
		else if(header.equals("CloseThread")) {
			t.setrunFlag(false);
			CloseThread(content,s);
		}
	}
	
	
	class CSendFileData{
	private String filePath;
	private String fileName;
	private String userName;
	private Parameter parameter = new Parameter();
	private int threadNumber;
	private long positionOfThread[];
	private long sizeOfThread[];
	
	CSendFileData(){
		threadNumber = parameter.threadNumber;
		positionOfThread = new long[parameter.threadNumber];
		sizeOfThread = new long[parameter.threadNumber];
	}
	public void setPosition(int index,long p){
		positionOfThread[index] = p;
	}
	public void setSize(int index,long s){
		sizeOfThread[index] = s;
	}
	public void setFilePath(String arg0){
		filePath = arg0;
	}
	public void setFileName(String arg0){
		fileName = arg0;
	}
	public void setUserName(String arg0){
		userName = arg0;
	}
	public void start(){
		HashMap<String, Socket> threadMap = FindThreadMapByUserNameAndFileName(userName,fileName);
		for(int i = 0;i<threadNumber;i++){
			Socket ss = threadMap.get(String.valueOf(i));
			CopyFileThread c;			
			c = new CopyFileThread(positionOfThread[i],sizeOfThread[i],ss);
			c.start();
		}
	}
	class CopyFileThread extends Thread{
		private long position;
		private long size;
		private Socket out;

		CopyFileThread(long p, long s,Socket arg0){
			super();
			position = p;
			out = arg0;
			size = s;
		}
		public void run(){
			File from = new File(filePath);
			try {
				System.out.println(position + ";" + size);
				RandomAccessFile read = new RandomAccessFile(from,"rw");		
				long currentPosition = position;
				long loadsize = 1024;
				long length = size;
				while(length>0){
					//Set<Entry<String, Socket>> sets = threadMap.entrySet();  
					//System.out.println("Close threadMap size: " + threadMap.size());
					if(loadsize > length)
						loadsize = length;
					byte [] b = new byte[(int) loadsize];				
					read.seek(currentPosition);
					read.read(b);
					//String sendContent = new String(b);
					//sendContent = sendContent + parameter.endOfInput;
					out.getOutputStream().write(b);			     
			        System.out.println(loadsize + ": " + new String(b));
					//CMessage c = new CMessage(sendContent);
					//while(c.sendNext(out) < 0);
					length = length - loadsize;
					currentPosition = currentPosition + loadsize;
					//System.out.println(currentPosition + ";" + loadsize);
				}
				out.getOutputStream().flush();
				//CloseThread(out);
				//out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
}



class CMessage{
	private String content;
	private Parameter parameter = new Parameter();
	private int loadsize;
	CMessage(String arg0){
		content = arg0;
		loadsize = parameter.loadsize;
	}
	public int sendNext(Socket out){
		if(content.length() > loadsize - 1){
			String current = content.substring(0, loadsize-1);
			current = "1" + current;
			content = content.substring(loadsize - 1);
			try {
				out.getOutputStream().write(current.getBytes());
				//System.out.println(current);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}else{
			content = "0" + content;
			try {
				out.getOutputStream().write(content.getBytes());
				//System.out.println(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
	}
}

class CMysql{
	private String user;
	private String password;
	private String url;
	private String driver;
	
	CMysql(String arg0,String arg1,String arg2,String arg3){
		user = arg1;
		password = arg2;
		url = arg0;
		driver = arg3;
	}
	String FindUserFileDirectory(String userName){
		String directoryPath = "";
		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(url, user, password);
			if(!conn.isClosed()) System.out.println("Succeeded connecting to the Database!");
			Statement statement = conn.createStatement();
			String sql = String.format("select * from file where username = '%s'",userName);
			//System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);   
			if(rs.next()) 
				directoryPath = rs.getString("directorypath");
			rs.close();  
			conn.close();   
		} catch(ClassNotFoundException e) {   
			System.out.println("Sorry,can`t find the Driver!");   
			e.printStackTrace();   
		} catch(SQLException e) {   
			e.printStackTrace();   
		} catch(Exception e) {   
			e.printStackTrace();   
		}
		return directoryPath;
	}
}

class Parameter{
	public String mysqlUrl = "jdbc:mysql://127.0.0.1:3306/filemanagement";
	public String mysqlDriver = "com.mysql.jdbc.Driver";
	public String mysqlUser = "root";
	public String mysqlPassword = "root";
	public String endOfInput = "%";
	public String contentSplit = ";";
	public String headerAndBodySplit = "#";
	public int loadsize = 1024;
	public String ip = "127.0.0.1";
	public int port = 88;
	public String fileAndSizeSplit = ":";
	public int threadNumber = 3;
	private Document document;
	private String defaultSettingFilePath = "D:\\FileManagementSetting.xml";
	
	public Parameter(){	
		beginXML(defaultSettingFilePath);
	}
	
	public void beginXML(String fileName){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		}
		File file=new File(fileName);
		if(!file.exists())
			createXml(fileName);
		parserXml(fileName);
	}

	public void createXml(String fileName) {
		Element root = this.document.createElement("Settings"); 
		Element ip = this.document.createElement("IP"); 
		ip.appendChild(this.document.createTextNode("localhost")); 
		root.appendChild(ip); 
		Element port = this.document.createElement("Port"); 
		port.appendChild(this.document.createTextNode("1025")); 
		root.appendChild(port); 
		this.document.appendChild(root); 
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			Transformer transformer = tf.newTransformer();
			DOMSource source = new DOMSource(document);
			transformer.setOutputProperty(OutputKeys.ENCODING, "gb2312");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
			StreamResult result = new StreamResult(pw);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			System.out.println(e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (TransformerException e) {
			System.out.println(e.getMessage());
		}
	}

	public void parserXml(String fileName) {
		System.out.println("Read defaultSetting File!");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(fileName);     
			NodeList settings = document.getChildNodes();
			Node items = settings.item(0);
			NodeList itemsList = items.getChildNodes();
			for (int i = 0; i < itemsList.getLength(); i++) {
				if(itemsList.item(i).getNodeName().equals("IP"))
					ip=itemsList.item(i).getTextContent();
				else if(itemsList.item(i).getNodeName().equals("Port"))
					port=Integer.parseInt(itemsList.item(i).getTextContent());
				System.out.println(ip + ":" + port);
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}