import dao.Studentdao;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        new Studentdao().initDatabase();
        System.out.println("[AppInitListener] Baza de date SQLite initializata.");
    }

}