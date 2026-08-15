import beans.StudentBean;
import dao.Studentdao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
public class Exportjsonservlet extends HttpServlet {

    private final Studentdao dao = new Studentdao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<StudentBean> studenti;
        try {
            studenti = dao.findAll();
        } catch (SQLException e) {
            throw new ServletException("Eroare la citirea studentilor din DB", e);
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"studenti.json\"");

        PrintWriter out = resp.getWriter();
        out.println(toJson(studenti));
    }

    private String toJson(List<StudentBean> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < lista.size(); i++) {
            sb.append("  ").append(studentToJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private String studentToJson(StudentBean s) {
        return String.format(
                "{\"id\":%d,\"nume\":\"%s\",\"prenume\":\"%s\",\"varsta\":%d}",
                s.getId(),
                escapeJson(s.getNume()),
                escapeJson(s.getPrenume()),
                s.getVarsta()
        );
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}