package me.jack.lat.lwsbackend.service.dataWarehouse;

import me.jack.lat.lwsbackend.util.OracleDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChiefLibrarianService {

    public double getAverageLoanDurationLastDuration(long durationMilliseconds) throws SQLException {

        // Convert milliseconds to days
        double durationDays = durationMilliseconds / 1000.0 / 60 / 60 / 24;

        // Validate the duration
        if (durationDays < 0) {
            throw new IllegalArgumentException("Duration must be a non-negative value");
        }

        String query =
                "SELECT AVG(EXTRACT(DAY FROM (dt_returned.DATE_TIME - dt_loaned.DATE_TIME)) * 24 * 60 + " +
                        "EXTRACT(HOUR FROM (dt_returned.DATE_TIME - dt_loaned.DATE_TIME)) * 60 + " +
                        "EXTRACT(MINUTE FROM (dt_returned.DATE_TIME - dt_loaned.DATE_TIME))) AS average_loan_duration " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMTIME dt_loaned ON flb.LOANEDATTIMEID = dt_loaned.DATEID " +
                        "JOIN DIMTIME dt_returned ON flb.RETURNEDATTIMEID = dt_returned.DATEID " +
                        "WHERE dt_loaned.DATE_TIME >= (SYSDATE - ?)";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            // Convert milliseconds to seconds and set the parameter
            statement.setDouble(1, durationDays);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_loan_duration"); // Time in minutes
            }
        }
        return 0;
    }

    public double getPercentageBooksReturnedLate() throws SQLException {
        String query =
                "SELECT ( " +
                        "   (SELECT COUNT(*) " +
                        "    FROM FACTLOANEDBOOKS " +
                        "    WHERE RETURNEDATTIMEID IS NOT NULL AND " +
                        "          RETURNEDATTIMEID > LOANEDATTIMEID) " +
                        "   * 100.0 / " +
                        "   (SELECT COUNT(*) FROM FACTLOANEDBOOKS WHERE RETURNEDATTIMEID IS NOT NULL) " +
                        ") AS late_return_percentage " +
                        "FROM DUAL";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("late_return_percentage");
            }
        }
        return 0;
    }

    public double getPercentageBooksCurrentlyOverdue() throws SQLException {
        String query =
                "SELECT ( " +
                        "   (SELECT COUNT(*) " +
                        "    FROM FACTLOANEDBOOKS flb " +
                        "    JOIN DIMTIME dt ON flb.LOANEDATTIMEID = dt.DATEID " +
                        "    WHERE flb.RETURNEDATTIMEID IS NULL AND dt.DATE_TIME + INTERVAL '14' DAY < SYSDATE) " +
                        "   * 100.0 / " +
                        "   (SELECT COUNT(*) FROM FACTLOANEDBOOKS WHERE RETURNEDATTIMEID IS NULL) " +
                        ") AS overdue_loans_percentage " +
                        "FROM DUAL";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("overdue_loans_percentage");
            }
        }
        return 0;
    }

    public double getAverageTimeToPayFine() throws SQLException {
        String query =
                "SELECT AVG(EXTRACT(DAY FROM (dt_paid.DATE_TIME - dt_returned.DATE_TIME)) * 24 * 60 + " +
                        "EXTRACT(HOUR FROM (dt_paid.DATE_TIME - dt_returned.DATE_TIME)) * 60 + " +
                        "EXTRACT(MINUTE FROM (dt_paid.DATE_TIME - dt_returned.DATE_TIME))) AS average_time_to_pay_fine " +
                        "FROM FACTLOANEDBOOKS flb " +
                        "JOIN DIMTIME dt_returned ON flb.RETURNEDATTIMEID = dt_returned.DATEID " +
                        "JOIN DIMTIME dt_paid ON flb.PAIDATTIMEID = dt_paid.DATEID " +
                        "WHERE flb.RETURNEDATTIMEID IS NOT NULL AND flb.PAIDATTIMEID IS NOT NULL " +
                        "AND dt_returned.DATE_TIME > (SELECT DATE_TIME FROM DIMTIME WHERE DATEID = flb.LOANEDATTIMEID) + INTERVAL '14' DAY";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_time_to_pay_fine"); // Time in minutes
            }
        }
        return 0;
    }

    public double getAverageBooksPerAuthor() throws SQLException {
        String query =
                "SELECT AVG(book_count) AS average_books_per_author " +
                        "FROM (SELECT COUNT(*) AS book_count FROM DIMBOOKS GROUP BY BOOKAUTHORID)";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_books_per_author");
            }
        }
        return 0;
    }

    public double getAverageBooksPerCategory() throws SQLException {
        String query =
                "SELECT AVG(book_count) AS average_books_per_category " +
                        "FROM (SELECT COUNT(*) AS book_count FROM DIMBOOKS GROUP BY BOOKCATEGORYID)";

        try (Connection connection = OracleDBUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("average_books_per_category");
            }
        }
        return 0;
    }
}
