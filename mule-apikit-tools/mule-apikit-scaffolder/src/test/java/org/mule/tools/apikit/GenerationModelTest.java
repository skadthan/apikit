/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.apikit.model.HttpMethod.DELETE;
import static org.mule.tools.apikit.model.HttpMethod.POST;
import static org.mule.tools.apikit.model.HttpMethod.PUT;
import static org.mule.tools.apikit.model.HttpMethod.GET;

import amf.model.EndPoint;
import amf.model.Example;
import amf.model.Operation;
import amf.model.Payload;
import amf.model.Response;
import amf.model.Shape;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.output.GenerationModel;

import org.junit.Test;

public class GenerationModelTest {

  @Test
  public void testGetVerb() throws Exception {
    Operation operation = mock(Operation.class);
    when(operation.method()).thenReturn(GET.getName());
    EndPoint endPoint = mock(EndPoint.class);
    when(endPoint.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("get", new GenerationModel(api, endPoint, operation).getVerb());
  }

  @Test
  public void testGetStringFromActionType() throws Exception {
    EndPoint resource = mock(EndPoint.class);
    when(resource.path()).thenReturn("/api/pet");
    API api = mock(API.class);

    Operation action = mock(Operation.class);
    when(action.method()).thenReturn(GET.getName());
    assertEquals("retrieve", new GenerationModel(api, resource, action).getStringFromActionType());

    action = mock(Operation.class);
    when(action.method()).thenReturn(PUT.getName());
    assertEquals("create", new GenerationModel(api, resource, action).getStringFromActionType());

    action = mock(Operation.class);
    when(action.method()).thenReturn(POST.getName());
    assertEquals("update", new GenerationModel(api, resource, action).getStringFromActionType());

    action = mock(Operation.class);
    when(action.method()).thenReturn(DELETE.getName());
    assertEquals("delete", new GenerationModel(api, resource, action).getStringFromActionType());

    action = mock(Operation.class);
    when(action.method()).thenReturn("options");
    assertEquals("options", new GenerationModel(api, resource, action).getStringFromActionType());
  }

  @Test
  public void testGetExample() throws Exception {
    Example example = mock(Example.class);
    when(example.value()).thenReturn("{\n\"hello\": \">world<\"\n}");
    Shape schema = mock(Shape.class);
    when(schema.examples()).thenReturn(singletonList(example));
    Payload payload = mock(Payload.class);
    when(payload.schema()).thenReturn(schema);
    when(payload.mediaType()).thenReturn("application/json");
    Response response = mock(Response.class);
    when(response.payloads()).thenReturn(singletonList(payload));
    when(response.statusCode()).thenReturn("200");
    Operation operation = mock(Operation.class);
    when(operation.responses()).thenReturn(singletonList(response));
    when(operation.method()).thenReturn(GET.getName());
    EndPoint endpoint = mock(EndPoint.class);
    when(endpoint.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("{\n\"hello\": \">world<\"\n}",
                 new GenerationModel(api, endpoint, operation).getExampleWrapper());
  }

  @Test
  public void testGetExample200Complex() throws Exception {
    Example example = mock(Example.class);
    when(example.value()).thenReturn("<hello>world</hello>");
    Shape schema = mock(Shape.class);
    when(schema.examples()).thenReturn(singletonList(example));
    Payload payload = mock(Payload.class);
    when(payload.schema()).thenReturn(schema);
    when(payload.mediaType()).thenReturn("application/xml");
    Response response = mock(Response.class);
    when(response.payloads()).thenReturn(singletonList(payload));
    when(response.statusCode()).thenReturn("200");
    Operation operation = mock(Operation.class);
    when(operation.responses()).thenReturn(singletonList(response));
    when(operation.method()).thenReturn(GET.getName());
    EndPoint endpoint = mock(EndPoint.class);
    when(endpoint.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("<hello>world</hello>",
                 new GenerationModel(api, endpoint, operation).getExampleWrapper());
  }

  @Test
  public void testGetExampleComplex() throws Exception {
    Example example = mock(Example.class);
    when(example.value()).thenReturn("<hello>world</hello>");
    Shape schema = mock(Shape.class);
    when(schema.examples()).thenReturn(singletonList(example));
    Payload payload = mock(Payload.class);
    when(payload.schema()).thenReturn(schema);
    when(payload.mediaType()).thenReturn("application/xml");
    Response response = mock(Response.class);
    when(response.payloads()).thenReturn(singletonList(payload));
    when(response.statusCode()).thenReturn("403");
    Operation operation = mock(Operation.class);
    when(operation.responses()).thenReturn(singletonList(response));
    when(operation.method()).thenReturn(GET.getName());
    EndPoint endpoint = mock(EndPoint.class);
    when(endpoint.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("<hello>world</hello>",
                 new GenerationModel(api, endpoint, operation).getExampleWrapper());
  }

  @Test
  public void testGetExampleNull() throws Exception {
    Operation action = mock(Operation.class);
    when(action.method()).thenReturn(GET.getName());
    EndPoint resource = mock(EndPoint.class);
    when(resource.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals(null, new GenerationModel(api, resource, action).getExampleWrapper());
  }

  @Test
  public void testGetMadeUpName() throws Exception {
    Operation action = mock(Operation.class);
    when(action.method()).thenReturn(GET.getName());
    EndPoint resource = mock(EndPoint.class);
    when(resource.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("retrievePet", new GenerationModel(api, resource, action).getName());
  }

  @Test
  public void testGetRealName() throws Exception {
    Operation action = mock(Operation.class);
    when(action.method()).thenReturn(GET.getName());
    EndPoint resource = mock(EndPoint.class);
    when(resource.name()).thenReturn("Animal");
    when(resource.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("retrieveAnimal", new GenerationModel(api, resource, action).getName());
  }

  @Test
  public void testGetMadeUpNameWithMimeTypes() throws Exception {
    Operation action = mock(Operation.class);
    when(action.method()).thenReturn(POST.getName());
    EndPoint resource = mock(EndPoint.class);
    when(resource.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    GenerationModel model1 = new GenerationModel(api, resource, action, "text/xml");
    GenerationModel model2 = new GenerationModel(api, resource, action, "application/json");
    assertTrue(model1.compareTo(model2) != 0);
    assertEquals("updatePetTextXml", model1.getName());
    assertEquals("updatePetApplicationJson", model2.getName());
  }

  @Test
  public void testGetRelativeURI() throws Exception {
    Operation action = mock(Operation.class);
    when(action.method()).thenReturn(GET.getName());
    EndPoint resource = mock(EndPoint.class);
    when(resource.path()).thenReturn("/api/pet");
    API api = mock(API.class);
    assertEquals("/pet", new GenerationModel(api, resource, action).getRelativeURI());
  }
}
