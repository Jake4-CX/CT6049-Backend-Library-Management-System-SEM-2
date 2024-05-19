package me.jack.lat.lwsbackend.service.dataWarehouse;

import me.jack.lat.lwsbackend.util.OracleDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LibrarianService {

    public double getAverageBooksBorrowedLastQuarter() throws SQLException {
        String query =
                "SELECT AVG(book_count) AS average_books_per_user " +
                        "FROM ( " +
                        "  SELECT COUNT(*) AS book_count " +
                        "  FROM FACTLOANEDBOOKS flb " +
                        "  JOIN DIMTIME dt ON flb.LOANEDATTIMEID = dt.DATEID " +
                        "  WHERE dt.QUARTER = (SELECT MAX(QUARTER) FROM DIMTIME) " +
                        "  GROUP BY flb.USERID " +
                        ")";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_books_per_user");
            }
        }
        return 0;
    }

    public double getAverageBooksBorrowedLastDuration(long durationMilliseconds) throws SQLException {
        // Convert milliseconds to days
        double durationDays = durationMilliseconds / 1000.0 / 60 / 60 / 24;

        // Validate the duration
        if (durationDays < 0) {
            throw new IllegalArgumentException("Duration must be a non-negative value");
        }

        String query =
                "SELECT AVG(book_count) AS average_books_per_user " +
                        "FROM ( " +
                        "  SELECT COUNT(*) AS book_count " +
                        "  FROM FACTLOANEDBOOKS flb " +
                        "  JOIN DIMTIME dt ON flb.LOANEDATTIMEID = dt.DATEID " +
                        "  WHERE dt.DATE_TIME >= (SYSDATE - ?) " +
                        "  GROUP BY flb.USERID " +
                        ")";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, durationDays);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_books_per_user");
            }
        }
        return 0;
    }

    public String getMostPopularBookCategoryCurrentlyLoaned() throws SQLException {
        String query =
                "SELECT dc.CATEGORYNAME, COUNT(*) AS loan_count " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMBOOKS db ON flb.BOOKID = db.BOOKID " +
                        "JOIN DIMCATEGORIES dc ON db.BOOKCATEGORYID = dc.CATEGORYID " +
                        "WHERE flb.RETURNEDATTIMEID IS NULL " +
                        "GROUP BY dc.CATEGORYNAME " +
                        "ORDER BY loan_count DESC " +
                        "FETCH FIRST 1 ROWS ONLY";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("CATEGORYNAME");
            }
        }
        return null;
    }

    public String getMostPopularAuthorCurrentlyLoaned() throws SQLException {
        String query =
                "SELECT da.AUTHORNAME, COUNT(*) AS loan_count " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMBOOKS db ON flb.BOOKID = db.BOOKID " +
                        "JOIN DIMAUTHORS da ON db.BOOKAUTHORID = da.AUTHORID " +
                        "WHERE flb.RETURNEDATTIMEID IS NULL " +
                        "GROUP BY da.AUTHORNAME " +
                        "ORDER BY loan_count DESC " +
                        "FETCH FIRST 1 ROWS ONLY";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("AUTHORNAME");
            }
        }
        return null;
    }

    public double getPercentageBooksCurrentlyLoaned() throws SQLException {
        String query =
                "SELECT ( " +
                        "   (SELECT COUNT(*) " +
                        "    FROM FACTLOANEDBOOKS " +
                        "    WHERE RETURNEDATTIMEID IS NULL) " +
                        "   * 100.0 / " +
                        "   (SELECT COUNT(*) FROM DIMBOOKS) " +
                        ") AS loaned_books_percentage " +
                        "FROM DUAL";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("loaned_books_percentage");
            }
        }
        return 0;
    }

    public HashMap<String, Object>[] getBooksPerCategory() throws SQLException {
        List<HashMap<String, Object>> categoryCounts = new ArrayList<>();

        String query =
                "SELECT dc.CATEGORYNAME, COUNT(*) AS bookCount " +
                        "FROM DIMBOOKS db " +
                        "JOIN DIMCATEGORIES dc ON db.BOOKCATEGORYID = dc.CATEGORYID " +
                        "GROUP BY dc.CATEGORYNAME";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                categoryCounts.add(new HashMap<>(){{
                    put("key", resultSet.getString("CATEGORYNAME"));
                    put("value", resultSet.getInt("BOOKCOUNT"));
                }});
            }

            return categoryCounts.toArray(new HashMap[0]);
        }
    }

    public HashMap<String, Object>[] getAuthorsPerCategory() throws SQLException {
        List<HashMap<String, Object>> authorCounts = new ArrayList<>();

        String query =
                "SELECT dc.CATEGORYNAME, COUNT(DISTINCT da.AUTHORNAME) AS authorCount " +
                        "FROM DIMBOOKS db " +
                        "JOIN DIMCATEGORIES dc ON db.BOOKCATEGORYID = dc.CATEGORYID " +
                        "JOIN DIMAUTHORS da ON db.BOOKAUTHORID = da.AUTHORID " +
                        "GROUP BY dc.CATEGORYNAME";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                authorCounts.add(new HashMap<>(){{
                    put("key", resultSet.getString("CATEGORYNAME"));
                    put("value", resultSet.getInt("AUTHORCOUNT"));
                }});
            }

            return authorCounts.toArray(new HashMap[0]);
        }
    }
}