package com.weatherapp.servlet;
import com.weatherapp.db.Database;
import com.weatherapp.config.AppConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
public abstract class BaseServlet extends HttpServlet {
 @Override public void init(ServletConfig c)throws ServletException{super.init(c);try{AppConfig.initialize(c.getServletContext());Database.init(c.getServletContext());}catch(Exception e){throw new ServletException(e);}}
 protected HttpSession requireSession(HttpServletRequest req,HttpServletResponse res)throws Exception{HttpSession s=req.getSession(false);if(s==null||s.getAttribute("UserID")==null){res.setStatus(401);throw new Exception("Login required.");}return s;}
}
