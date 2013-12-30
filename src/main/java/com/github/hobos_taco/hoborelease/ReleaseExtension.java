package com.github.hobos_taco.hoborelease;

import org.gradle.api.Project;

public class ReleaseExtension {
  public ReleaseExtension(@SuppressWarnings("unused") Project project) {}

  protected String repositoryUsername;     //github
  protected String repositoryPassword;     //github
  protected String repositoryOwner;        //github
  protected String repositoryName;         //github
  protected String commitSha;              //github

  protected String ftpUsername;            //ftp
  protected String ftpPassword;            //ftp
  protected String ftpServer;              //ftp
  protected String ftpFolder;              //ftp
  protected int ftpPort;                   //ftp

  protected String modJsonDirectory;       //dlpage
  protected String downloadLocation;       //dlpage
}
