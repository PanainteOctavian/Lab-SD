import beans.StudentBean;
import dao.Studentdao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class StudentServlet extends HttpServlet {

    private final Studentdao dao = new Studentdao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {

                case "list":
                    showList(req, resp);
                    break;

                case "search":
                    doSearch(req, resp);
                    break;

                case "new":
                    req.getRequestDispatcher("./student-form.jsp").forward(req, resp);
                    break;

                case "edit":
                    showEditForm(req, resp);
                    break;

                case "delete":
                    deleteStudent(req, resp);
                    break;

                default:
                    resp.sendError(400, "Actiune necunoscuta: " + action);
            }
        } catch (SQLException e) {
            throw new ServletException("Eroare baza de date", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {

                case "insert":
                    insertStudent(req, resp);
                    break;

                case "update":
                    updateStudent(req, resp);
                    break;

                default:
                    resp.sendError(400, "Actiune POST necunoscuta: " + action);
            }
        } catch (SQLException e) {
            throw new ServletException("Eroare baza de date", e);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        List<StudentBean> lista = dao.findAll();
        req.setAttribute("studenti", lista);
        req.setAttribute("titlu", "Toti studentii");
        req.getRequestDispatcher("./student-list.jsp").forward(req, resp);
    }

    private void doSearch(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        String criteriu = req.getParameter("criteriu");
        String valoare  = req.getParameter("valoare");

        if (criteriu == null || criteriu.isEmpty()) criteriu = "nume";
        if (valoare  == null) valoare = "";

        List<StudentBean> lista = dao.search(criteriu, valoare);
        req.setAttribute("studenti", lista);
        req.setAttribute("titlu", "Rezultate cautare: \"" + valoare + "\" dupa " + criteriu);
        req.setAttribute("criteriu", criteriu);
        req.setAttribute("valoare",  valoare);
        req.getRequestDispatcher("./student-list.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        int id = parseId(req, resp);
        if (id < 0) return;

        StudentBean student = dao.findById(id);
        if (student == null) {
            resp.sendError(404, "Studentul cu id=" + id + " nu a fost gasit.");
            return;
        }

        req.setAttribute("student", student);
        req.getRequestDispatcher("./student-form.jsp").forward(req, resp);
    }

    private void deleteStudent(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        int id = parseId(req, resp);
        if (id < 0) return;

        dao.delete(id);
        resp.sendRedirect(req.getContextPath() + "/studenti?action=list");
    }

    private void insertStudent(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        StudentBean s = buildFromRequest(req);
        dao.insert(s);
        resp.sendRedirect(req.getContextPath() + "/studenti?action=list");
    }

    private void updateStudent(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        StudentBean s = buildFromRequest(req);
        s.setId(id);
        dao.update(s);
        resp.sendRedirect(req.getContextPath() + "/studenti?action=list");
    }

    private StudentBean buildFromRequest(HttpServletRequest req) {
        StudentBean s = new StudentBean();
        s.setNume   (req.getParameter("nume"));
        s.setPrenume(req.getParameter("prenume"));
        s.setVarsta (Integer.parseInt(req.getParameter("varsta")));
        return s;
    }

    private int parseId(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            return Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendError(400, "Parametrul 'id' lipseste sau este invalid.");
            return -1;
        }
    }
}