import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AlarmViewServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        Path path = Paths.get("/home/octavian/Documents/Facultate/SD/lab2/alarm.html");
        if (Files.exists(path)) {
            byte[] content = Files.readAllBytes(path);
            response.getOutputStream().write(content);
        } else {
            response.getWriter().print("<h1>Pagina de alarme nu a fost generata inca.</h1>");
        }
    }
}