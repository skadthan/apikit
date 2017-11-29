/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import amf.ProfileNames;
import amf.client.AmfParser;
import amf.client.BaseParser;
import amf.client.OasParser;
import amf.client.RamlParser;
import amf.model.Document;
import amf.model.WebApi;
import amf.validation.AMFValidationReport;
import org.mule.tools.apikit.input.parsers.exception.ParserException;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DocumentParser {

  private DocumentParser() {}

  public static RamlParser ramlParser() {
    return new RamlParser();
  }

  public static OasParser oasParser() {
    return new OasParser();
  }

  public static AmfParser amfParser() {
    return new AmfParser();
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happend while parsing the api. Message: " + e.getMessage(), e);
    }
  }

  public static Document parseFile(BaseParser parser, String url) throws ParserException {
    return handleFuture(parser.parseFileAsync(url));
  }

  public static Document parseString(BaseParser parser, String content) throws ParserException {
    return handleFuture(parser.parseStringAsync(content));
  }

  public static WebApi getWebApi(BaseParser parser, String content) throws ParserException {
    return getWebApi(parseString(parser, content));
  }

  public static WebApi getWebApi(BaseParser parser, Path path) throws ParserException {
    return getWebApi(parseFile(parser, path.toUri().toString()));
  }

  public static WebApi getWebApi(Document document) throws ParserException {
    return (WebApi) document.encodes();
  }

  public static AMFValidationReport getParsingReport(RamlParser parser) throws ParserException {
    return getParsingReport(parser, ProfileNames.RAML());
  }

  public static AMFValidationReport getParsingReport(OasParser parser) throws ParserException {
    return getParsingReport(parser, ProfileNames.OAS());
  }

  public static AMFValidationReport getParsingReport(AmfParser parser) throws ParserException {
    return getParsingReport(parser, ProfileNames.AMF());
  }

  private static AMFValidationReport getParsingReport(BaseParser parser, String profile) throws ParserException {
    return handleFuture(parser.reportValidation(profile));
  }
}
