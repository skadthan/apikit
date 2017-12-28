/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters.uri;

import com.jayway.restassured.RestAssured;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;

@ArtifactClassLoaderRunnerConfig
@Ignore
public class ResolveUriVersionParameterTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigResources() {
    return "org/mule/module/apikit/parameters/uri/uri-parameters-config.xml";
  }

  @Test
  public void failWhenInvokingEndpointWithoutVersion() throws Exception {
    given()
        .expect()
        .statusCode(404)
        .when().get("api/resource/{version}");
  }

  @Test
  public void successWhenInvokingEndpointWithVersion() throws Exception {
    given()
        .expect()
        .statusCode(200)
        .when().get("api/resource/v1");
  }
}
