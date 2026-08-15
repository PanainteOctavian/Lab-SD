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

public class UpdateStudentServlet extends HttpServlet {
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
            responseText.append("<h3>Editare student:</h3>");
            responseText.append("<form action='./update-student' method='post'>");
            responseText.append("<input type='hidden' name='id' value='" + student.getId() + "' />");
            responseText.append("Nume nou: <input type='text' name='numeNou' value='" + student.getNume() + "' /><br/>");
            responseText.append("Prenume nou: <input type='text' name='prenumeNou' value='" + student.getPrenume() + "' /><br/>");
            responseText.append("Varsta noua: <input type='number' name='varstaNoua' value='" + student.getVarsta() + "' /><br/><br/>");
            responseText.append("<input type='submit' value='Actualizeaza' />");
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
        String numeNou = request.getParameter("numeNou");
        String prenumeNou = request.getParameter("prenumeNou");
        int varstaNoua = Integer.parseInt(request.getParameter("varstaNoua"));

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("bazaDeDateSQLite");
        EntityManager em = factory.createEntityManager();

        em.getTransaction().begin();

        TypedQuery<StudentEntity> query = em.createQuery(
                "update StudentEntity s set s.nume = :numeNou, s.prenume = :prenumeNou, " +
                        "s.varsta = :varstaNoua where s.id = :id",
                    StudentEntity.class);
        query.setParameter("numeNou", numeNou);
        query.setParameter("prenumeNou", prenumeNou);
        query.setParameter("varstaNoua", varstaNoua);
        query.setParameter("id", id);

        int updated = query.executeUpdate();
        em.getTransaction().commit();

        em.close();
        factory.close();

        response.setContentType("text/html");
        if (updated > 0) {
            response.getWriter().print("<p>Student actualizat cu succes</p><a href='./'>Inapoi</a>");
        } else {
            response.getWriter().print("<p>Eroare la actualizare</p><a href='./'>Inapoi</a>");
        }
    }
}