/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import static org.mule.runtime.core.api.extension.MuleExtensionModelProvider.MULE_NAME;
import org.mule.runtime.app.declaration.api.ConfigurationElementDeclaration;
import org.mule.runtime.app.declaration.api.ConstructElementDeclaration;

/**
 * Helper class that tells if a given XML Component is some of the valid APIKit
 * XML Elements.
 *
 * Related Clases:
 * {@link org.mule.module.apikit.metadata.model.ApikitConfig}
 * {@link org.mule.module.apikit.metadata.model.Flow}
 * {@link org.mule.module.apikit.metadata.model.FlowMapping}
 */
public class ApikitElementIdentifiers {

  private ApikitElementIdentifiers() {}

  private static final String FLOW = "flow";

  private static final String APIKIT_EXTENSION_NAME = "APIKit";

  private static final String CONFIG = "config";

  public static boolean isFlow(ConstructElementDeclaration constructElementDeclaration) {
    return constructElementDeclaration.getName().equals(FLOW)
        && constructElementDeclaration.getDeclaringExtension().equals(MULE_NAME);
  }

  public static boolean isApikitConfig(ConfigurationElementDeclaration configurationElementDeclaration) {
    return configurationElementDeclaration.getName().equals(CONFIG)
        && configurationElementDeclaration.getDeclaringExtension().equals(APIKIT_EXTENSION_NAME);
  }

}
