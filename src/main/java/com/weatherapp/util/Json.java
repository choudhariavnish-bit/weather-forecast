package com.weatherapp.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
public final class Json { public static final ObjectMapper M=new ObjectMapper(); private Json(){} public static void write(HttpServletResponse r,Object o)throws IOException{r.setContentType("application/json;charset=UTF-8");r.getWriter().write(M.writeValueAsString(o));} public static String body(jakarta.servlet.http.HttpServletRequest req)throws IOException{return req.getReader().lines().reduce("",(a,b)->a+b);} }
