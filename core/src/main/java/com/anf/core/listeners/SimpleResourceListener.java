/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anf.core.listeners;

import java.util.HashMap;

import java.util.List;
import java.util.Map;


import javax.jcr.Node;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A service to demonstrate how changes in the resource tree
 * can be listened for. It registers an event handler service.
 * The component is activated immediately after the bundle is
 * started through the immediate flag.
 * Please note, that apart from EventHandler services,
 * the immediate flag should not be set on a service.
 */
@Component(service = ResourceChangeListener.class,
           immediate = true,
           property = {
                   ResourceChangeListener.PATHS+"=/content/anf-code-challenge/us/en",
                   ResourceChangeListener.CHANGES+"=ADDED"
                })
@ServiceDescription("Demo to listen on changes in the resource tree")
public class SimpleResourceListener implements ResourceChangeListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Reference
    private ResourceResolverFactory resolverFactory;

  
        @Override
        public void onChange(List<ResourceChange> list) {
           
            final Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put( ResourceResolverFactory.SUBSERVICE, "customSystemuser" );
    
            // fetches the admin service resolver using service user.
          
            for(ResourceChange rc: list){
             try{
                ResourceResolver resolver = resolverFactory.getServiceResourceResolver(paramMap);
                Resource resource=resolver.getResource(rc.getPath());
                Node node=resource.adaptTo(Node.class);
                node.setProperty("pageCreated",true);
                resolver.commit();
             }catch(Exception e){
                logger.error("cant save property");
             }
            }
        }
}

