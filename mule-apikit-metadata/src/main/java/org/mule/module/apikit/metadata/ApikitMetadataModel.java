/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mule.apikit.common.CollectionUtils.merge;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.model.ApikitConfig;
import org.mule.module.apikit.metadata.model.Flow;
import org.mule.module.apikit.metadata.model.FlowMapping;
import org.mule.module.apikit.metadata.model.RamlCoordinate;
import org.mule.module.apikit.metadata.raml.RamlCoordsSimpleFactory;
import org.mule.module.apikit.metadata.raml.RamlHandler;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.app.declaration.api.ArtifactDeclaration;
import org.mule.runtime.app.declaration.api.ConfigurationElementDeclaration;
import org.mule.runtime.app.declaration.api.ConstructElementDeclaration;
import org.mule.runtime.app.declaration.api.ParameterElementDeclaration;
import org.mule.runtime.app.declaration.api.ParameterGroupElementDeclaration;
import org.mule.runtime.app.declaration.api.ParameterValueVisitor;
import org.mule.runtime.app.declaration.api.fluent.ParameterListValue;
import org.mule.runtime.app.declaration.api.fluent.ParameterObjectValue;
import org.mule.runtime.app.declaration.api.fluent.ParameterSimpleValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class ApikitMetadataModel {

  private final static String PARAMETER_NAME = "name";
  private final static String PARAMETER_RAML = "raml";
  private final static String PARAMETER_OUTPUT_HEADERS_VAR = "outboundHeadersMapName";
  private final static String PARAMETER_HTTP_STATUS_VAR = "httpStatusVarName";
  private final static String PARAMETER_RESOURCE = "resource";
  private final static String PARAMETER_ACTION = "action";
  private final static String PARAMETER_CONTENT_TYPE = "content-type";
  private final static String PARAMETER_FLOW_REF = "flow-ref";

  private ArtifactDeclaration artifactDeclaration;
  private RamlHandler ramlHandler;
  private Notifier notifier;

  private Map<String, ApikitConfig> configMap;
  private Map<String, RamlCoordinate> metadataFlows;

  public ApikitMetadataModel(ArtifactDeclaration artifactDeclaration, RamlHandler ramlHandler, Notifier notifier) {
    this.artifactDeclaration = artifactDeclaration;
    this.ramlHandler = ramlHandler;
    this.notifier = notifier;
    initialize();
  }

  private void initialize() {
    loadConfigs();
    loadFlows();
  }

  private void loadFlows() {
    // Finding all valid flows
    final List<Flow> flows = findFlows();

    // Creating a Coords Factory, giving the list of all valid config names
    final RamlCoordsSimpleFactory coordsFactory = new RamlCoordsSimpleFactory(getConfigNames());
    final Map<String, RamlCoordinate> conventionCoordinates = createCoordinatesForConventionFlows(flows, coordsFactory);
    final Map<String, RamlCoordinate> flowMappingCoordinates = createCoordinatesForMappingFlows(flows, coordsFactory);

    // Merging both results
    metadataFlows = merge(asList(conventionCoordinates, flowMappingCoordinates));
  }

  private void loadConfigs() {
    configMap = artifactDeclaration.getGlobalElements().stream()
        .filter(declaration -> declaration instanceof ConfigurationElementDeclaration)
        .map(declaration -> (ConfigurationElementDeclaration) declaration)
        .filter(ApikitElementIdentifiers::isApikitConfig)
        .map(this::createApikitConfig)
        .collect(toMap(ApikitConfig::getName, identity()));
  }

  private Set<String> getConfigNames() {
    return configMap.keySet();
  }

  public Collection<ApikitConfig> getConfigurations() {
    return configMap.values();
  }

  private Map<String, RamlCoordinate> createCoordinatesForMappingFlows(List<Flow> flows, RamlCoordsSimpleFactory coordsFactory) {
    final Set<String> flowNames = flows.stream().map(Flow::getName).collect(toSet());

    return configMap.values().stream()
        .flatMap(config -> config.getFlowMappings().stream())
        .filter(mapping -> flowNames.contains(mapping.getFlowRef()))
        .map(coordsFactory::createFromFlowMapping)
        .collect(toMap(RamlCoordinate::getFlowName, identity()));
  }

  private Map<String, RamlCoordinate> createCoordinatesForConventionFlows(final List<Flow> flows,
                                                                          final RamlCoordsSimpleFactory coordsFactory) {
    return flows
        .stream()
        .map(flow -> coordsFactory.createFromFlowName(flow.getName()))
        .filter(Optional::isPresent).map(Optional::get)
        .collect(toMap(RamlCoordinate::getFlowName, identity()));
  }

  private ApikitConfig createApikitConfig(ConfigurationElementDeclaration configurationElementDeclaration) {
    // Default parameter group, this should be changed if the extension model is changed
    ParameterGroupElementDeclaration parameterGroupElementDeclaration =
        configurationElementDeclaration.getParameterGroups().get(0);

    Function<ParameterElementDeclaration, String> parameterElementDeclarationStringFunction = parameterElementDeclaration -> {
      CollectParameterValueVisitor collectParameterValueVisitor = new CollectParameterValueVisitor();
      parameterElementDeclaration.getValue().accept(collectParameterValueVisitor);
      return collectParameterValueVisitor.getValue();
    };

    Optional<String> configName =
        parameterGroupElementDeclaration.getParameter(PARAMETER_NAME).map(parameterElementDeclarationStringFunction);
    Optional<String> configRaml =
        parameterGroupElementDeclaration.getParameter(PARAMETER_RAML).map(parameterElementDeclarationStringFunction);
    Optional<String> outputHeadersVarName = parameterGroupElementDeclaration.getParameter(PARAMETER_OUTPUT_HEADERS_VAR)
        .map(parameterElementDeclarationStringFunction);
    Optional<String> httpStatusVarName =
        parameterGroupElementDeclaration.getParameter(PARAMETER_HTTP_STATUS_VAR).map(parameterElementDeclarationStringFunction);

    Optional<ParameterListValue> flowMappingsParameterListValue =
        parameterGroupElementDeclaration.getParameter("flowMappings").map(parameterElementDeclaration -> {
          CollectParameterValueVisitor collectParameterValueVisitor = new CollectParameterValueVisitor();
          parameterElementDeclaration.getValue().accept(collectParameterValueVisitor);
          return collectParameterValueVisitor.getList();
        });

    Optional<List<FlowMapping>> flowMappings = flowMappingsParameterListValue.map(parameterListValue -> {
      return parameterListValue.getValues().stream().map(parameterValue -> {
        CollectParameterValueVisitor collectParameterValueVisitor = new CollectParameterValueVisitor();
        parameterValue.accept(collectParameterValueVisitor);
        ParameterObjectValue parameterObjectValue = collectParameterValueVisitor.getParameterObjectValue();

        // flow-mapping
        collectParameterValueVisitor = new CollectParameterValueVisitor();
        parameterObjectValue.getParameters().get(PARAMETER_RESOURCE).accept(collectParameterValueVisitor);
        String resource = collectParameterValueVisitor.getValue();

        collectParameterValueVisitor = new CollectParameterValueVisitor();
        parameterObjectValue.getParameters().get(PARAMETER_ACTION).accept(collectParameterValueVisitor);
        String action = collectParameterValueVisitor.getValue();

        collectParameterValueVisitor = new CollectParameterValueVisitor();
        parameterObjectValue.getParameters().get(PARAMETER_CONTENT_TYPE).accept(collectParameterValueVisitor);
        String contentType = collectParameterValueVisitor.getValue();

        collectParameterValueVisitor = new CollectParameterValueVisitor();
        parameterObjectValue.getParameters().get(PARAMETER_FLOW_REF).accept(collectParameterValueVisitor);
        String flowRef = collectParameterValueVisitor.getValue();
        return new FlowMapping(configName.get(), resource, action, contentType, flowRef);
      }).collect(toList());
    });

    final RamlHandlerSupplier ramlSupplier = RamlHandlerSupplier.create(configRaml.get(), ramlHandler);

    return new ApikitConfig(configName.get(), configRaml.get(), flowMappings.get(), ramlSupplier, httpStatusVarName.get(),
                            outputHeadersVarName.get(),
                            notifier);
  }

  private class CollectParameterValueVisitor implements ParameterValueVisitor {

    private String value;
    private ParameterListValue list;
    private ParameterObjectValue parameterObjectValue;

    @Override
    public void visitSimpleValue(ParameterSimpleValue text) {
      this.value = text.getValue();
    }

    public String getValue() {
      return value;
    }

    @Override
    public void visitListValue(ParameterListValue list) {
      this.list = list;
    }

    public ParameterListValue getList() {
      return list;
    }

    @Override
    public void visitObjectValue(ParameterObjectValue parameterObjectValue) {
      this.parameterObjectValue = parameterObjectValue;
    }

    public ParameterObjectValue getParameterObjectValue() {
      return parameterObjectValue;
    }
  }

  private static class RamlHandlerSupplier implements Supplier<Optional<IRaml>> {

    private String configRaml;
    private RamlHandler handler;

    private RamlHandlerSupplier(String configRaml, RamlHandler handler) {
      this.configRaml = configRaml;
      this.handler = handler;
    }

    private static RamlHandlerSupplier create(String configRaml, RamlHandler handler) {
      return new RamlHandlerSupplier(configRaml, handler);
    }

    @Override
    public Optional<IRaml> get() {
      return handler.getRamlApi(configRaml);
    }
  }

  public List<Flow> findFlows() {
    return artifactDeclaration.getGlobalElements().stream()
        .filter(globalElementDeclaration -> globalElementDeclaration instanceof ConstructElementDeclaration)
        .map(globalElementDeclaration -> (ConstructElementDeclaration) globalElementDeclaration)
        .filter(ApikitElementIdentifiers::isFlow)
        .map(this::createFlow)
        .collect(toList());
  }

  private Flow createFlow(ConstructElementDeclaration constructElementDeclaration) {
    return new Flow(constructElementDeclaration.getRefName());
  }

  public Optional<RamlCoordinate> getRamlCoordinatesForFlow(String flowName) {
    return ofNullable(metadataFlows.get(flowName));
  }

  public Optional<ApikitConfig> getConfig(String configName) {
    if (configMap.isEmpty()) {
      return empty();
    }

    // If the flow is not explicitly naming the config it belongs, we assume there is only one API
    return Optional.of(configMap.getOrDefault(configName, configMap.values().iterator().next()));
  }


}
