/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

import static org.mule.module.apikit.AbstractConfiguration.APPLICATION_RAML;

import org.mule.common.metadata.datatype.DataTypeFactory;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.Event;
//import org.mule.runtime.core.api.MuleEvent;
import org.mule.runtime.core.api.MuleException;
//import org.mule.transformer.types.DataTypeFactory;
import org.mule.compatibility.transport.http.HttpConstants;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.module.http.internal.ParameterMap;

import java.util.HashMap;
import java.util.Map;

import org.raml.model.ActionType;

public class RamlDescriptorHandler
{

    private AbstractConfiguration config;

    public RamlDescriptorHandler(AbstractConfiguration config)
    {
        this.config = config;
    }

    public boolean handles(HttpRestRequest request)
    {
        String path = request.getResourcePath();
        return (!config.isParserV2() && isValidPath(path) &&
                ActionType.GET.toString().equals(request.getMethod().toUpperCase()) &&
                request.getAdapter().getAcceptableResponseMediaTypes().contains(APPLICATION_RAML));
    }

    private boolean isValidPath(String path)
    {
        if (config instanceof Configuration && ((Configuration) config).isConsoleEnabled())
        {
            if (path.equals("/" + ((Configuration) config).getConsolePath()))
            {
                return true;
            }
        }
        return path.equals(config.getApi().getUri());
    }

    public Event processConsoleRequest(Event event) throws MuleException
    {
        return process(event, config.getApikitRamlConsole(event));
    }

    public Event processRouterRequest(Event event) throws MuleException
    {
        return process(event, config.getApikitRaml(event));
    }

    private Event process(Event event, String raml) throws MuleException
    {
        Map<String,String> headers = (Map<String, String>) event.getVariable("_outboundHeaders_");
        headers.put(HttpConstants.HEADER_CONTENT_TYPE, APPLICATION_RAML);
        headers.put(HttpConstants.HEADER_EXPIRES, "-1");//avoid IE ajax response caching
        headers.put(HttpConstants.HEADER_CONTENT_LENGTH, Integer.toString(raml.length()));
        headers.put("Access-Control-Allow-Origin", "*");

        event = EventHelper.addOutboundProperties(event,headers);
        event = EventHelper.setPayload(event, raml, "application", "raml+yaml");
        return event;
    }
}
