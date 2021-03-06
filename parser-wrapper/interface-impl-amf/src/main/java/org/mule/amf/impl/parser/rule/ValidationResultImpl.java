/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.rule;

import amf.client.validate.ValidationResult;
import amf.core.parser.Position;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.rule.Severity;

import java.net.URLDecoder;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mule.raml.interfaces.parser.rule.Severity.ERROR;

public class ValidationResultImpl implements IValidationResult {

  private static final String ERROR_FORMAT = "%s\n  Location: %s\n  Position: %s";
  private static final String POSITION_FORMAT = "Line %s,  Column %s";

  private ValidationResult validationResult;
  private List<String> severities;

  public ValidationResultImpl(ValidationResult validationResult) {
    this.validationResult = validationResult;
    severities = stream(Severity.values()).map(Enum::name).collect(toList());
  }

  public String getMessage() {
    return buildErrorMessage(validationResult.message(), validationResult.location().orElse(""),
                             validationResult.position().start());
  }

  public String getIncludeName() {
    return null;
  }

  public int getLine() {
    return -1;
  }

  public boolean isLineUnknown() {
    return false;
  }

  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Severity getSeverity() {
    if (!severities.contains(validationResult.level()))
      return ERROR;
    return Severity.fromString(validationResult.level());
  }

  private static String buildErrorMessage(String message, String location, Position startPosition) {
    return format(ERROR_FORMAT, message, URLDecoder.decode(location), getPositionMessage(startPosition));
  }

  private static String getPositionMessage(Position startPosition) {
    return format(POSITION_FORMAT, startPosition.line(), startPosition.column());
  }
}
