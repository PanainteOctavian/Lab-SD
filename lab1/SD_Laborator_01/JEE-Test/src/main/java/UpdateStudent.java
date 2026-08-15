import beans.StudentBean;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class UpdateStudent extends HttpServlet {

    private static final String XML_PATH = "/home/octavian/Documents/Facultate/SD/lab1/SD_Laborator_01/bd.xml";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        File file = new File(XML_PATH);

        if (!file.exists()) {
            response.sendError(404, "Nu a fost gasit niciun student serializat pe disc!");
            return;
        }

        XmlMapper xmlMapper = new XmlMapper();
        StudentBean bean = xmlMapper.readValue(file, StudentBean.class);

        request.setAttribute("nume", bean.getNume());
        request.setAttribute("prenume", bean.getPrenume());
        request.setAttribute("varsta", bean.getVarsta());

        request.getRequestDispatcher("./update-student.jsp").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        File file = new File(XML_PATH);

        if ("delete".equals(action)) {
            if (file.exists()) {
                file.delete();
            }
            request.getRequestDispatcher("./delete-success.jsp").forward(request, response);

        } else {
            String nume = request.getParameter("nume");
            String prenume = request.getParameter("prenume");
            int varsta = Integer.parseInt(request.getParameter("varsta"));

            StudentBean bean = new StudentBean();
            bean.setNume(nume);
            bean.setPrenume(prenume);
            bean.setVarsta(varsta);

            XmlMapper mapper = new XmlMapper();
            mapper.writeValue(file, bean);

            request.setAttribute("nume",    nume);
            request.setAttribute("prenume", prenume);
            request.setAttribute("varsta",  varsta);
            request.getRequestDispatcher("./info-student.jsp").forward(request, response);
        }
    }
}