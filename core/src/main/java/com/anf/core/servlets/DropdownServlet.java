package com.anf.core.servlets;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;


@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_ID + "=" + "DropdownServlet",
                SLING_SERVLET_RESOURCE_TYPES + "=" + "/apps/anfcodechallenge/dropdown"
        }
)
public class DropdownServlet extends SlingSafeMethodsServlet {

   private static final long serialVersionUID = 4235730140092283425L;
    private static final String TAG = DropdownServlet.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            System.out.println("subrat");
            // Getting resource resolver from the current request
            ResourceResolver resourceResolver = request.getResourceResolver();
            // Get the resource object for the path from where the request is fired
            Resource currentResource = request.getResource();
            // Get the dropdown selector
            String dropdownSelector = Objects.requireNonNull(currentResource.getChild("datasource"))
                    .getValueMap()
                    .get("dropdownSelector", String.class);
            // Get json resource based on the dropdown selector
            Resource jsonResource = resourceResolver.getResource("/content/dam/anf-code-challenge/exercise-1/countries.json");
            // Converting this json resource to an Asset
            Asset asset = DamUtil.resolveToAsset(jsonResource);
            // Get the original rendition
            Rendition originalAsset = Objects.requireNonNull(asset).getOriginal();
            // Adapt this to InputStream
            InputStream content = Objects.requireNonNull(originalAsset).adaptTo(InputStream.class);
            // Read all the data in the json file as a string
            StringBuilder jsonContent = new StringBuilder();
            BufferedReader jsonReader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(content), StandardCharsets.UTF_8));
            // Loop through each line
            String line;
            while ((line = jsonReader.readLine()) != null) {
                jsonContent.append(line);
            }
            //JSONArray jsonArray = new JSONArray(jsonContent.toString());
            JSONObject jobj  = new JSONObject(jsonContent.toString());
            System.out.println(jobj);
            Iterator<String> keys = jobj.keys();
            Map<String, String> data = new TreeMap<>();
while(keys.hasNext()) {
    String key = keys.next();
    
          // do something with jsonObject here   
          data.put(key,jobj.get(key).toString());   
    
}
System.out.println(data.toString());
            
            //for (int i = 0; i < jsonArray.length(); i++) {
            //    data.put(jsonArray.getJSONObject(i).getString("text"),
            //            jsonArray.getJSONObject(i).getString("value"));
           // }
           
            // Creating the data source object
            @SuppressWarnings({"unchecked", "rawtypes"})
            DataSource ds = new SimpleDataSource(new TransformIterator<>(data.keySet().iterator(), (Transformer) o -> {
                String dropValue = (String) o;
                ValueMap vm = new ValueMapDecorator(new HashMap<>());
                vm.put("text", dropValue);
                vm.put("value", data.get(dropValue));
                return new ValueMapResource(resourceResolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, vm);
            }));
            request.setAttribute(DataSource.class.getName(), ds);
        } catch (IOException | JSONException e) {
            LOGGER.error("{}: exception occurred: {}", TAG, e.getMessage());
        }
    }

  
}