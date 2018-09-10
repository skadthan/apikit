/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.interfaces.Parseable;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.core.api.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

public class RamlHandler {

  private static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

  private final ResourceLoader resourceLoader;
  private final Notifier notifier;

  public RamlHandler(ResourceLoader resourceLoader, Notifier notifier) {
    this.resourceLoader = resourceLoader;
    this.notifier = notifier;
  }

  public Optional<IRaml> getRamlApi(String uri) {
    try {

      if (StringUtils.isEmpty(uri)) {
        notifier.error("RAML document is undefined.");
        return empty();
      }

      final InputStream resource = resourceLoader.getRamlResource(uri);

      if (resource == null) {
        notifier.error(format("RAML document '%s' not found.", uri));
        return empty();
      }

      final String content = getRamlContent(resource);
      final Parseable parser = getParser(content);

      return of(parser.build(resource, content));
    } catch (IOException e) {
      notifier.error(format("Error reading RAML document '%s'. Detail: %s", uri, e.getMessage()));
    }

    return empty();
  }

  private Parseable getParser(String ramlContent) {
    return useParserV2(ramlContent) ? new RamlV2Parser() : new RamlV1Parser();
  }

  private String getRamlContent(InputStream inputStream) throws IOException {
    try (final InputStream is = inputStream) {
      return IOUtils.toString(is);
    }
  }

  private static boolean useParserV2(String content) {
    return getBoolean(PARSER_V2_PROPERTY) || content.startsWith("#%RAML 1.0");
  }
}
