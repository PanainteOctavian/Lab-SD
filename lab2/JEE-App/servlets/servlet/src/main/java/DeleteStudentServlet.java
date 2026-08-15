import ejb.StudentEntity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DeleteStudentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String nume = request.getParameter("nume");
        String prenume = request.getParameter("prenume");

        // pregatire EntityManager
        EntityManagerFactory factory =
                Persistence.createEntityManagerFactory("bazaDeDateSQLite");
        EntityManager em = factory.createEntityManager();

        TypedQuery<StudentEntity> query = em.createQuery(
                "select s from StudentEntity s where s.nume = :nume and s.prenume = :prenume",
                StudentEntity.class);
        query.setParameter("nume", nume);
        query.setParameter("prenume", prenume);

        List<StudentEntity> results = query.getResultList();

        StringBuilder responseText = new StringBuilder("<html><body>");

        if (results.isEmpty()) {
            responseText.append("<p>Niciun student gasit.</p>");
        } else {
            StudentEntity student = results.get(0);
            responseText.append("<h3>Stergere student:</h3>");
            responseText.append("<form action='./delete-student' method='post'>");
            responseText.append("<input type='hidden' name='id' value='" + student.getId() + "' />");
            responseText.append("<input type='submit' value='Sterge' />");
            responseText.append("</form>");
        }

        responseText.append("<br/><a href='./'>Inapoi la meniu</a></body></html>");

        em.close();
        factory.close();

        response.setContentType("text/html");
        response.getWriter().print(responseText.toString());
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("bazaDeDateSQLite");
        EntityManager em = factory.createEntityManager();

        em.getTransaction().begin();

        TypedQuery<StudentEntity> query = em.createQuery(
                "delete from StudentEntity s where s.id = :id",
                StudentEntity.class);
        query.setParameter("id", id);

        int updated = query.executeUpdate();
        em.getTransaction().commit();

        em.close();
        factory.close();

        response.setContentType("text/html");
        if (updated > 0) {
            response.getWriter().print("<p>Student sters cu succes</p><a href='./'>Inapoi</a>");
        } else {
            response.getWriter().print("<p>Eroare la stergere</p><a href='./'>Inapoi</a>");
        }
    }
}