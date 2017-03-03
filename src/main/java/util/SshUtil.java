package util;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SshUtil {

    private static final Logger LOGGER = LogManager.getLogger(SshUtil.class);

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

    private static ChannelExec getChannelExec(String command, Session sshConn) throws JSchException {
        ChannelExec channel = (ChannelExec) sshConn.openChannel("exec");
        channel.setCommand(command);
        channel.connect();
        return channel;
    }

    private static String executeCommand(ChannelExec channel, String command, boolean displayCommandResult) throws JSchException {
        StringBuilder stringBuilder = new StringBuilder();

        try(BufferedReader inputBufferedReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(channel.getErrStream()))) {

            String line;
            String lineError = null;
            while (((line = inputBufferedReader.readLine()) != null) || (lineError = errorBufferedReader.readLine()) != null) {
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

        } catch (IOException e) {
            LOGGER.error("Error during <executeCommand>:" + e);
            stringBuilder = new StringBuilder();
            stringBuilder.append(e.getMessage());
        } finally {
            channel.disconnect();
        }

        return stringBuilder.toString();
    }

    /*public static String executeCommand(String command, Session sshConn, boolean displayCommandResult) {
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
*/
    public static boolean CopySftpFileToFile(String sourceFile, Session sourceFileSession, String targetFile) throws JSchException, SftpException, IOException {

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

    public static String getInstallerKitPath(String jenkinsAdapterProject, String subFolder, String fileType, Session sshConn) throws Exception {
        String command = "cat \"" + jenkinsAdapterProject + subFolder + "/log\" |grep \"#\"";
        ChannelExec channelExec = getChannelExec(command, sshConn);
        String buildNumber = executeCommand(channelExec, command, true);

        if ((buildNumber.length() - buildNumber.replace("#", "").length()) > 1) {
            LOGGER.warn("The build number couldn't be taken from <" + subFolder + ">. It was:" + buildNumber);
            return null;
        }

        buildNumber = buildNumber.substring(buildNumber.indexOf("#") + 1, buildNumber.length());
        command = "find " + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/ -name *" + fileType;
        channelExec = getChannelExec(command, sshConn);
        String filePath = executeCommand(channelExec, command, true);

        if ((filePath.length() - filePath.replace(fileType, "").length()) > fileType.length()) {
            LOGGER.warn("The are more <" + fileType + "> files in path:" + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/");
            return null;
        } else if ((filePath.length() - filePath.replace(fileType, "").length()) < fileType.length()) {
            LOGGER.warn("The are NO <" + fileType + "> files in path:" + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/");
            return null;
        }

        return filePath;
    }

    public static String getZipFilePath(String jenkinsAdapterProject, String fileType, Session sshConn) throws JSchException {
        String command = "find " + jenkinsAdapterProject + " -name *" + fileType;
        LOGGER.info(command);
        ChannelExec channelExec = getChannelExec(command, sshConn);
        String filePath = executeCommand(channelExec, command, true);

/*        if ((filePath.length() - filePath.replace(fileType, "").length()) > fileType.length()) {
            LOGGER.warn("The are more <" + fileType + "> files in path:" + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/");
            return null;
        } else if ((filePath.length() - filePath.replace(fileType, "").length()) < fileType.length()) {
            LOGGER.warn("The are NO <" + fileType + "> files in path:" + jenkinsAdapterProject + "/builds/" + buildNumber + "/archive/target/");
            return null;
        }*/

        return filePath;
    }

    public static String getLatestBuildPath(String jenkinsAdapterProject, String fileType, Session sshConn) throws Exception {
        String command = "find " + jenkinsAdapterProject + "/lastSuccessful/archive/target/ -name *" + fileType;
        ChannelExec channelExec = getChannelExec(command, sshConn);
        String filePath = executeCommand(channelExec, command, true);

        if ((filePath.length() - filePath.replace(fileType, "").length()) > fileType.length()) {
            System.out.println("The are more <" + fileType + "> files in path:" + jenkinsAdapterProject + "/lastSuccessful/archive/target/");
            return null;
        } else if ((filePath.length() - filePath.replace(fileType, "").length()) < fileType.length()) {
            System.out.println("The are NO <" + fileType + "> files in path:" + jenkinsAdapterProject + "/lastSuccessful/archive/target/");
            return null;
        }

        return filePath;
    }

}
