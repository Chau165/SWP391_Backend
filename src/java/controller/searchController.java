package controller;

import DAO.StationDAO;
import DTO.Station;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/searchStation")
public class searchController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lấy từ khóa tìm kiếm từ query parameter
        String keyword = req.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        }

        // Gọi DAO để lấy danh sách trạm
        StationDAO dao = new StationDAO();
        List<Station> stations = dao.searchStation(keyword);

        // Chuyển sang JSON bằng Gson
        Gson gson = new Gson();
        String json = gson.toJson(stations);

        // Trả về response JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
            out.flush();
        }
    }
}
