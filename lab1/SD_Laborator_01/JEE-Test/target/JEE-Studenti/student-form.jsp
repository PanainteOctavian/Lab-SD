<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="beans.StudentBean" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%
        StudentBean student = (StudentBean) request.getAttribute("student");
        boolean isEdit = (student != null);
        String titlu = isEdit ? "Editeaza student" : "Adauga student nou";
    %>
    <title><%= titlu %></title>
    <style>
        body  { font-family: Arial, sans-serif; margin: 40px; }
        label { display: block; margin-top: 12px; font-weight: bold; }
        input[type=text], input[type=number] {
            padding: 6px; font-size: 14px; width: 260px; margin-top: 4px;
        }
        .btn-save   { margin-top: 16px; padding: 8px 20px; background: #4a90d9;
                      color: white; border: none; border-radius: 4px;
                      font-size: 15px; cursor: pointer; }
        .btn-cancel { margin-left: 10px; font-size: 14px; }
    </style>
</head>
<body>

<h2><%= titlu %></h2>

<form action="./studenti" method="post">

    <%-- actiune INSERT sau UPDATE --%>
    <input type="hidden" name="action" value="<%= isEdit ? "update" : "insert" %>" />

    <%-- la editare trimitem si id-ul --%>
    <% if (isEdit) { %>
        <input type="hidden" name="id" value="<%= student.getId() %>" />
    <% } %>

    <label for="nume">Nume:</label>
    <input type="text" id="nume" name="nume" required
           value="<%= isEdit ? student.getNume() : "" %>" />

    <label for="prenume">Prenume:</label>
    <input type="text" id="prenume" name="prenume" required
           value="<%= isEdit ? student.getPrenume() : "" %>" />

    <label for="varsta">Varsta:</label>
    <input type="number" id="varsta" name="varsta" required min="1" max="120"
           value="<%= isEdit ? student.getVarsta() : "" %>" />

    <br/>
    <button type="submit" class="btn-save">
        <%= isEdit ? "ðŸ’¾ Salveaza modificarile" : "âž• Adauga studentul" %>
    </button>
    <a class="btn-cancel" href="./studenti?action=list">Anuleaza</a>

</form>

</body>
</html>