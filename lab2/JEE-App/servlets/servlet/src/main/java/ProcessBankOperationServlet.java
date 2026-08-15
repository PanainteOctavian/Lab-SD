import interfaces.BankAccountBeanRemote;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
public class ProcessBankOperationServlet extends HttpServlet {@Override
protected void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException{// preluare parametri din cererea HTTP
    String operation = request.getParameter("operation");String amountString = request.getParameter("amount");
    Integer amount = (!amountString.equals("")) ?
            Integer.parseInt(amountString) : 0;
    BankAccountBeanRemote bankAccount;
    bankAccount =
            (BankAccountBeanRemote)request.getSession().getAttribute("bankAccountBean");
    if (bankAccount == null) {
        try {
            InitialContext ctx = new InitialContext();
            bankAccount = (BankAccountBeanRemote)
                    ctx.lookup("bankaccount#interfaces.BankAccountBeanRemote");
            request.getSession().setAttribute("bankAccountBean",bankAccount);
        } catch (NamingException e) {
            e.printStackTrace();
            return;
        }
    }
    Integer accountBalance = null;
    String message = "";
    if (operation.equals("deposit")) {
        bankAccount.deposit(amount);
        message = "In contul dvs. au fost depusa suma: " +amount+ ".";
    } else if (operation.equals("withdraw")) {
        if (bankAccount.withdraw(amount)) {
            message = "Din contul dvs. s-a retras suma de:"+amount + ".";
        } else {
            message = "Operatiunea a esuat! Fonduri insuficiente.";}
    } else if (operation.equals("balance")) {
        accountBalance = bankAccount.getBalance();
    }
    message += "<br /><br />";
    if (accountBalance != null) {
        message += "Sold cont: " + accountBalance;
    }
    message += "<br /><a href='./'>Inapoi la meniul principal</a>";
    response.setContentType("text/html");
    response.getWriter().print(message);
}
}