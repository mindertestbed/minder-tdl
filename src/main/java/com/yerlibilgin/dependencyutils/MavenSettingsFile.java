package com.yerlibilgin.dependencyutils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author yerlibilgin
 */
public class MavenSettingsFile {

  public File localRepository;

  public List<String[]> repositoryList = Collections.EMPTY_LIST;
}
