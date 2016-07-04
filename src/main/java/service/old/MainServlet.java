package service.old;

import com.jcraft.jsch.Session;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {

    private static final int BUFFER = 1024;

    private static final String tomcatRegressionWebLocation = "/usr/tomcat/apache-tomcat-7.0.33/webapps/MSBARWeb/";
    private static final String ResultsLocation = tomcatRegressionWebLocation + "results";
    private static final String jarInLocation = "/home/mai/tests_msb_arch/";
    private static final String cdJarLocation = "cd /home/mai/tests_msb_arch;";

    HashMap<String, CmdRun> backgroundObjects = new HashMap<String, CmdRun>();
    HashMap<String, Thread> backgroundThreads = new HashMap<String, Thread>();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // If it is a get request forward to doPost()
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String env = request.getParameter("env");
        String stop = request.getParameter("stop");
        String initial = request.getParameter("initial");

        Thread correspondingThread;
        CmdRun correspondingObject;

        if (env == null) {
            out.println("Sent request got no environtment set!");
            return;
        } else {
            correspondingThread = backgroundThreads.get(env);
            correspondingObject = backgroundObjects.get(env);
        }

        if (env.equals("reindex")) {
            Session localSession = null, jenkinsSession = null;
            try {
                if ((correspondingThread != null) && correspondingThread.isAlive()) {
                    out.println("Reindexing is already running:");
                    out.flush();

                    correspondingObject.pws.add(out);
                    while (correspondingThread.isAlive()) { /* wait */}
                } else {

                    // get the latest release Path from jenkins
                    out.println("Get the latest release Path from jenkins<br/>");
                    out.flush();
                    UtilsSsh.setHashMapValues("../webapps/MSBARWeb/settings.xml");
                    localSession = UtilsSsh.initSSHAuth(UtilsSsh.hMap.get("LOCAL_MACHINE"), UtilsSsh.hMap.get("LOCAL_PORT"),
                            UtilsSsh.hMap.get("LOCAL_USERNAME"), UtilsSsh.hMap.get("LOCAL_PASSWORD"));
                    jenkinsSession = UtilsSsh.initSSHAuth(UtilsSsh.hMap.get("JENKINS_MACHINE"), UtilsSsh.hMap.get("JENKINS_PORT"),
                            UtilsSsh.hMap.get("JENKINS_USERNAME"), UtilsSsh.hMap.get("JENKINS_PASSWORD"));
                    String installaterKitPath = UtilsSsh.getInstallerKitPath(UtilsSsh.hMap.get("JENKINS_PROJECT"), "/promotions/Approved/lastSuccessful",
                            ".zip", jenkinsSession);
                    if (installaterKitPath == null) {
                        return;
                    }
                    String kitFileName = installaterKitPath.substring(installaterKitPath.lastIndexOf("/") + 1);

                    // check if the .Zip file already exist
                    out.println("Check if the .Zip file already exist<br/>");
                    out.flush();
                    File localDir = new File(jarInLocation);
                    boolean found = false;
                    for (String aux : localDir.list()) {
                        if (aux.equals(kitFileName)) {
                            found = true;
                            out.println("The .zip file already exist:" + aux + "<br/>");
                            out.flush();
                            break;
                        }
                    }

                    // if other clean up the folder
                    if (!found) {
                        out.println("Clean-up the folder<br/>");
                        out.flush();

                        CmdRun clearCmd = new CmdRun("rm -rf " + jarInLocation + "*", out, false, "", false);
                        clearCmd.run();
                    }

                    // copy the new .zip file and unzip it
                    out.println("Copy the new .zip file and unzip it<br/>");
                    out.flush();
                    UtilsSsh.CopySftpFileToSftpFile(installaterKitPath, jenkinsSession, jarInLocation + kitFileName, localSession);
                    CmdRun unzipCmd = new CmdRun(cdJarLocation + " unzip -o " + kitFileName, out, false, "", false);
                    unzipCmd.run();

                    // delete the existing .jar file - a new build will exist for sure
                    out.println("Delete the existing .jar file - a new build will exist for sure<br/>");
                    out.flush();
                    CmdRun deleteCmd = new CmdRun("rm -f " + jarInLocation + "*.jar", out, false, "", false);
                    deleteCmd.run();

                    // get the latest build from jenkins
                    out.println("Get the latest build from jenkins<br/>");
                    out.flush();
                    String latestBuildPath = UtilsSsh.getLatestBuildPath(UtilsSsh.hMap.get("JENKINS_PROJECT"), ".jar", jenkinsSession);
                    String buildFileName = latestBuildPath.substring(latestBuildPath.lastIndexOf("/") + 1);

                    // copy the latest build
                    out.println("Copy the latest build<br/>");
                    out.flush();
                    UtilsSsh.CopySftpFileToSftpFile(latestBuildPath, jenkinsSession, jarInLocation + buildFileName, localSession);

                    // execute generation of tests.xml
                    out.println("Execute generation of tests.xml<br/>");
                    out.flush();
                    CmdRun reindexCmd = new CmdRun(
                            cdJarLocation + " java -jar *.jar webtests && cp " +
                                    jarInLocation + "tests.xml " + tomcatRegressionWebLocation + "tests.xml",
                            out, false, "reindex");
                    Thread reindexThread = new Thread(reindexCmd);

                    backgroundThreads.put("reindex", reindexThread);
                    backgroundObjects.put("reindex", reindexCmd);
                    reindexThread.start();

                    while (reindexThread.isAlive()) { /* wait */}
                }
            } catch (Exception e) {
                out.println("Error during <reindex>:" + e.getMessage());
            } finally {
                if (localSession != null) {
                    localSession.disconnect();
                }
                if (jenkinsSession != null) {
                    jenkinsSession.disconnect();
                }
            }
        } else if (stop != null) {
            try {
                CmdRun stopCmd = new CmdRun("kill -9 `ps -ef | grep " + env + " | grep -v grep | awk '{print $2}'`", out, false, "");
                correspondingObject.sendResponse("<b color=\"red\">Stopping tests!</b>");
                stopCmd.run();
            } catch (Exception e) {
                out.println("Exeption during stoping:" + e.getMessage());
            }
        } else if ((initial != null) && ((correspondingThread == null) || !correspondingThread.isAlive())) {
            getMachinesVersion(out, env);
            zipResultFiles(out);

            out.println("<b>Select tests from left side and click \"Run\"</b></br>");
            out.flush();
        } else if (((initial != null) && (correspondingThread != null) && correspondingThread.isAlive())
                || ((correspondingThread != null) && correspondingThread.isAlive())) {
            out.println("<html><head><script>function scrollDown() { document.body.scrollTop = document.body.scrollHeight; }</script></head><body>");
            out.println("<b>Tests are already running:</b></br>");
            out.flush();
            try {
                correspondingObject.pws.add(out);
                while (correspondingThread.isAlive()) { /* wait */}
            } catch (Exception e) {
                out.println("Error during <setWriter()>:" + e.getMessage());
            }
        } else {
            String[] arguments = request.getParameterValues("arg");
            if (arguments == null) {
                out.println("No test were selected for request!");
                return;
            }
            String argsCommand = "";
            for (String aux : arguments) {
                argsCommand = argsCommand + " " + aux;
            }
            out.println(cdJarLocation + " java -jar *.jar " + env + argsCommand);

            try {
                CmdRun runCmd = new CmdRun(cdJarLocation + " java -jar *.jar " + env + argsCommand, out, true, env);
                Thread runningThread = new Thread(runCmd);

                backgroundThreads.put(env, runningThread);
                backgroundObjects.put(env, runCmd);

                out.println("<html><head><script>function scrollDown() { document.body.scrollTop = document.body.scrollHeight; }</script></head><body>");

                runningThread.start();
                while (runningThread.isAlive()) { /* wait */}
            } catch (Exception e) {
                out.println("Error during <CmdRun.execCommand>:" + e.getMessage());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void destroy() {
        super.destroy();

        for (Map.Entry<String, Thread> item : backgroundThreads.entrySet()) {
            if (item.getValue() != null && item.getValue().isAlive()) {
                item.getValue().stop();
            }
        }
    }

    private void getMachinesVersion(PrintWriter out, String env) {

        CmdRun getVersionCmd = new CmdRun(cdJarLocation + " java -jar *.jar " + env + " version", out, false, env);
        Thread getVersionThread = new Thread(getVersionCmd);

        backgroundThreads.put("version", getVersionThread);

        getVersionThread.start();

        while (getVersionThread.isAlive()) { /* wait */}

    }

    private void zipResultFiles(PrintWriter out) throws IOException {

        String archivePath = ResultsLocation;

        // only if archive is available and the tag is not empty
        if (archivePath != null) {
            archivePath = archivePath + "/";
            List<File> files = filterFilesForHousekeeping(new File(archivePath).listFiles());

            out.println("HOUSEKEEPING Check Started ");

            if (files.size() > 0) {
                Date sysdate = new Date();
                String formatedDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(sysdate);

                File zipFile = new File(archivePath + formatedDate + ".zip");
                out.println("Creating zip file: " + zipFile.getName());

                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile, true)));

                for (File file : files) {

                    // create a new ZIP entry for the file
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    FileInputStream fis = new FileInputStream(file);
                    zos.putNextEntry(zipEntry);

                    // add file content to zip
                    int read = 0;
                    byte[] data = new byte[1024];
                    while ((read = fis.read(data, 0, BUFFER)) != -1) {
                        zos.write(data, 0, read);
                    }

                    zos.closeEntry();
                    fis.close();

                    // delete the original file
                    file.delete();
                }

                zos.close();
            }

        }

    }

    private List<File> filterFilesForHousekeeping(File[] files) {

        List<File> result = new ArrayList<File>();
        for (File file : files) {
            if (file.getName().contains(".zip")) {
                continue;
            }
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().contains(MonthYear())) {
                continue;
            }
            result.add(file);
        }
        return result;
    }

    private String MonthYear() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        String actualDateTime = sdf.format(Calendar.getInstance().getTime());

        return actualDateTime.substring(actualDateTime.indexOf("-") + 1, actualDateTime.indexOf("_"));

    }

}
