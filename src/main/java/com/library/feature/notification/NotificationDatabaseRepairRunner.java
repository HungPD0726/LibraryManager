package com.library.feature.notification;

import com.library.shared.support.NotificationTextSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationDatabaseRepairRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NotificationDatabaseRepairRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final NotificationTextSupport notificationTextSupport;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!isSqlServer()) {
                return;
            }

            if (!notificationTableExists()) {
                return;
            }

            if (!ensureUnicodeColumns()) {
                return;
            }

            int repairedRows = repairLegacyNotifications();
            if (repairedRows > 0) {
                log.info("Repaired {} legacy notification rows after enabling Unicode storage.", repairedRows);
            }
        } catch (DataAccessException ex) {
            log.warn("Skipping notification database repair because the database operation failed: {}", ex.getMessage());
        }
    }

    private boolean isSqlServer() {
        Boolean sqlServer = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                connection.getMetaData()
                        .getDatabaseProductName()
                        .toLowerCase(Locale.ROOT)
                        .contains("sql server"));
        return Boolean.TRUE.equals(sqlServer);
    }

    private boolean notificationTableExists() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = 'Notification'
                """, Integer.class);
        return count != null && count > 0;
    }

    private boolean ensureUnicodeColumns() {
        Map<String, String> columnTypes = jdbcTemplate.queryForList("""
                        SELECT COLUMN_NAME, DATA_TYPE
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_NAME = 'Notification'
                          AND COLUMN_NAME IN ('Title', 'Message', 'Type')
                        """)
                .stream()
                .collect(Collectors.toMap(
                        row -> String.valueOf(row.get("COLUMN_NAME")),
                        row -> String.valueOf(row.get("DATA_TYPE"))
                ));

        if (columnTypes.isEmpty()) {
            return false;
        }

        alterToUnicodeIfNeeded(columnTypes.get("Title"),
                "ALTER TABLE dbo.Notification ALTER COLUMN Title NVARCHAR(200) NOT NULL");
        alterToUnicodeIfNeeded(columnTypes.get("Message"),
                "ALTER TABLE dbo.Notification ALTER COLUMN Message NVARCHAR(1000) NULL");
        alterToUnicodeIfNeeded(columnTypes.get("Type"),
                "ALTER TABLE dbo.Notification ALTER COLUMN Type NVARCHAR(50) NULL");

        Map<String, String> refreshedTypes = jdbcTemplate.queryForList("""
                        SELECT COLUMN_NAME, DATA_TYPE
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_NAME = 'Notification'
                          AND COLUMN_NAME IN ('Title', 'Message', 'Type')
                        """)
                .stream()
                .collect(Collectors.toMap(
                        row -> String.valueOf(row.get("COLUMN_NAME")),
                        row -> String.valueOf(row.get("DATA_TYPE"))
                ));

        return "nvarchar".equalsIgnoreCase(refreshedTypes.get("Title"))
                && "nvarchar".equalsIgnoreCase(refreshedTypes.get("Message"))
                && "nvarchar".equalsIgnoreCase(refreshedTypes.get("Type"));
    }

    private void alterToUnicodeIfNeeded(String currentType, String sql) {
        if ("nvarchar".equalsIgnoreCase(currentType)) {
            return;
        }
        jdbcTemplate.execute(sql);
    }

    private int repairLegacyNotifications() {
        List<StoredNotification> notifications = jdbcTemplate.query("""
                        SELECT NotificationID, Title, Message, Type
                        FROM Notification
                        """,
                (rs, rowNum) -> new StoredNotification(
                        rs.getInt("NotificationID"),
                        rs.getString("Title"),
                        rs.getString("Message"),
                        rs.getString("Type")
                ));

        int repairedRows = 0;
        for (StoredNotification notification : notifications) {
            NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                    notification.type(),
                    notification.title(),
                    notification.message()
            );

            if (Objects.equals(notification.title(), normalized.title())
                    && Objects.equals(notification.message(), normalized.message())
                    && Objects.equals(notification.type(), normalized.type())) {
                continue;
            }

            jdbcTemplate.update(connection -> buildUpdateStatement(connection, notification.id(), normalized));
            repairedRows++;
        }
        return repairedRows;
    }

    private PreparedStatement buildUpdateStatement(java.sql.Connection connection,
                                                   Integer id,
                                                   NotificationTextSupport.NotificationText normalized) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                UPDATE Notification
                SET Title = ?, Message = ?, Type = ?
                WHERE NotificationID = ?
                """);
        setNullableNString(statement, 1, normalized.title());
        setNullableNString(statement, 2, normalized.message());
        setNullableNString(statement, 3, normalized.type());
        statement.setInt(4, id);
        return statement;
    }

    private void setNullableNString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.NVARCHAR);
            return;
        }
        statement.setNString(index, value);
    }

    private record StoredNotification(Integer id, String title, String message, String type) {
    }
}
