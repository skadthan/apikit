/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.config;

import org.mule.module.apikit.api.RamlHandler;
import org.mule.raml.interfaces.ParserType;

public interface ConsoleConfig {

  RamlHandler getRamlHandler();

  ParserType getParser();
}
