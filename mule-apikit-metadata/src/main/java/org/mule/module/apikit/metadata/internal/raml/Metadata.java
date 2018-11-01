/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import static org.mule.apikit.common.APISyncUtils.isExchangeModules;
import static org.mule.apikit.common.APISyncUtils.toApiSyncResource;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.module.apikit.metadata.internal.model.ApplicationModelWrapper;
import org.mule.runtime.config.internal.model.ApplicationModel;

import java.util.Optional;

public class Metadata implements org.mule.module.apikit.metadata.api.Metadata {

  private MetadataHandler metadataHandler;

  public Metadata(ApplicationModel applicationModel, ResourceLoader resourceLoader, Notifier notifier) {
    init(applicationModel, resourceLoader, notifier);
  }

  private void init(ApplicationModel applicationModel, ResourceLoader resourceLoader, Notifier notifier) {

    final RamlHandler ramlHandler = new RamlHandler(resourceLoader, notifier);
    final ApplicationModelWrapper wrapper = new ApplicationModelWrapper(applicationModel, ramlHandler, notifier);
    metadataHandler = new MetadataHandler(wrapper, notifier);
  }

  /**
   * Gets the metadata for a Flow
   * @param flowName Name of the flow
   * @return The Metadata
   */
  public Optional<FunctionType> getMetadataForFlow(String flowName) {
    return metadataHandler.getMetadataForFlow(flowName);
  }


  /**
   * Builder for Metadata module
   */
  public static class Builder {

    private ResourceLoader resourceLoader;
    private ApplicationModel applicationModel;
    private Notifier notifier;

    public Builder() {

    }

    public Builder withResourceLoader(ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;
      return this;
    }

    public Builder withApplicationModel(ApplicationModel applicationModel) {
      this.applicationModel = applicationModel;
      return this;
    }

    public Builder withNotifier(Notifier notifier) {
      this.notifier = notifier;
      return this;
    }

    public Metadata build() {
      return new Metadata(applicationModel, s -> {
        if (isExchangeModules(s)) {
          String apiSyncResource = toApiSyncResource(s);
          if (apiSyncResource != null)
            return resourceLoader.getResource(apiSyncResource);
        }
        return resourceLoader.getResource(s);
      }, notifier);

    }

  }

}