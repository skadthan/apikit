/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public enum HttpMethod {

  GET("get"),
  POST("post"),
  PUT("put"),
  DELETE("delete"),
  HEAD("head"),
  PATCH("patch"),
  OPTIONS("options"),
  TRACE("trace"),
  CONNECT("connect");

  private String name;

  HttpMethod(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static boolean isValidAction(String name) {
    for (HttpMethod httpMethod : values()) {
      if (httpMethod.getName().equals(name.toLowerCase()))
        return true;
    }
    return false;
  }

}
