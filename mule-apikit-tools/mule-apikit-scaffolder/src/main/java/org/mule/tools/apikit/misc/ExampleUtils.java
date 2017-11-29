/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.mule.weave.v2.runtime.ScriptingBindings;
import org.yaml.model.YDocument;
import org.yaml.model.YPart;
import org.yaml.parser.YamlParser;
import org.yaml.render.JsonRender;
import scala.collection.IndexedSeq;

import java.io.IOException;

public class ExampleUtils {

  private static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
  private static final String DEFAULT_CONTENT_TYPE = "application/json";

  private ExampleUtils() {}

  public static String getExampleContentType(String example) {

    if (isValidXML(example)) {
      return APPLICATION_XML_CONTENT_TYPE;
    }

    return DEFAULT_CONTENT_TYPE;
  }

  public static String getExampleAsJSONIfNeeded(String payload) {

    if (!(isValidXML(payload) || isValidJSON(payload))) {
      return transformYamlExampleIntoJSON(payload);
    }

    return payload;
  }

  public static String getDataWeaveExpressionText(String example) {
    String transformContentType = getExampleContentType(example);
    example = getExampleAsJSONIfNeeded(example);

    final String weaveResult = asDataWeave(example, transformContentType);

    return "%dw 2.0\n" +
        "output " + transformContentType + "\n" +
        "---\n" + weaveResult + "\n";
  }

  private static String asDataWeave(String payload, String mimeType) {
    String script = "output application/dw --- payload";
    ScriptingBindings bindings = new ScriptingBindings()
        .addBinding("payload", payload, mimeType);
    DataWeaveResult result = DataWeaveScriptingEngine.write(script, bindings);
    return result.getContentAsString();
  }

  private static String transformYamlExampleIntoJSON(String example) {
    final YamlParser yamlParser = YamlParser.apply(example);
    final IndexedSeq<YPart> parseSeq = yamlParser.parse(true);
    final String prettyPrintedJson = JsonRender.render(toDocument(parseSeq));

    try {
      return new ObjectMapper().readValue(prettyPrintedJson, JsonNode.class).toString();
    } catch (IOException e) {
      // If example couldn't have been processed, we return a null JSON.
      return "null";
    }
  }

  private static YDocument toDocument(IndexedSeq<YPart> parts) {
    if (parts.exists(v -> v instanceof YDocument)) {
      return parts.find(v -> v instanceof YDocument).map(c -> (YDocument) c).get();
    }
    return null;
  }

  public static boolean isValidXML(String payload) {
    return payload.startsWith("<");
  }

  public static boolean isValidJSON(String payload) {

    try {
      new ObjectMapper().readTree(payload);

    } catch (IOException e) {
      return false;
    }

    return true;
  }
}
