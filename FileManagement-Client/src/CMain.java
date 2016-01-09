import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
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
		CFrame c = new CFrame(800,700);
		c.setVisible(true);
	}

}

class CFrame extends JFrame{
	private Parameter parameter = new Parameter();
	private Socket s;
	JButton confirm;
	JButton connect;
	JButton logIn;
	JButton logOut;
	JButton downLoad;
	JButton browse;
	JButton pauseAndContinue;
	JLabel userStatus;
	JLabel connectionStatus;
	JLabel chatError;
	JTable fileTable;
	JLabel progressLabel;
	JTextField userName;
	mTableModel tableModel;
	String storePath;
	JLabel targetFile;
	JLabel storeSite;
	JProgressBar progressBar;
	CDownloadFile downloadFile;
	private HashMap<String, CDownloadFile> fileMap = new HashMap<String,CDownloadFile>();
	int sizeOfFile;
	
	CFrame(int arg0,int arg1){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(arg0, arg1);
		setLocation(200, 300);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String content = "Logout" + parameter.headerAndBodySplit + parameter.endOfInput;
				CThreadOutput c;
				c = new CThreadOutput(s,content);
				c.start();
				userName.setText("");
				userName.setEnabled(true);
				logIn.setEnabled(true);
				logOut.setEnabled(false);
				Object[] columnTitle = {"File","Size"};
				Object[][] data = {};
				//fileTable.clearSelection();
				fileTable.clearSelection();
				
				tableModel.setDataVector(data, columnTitle);
				fileTable.setModel(tableModel);
				
				System.exit(0);
			}
		});
		
		
		JPanel panelResult = new JPanel();
		//JLabel label = new JLabel("Result:");		
		JLabel jl = new JLabel("Your username: ");
		userName = new JTextField();
		userName.setColumns(10);
		connectionStatus = new JLabel();
		connectionStatus.setText("Connecting...");
		connect = new JButton("Connect");
		connect.addActionListener(new CconnectListener());
		logIn = new JButton("Log in");
		logOut = new JButton("Log out");
		logOut.setEnabled(false);
		logIn.addActionListener(new ClogInListener());
		logOut.addActionListener(new ClogOutListener());
		userStatus = new JLabel();
		//txtResult.setEditable(false);
		getContentPane().add(panelResult, BorderLayout.NORTH);		
		//panelResult.add(label);
		panelResult.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelResult.add(connectionStatus);
		panelResult.add(connect);		
		panelResult.add(jl);
		panelResult.add(userName);
		panelResult.add(logIn);
		panelResult.add(logOut);
		panelResult.add(userStatus);
		
		JPanel panelTemp1 = new JPanel();
		panelTemp1.setLayout(new BorderLayout());
		//getContentPane().add(panelTemp1);
		panelResult.add(panelTemp1);
		String[] columnTitle = {"File","Size"};
		Object[][] data = {};
		fileTable = new JTable(data,columnTitle);
		fileTable.setPreferredScrollableViewportSize(new Dimension(600,400));
		tableModel = new mTableModel();
		fileTable.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				int rows = fileTable.getSelectedRow();
				targetFile.setText(fileTable.getValueAt(rows, 0).toString());
				sizeOfFile = Integer.parseInt(fileTable.getValueAt(rows, 1).toString());
				CDownloadFile c = fileMap.get(targetFile.getText());
				if(c == null){
					progressBar.setValue(0);
					progressLabel.setText("0");
					downLoad.setEnabled(true);
					pauseAndContinue.setEnabled(false);
				}
				else{
					if(c.getStatus() == 1){
						progressBar.setValue(c.getProgress());
						downLoad.setEnabled(false);
						pauseAndContinue.setText("暂停");
						pauseAndContinue.setEnabled(true);		
					}else if(c.getStatus() == 3){
						progressBar.setValue(0);
						progressLabel.setText("0");
						downLoad.setEnabled(true);
						pauseAndContinue.setEnabled(false);
						fileMap.remove(targetFile.getText());
					}else if(c.getStatus() == 2){
						progressBar.setValue(c.getProgress());
						downLoad.setEnabled(false);
						pauseAndContinue.setText("继续");
						pauseAndContinue.setEnabled(true);		
					}
				}
			}

			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		JPanel panelFirst = new JPanel();
		//panelFirst.setLayout(new FlowLayout(FlowLayout.LEFT));
		JScrollPane pane = new JScrollPane(fileTable);
		panelTemp1.add(panelFirst,BorderLayout.NORTH);
		panelFirst.add(pane);
		
		JPanel panelTemp2 = new JPanel();
		panelTemp2.setLayout(new BorderLayout());
		panelTemp1.add(panelTemp2);
		JPanel panelSecond = new JPanel();
		downLoad = new JButton("Download");
		downLoad.addActionListener(new CDownloadListener());
		browse = new JButton("浏览");
		browse.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser fDialog = new JFileChooser();
				//设置文件选择框的标题 
				fDialog.setDialogTitle("请选择存放文件目录");
				//弹出选择框
				fDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fDialog.showOpenDialog(null);
				// 如果是选择了文件
				if(JFileChooser.APPROVE_OPTION == returnVal){
				       //打印出文件的路径，你可以修改位 把路径值 写到 textField 中
					//System.out.println(fDialog.getSelectedFile());
					storePath = fDialog.getSelectedFile().getAbsoluteFile().toString() + "\\";
					storeSite.setText(storePath);
					System.out.println(storePath);
				}
			}
		});
		targetFile = new JLabel();
		targetFile.setText("");
		pauseAndContinue = new JButton();
		pauseAndContinue.setText("暂停");
		pauseAndContinue.addActionListener(new CPauseAndContinueListener());
		panelTemp2.add(panelSecond,BorderLayout.NORTH);
		panelSecond.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelSecond.add(browse);
		panelSecond.add(downLoad);
		panelSecond.add(pauseAndContinue);
		panelSecond.add(targetFile);
		progressLabel = new JLabel();
		
		JPanel panelTemp3 = new JPanel();
		panelTemp3.setLayout(new BorderLayout());
		panelTemp2.add(panelTemp3);
		JPanel panelThird = new JPanel();
		panelTemp3.add(panelThird,BorderLayout.NORTH);
		panelThird.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel jl2 = new JLabel("存放路径：");
		storeSite = new JLabel("");
		panelThird.add(jl2);
		panelThird.add(storeSite);
		panelThird.add(progressLabel);
		
		JPanel panelTemp4 = new JPanel();
		panelTemp4.setLayout(new BorderLayout());
		panelTemp3.add(panelTemp4);
		JPanel panelForth = new JPanel();
		panelTemp4.add(panelForth,BorderLayout.NORTH);
		panelForth.setLayout(new FlowLayout(FlowLayout.LEFT));
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		panelForth.add(progressBar);
		
		setConnection();
	}
	
	public void setConnection(){
		boolean connected = true;
		try {
			//if(userName.getText().equals("")){
				//connectionStatus.setText("Please enter your user name!");
				//connected = false;
			//}else
				s = new Socket(parameter.ip,parameter.port);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			connected = false;
			connectionStatus.setText("Connection failed!");
			userName.setEnabled(false);
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			connected = false;
			connectionStatus.setText("Connection failed!");
			userName.setEnabled(false);
			e1.printStackTrace();
		}
		System.out.println(connected);
		if(connected){	
			connectionStatus.setText("Connected!");
			CThreadOutput c;
			try {
				//c = new CThreadOutput(s.getOutputStream(),"Login" + parameter.headerAndBodySplit + userName.getText() + parameter.endOfInput);
				//c.start();
				Thread c2 = new CThreadInput(s.getInputStream());
				c2.start();
				userName.setEnabled(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		private InputStream in;
		private boolean runFlag;
		
		CThreadInput(InputStream arg0){
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
						in.read(b);
						//System.out.println(b);
					    current = new String(b);
						content += current.substring(1);
					}while(current.substring(0,1).equals("1"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					runFlag = false;
					e.printStackTrace();
				}
				System.out.println(content);
				String s[] = content.split(parameter.endOfInput);
				ContentClassify(s[0],this);
			}
		}
		public void setrunFlag(boolean flag){
			runFlag = flag;
		}
	}
	
	
	class CPauseAndContinueListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(pauseAndContinue.getText().equals("暂停")){
				pauseAndContinue.setText("继续");
				CDownloadFile c = fileMap.get(targetFile.getText());
				c.stop();
			}
			else if(pauseAndContinue.getText().equals("继续")){
				pauseAndContinue.setText("暂停");
				if(storeSite.getText().equals("")){
					System.out.println("Error 1！");
					JOptionPane.showMessageDialog(null, "请选择存放目录!","警告",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(targetFile.getText().equals("")){
					System.out.println("Error 2！");
					JOptionPane.showMessageDialog(null, "请选择下载的目标文件名!","警告",JOptionPane.ERROR_MESSAGE);
					return;
				}
				CThreadOutput c2 = new CThreadOutput(s,"Download" + parameter.headerAndBodySplit + targetFile.getText() + parameter.endOfInput);
				c2.start();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				CDownloadFile c = fileMap.get(targetFile.getText());
				c.start();
			}
		}
		
	}
	class CDownloadListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(storeSite.getText().equals("")){
				System.out.println("Error 1！");
				JOptionPane.showMessageDialog(null, "请选择存放目录!","警告",JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(targetFile.getText().equals("")){
				System.out.println("Error 2！");
				JOptionPane.showMessageDialog(null, "请选择下载的目标文件名!","警告",JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(fileMap.get(targetFile.getText()) != null){
				CDownloadFile c = fileMap.get(targetFile.getText());
				if(c.isFinished()){
					fileMap.remove(targetFile.getText());
				}else return;		
			}					
			CThreadOutput c2 = new CThreadOutput(s,"Download" + parameter.headerAndBodySplit + targetFile.getText() + parameter.endOfInput);
			c2.start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			downloadFile = new CDownloadFile(targetFile.getText(),storeSite.getText(),sizeOfFile);
			
			String temptFile = targetFile.getText() + ".tmp";
			String temptContent;
			File file = new File(storeSite.getText() + temptFile);
			int count = 0;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				try {
					while ((temptContent = reader.readLine()) != null) {
						System.out.println(temptContent);
						String[] sss = temptContent.split(";");
						long position = Long.parseLong(sss[0]);
						long size = Long.parseLong(sss[1]);
						downloadFile.setPosition(count, position);
						downloadFile.setSize(count, size);
						count++;
						if(count == parameter.threadNumber) break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fileMap.put(targetFile.getText(), downloadFile);
			downloadFile.start();
			downLoad.setEnabled(false);
			pauseAndContinue.setText("暂停");
			pauseAndContinue.setEnabled(true);
			
		}
	}
	class ClogInListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(userName.getText().equals("")){
				userStatus.setText("Please enter your username!");
			}else{
				CThreadOutput c;
				c = new CThreadOutput(s,"Login" + parameter.headerAndBodySplit + userName.getText() + parameter.endOfInput);
				c.start();
				userName.setEnabled(false);
				logIn.setEnabled(false);
				logOut.setEnabled(true);
			}
		}
		
	}	
	
	class CconnectListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			setConnection();
		}
	}
	class ClogOutListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String content = "Logout" + parameter.headerAndBodySplit + parameter.endOfInput;
			CThreadOutput c;
			c = new CThreadOutput(s,content);
			c.start();
			userName.setText("");
			userName.setEnabled(true);
			logIn.setEnabled(true);
			logOut.setEnabled(false);
			Object[] columnTitle = {"File","Size"};
			Object[][] data = {};
			//fileTable.clearSelection();
			fileTable.clearSelection();
			
			tableModel.setDataVector(data, columnTitle);
			fileTable.setModel(tableModel);
		}
	}
	void ContentClassify(String content,CThreadInput c){
		String[] s = content.split(parameter.headerAndBodySplit);
		String header = s[0];
		if(header.equals("UpdateFileList")) UpdateFileList(s[1]);
		else if(header.equals("ReLogin")){
			logIn.setEnabled(true);
			logOut.setEnabled(false);
			userStatus.setText("This ip has already been used!");
		}
	}

	void UpdateFileList(String content) {
		String[] s = content.split(parameter.contentSplit);
		String[] columnTitle = {"File","Size"};
		Object data[][] = new Object[s.length][];
		for(int i = 0;i<s.length;i++){
			//System.out.println(s[i]);
			String ss[] = s[i].split(parameter.fileAndSizeSplit);
			Object[] o = {ss[0],ss[1]}; 
			data[i] = o;
			//for(int j = 0;j<s[i].length();j++){
				//data[i][j] = "" + s[i].charAt(j);
			//}
			//System.out.println(data[i]);
		}
		tableModel.setDataVector(data, columnTitle);
		fileTable.setModel(tableModel);
	}
	class CDownloadFile{
		private String fileName;
		private String path;
		private String filePath;
		private Parameter parameter = new Parameter();
		private int threadNumber;
		private int totalSize;
		private int currentSize;
		private long[] sizeOfThread;
		private long[] positionOfThread;
		private boolean threadRunFlag[];
		private CThreadFileInput c[];
		private int status;
		Socket[] s;
		
		CDownloadFile(String arg0,String arg1,int arg2){
			status = 0;
			currentSize = 0;
			fileName = arg0;
			path = arg1;
			filePath = path + fileName;
			totalSize = arg2;
			threadNumber = parameter.threadNumber;
			threadRunFlag = new boolean[threadNumber];
			c = new CThreadFileInput[threadNumber];
			sizeOfThread = new long[threadNumber];
			positionOfThread = new long[threadNumber];
			
			long size1 = totalSize/threadNumber;
			long size2 = totalSize - totalSize/threadNumber*(threadNumber-1);
			
			for(int i = 0;i<threadNumber - 1;i++){
				threadRunFlag[i] = true;
				long position = i*size1;
				if(position<0)
					position = 0;
				positionOfThread[i] = position;
				sizeOfThread[i] = size1;
			}
			int index = threadNumber - 1;
			sizeOfThread[index] = size2;
			positionOfThread[index] = size1*(threadNumber-1);
		}
		
		public void setPosition(int index,long p){
			positionOfThread[index] = p;
		}
		public void setSize(int index,long s){
			sizeOfThread[index] = s;
		}
		void stop(){
			status = 2;
			
			String temptFile = fileName + ".tmp";
			
			
			File file = new File(path + temptFile);
			if(file.exists()){
				file.delete();
			}
			
			for(int i = 0; i<threadNumber;i++){				
				CThreadOutput c1 = new CThreadOutput(s[i],"CloseThread" + parameter.headerAndBodySplit + fileName + parameter.endOfInput); 
				c1.start();
				c[i].setrunFlag(false);
				try {
					s[i].getOutputStream().flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
				String content;
				content = "" + positionOfThread[i] + ";" + sizeOfThread[i];
				BufferedWriter bw = null;
				OutputStream os = null;
				try {
					os = new FileOutputStream(file,true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bw = new BufferedWriter(new OutputStreamWriter(os));
				try {
					bw.write(content);
					bw.newLine();
					System.out.println(content);
					bw.close();
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public boolean isFinished(){
			for(int i = 0;i<threadNumber;i++){
				if(threadRunFlag[i])
					return false;
			}
			return true;
		}
		
		public int getProgress(){
			double percent = (double) currentSize/totalSize;
			return (int)(percent*100);
		}
		public int getStatus(){
			if(currentSize == totalSize) return 3;
			return status;
		}
		public void start(){
			status = 1;
			
			int leftToDownload = 0;
			for(int i = 0;i<parameter.threadNumber;i++){
				leftToDownload += sizeOfThread[i];
			}
			System.out.println("1:" + leftToDownload);
			currentSize = totalSize - leftToDownload;
			progressLabel.setText("1" + currentSize );
			
			s = new Socket[threadNumber];
			for(int i = 0;i<threadNumber;i++){
				try {
					s[i] = new Socket(parameter.ip,parameter.port);
					c[i] = new CThreadFileInput(s[i],positionOfThread[i],sizeOfThread[i],i);
					c[i].start();
					String sendContent = "ReadyAcceptFile" + parameter.headerAndBodySplit + i + parameter.contentSplit + positionOfThread[i] + 
							parameter.contentSplit + sizeOfThread[i] + parameter.contentSplit + fileName + parameter.endOfInput;
					CThreadOutput c2 = new CThreadOutput(s[i],sendContent);
					c2.start();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		class CThreadFileInput extends Thread{
			private Socket in;
			private boolean runFlag;
			private int numberOfThread;
			RandomAccessFile write;
			
			CThreadFileInput(Socket arg0,long arg1,long arg2,int number){
				numberOfThread = number;
				in = arg0;
				runFlag = true;
				try {
					write = new RandomAccessFile(filePath,"rw");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			public void setrunFlag(boolean flag){
				runFlag = flag;
			}
			public void run(){
				System.out.println(this.toString() + "Position: " + positionOfThread[numberOfThread] + "Left to download: " + sizeOfThread[numberOfThread]);
				while(runFlag){
					try {
						write.seek(positionOfThread[numberOfThread]);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					int loadsize = parameter.loadsize;				
						//String content = "";
						//String current = null;
						try {					
								byte[] b = new byte[loadsize];
								int actualSize = in.getInputStream().read(b);
								byte[] actualWrite = new byte[actualSize];
								for (int i = 0; i < actualSize; i++) {
									actualWrite[i] = b[i];
								}
								write.write(actualWrite);
								System.out.println(this.toString() + actualSize + ": " + new String(b));
								currentSize += actualSize;
								sizeOfThread[numberOfThread] -= actualSize;
								System.out.println(this.toString() + "Left to download: " + sizeOfThread[numberOfThread]);
								positionOfThread[numberOfThread] += actualSize;
								if(targetFile.getText().equals(fileName)){
									double percent = (double) currentSize/totalSize;
									progressLabel.setText("" + percent); 
									progressBar.setValue((int) (percent*100));
								}
								if(currentSize == totalSize){
									String temptFile = fileName + ".tmp";
									File file = new File(path + temptFile);
									if(file.exists()){
										file.delete();
									}
								}
								if(sizeOfThread[numberOfThread] == 0){
									CThreadOutput c = new CThreadOutput(in,"CloseThread" + parameter.headerAndBodySplit + parameter.endOfInput);
									c.start();
									write.close();
									if(!in.isClosed()){
										//in.close();
										//System.out.println("Thread " + numberOfThread + "closed!");
									}
									runFlag = false;
								}
						} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						runFlag = false;
						}
				}
				threadRunFlag[numberOfThread] = false;
				try {
					System.out.println("RandomAccessFile closed!" + numberOfThread);
					write.close();
					
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
				System.out.println(current);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}else{
			content = "0" + content;
			try {
				out.getOutputStream().write(content.getBytes());
				System.out.println(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
	}
}

class mTableModel extends DefaultTableModel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public mTableModel(){
		super();
	}
	public mTableModel(Object[][] data, Object[] columnNames)
	{
		super(data, columnNames);
	}

	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
}



class Parameter{
	public String endOfInput = "%";
	public String contentSplit = ";";
	public String fileAndSizeSplit = ":";
	public String headerAndBodySplit = "#";
	public int loadsize = 1024;
	public String ip = "127.0.0.1";
	public int port = 88;
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
					ip = itemsList.item(i).getTextContent();
				else if(itemsList.item(i).getNodeName().equals("Port"))
					port = Integer.parseInt(itemsList.item(i).getTextContent());
				else if(itemsList.item(i).getNodeName().equals("threadNumber"))
					threadNumber = Integer.parseInt(itemsList.item(i).getTextContent());
				System.out.println(ip + ":" + port + ":" + threadNumber);
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