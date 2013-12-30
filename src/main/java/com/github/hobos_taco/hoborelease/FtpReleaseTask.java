package com.github.hobos_taco.hoborelease;

import java.io.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.hobos_taco.githobo.repository.Commit;
import com.github.hobos_taco.githobo.repository.Repository;
import com.github.hobos_taco.githobo.user.AuthenticatedUser;

public class FtpReleaseTask extends DefaultTask {
  @SuppressWarnings("unused") @TaskAction public void release() {
    ReleaseExtension extension = (ReleaseExtension)HoboRelease.project.getExtensions().getByName("hoboRelease");

    AuthenticatedUser user = new AuthenticatedUser(extension.repositoryUsername, extension.repositoryPassword);
    Repository repository = new Repository(extension.repositoryOwner, extension.repositoryName);
    Commit commit = new Commit(repository, extension.commitSha);

    if (commit.getMessage() == null || commit.getMessage().isEmpty()) {
      System.out.println("No commit found!");
    }

    if (commit.getMessage().contains("[NO-DL]")) {
      System.out.println("No download specified by commit message");
      return;
    }

    File buildDirectory = new File(".");
    FTPClient client = new FTPClient();

    try {
      client.connect(extension.ftpServer, extension.ftpPort);
      client.login(extension.ftpUsername, extension.ftpPassword);
      client.enterLocalPassiveMode();
      client.setFileType(FTP.BINARY_FILE_TYPE);
      if (extension.ftpFolder != null && !extension.ftpFolder.equals("")) {
        client.changeWorkingDirectory(extension.ftpFolder);
      }
    } catch (IOException e) {
      System.out.println("Could not connect and login to the server!");
      e.printStackTrace();
      return;
    }

    File[] assetFiles = new File(buildDirectory, "build/libs/").listFiles();
    System.out.println(assetFiles.length + " asset(s) found!");

    for (File assetFile : assetFiles) {
      try {
        client.storeFile(assetFile.getName(), new BufferedInputStream(new FileInputStream(assetFile)));
      } catch (IOException e) {
        System.out.println("Could not store asset!");
        e.printStackTrace();
        return;
      }
    }

    try {
      client.logout();
      client.disconnect();
    } catch (IOException e) {
      System.out.println("Could not logout and disconnect!");
      e.printStackTrace();
      return;
    }
  }
}
