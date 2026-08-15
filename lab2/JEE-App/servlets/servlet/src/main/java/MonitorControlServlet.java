import monitorization.DatabaseMonitor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MonitorControlServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        DatabaseMonitor monitor = DatabaseMonitor.getInstance();

        StringBuilder html = new StringBuilder();
        html.append("<html><head>")
                .append("<title>Control Monitor</title>")
                .append("<meta charset='UTF-8'>")
                .append("</head><body>")
                .append("<h1>Control Monitor Baza Date</h1>")
                .append("<hr>");

        if ("start".equals(action)) {
            try {
                int minAge = Integer.parseInt(request.getParameter("minAge"));
                int maxAge = Integer.parseInt(request.getParameter("maxAge"));
                int maxLen = Integer.parseInt(request.getParameter("maxNameLength"));

                monitor.startMonitor(minAge, maxAge, maxLen);
                html.append("<p><b>Monitor pornit cu succes</b></p>");
            } catch (NumberFormatException e) {
                html.append("<p><b>Eroare: Introduceți numere valide</b></p>");
            }
        }else if ("stop".equals(action)) {
            monitor.stopMonitoring();
            html.append("<p><b>Monitor oprit</b></p>");
        } else if ("update".equals(action)) {
            try {
                int minAge = Integer.parseInt(request.getParameter("minAge"));
                int maxAge = Integer.parseInt(request.getParameter("maxAge"));
                int maxLen = Integer.parseInt(request.getParameter("maxNameLength"));

                monitor.setMonitoringRanges(minAge, maxAge, maxLen);
                html.append("<p><b>Intervale actualizate</b></p>");
            } catch (NumberFormatException e) {
                html.append("<p><b>Eroare: Parametri invalizi</b></p>");
            }
        }

        html.append("<p><b>Status:</b> ")
                .append(monitor.isRunning() ? "RULEAZA" : "OPRIT")
                .append("</p>")
                .append("<hr>")
                .append("<h3>Pornire monitor:</h3>")
                .append("<form action='./monitor-control' method='get'>")
                .append("<input type='hidden' name='action' value='start'>")
                .append("Varsta minimă: <input type='number' name='minAge' value='18'><br>")
                .append("Varsta maximă: <input type='number' name='maxAge' value='30'><br>")
                .append("Lungime max. nume: <input type='number' name='maxNameLength' value='10'><br><br>")
                .append("<input type='submit' value='Porneste Monitor'>")
                .append("</form>")
                .append("<hr>")
                .append("<h3>Oprire monitor:</h3>")
                .append("<form action='./monitor-control' method='get'>")
                .append("<input type='hidden' name='action' value='stop'>")
                .append("<input type='submit' value='Opreste Monitor'>")
                .append("</form>")
                .append("<hr>")
                .append("<h3>Actualizare intervale:</h3>")
                .append("<form action='./monitor-control' method='get'>")
                .append("<input type='hidden' name='action' value='update'>")
                .append("Varsta minima: <input type='number' name='minAge' value='18' required><br>")
                .append("Varsta maxima: <input type='number' name='maxAge' value='30' required><br>")
                .append("Lungime maxima nume: <input type='number' name='maxNameLength' value='10' required><br><br>")
                .append("<input type='submit' value='Actualizeaza Intervale'>")
                .append("</form>")
                .append("<hr>")
                .append("<p><a href='./monitor-control'>Refresh pagina</a> | ")
                .append("<a href='./view-alarm' target='_blank'>Vezi pagina alarme</a>")
                .append("<p><a href='./'>Inapoi la meniul principal</a></p>")
                .append("</body></html>");

        response.setContentType("text/html");
        response.getWriter().print(html.toString());
    }
}