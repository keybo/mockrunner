package com.mockrunner.servlet;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import com.mockrunner.base.HTMLOutputModule;
import com.mockrunner.base.VerifyFailedException;
import com.mockrunner.mock.web.WebMockObjectFactory;

/**
 * Module for servlet and filter tests. Can test
 * single servlets and filters and simulate a filter
 * chain.
 */
public class ServletTestModule extends HTMLOutputModule
{
    private WebMockObjectFactory mockFactory;
    private HttpServlet servlet;
    private boolean doChain;
    private boolean caseSensitive;
      
    public ServletTestModule(WebMockObjectFactory mockFactory)
    {
        this.mockFactory = mockFactory;
        doChain = false;
        caseSensitive = true;
    }
    
    /**
     * Set if {@link #verifyOutput} and {@link #verifyOutputContains}
     * should compare case sensitive. Default is <code>true</code>.
     * @param caseSensitive enable or disable case sensitivity
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }
    
    /**
     * Creates a servlet and initializes it. <i>servletClass</i> must
     * be of the type <code>HttpServlet</code>, otherwise a
     * <code>RuntimeException</code> will be thrown.
     * Sets the specified servlet as the current servlet and
     * initializes the filter chain with it.
     * @param servletClass the class of the servlet
     * @return instance of <code>HttpServlet</code>
     * @throws RuntimeException if <code>servletClass</code> is not an
     *         instance of <code>HttpServlet</code>
     */
    public HttpServlet createServlet(Class servletClass)
    {
        if(!HttpServlet.class.isAssignableFrom(servletClass))
        {
            throw new RuntimeException("servletClass must be an instance of javax.servlet.http.HttpServlet");
        }
        try
        {
            servlet = (HttpServlet)servletClass.newInstance();
            servlet.init(mockFactory.getMockServletConfig());
            mockFactory.getMockFilterChain().setServlet(servlet);
            return servlet;
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            throw new RuntimeException(exc.getMessage());
        }
    }
    
    /**
     * Returns the current servlet.
     * @return the servlet
     */
    public HttpServlet getServlet()
    {
        return servlet;
    }
    
    /**
     * Creates a filter, initializes it and adds it to the
     * filter chain. <i>filterClass</i> must be of the type 
     * <code>Filter</code>, otherwise a <code>RuntimeException</code> 
     * will be thrown. You can loop through the filter chain with
     * {@link #doFilter}. If you set <i>doChain</i> to
     * <code>true</code> every call of one of the servlet methods 
     * will go through the filter chain before calling the servlet 
     * method.
     * @param filterClass the class of the filter
     * @return instance of <code>Filter</code>
     * @throws RuntimeException if <code>filterClass</code> is not an
     *         instance of <code>Filter</code>
     */
    public Filter createFilter(Class filterClass)
    {
        if(!Filter.class.isAssignableFrom(filterClass))
        {
            throw new RuntimeException("filterClass must be an instance of javax.servlet.Filter");
        }
        try
        {
            Filter theFilter = (Filter)filterClass.newInstance();
            theFilter.init(mockFactory.getMockFilterConfig());
            mockFactory.getMockFilterChain().addFilter(theFilter);
            return theFilter;
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            throw new RuntimeException(exc.getMessage());
        }
    }
    
    /**
     * Adds the specified filter it to the filter chain without
     * initializing it. 
     * You have to set the <code>FilterConfig</code> on your own.
     * Usually you can use 
     * {@link com.mockrunner.mock.web.WebMockObjectFactory#getMockFilterConfig}.
     * @param filter the filter
     */
    public void addFilter(Filter filter)
    {
        mockFactory.getMockFilterChain().addFilter(filter);
    }
    
    /**
     * Deletes all filters in the filter chain.
     */
    public void releaseFilters()
    {
        mockFactory.getMockFilterChain().release();
        mockFactory.getMockFilterChain().setServlet(servlet);
    }

    /**
     * If <code>doChain</code> is set to <code>true</code>
     * (default is <code>false</code>) every call of
     * one of the servlet methods will go through the filter chain
     * before calling the servlet method.
     * @param doChain <code>true</code> if the chain should be called
     */
    public void setDoChain(boolean doChain)
    {
        this.doChain = doChain;
    }
    
    /**
     * Adds an empty request parameter. Same as
     * <code>addRequestParameter(key, "")</code>.
     * @param key the request key
     */
    public void addRequestParameter(String key)
    {
        addRequestParameter(key, "");
    }

    /**
     * Adds a request parameter.
     * @param key the request key
     * @param value the request value
     */
    public void addRequestParameter(String key, String value)
    {
        mockFactory.getMockRequest().setupAddParameter(key, value);
    }
    
    /**
     * Loops through the filter chain and calls the current servlets
     * <code>service</code> method at the end (only if a current servlet
     * is set). You can use it to test single filters or the interaction 
     * of filters and servlets.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * this method is called before any call of a servlet method. If a filter
     * does not call it's chains <code>doFilter</code> method, the chain
     * breaks and the servlet will not be called (just like it in the
     * real container).
     */
    public void doFilter()
    {
        try
        {
            mockFactory.getMockFilterChain().doFilter(mockFactory.getWrappedRequest(), mockFactory.getWrappedResponse());
        }
        catch(Exception exc)
        {
            exc.printStackTrace();
            throw new RuntimeException(exc.getMessage());
        }
    }
    
    /**
     * Calls the current servlets <code>init</code> method. Is automatically
     * done when calling {@link #createServlet}.
     */
    public void init()
    {
        try
        {
            servlet.init();
        }
        catch(ServletException exc)
        {
            exc.printStackTrace();
            throw new RuntimeException(exc.getMessage());
        }
    }
    
    /**
     * Calls the current servlets <code>doDelete</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>doDelete</code>.
     */
    public void doDelete()
    {
        mockFactory.getMockRequest().setMethod("DELETE");
        callService();
    }
    
    /**
     * Calls the current servlets <code>doGet</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>doGet</code>.
     */          
    public void doGet()
    {
        mockFactory.getMockRequest().setMethod("GET");
        callService();
    }
    
    /**
     * Calls the current servlets <code>doOptions</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>doOptions</code>.
     */          
    public void doOptions()
    {
        mockFactory.getMockRequest().setMethod("OPTIONS");
        callService();
    }
    
    /**
     * Calls the current servlets <code>doPost</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>doPost</code>.
     */         
    public void doPost()
    {
        mockFactory.getMockRequest().setMethod("POST");
        callService();
    }
    
    /**
     * Calls the current servlets <code>doPut</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>doPut</code>.
     */         
    public void doPut()
    {
        mockFactory.getMockRequest().setMethod("PUT");
        callService();
    }
    
    /**
     * Calls the current servlets <code>doTrace</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>doTrace</code>.
     */          
    public void doTrace()
    {
        mockFactory.getMockRequest().setMethod("TRACE");
        callService();
    }
    
    /**
     * Calls the current servlets <code>service</code> method.
     * If you set <i>doChain</i> to <code>true</code> (use {@link #setDoChain}),
     * the filter chain will be called before <code>service</code>.
     */          
    public void service()
    {
        callService();
    }
    
    /**
     * Returns the last request from the filter chain. Since
     * filters can replace the request with a request wrapper,
     * this method makes only sense after calling at least
     * one filter, i.e. after calling {@link #doFilter} or
     * after calling one servlet method with <i>doChain</i> 
     * set to <code>true</code>.
     * @return the filtered request
     */  
    public ServletRequest getFilteredRequest()
    {
        return mockFactory.getMockFilterChain().getLastRequest();
    }
    
    /**
     * Returns the last resposne from the filter chain. Since
     * filters can replace the resposne with a resposne wrapper,
     * this method makes only sense after calling at least
     * one filter, i.e. after calling {@link #doFilter} or
     * after calling one servlet method with <i>doChain</i> 
     * set to <code>true</code>.
     * @return the filtered resposne
     */  
    public ServletResponse getFilteredResponse()
    {
        return mockFactory.getMockFilterChain().getLastResponse();
    }
    
    /**
     * Returns the servlet output as a string. Flushes the output
     * before returning it.
     * @return the servlet output
     */
    public String getOutput()
    {
        try
        {
            mockFactory.getMockResponse().getWriter().flush();    
        }
        catch(Exception exc)
        {
        
        }
        return mockFactory.getMockResponse().getOutputStreamContent();
    }
    
    /**
     * Clears the output content
     */ 
    public void clearOutput()
    {
        mockFactory.getMockResponse().resetBuffer();
    }
   
    /**
     * Verifies the servlet output.
     * @param output the expected output.
     * @throws VerifyFailedException if verification fails
     */  
    public void verifyOutput(String output)
    {
        String actualOutput = getOutput();
        if(!caseSensitive)
        {
            output = output.toLowerCase();
            actualOutput = actualOutput.toLowerCase();
        }
        if(!output.equals(actualOutput))
        {
            throw new VerifyFailedException("actual output: " + actualOutput + " does not match expected output");
        }
    }
    
    /**
     * Verifies if the servlet output contains the specified data.
     * @param output the data
     * @throws VerifyFailedException if verification fails
     */   
    public void verifyOutputContains(String output)
    {
        String actualOutput = getOutput();
        if(!caseSensitive)
        {
            output = output.toLowerCase();
            actualOutput = actualOutput.toLowerCase();
        }
        if(-1 == actualOutput.indexOf(output))
        {
            throw new VerifyFailedException("actual output: " + actualOutput + " does not match expected output");
        }
    }
    
    private void callService()
    {
        try
        {
            if(doChain)
            { 
                doFilter(); 
            }
            else
            {
                servlet.service(mockFactory.getWrappedRequest(), mockFactory.getWrappedResponse());
            }            
        }
        catch(Exception exc)
        {
            exc.printStackTrace();
            throw new RuntimeException(exc.getMessage());
        }
    }  
}
