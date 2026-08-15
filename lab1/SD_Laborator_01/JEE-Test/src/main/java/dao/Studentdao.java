package dao;

import beans.StudentBean;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Studentdao {

    private static final String DB_PATH =
            "/home/octavian/Documents/Facultate/SD/lab1/SD_Laborator_01/studenti.db";

    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    static {
        try {
            // driver SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite JDBC negasit", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS studenti (" +
                "id      INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nume    TEXT    NOT NULL, " +
                "prenume TEXT    NOT NULL, " +
                "varsta  INTEGER NOT NULL" +
                ")";
        try (Connection con = getConnection();
             Statement  st  = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la initializarea bazei de date", e);
        }
    }

    public int insert(StudentBean student) throws SQLException {
        String sql = "INSERT INTO studenti (nume, prenume, varsta) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getNume());
            ps.setString(2, student.getPrenume());
            ps.setInt   (3, student.getVarsta());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<StudentBean> findAll() throws SQLException {
        String sql = "SELECT id, nume, prenume, varsta FROM studenti ORDER BY id";
        List<StudentBean> lista = new ArrayList<>();
        try (Connection con = getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public StudentBean findById(int id) throws SQLException {
        String sql = "SELECT id, nume, prenume, varsta FROM studenti WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<StudentBean> search(String criteriu, String valoare) throws SQLException {
        if (!"nume".equals(criteriu) && !"prenume".equals(criteriu)) {
            throw new IllegalArgumentException("Criteriu invalid: " + criteriu);
        }
        String sql = "SELECT id, nume, prenume, varsta FROM studenti " +
                "WHERE " + criteriu + " LIKE ? ORDER BY id";
        List<StudentBean> lista = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + valoare + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    public boolean update(StudentBean student) throws SQLException {
        String sql = "UPDATE studenti SET nume = ?, prenume = ?, varsta = ? WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getNume());
            ps.setString(2, student.getPrenume());
            ps.setInt   (3, student.getVarsta());
            ps.setInt   (4, student.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM studenti WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private StudentBean mapRow(ResultSet rs) throws SQLException {
        StudentBean s = new StudentBean();
        s.setId     (rs.getInt   ("id"));
        s.setNume   (rs.getString("nume"));
        s.setPrenume(rs.getString("prenume"));
        s.setVarsta (rs.getInt   ("varsta"));
        return s;
    }
}