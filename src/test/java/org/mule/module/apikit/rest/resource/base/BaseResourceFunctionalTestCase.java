/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource.base;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class BaseResourceFunctionalTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/rest/resource/base/base-functional-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void updateNotSupported() throws Exception
    {
        given().expect().response().statusCode(405).header("Content-Length", "0").when().put("/api");
        given().expect().response().statusCode(405).header("Content-Length", "0").when().put("/api/");
    }

    @Test
    public void createNotSupported() throws Exception
    {
        given().expect().response().statusCode(405).header("Content-Length", "0").when().post("/api");
        given().expect().response().statusCode(405).header("Content-Length", "0").when().post("/api/");
    }

    @Test
    public void deleteNotSupported() throws Exception
    {
        given().expect().response().statusCode(405).header("Content-Length", "0").when().delete("/api");
        given().expect().response().statusCode(405).header("Content-Length", "0").when().delete("/api/");
    }

    @Test
    public void retrieveNotSupported() throws Exception
    {
        given().header("Accept", "text/html").expect().response().statusCode(405).when().get("/api");
        given().header("Accept", "text/html").expect().response().statusCode(405).when().get("/api/");
    }

    @Test
    public void existsNotSupport() throws Exception
    {
        given().expect().response().statusCode(405).header("Content-Length", "0").when().head("/api");
        given().expect().response().statusCode(405).header("Content-Length", "0").when().head("/api/");
    }

}
