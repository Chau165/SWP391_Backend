package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS Filter to allow cross-origin requests from frontend (localhost:5173)
 * This filter adds necessary CORS headers to all HTTP responses
 */
@WebFilter(filterName = "CORSFilter", urlPatterns = {"/*"})
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("CORSFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow requests from Frontend running on localhost:5173
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        
        // Allow credentials (cookies, authorization headers)
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        
        // Allow specific HTTP methods
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // Allow specific headers
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        
        // Cache preflight response for 1 hour (3600 seconds)
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Continue with the next filter or servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("CORSFilter destroyed");
    }
}
