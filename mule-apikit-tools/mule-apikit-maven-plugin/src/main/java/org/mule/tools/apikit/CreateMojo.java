/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.Scanner;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Goal for apikit:create
 */
@Mojo(name = "create")
public class CreateMojo
    extends AbstractMojo {

  @Component
  private BuildContext buildContext;

  /**
   * Pattern of where to find the spec .raml, .yaml or .yml files.
   */
  @Parameter
  private String[] specIncludes =
      new String[] {"src/main/resources/api/**/*.yaml", "src/main/resources/api/**/*.yml", "src/main/resources/api/**/*.raml",
          "src/main/resources/api/**/*.json"};

  /**
   * Pattern of what to exclude searching for .yaml files.
   */
  @Parameter
  private String[] specExcludes = new String[] {};

  /**
   * Spec source directory to use as root of specInclude and specExclude patterns.
   */
  @Parameter(defaultValue = "${basedir}")
  private File specDirectory;

  /**
   * Pattern of where to find the Mule XMLs.
   */
  @Parameter
  private String[] muleXmlIncludes = new String[] {"src/main/mule/**/*.xml", "src/main/resources/**/*.xml"};

  /**
   * Pattern of what to exclude searching for Mule XML files.
   */
  @Parameter
  private String[] muleXmlExcludes = new String[] {};

  /**
   * Spec source directory to use as root of muleInclude and muleExclude patterns.
   */
  @Parameter(defaultValue = "${basedir}")
  private File muleXmlDirectory;

  /**
   * Where to output the generated mule config files.
   */
  @Parameter(defaultValue = "${basedir}/src/main/mule")
  private File muleXmlOutputDirectory;

  /**
   * Spec source directory to use as root of muleDomain.
   */
  @Parameter(property = "domainDirectory")
  private File domainDirectory;

  /**
   * Mule version that is being used.
   */
  @Parameter(property = "minMuleVersion")
  private String minMuleVersion;

  /**
   * Mule runtime edition that is being used.
   */
  @Parameter(property = "runtimeEdition", defaultValue = "CE")
  private String runtimeEdition;

  private Log log;

  List<String> getIncludedFiles(File sourceDirectory, String[] includes, String[] excludes) {
    Scanner scanner = buildContext.newScanner(sourceDirectory, true);
    scanner.setIncludes(includes);
    scanner.setExcludes(excludes);
    scanner.scan();

    String[] includedFiles = scanner.getIncludedFiles();
    for (int i = 0; i < includedFiles.length; i++) {
      includedFiles[i] = new File(scanner.getBasedir(), includedFiles[i]).getAbsolutePath();
    }

    String[] result = new String[includedFiles.length];
    System.arraycopy(includedFiles, 0, result, 0, includedFiles.length);
    return Arrays.asList(result);
  }

  public void execute()
      throws MojoExecutionException {
    Validate.notNull(muleXmlDirectory, "Error: muleXmlDirectory parameter cannot be null");
    Validate.notNull(specDirectory, "Error: specDirectory parameter cannot be null");

    log = getLog();

    List<String> specFiles = getIncludedFiles(specDirectory, specIncludes, specExcludes);
    List<String> muleXmlFiles = getIncludedFiles(muleXmlDirectory, muleXmlIncludes, muleXmlExcludes);
    String domainFile = processDomain();
    if (minMuleVersion != null) {
      log.info("Mule version provided: " + minMuleVersion);
    }
    log.info("Processing the following RAML files: " + specFiles);
    log.info("Processing the following xml files as mule configs: " + muleXmlFiles);

    try {
      final RuntimeEdition muleRuntimeEdition = RuntimeEdition.valueOf(this.runtimeEdition);
      Scaffolder scaffolder = Scaffolder.createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, domainFile,
                                                          minMuleVersion, muleRuntimeEdition);
      scaffolder.run();
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }
  }

  private String processDomain() {
    String domainFile = null;

    if (domainDirectory != null) {
      List<String> domainFiles = getIncludedFiles(domainDirectory, new String[] {"*.xml"}, new String[] {});
      if (domainFiles.size() > 0) {
        domainFile = domainFiles.get(0);
        if (domainFiles.size() > 1) {
          log.info("There is more than one domain file inside of the domain folder. The domain: " + domainFile
              + " will be used.");
        }
      } else {
        log.error("The specified domain directory [" + domainDirectory + "] does not contain any xml file.");
      }
    } else {
      log.info("No domain was provided. To send it, use -DdomainDirectory.");
    }
    return domainFile;
  }
}
