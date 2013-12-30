package com.github.hobos_taco.hoborelease;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.hobos_taco.githobo.repository.Commit;
import com.github.hobos_taco.githobo.repository.Repository;
import com.github.hobos_taco.githobo.user.AuthenticatedUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UpdateJsonTask extends DefaultTask {
  public class ModJson {
    public String news;
    public String latestModVersion;
    public String latestMinecraftVersion;
    public String stableModVersion;
    public String stableMinecraftVersion;
    public ModRelease[] releases;
  }

  public class ModRelease {
    public String modVersion;
    public String minecraftVersion;
    public boolean stable;
    public String fileUrl;
    public String changes;

    public ModRelease(String modVersion, String minecraftVersion, boolean stable, String fileUrl, String changes) {
      this.modVersion = modVersion;
      this.minecraftVersion = minecraftVersion;
      this.stable = stable;
      this.fileUrl = fileUrl;
      this.changes = changes;
    }

    public ModRelease() {}
  }

  @SuppressWarnings("unused")
  @TaskAction
  public void task() {
    ReleaseExtension extension = (ReleaseExtension)HoboRelease.project.getExtensions().getByName("hoboRelease");

    AuthenticatedUser user = new AuthenticatedUser(extension.repositoryUsername, extension.repositoryPassword);
    Repository repository = new Repository(extension.repositoryOwner, extension.repositoryName);
    Commit commit = new Commit(repository, extension.commitSha);

    URL url = null;
    try {
      url = new URL(extension.modJsonDirectory + "/mod.json");
    } catch (MalformedURLException e) {
      System.out.println("Could not find json to download!");
      e.printStackTrace();
    }
    String json = null;
    try {
      if (url != null) {
        json = IOUtils.toString(url.openStream());
      }
    } catch (IOException e) {
      System.out.println("Could not download json!");
      e.printStackTrace();
    }
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    ModJson modJson = gson.fromJson(json, ModJson.class);
    String minecraftVersion = ((String)HoboRelease.project.getVersion()).split("-")[0];
    String modVersion = ((String)HoboRelease.project.getVersion()).split("-")[1];
    boolean isStable = false;
    //Set latest and stable
    modJson.latestMinecraftVersion = minecraftVersion;
    modJson.latestModVersion = modVersion;
    if (commit.getMessage().contains("[STABLE]")) {
      isStable = true;
      modJson.stableMinecraftVersion = minecraftVersion;
      modJson.stableModVersion = modVersion;
    }
    //Create new release
    ModRelease newRelease;
    newRelease = new ModRelease();
    newRelease.modVersion = modVersion;
    newRelease.minecraftVersion = minecraftVersion;
    newRelease.stable = isStable;
    newRelease.fileUrl = extension.downloadLocation + "/" + HoboRelease.project.getName() + "-" + HoboRelease.project.getVersion() + ".jar";
    newRelease.changes = commit.getMessage().replaceAll(Pattern.quote("[STABLE]"), "").trim();
    //Add new release to array
    List<ModRelease> modReleases = new ArrayList<ModRelease>();
    modReleases.add(newRelease);
    Collections.addAll(modReleases, modJson.releases);
    modJson.releases = modReleases.toArray(modJson.releases);
    //Save json
    json = gson.toJson(modJson);
    File jsonFile = new File(new File("."), "mod.json");
    try {
      FileWriter writer = new FileWriter(jsonFile);
      writer.write(json);
      writer.close();
    } catch (IOException e) {
      System.out.println("Could not save updated json!");
      e.printStackTrace();
    }

    //Upload json
    FTPClient client = new FTPClient();

    try {
      client.connect(extension.ftpServer, extension.ftpPort);
      client.login(extension.ftpUsername, extension.ftpPassword);
      client.enterLocalPassiveMode();
      client.setFileType(FTP.BINARY_FILE_TYPE);
    } catch (IOException e) {
      System.out.println("Could not connect and login to the server!");
      e.printStackTrace();
      return;
    }

    try {
      client.storeFile(jsonFile.getName(), new BufferedInputStream(new FileInputStream(jsonFile)));
    } catch (IOException e) {
      System.out.println("Could not store fil!e");
      e.printStackTrace();
    }

    try {
      client.logout();
      client.disconnect();
    } catch (IOException e) {
      System.out.println("Could not logout and disconnect!");
      e.printStackTrace();
    }
  }
}
