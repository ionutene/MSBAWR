package service.old;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class UtilsSsh {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(UtilsSsh.class);

    public static HashMap<String, String > hMap = new HashMap<String, String >();

    public static Session initSSHAuth(String host, String port, String userName, String password) throws JSchException {
        Session sshCon;

        JSch jsch = new JSch();
        sshCon = jsch.getSession(userName, host, Integer.parseInt(port));
        sshCon.setConfig("StrictHostKeyChecking", "no");
        sshCon.setConfig("PreferredAuthentications", "password");
        sshCon.setPassword(password);
        sshCon.connect();

        return sshCon;
    }


    public static String executeCommand(String command, Session sshConn, boolean displayCommandResult) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader in = null;
        InputStreamReader esr = null;
        BufferedReader ine = null;
        try {
            ChannelExec channel = (ChannelExec) sshConn.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            isr = new InputStreamReader(channel.getInputStream());
            in = new BufferedReader(isr);

            esr = new InputStreamReader(channel.getErrStream());
            ine = new BufferedReader(esr);

            String line;
            String lineError = null;
            while (((line = in.readLine()) != null) || (lineError = ine.readLine()) != null) {
                if (lineError != null) {
                    LOGGER.warn("Executing command: <" + command + "> with error:" + lineError);
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(lineError);
                    break;
                }
                if (displayCommandResult) {
                    LOGGER.info("Command Result: " + line);
                }
                stringBuilder.append(line).append("\n");
            }

            if (stringBuilder.length() > 0) {
//              Delete last \n appended
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }

            channel.disconnect();

        } catch (Exception e) {
            LOGGER.error("Error during <executeCommand>:" + e);
            stringBuilder = new StringBuilder();
            stringBuilder.append(e.getMessage());
        } finally {
            try {
                if (ine != null) {
                    ine.close();
                }
                if (esr != null) {
                    esr.close();
                }
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {
                System.out.println("Error when closing readers: " + e);
                stringBuilder = new StringBuilder();
                stringBuilder.append(e.getMessage());
            }
        }

        return stringBuilder.toString();
    }

    public static boolean CopySftpFileToFile(String sourceFile, Session sourceFileSession, String targetFile) throws Exception {

        ChannelSftp channelSourceSftp = (ChannelSftp) sourceFileSession.openChannel("sftp");
        channelSourceSftp.connect();

        InputStream is = channelSourceSftp.get(sourceFile);
        Files.copy(is, Paths.get(targetFile));
        is.close();
        channelSourceSftp.disconnect();

        if (Files.notExists(Paths.get(targetFile))) {
            LOGGER.warn("The file wasn't copied: " + targetFile);
            return false;
        }
        return true;
    }


    public static boolean CopySftpFileToSftpFile(String sourceFile, Session sourceFileSession, String targetFile, Session targetFileSession) throws Exception {

        ChannelSftp channelSourceSftp = (ChannelSftp) sourceFileSession.openChannel("sftp");
        channelSourceSftp.connect();

        ChannelSftp channelTargetSftp = (ChannelSftp) targetFileSession.openChannel("sftp");
        channelTargetSftp.connect();

        InputStream is = channelSourceSftp.get(sourceFile);
        channelTargetSftp.put(is, targetFile);
        is.close();
        channelTargetSftp.chgrp(777, targetFile);
        channelSourceSftp.disconnect();
        channelTargetSftp.disconnect();

        String command = "ls " + targetFile.substring(0, targetFile.indexOf("/")) + " | grep " + targetFile.substring(targetFile.indexOf("/") + 1);
        String fileExist = executeCommand(command, targetFileSession, true);

        if (fileExist.length() == 0) {
            System.out.println("The file wasn't copied: " + targetFile);
            return false;
        }
        return true;
    }


    public static String getInstallerKitPath(String jenkinsAdapterProject, String subFolder, String fileType, Session sshConn) throws Exception {
        String command = "cat \"" + jenkinsAdapterProject + subFolder + "/log\" |grep \"#\"";
        String buildNumber = executeCommand(command, sshConn, true);

        if ((buildNumber.length() - buildNumber.replace("#", "").length()) > 1) {
            LOGGER.warn("The build number couldn't be taken from <" + subFolder + ">. It was:" + buildNumber);
            return null;
        }

        buildNumber = buildNumber.substring(buildNumber.indexOf("#") + 1, buildNumber.length());
        command = "find " + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/ -name *" + fileType;
        String filePath = executeCommand(command, sshConn, true);

        if ((filePath.length() - filePath.replace(fileType, "").length()) > fileType.length()) {
            LOGGER.warn("The are more <" + fileType + "> files in path:" + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/");
            return null;
        } else if ((filePath.length() - filePath.replace(fileType, "").length()) < fileType.length()) {
            LOGGER.warn("The are NO <" + fileType + "> files in path:" + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/");
            return null;
        }

        return filePath;
    }

    public static String getLatestBuildPath(String jenkinsAdapterProject, String fileType, Session sshConn) throws Exception {
        String command = "find " + jenkinsAdapterProject + "/lastSuccessful/archive/target/ -name *" + fileType;
        String filePath = executeCommand(command, sshConn, true);

        if ((filePath.length() - filePath.replace(fileType, "").length()) > fileType.length()) {
            System.out.println("The are more <" + fileType + "> files in path:" + jenkinsAdapterProject + "/lastSuccessful/archive/target/");
            return null;
        } else if ((filePath.length() - filePath.replace(fileType, "").length()) < fileType.length()) {
            System.out.println("The are NO <" + fileType + "> files in path:" + jenkinsAdapterProject + "/lastSuccessful/archive/target/");
            return null;
        }

        return filePath;
    }

    public static void setHashMapValues (String xmlPath)throws Exception{
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(xmlPath));
        doc.getDocumentElement().normalize();

        NodeList nodes = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            if(  !nodes.item(i).getNodeName().equals("#text") ){
                hMap.put((nodes.item(i).getNodeName()), nodes.item(i).getTextContent());
            }

        }
    }

}
