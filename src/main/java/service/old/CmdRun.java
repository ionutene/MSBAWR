package service.old;
/**
 *
 * @author costache.vlad
 */

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CmdRun implements Runnable {
	private BufferedReader bri;
	private BufferedReader bro;
	private InputStreamReader isr;
	private InputStreamReader osr;
	private String command;
	private Process p;
	private String environment;
	private boolean closeWriters;
	private boolean endMsg = false;
	public List<PrintWriter> pws = new ArrayList<PrintWriter>();
	public List<PrintWriter> closedPws = new ArrayList<PrintWriter>();

	public CmdRun(String cmd, PrintWriter pwr, boolean showEndMsg, String env){
		command = cmd;
		pws.add(pwr);
		endMsg = showEndMsg;
		environment = env.toLowerCase();
	}
	
	public CmdRun(String cmd, PrintWriter pwr, boolean showEndMsg, String env, boolean closeWriters){
		command = cmd;
		pws.add(pwr);
		endMsg = showEndMsg;
		environment = env.toLowerCase();
		this.closeWriters = closeWriters;
	}

	public void run(){	
		try { 

			String [] auxCommand = {"/bin/sh", "-c", command };
			p = Runtime.getRuntime().exec(auxCommand);

			isr = new InputStreamReader(p.getInputStream());
			bri = new BufferedReader(isr);

			osr = new InputStreamReader(p.getErrorStream());
			bro = new BufferedReader(osr);

			String line;
			while ( (line = bri.readLine()) != null){				
				sendResponse(line + "<br/><script>scrollDown();</script>");				
			}

			while ( (line = bro.readLine()) != null){
				sendResponse("<span style=\"color:red; font-weight:bold\">" + line + "</span><br/><script>scrollDown();</script>");				
			}			

			p.waitFor();
			if (endMsg){
				sendResponse("<p style=\"color:blue; font-weight:bold\">Running tests finished!</p><script>scrollDown();</script></body></html>");
				copyResults();		
			}	
			if (environment.equals("reindex")){		
				sendResponse("<img style=\"align:center\" src=\"./images/loading.gif\" />");
				Thread.sleep(4000);
				sendResponse("<script>window.location.href=\"tests.html\"</script>");				
			}
			close();
		}catch (Exception e) {			
			sendResponse("Error during executing command <" + command + ">: " + e.getMessage());	
		}
	}

	public void close() throws Exception{
		if (closeWriters){
			closeWriters();
		}		

		if (p != null){
			p.destroy();
			p = null;
		}	

		if (bri != null){
			bri.close();
			bri = null;
		}

		if (isr != null){
			isr.close();
			isr = null;
		}

		if (bro != null){
			bro.close();
			bro = null;
		}

		if (osr != null){
			osr.close();
			osr = null;
		}	

	}	

	public void sendResponse(String msg){
		for(PrintWriter auxPw: pws){
			if (auxPw != null){
				try{
					auxPw.write(msg); 
					auxPw.flush();	
				}catch (Exception e) {
					closedPws.add(auxPw);
				}				
			}else{
				removeNullWriters();
			}
		}

		for(Writer auxPw: closedPws){
			pws.remove(auxPw);
		}
		closedPws.clear();
	}

	private void removeNullWriters(){
		for(int i=0; i< pws.size() && (pws.get(i) == null); i++){
			pws.remove(i);
		}
	}

	public void closeWriters() throws IOException{
		for(Writer auxPw: pws){
			if(auxPw != null){
				auxPw.close();
			}
		}
		pws.clear();
	}

	public void copyResults() throws Exception{		
		Process proc = null;
		InputStreamReader verIsr = null;
		BufferedReader verBri = null;		
		InputStreamReader verEsr = null;
		BufferedReader verBre = null;
		String content = "";
		String masAdapterVersion = "-";
		String mposAdapetrVersion = "-";
		
		try { 
			String [] auxCommandVer = {"/bin/sh", "-c", "cd /home/mai/tests_msb_arch; java -jar *.jar " + environment + " version" };
			proc = Runtime.getRuntime().exec(auxCommandVer);

			verIsr = new InputStreamReader(proc.getInputStream());
			verBri = new BufferedReader(verIsr);
			
			verEsr = new InputStreamReader(proc.getErrorStream());
			verBre = new BufferedReader(verEsr);

			String line;
			StringBuilder result = new StringBuilder();
								
			while ( (line = verBri.readLine()) != null){				
				result.append(line );	
			}
			
			while ( (line = verBre.readLine()) != null){				
				content = "";
				break;	
			}
			
			proc.waitFor();
			content = result.toString();	
			
			if(content.length() > 1 && content.contains("MAS:") && content.contains("MPOS:")){
				masAdapterVersion = content.substring(content.indexOf("MAS:") + 4, content.indexOf(")") + 1 );
				masAdapterVersion = masAdapterVersion.substring(0, masAdapterVersion.indexOf(" (")) + "\\&lt\\;br\\/\\&gt\\;" + masAdapterVersion.substring(masAdapterVersion.indexOf("("));
				mposAdapetrVersion = content.substring(content.indexOf("MPOS:") + 5, content.lastIndexOf(")") + 1 );
				mposAdapetrVersion = mposAdapetrVersion.substring(0, mposAdapetrVersion.indexOf(" (")) + "\\&lt\\;br\\/\\&gt\\;" + mposAdapetrVersion.substring(mposAdapetrVersion.indexOf("("));
			}				
		}catch (Exception e) {
			sendResponse("Error during taking version of adapters: " + content + " <br/><script>scrollDown();</script>");			
		}finally{
			if (proc != null){
				proc.destroy();
			}
			if (verBri != null){
				verBri.close();
			}
			if (verIsr != null){
				verIsr.close();
			}	
		}

		SimpleDateFormat sdfFolder = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
		SimpleDateFormat sdfTable = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		Date now = new Date();
		String actualDateTimeFolder = sdfFolder.format(now);
		String actualDateTimeTable = sdfTable.format(now);

		String command = "mv /home/mai/tests_msb_arch/test-output /usr/tomcat/apache-tomcat-7.0.33/webapps/MSBARWeb/results/test-output_" + actualDateTimeFolder;
		String replacement = "<temp\\/>\\n<Result><Name>test-output_" + actualDateTimeFolder + "<\\/Name><Date>" + actualDateTimeTable + "<\\/Date><Log>RegressionTestMSBAdapter_" + environment + ".log<\\/Log>"  
							+ "<Mas>" + masAdapterVersion + "<\\/Mas>"
							+ "<Mpos>" + mposAdapetrVersion + "<\\/Mpos><\\/Result>";
		command = command + " && sed -i 's/<temp\\/>/" + replacement + "/g' /usr/tomcat/apache-tomcat-7.0.33/webapps/MSBARWeb/results.xml";
		command = command + " && rm /home/mai/tests_msb_arch/RegressionTestMSBAdapter_" + environment + ".log";	
	
		String [] auxCommand = {"/bin/sh", "-c", command };
		Process p = Runtime.getRuntime().exec(auxCommand);

		InputStreamReader osr = new InputStreamReader(p.getErrorStream());
		BufferedReader bro = new BufferedReader(osr);

		boolean showMsg = true;
		String line;
		while ( (line = bro.readLine()) != null){
			sendResponse("<span style=\"color:red; font-weight:bold\">" + line + "</span><br/><script>scrollDown();</script>");	
			showMsg = false;							
		}			

		p.waitFor();		
		bro.close();
		osr.close();

		if (showMsg){
			sendResponse("<p style=\"color:blue; font-weight:bold\">Tests Results was copied and updated!</span><a href=\"./results/test-output_" + actualDateTimeFolder +"/index.html\"> Click here for results!</a><br/><script>scrollDown();</script>");
		}
	}		
}
