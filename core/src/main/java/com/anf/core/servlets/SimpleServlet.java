
package com.anf.core.servlets;

import com.day.cq.search.PredicateGroup;

import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet that fetches the  first 10 pages having anf
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="anf-code-challenge/components/page",
        methods=HttpConstants.METHOD_GET,
        extensions="txt")
@ServiceDescription("Simple Demo Servlet")
public class SimpleServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(SimpleServlet.class);
    @Reference
	private QueryBuilder builder;
    private Session session;
  
    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        
        resp.setContentType("text/plain");
        final PrintWriter out = resp.getWriter();
        
      try {
            final QueryManager queryManager = getQueryManager(req);
            log.info("Fetching the pages using JCR-SQL2");
            printNodes(out, executeQuery(queryManager,
            "SELECT parent.* FROM [cq:Page] AS parent INNER JOIN [cq:PageContent] AS child ON ISCHILDNODE(child,parent) WHERE ISDESCENDANTNODE(parent, '/content/anf-code-challenge/us/en') AND child.[anfCodeChallenge]  IS NOT NULL   ORDER BY parent.[jcr:created]"));
       

            out.println("Fetching the pages using querybuilder");
            Map<String, String> predicate = new HashMap<>();
            ResourceResolver resourceResolver = req.getResourceResolver();
            session = resourceResolver.adaptTo(Session.class);
			/**
			 * Configuring the Map for the predicate
			 */
			predicate.put("path", "/content/anf-code-challenge/us/en");
			predicate.put("type", "cq:page");
			predicate.put("property", "jcr:content/anfCodeChallenge");
			predicate.put("property.value", "true");
			predicate.put("orderby", "@jcr:created");
            predicate.put("p.limit", "10");
            com.day.cq.search.Query query = builder.createQuery(PredicateGroup.create(predicate), session);
			
			
			
			/**
			 * Getting the search results
			 */
			SearchResult searchResult = query.getResult();
			
			for(Hit hit : searchResult.getHits()) {
				
				String path = hit.getPath();
				
				resp.getWriter().println(path);
			}
       
       
       
        } catch (final Exception e) {
                log.info(e.getMessage());
                out.println("Error during execution: ");
           
            }
          
    }
          
          private QueryManager getQueryManager(final SlingHttpServletRequest request) throws RepositoryException {
          
                 return request.getResourceResolver().adaptTo(Session.class).getWorkspace().getQueryManager();
            }
          
            private void printNodes(final PrintWriter out, final QueryResult result) throws RepositoryException {
          
                final NodeIterator nodes = result.getNodes();
                while (nodes.hasNext()) {
                    out.println(nodes.nextNode().getPath());
                 }
          
            }
          
            private QueryResult executeQuery(final QueryManager queryManager, final String queryString) throws RepositoryException {
          
            final Query query = queryManager.createQuery(queryString, "JCR-SQL2");
                query.setLimit(10);
                return query.execute();
            }
           
    }

