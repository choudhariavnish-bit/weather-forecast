package com.weatherapp.servlet;

import com.weatherapp.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/me")
public class MeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws IOException {
        try {
            HttpSession x = requireSession(r, s);
            Object name = x.getAttribute("UserName");
            Object cityId = x.getAttribute("UserCityID");
            Map<String, Object> map = new HashMap<>();
            map.put("name", name != null ? name : "User");
            map.put("cityId", cityId != null ? cityId : 1);
            Json.write(s, map);
        } catch (Exception e) {
            s.setStatus(401);
            Json.write(s, Map.of("message", e.getMessage() != null ? e.getMessage() : "Unauthorized"));
        }
    }
}
