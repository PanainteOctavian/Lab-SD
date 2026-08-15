package monitorization;

import ejb.*;
import javax.persistence.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseMonitor {

    private static DatabaseMonitor monitor;
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    private int minAge = 18;
    private int maxAge = 30;
    private int maxNameLength = 10;

    private static final String ALARM_PAGE_PATH = "/home/octavian/Documents/Facultate/SD/lab2/alarm.html";

    private DatabaseMonitor() {

    }

    public static synchronized DatabaseMonitor getInstance() {
        if (monitor == null) {
            monitor = new DatabaseMonitor();
        }
        return monitor;
    }

    public void startMonitor(int minAge, int maxAge, int maxNameLength) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.maxNameLength = maxNameLength;

        if (!isRunning) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::checkDatabase, 0, 10, TimeUnit.SECONDS);
            isRunning = true;
        }
    }

    public void stopMonitoring() {
        if (isRunning && scheduler != null) {
            scheduler.shutdown();
            isRunning = false;
        }
    }

    private void checkDatabase() {
        EntityManagerFactory factory = null;
        EntityManager em = null;

        try {
            factory = Persistence.createEntityManagerFactory("bazaDeDateSQLite");
            em = factory.createEntityManager();

            StringBuilder alarmBuilder = new StringBuilder();
            boolean hasAlarms = false;

            TypedQuery<StudentEntity> query = em.createQuery(
                    "SELECT s FROM StudentEntity s WHERE (s.varsta < :minAge OR s.varsta > :maxAge) " +
                            "OR (LENGTH(s.nume) > :maxLen)", StudentEntity.class);

            query.setParameter("minAge", minAge);
            query.setParameter("maxAge", maxAge);
            query.setParameter("maxLen", maxNameLength);

            List<StudentEntity> violations = query.getResultList();

            for (StudentEntity student : violations) {
                hasAlarms = true;
                String detail = "";

                if (student.getVarsta() < minAge || student.getVarsta() > maxAge) {
                    detail += "Vârstă: " + student.getVarsta() + " (Limită: [" + minAge + "-" + maxAge + "]) ";
                }
                if (student.getNume().length() > maxNameLength) {
                    detail += "Nume: '" + student.getNume() + "' (Lungime: " + student.getNume().length() + " > " + maxNameLength + ")";
                }

                alarmBuilder.append("<tr>")
                        .append("<td>").append(student.getId()).append("</td>")
                        .append("<td>").append(student.getNume()).append("</td>")
                        .append("<td>").append(student.getPrenume()).append("</td>")
                        .append("<td><b>").append(detail).append("</b></td>")
                        .append("</tr>");
            }

            if (hasAlarms) generateAlarmPage(alarmBuilder.toString());
                else generateCleanPage();

        } catch (Exception e) {
            System.err.println("[Monitor] Error: " + e.getMessage());
        } finally {
            if (em != null) em.close();
            if (factory != null) factory.close();
        }
    }

    private void generateAlarmPage(String alarmRows) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String htmlContent = String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>Pagina cu alarme</title>\n" +
                        "    <meta charset='UTF-8'>\n" +
                        "    <meta http-equiv='refresh' content='10'>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h1>ALARMA ACTIVA</h1>\n" +
                        "    <p><b>Ultima verificare:</b> %s</p>\n" +
                        "    <p><b>Parametri monitorizati:</b> Varsta [%d - %d] si Lungime maxima nume [%d]</p>\n" +
                        "    <hr>\n" +
                        "    <h3>Inregistrari care au declansat alarma:</h3>\n" +
                        "    <table>\n" +
                        "        <tr>\n" +
                        "            <th>ID</th>\n" +
                        "            <th>Nume</th>\n" +
                        "            <th>Prenume</th>\n" +
                        "            <th>Eroare</th>\n" +
                        "        </tr>\n" +
                        "%s" +
                        "    </table>\n" +
                        "    <hr>\n" +
                        "    <p>Pagina isi da refresh automat la fiecare 10 secunde.</p>\n" +
                        "    <p>Ultima actualizare: %s</p>\n" +
                        "</body>\n" +
                        "</html>",
                timestamp, minAge, maxAge, maxNameLength, alarmRows, timestamp);

        writeHtmlToFile(htmlContent);
    }

    private void generateCleanPage() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String htmlContent = String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>Monitor Baza Date</title>\n" +
                        "    <meta charset='UTF-8'>\n" +
                        "    <meta http-equiv='refresh' content='10'>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h1>NICI O ALARMA</h1>\n" +
                        "    <p><b>Ultima verificare:</b> %s</p>\n" +
                        "    <p><b>Parametri monitorizati:</b> Varsta [%d - %d] si Lungime maxima nume [%d]</p>\n" +
                        "    <p>Toate valorile sunt in intervalul specificat.</p>\n" +
                        "    <hr>\n" +
                        "    <p>Pagina se reimprospateaza automat la fiecare 10 secunde.</i></p>\n" +
                        "    <p>Ultima actualizare: %s</p>\n" +
                        "</body>\n" +
                        "</html>",
                timestamp, minAge, maxAge, maxNameLength, timestamp);

        writeHtmlToFile(htmlContent);
    }

    private void writeHtmlToFile(String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ALARM_PAGE_PATH))) {
            writer.println(content);
            System.out.println("[Monitor] Alarm page updated at " + ALARM_PAGE_PATH);
        } catch (IOException e) {
            System.err.println("[Monitor] Error writing alarm page: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setMonitoringRanges(int minAge, int maxAge, int maxNameLength) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.maxNameLength = maxNameLength;
        System.out.println("[Monitor] Monitoring ranges updated: Age [" + minAge + "-" + maxAge +
                "] + maxNameLength = " + maxNameLength);
    }
}