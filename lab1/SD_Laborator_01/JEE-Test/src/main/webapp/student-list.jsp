<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, beans.StudentBean" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lista Studenti</title>
    <style>
        body  { font-family: Arial, sans-serif; margin: 40px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ccc; padding: 8px 12px; text-align: left; }
        th { background-color: #4a90d9; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .btn { padding: 4px 10px; border: none; border-radius: 4px;
               cursor: pointer; text-decoration: none; font-size: 13px; }
        .btn-edit   { background: #f0a500; color: white; }
        .btn-delete { background: #e53935; color: white; }
        .search-form { margin-top: 20px; }
        .search-form input, .search-form select { padding: 6px; font-size: 14px; }
        .search-form button { padding: 6px 14px; font-size: 14px; cursor: pointer; }
        .nav { margin-bottom: 20px; }
        .nav a { margin-right: 16px; }
    </style>
</head>
<body>

<h2><%= request.getAttribute("titlu") != null ? request.getAttribute("titlu") : "Studenti" %></h2>

<div class="nav">
    <a href="./index.jsp">Meniu principal</a>
    <a href="./studenti?action=new">Adauga student nou</a>
    <a href="./export-json">Export JSON</a>
</div>

<%-- Formular cautare --%>
<div class="search-form">
    <form action="./studenti" method="get">
        <input type="hidden" name="action" value="search" />
        Cauta dupa:
        <select name="criteriu">
            <option value="nume"
                <%= "nume".equals(request.getAttribute("criteriu")) ? "selected" : "" %>>
                Nume
            </option>
            <option value="prenume"
                <%= "prenume".equals(request.getAttribute("criteriu")) ? "selected" : "" %>>
                Prenume
            </option>
        </select>
        <input type="text" name="valoare"
               value="<%= request.getAttribute("valoare") != null ? request.getAttribute("valoare") : "" %>"
               placeholder="Termen cautat..." />
        <button type="submit">🔍 Cauta</button>
        <a href="./studenti?action=list">Reseteaza</a>
    </form>
</div>

<%-- Tabel studenti --%>
<%
    List<StudentBean> studenti = (List<StudentBean>) request.getAttribute("studenti");
    if (studenti == null || studenti.isEmpty()) {
%>
    <p><em>Niciun student gasit.</em></p>
<%
    } else {
%>
<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Nume</th>
            <th>Prenume</th>
            <th>Varsta</th>
            <th>Actiuni</th>
        </tr>
    </thead>
    <tbody>
    <%
        for (StudentBean s : studenti) {
    %>
        <tr>
            <td><%= s.getId() %></td>
            <td><%= s.getNume() %></td>
            <td><%= s.getPrenume() %></td>
            <td><%= s.getVarsta() %></td>
            <td>
                <a class="btn btn-edit"
                   href="./studenti?action=edit&id=<%= s.getId() %>">Editeaza</a>
                &nbsp;
                <a class="btn btn-delete"
                   href="./studenti?action=delete&id=<%= s.getId() %>"
                   onclick="return confirm('Stergi studentul <%= s.getNume() %> <%= s.getPrenume() %>?');">
                    🗑️ Sterge
                </a>
            </td>
        </tr>
    <%
        }
    %>
    </tbody>
</table>
<p>Total: <strong><%= studenti.size() %></strong> student(i)</p>
<%
    }
%>

</body>
</html>