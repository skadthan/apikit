/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import amf.model.EndPoint;
import amf.model.Example;
import amf.model.Operation;
import amf.model.Payload;
import amf.model.Response;
import org.mule.tools.apikit.model.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mule.apikit.common.FlowName;
import org.mule.tools.apikit.model.HttpMethod;

import static org.mule.apikit.common.FlowName.FLOW_NAME_SEPARATOR;

public class GenerationModel implements Comparable<GenerationModel> {

  private final String verb;
  private Operation operation;
  private EndPoint endpoint;
  private String mimeType;
  private List<String> splitPath;
  private API api;

  public GenerationModel(API api, EndPoint endpoint, Operation operation) {
    this(api, endpoint, operation, null);
  }

  public GenerationModel(API api, EndPoint endpoint, Operation operation, String mimeType) {
    this.api = api;
    Validate.notNull(api);
    Validate.notNull(operation);
    Validate.notNull(operation.method());
    Validate.notNull(endpoint.path());

    this.endpoint = endpoint;
    this.operation = operation;
    this.splitPath = new ArrayList<>(Arrays.asList(this.endpoint.path().split("/")));
    this.verb = operation.method();
    this.mimeType = mimeType;
    if (!splitPath.isEmpty()) {
      splitPath.remove(0);
      splitPath.remove(0);
    }
  }

  public String getVerb() {
    return verb;
  }

  public String getStringFromActionType() {
    final String method = operation.method().toLowerCase();
    try {
      switch (HttpMethod.valueOf(method.toUpperCase())) {
        case GET:
          return "retrieve";
        case POST:
          return "update";
        case PUT:
          return "create";
        case DELETE:
          return "delete";
      }
    } catch (IllegalArgumentException ignored) {
    }

    return method;
  }

  public String getExampleWrapper() {
    final List<Response> responses = operation.responses();

    // Looking for an example in ok responses
    final Optional<Response> response = findFirst(responses, GenerationModel::isOkResponse);
    if (response.isPresent()) {
      final Optional<String> okExample = getResponseExample(response.get());
      if (okExample.isPresent())
        return okExample.get();
    }

    // Looking for an example in all responses
    for (Response errorResponse : operation.responses()) {
      final Optional<String> nonOkExample = getResponseExample(errorResponse);
      if (nonOkExample.isPresent())
        return nonOkExample.get();
    }

    return null;

  }

  private Optional<String> getResponseExample(Response response) {
    final List<Payload> payloadWithExamples = getPayloadWithExamples(response);
    final Optional<Example> jsonExample = findFirst(payloadWithExamples, p -> "application/json".equals(p.mediaType()))
        .flatMap(p -> first(p.schema().examples()));

    if (jsonExample.isPresent())
      return jsonExample.map(Example::value);

    return first(payloadWithExamples).flatMap(p -> first(p.schema().examples())).map(Example::value);
  }

  private List<Payload> getPayloadWithExamples(Response rs) {
    return filter(rs.payloads(), p -> !p.schema().examples().isEmpty());
  }

  private static boolean isOkResponse(Response response) {
    return "200".equals(response.statusCode());
  }

  private static <T> Optional<T> findFirst(List<T> list, Predicate<T> predicate) {
    return list.stream().filter(predicate).findFirst();
  }

  private static <T> Optional<T> first(List<T> list) {
    return list.stream().findFirst();
  }

  private static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
    return list.stream().filter(predicate).collect(Collectors.toList());
  }

  public String getName() {
    StringBuilder name = new StringBuilder();
    name.append(this.getStringFromActionType());
    String resourceName = this.endpoint.name();

    if (resourceName == null) {
      StringBuffer buff = new StringBuffer();
      for (String i : this.splitPath) {
        buff.append(StringUtils.capitalize(i));
      }
      resourceName = buff.toString();
    }

    name.append(resourceName);

    if (this.mimeType != null) {
      StringBuffer buff = new StringBuffer();
      for (String part : mimeType.split("/")) {
        buff.append(StringUtils.capitalize(part));
      }
      name.append(buff.toString());
    }

    return name.toString().replace(" ", "");
  }

  public String getRelativeURI() {
    return "/" + StringUtils.join(splitPath.toArray(), "/");
  }

  public API getApi() {
    return api;
  }

  public String getContentType() {
    final List<Response> responses = operation.responses();

    if (responses != null) {
      for (Response response : responses) {
        int statusCode = Integer.parseInt(response.statusCode());
        if (statusCode >= 200 && statusCode < 299) {
          if (response.payloads() != null && !response.payloads().isEmpty()) {
            return response.payloads().get(0).mediaType();
          }
        }
      }
    }

    return null;
  }

  public String getFlowName() {
    StringBuilder flowName = new StringBuilder("");
    flowName.append(operation.method().toLowerCase())
        .append(FLOW_NAME_SEPARATOR)
        .append(endpoint.path());

    if (mimeType != null) {
      flowName.append(FLOW_NAME_SEPARATOR)
          .append(mimeType);
    }


    if (api.getConfig() != null && !StringUtils.isEmpty(api.getConfig().getName())) {
      flowName.append(FLOW_NAME_SEPARATOR)
          .append(api.getConfig().getName());
    }
    return FlowName.encode(flowName.toString());
  }

  @Override
  public int compareTo(GenerationModel generationModel) {
    return this.getName().compareTo(generationModel.getName());
  }
}
