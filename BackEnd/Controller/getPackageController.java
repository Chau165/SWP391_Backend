package controller;

import DAO.PackageDAO;
import DTO.Package;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/getpackages")
public class getPackageController extends HttpServlet {

    private final PackageDAO packageDAO = new PackageDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        List<Package> packages = packageDAO.getAllPackage();

        try ( PrintWriter out = resp.getWriter()) {
            if (packages.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.print("{\"status\":\"fail\",\"message\":\"No packages found\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                String json = gson.toJson(packages);
                out.print(json);
            }
        }
    }
}
