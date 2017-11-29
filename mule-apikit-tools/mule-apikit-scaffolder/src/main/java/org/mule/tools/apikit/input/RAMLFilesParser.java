/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import amf.client.RamlParser;
import amf.model.EndPoint;
import amf.model.Operation;
import amf.model.Payload;
import amf.model.Request;
import amf.model.WebApi;
import amf.validation.AMFValidationReport;
import amf.validation.AMFValidationResult;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.input.parsers.DocumentParser;
import org.mule.tools.apikit.input.parsers.exception.ParserException;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;
import scala.collection.Iterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mule.tools.apikit.input.parsers.DocumentParser.getWebApi;
import static org.mule.tools.apikit.input.parsers.DocumentParser.ramlParser;

public class RAMLFilesParser {

  private Map<ResourceActionMimeTypeTriplet, GenerationModel> entries =
      new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
  private final APIFactory apiFactory;
  private final Log log;

  public RAMLFilesParser(Log log, Map<File, InputStream> fileStreams, APIFactory apiFactory) {
    this.log = log;
    this.apiFactory = apiFactory;
    List<File> processedFiles = new ArrayList<>();
    for (Map.Entry<File, InputStream> fileInputStreamEntry : fileStreams.entrySet()) {
      File ramlFile = fileInputStreamEntry.getKey();
      try {
        final RamlParser ramlParser = ramlParser();
        final String content = IOUtils.toString(fileInputStreamEntry.getValue(), Charset.defaultCharset());
        final WebApi api = getWebApi(ramlParser, ramlFile.toPath());
        if (isValidRaml(ramlParser, ramlFile.getName())) {
          collectResources(ramlFile, api.endPoints(), API.DEFAULT_BASE_URI);
          processedFiles.add(ramlFile);
        }
      } catch (Exception e) {
        log.info("Could not parse [" + ramlFile + "] as root RAML file. Reason: " + e.getMessage());
        log.debug(e);
      }

    }
    if (processedFiles.size() > 0) {
      this.log.info("The following RAML files were parsed correctly: " +
          processedFiles);
    } else {
      this.log.error("RAML Root not found. None of the files were recognized as valid root RAML files.");
    }
  }

  private boolean isValidRaml(RamlParser ramlParser, String fileName) throws ParserException {
    final AMFValidationReport report = DocumentParser.getParsingReport(ramlParser);
    //TODO uncomment following lines when APIMF-303 is solved
    //    if (!report.conforms() || report.results().nonEmpty()) {
    //      log.info("File '" + fileName + "' is not a valid root RAML file. It contains some errors/warnings. See below: ");
    //      int problemCount = 0;
    //      final Iterator<AMFValidationResult> iterator = report.results().iterator();
    //      while (iterator.hasNext()) {
    //        log.info("ERROR " + (++problemCount) + ": " + iterator.next().message());
    //      }
    //      return false;
    //    }

    log.info("File '" + fileName + "' is a VALID root RAML file.");
    return true;
  }

  void collectResources(File filename, List<EndPoint> endPoints, String baseUri) {
    endPoints.forEach(endpoint -> endpoint.operations().forEach(operation -> {
      API api = apiFactory.createAPIBinding(filename, null, baseUri, APIKitTools.getPathFromUri(baseUri, false), null, null);

      final Request request = operation.request();
      boolean addGenericAction = false;
      if (request != null && !request.payloads().isEmpty()) {
        for (Payload payload : request.payloads()) {
          if (payload.schema() != null) {
            addResource(api, endpoint, operation, payload.mediaType());
          } else {
            addGenericAction = true;
          }
        }
      } else {
        addGenericAction = true;
      }

      if (addGenericAction) {
        addResource(api, endpoint, operation, null);
      }

    }));
  }

  void addResource(API api, EndPoint endpoint, Operation operation, String mimeType) {

    String completePath = APIKitTools
        .getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());

    ResourceActionMimeTypeTriplet resourceActionTriplet = new ResourceActionMimeTypeTriplet(api, completePath + endpoint.path(),
                                                                                            operation.method(),
                                                                                            mimeType);
    entries.put(resourceActionTriplet, new GenerationModel(api, endpoint, operation, mimeType));
  }

  public Map<ResourceActionMimeTypeTriplet, GenerationModel> getEntries() {
    return entries;
  }

}
