/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class UrlEncodedV1FunctionalTestCase extends MuleArtifactFunctionalTestCase {

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
    return "org/mule/module/apikit/validation/formParameters/mule-config-v1.xml";
  }

  @Test
  public void simpleUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "primo")
        .expect()
        .response()
        .body(is("first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-simple");
  }

  @Ignore //This test fails for RAML 0.8 because of APIMF-753
  @Test
  public void setDefaultFormParameterForUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("second", "segundo")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is("second=segundo&third=true&first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-with-default");
  }

  @Ignore //This test fails for RAML 0.8 because of APIMF-753
  @Test
  public void getKeyWithMultipleValuesUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("second", "segundo")
        .formParam("second", "segundo2")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is("second=segundo&second=segundo2&third=true&first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-duplicated-key");
  }

}
