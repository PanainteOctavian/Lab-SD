<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html xmlns:jsp="http://java.sun.com/JSP/Page">
<head>
    <meta charset="UTF-8">
    <title>Actualizare student</title>
</head>
<body>

<h3>Actualizare informatii student</h3>

<form action="./update-student" method="post">

    Nume:
    <input type="text" name="nume" value="<%= request.getAttribute("nume") %>" />
    <br/>

    Prenume:
    <input type="text" name="prenume" value="<%= request.getAttribute("prenume") %>" />
    <br/>

    Varsta:
    <input type="number" name="varsta" value="<%= request.getAttribute("varsta") %>" />
    <br/>
    <br/>

    <button type="submit" name="action" value="update">Actualizeaza</button>

    <button type="submit" name="action" value="delete"
            onclick="return confirm('Confirmi stergerea studentului?');">
        Sterge
    </button>

</form>

<br/>
<a href="./index.jsp">Inapoi la meniu  </a>

</body>
</html>