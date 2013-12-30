package com.github.hobos_taco.hoborelease;

import java.util.HashMap;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HoboRelease implements Plugin<Project> {
  public static Project project;

  @Override public void apply(Project project) {
    HoboRelease.project = project;

    project.getExtensions().create("hoboRelease", ReleaseExtension.class, project);

    HashMap<String, Object> ftpMap = new HashMap<String, Object>();
    ftpMap.put("name", "uploadFtp");
    ftpMap.put("description", "Uploads the release assets to the given server via FTP.");
    ftpMap.put("type", FtpReleaseTask.class);
    project.task(ftpMap, "uploadFtp");

    HashMap<String, Object> jsonMap = new HashMap<String, Object>();
    jsonMap.put("name", "updateJson");
    jsonMap.put("description", "Updates the mod Json.");
    jsonMap.put("type", UpdateJsonTask.class);
    project.task(jsonMap, "updateJson");
  }
}
