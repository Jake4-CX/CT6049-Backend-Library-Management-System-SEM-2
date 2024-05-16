package me.jack.lat.lwsbackend.service.dataWarehouse;

import me.jack.lat.lwsbackend.util.OracleDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FinanceDirectorService {

    public double getAverageFinesLast30Days() throws SQLException {
        String query =
                "SELECT AVG(FINEAMOUNT) AS average_fine " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMTIME dt ON flb.PAIDATTIMEID = dt.DATEID " +
                        "WHERE dt.DATE_TIME >= SYSDATE - 30 AND flb.FINEAMOUNT IS NOT NULL";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_fine");
            }
        }
        return 0;
    }

    public double getTotalUnpaidFines() throws SQLException {
        String query =
                "SELECT SUM(FINEAMOUNT) AS total_unpaid_fines " +
                        "FROM FACTLOANEDBOOKS " +
                        "WHERE PAIDATTIMEID IS NULL AND FINEAMOUNT IS NOT NULL";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("total_unpaid_fines");
            }
        }
        return 0;
    }

    public double getPercentageUnpaidFines() throws SQLException {
        String query =
                "SELECT ( " +
                        "   (SELECT COUNT(*) " +
                        "    FROM FACTLOANEDBOOKS " +
                        "    WHERE PAIDATTIMEID IS NULL AND FINEAMOUNT IS NOT NULL) " +
                        "   * 100.0 / " +
                        "   (SELECT COUNT(*) FROM FACTLOANEDBOOKS WHERE FINEAMOUNT IS NOT NULL) " +
                        ") AS unpaid_fines_percentage " +
                        "FROM DUAL";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("unpaid_fines_percentage");
            }
        }
        return 0;
    }

    public String getMostPopularCategoryUnpaidFines() throws SQLException {
        String query =
                "SELECT dc.CATEGORYNAME, SUM(flb.FINEAMOUNT) AS total_unpaid_fines " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMBOOKS db ON flb.BOOKID = db.BOOKID " +
                        "JOIN DIMCATEGORIES dc ON db.BOOKCATEGORYID = dc.CATEGORYID " +
                        "WHERE flb.PAIDATTIMEID IS NULL AND flb.FINEAMOUNT IS NOT NULL " +
                        "GROUP BY dc.CATEGORYNAME " +
                        "ORDER BY total_unpaid_fines DESC " +
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

    public String getMostPopularAuthorUnpaidFines() throws SQLException {
        String query =
                "SELECT da.AUTHORNAME, SUM(flb.FINEAMOUNT) AS total_unpaid_fines " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMBOOKS db ON flb.BOOKID = db.BOOKID " +
                        "JOIN DIMAUTHORS da ON db.BOOKAUTHORID = da.AUTHORID " +
                        "WHERE flb.PAIDATTIMEID IS NULL AND flb.FINEAMOUNT IS NOT NULL " +
                        "GROUP BY da.AUTHORNAME " +
                        "ORDER BY total_unpaid_fines DESC " +
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
}