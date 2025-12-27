#!/bin/bash

echo "=== Checking MySQL Binlog Configuration ==="
echo ""

# Check if MySQL is running
if ! mysql -u root -e "SELECT 1" 2>/dev/null; then
    echo "✗ Cannot connect to MySQL. Please check your MySQL is running."
    echo "  Try: mysql -u root -p"
    exit 1
fi

echo "✓ MySQL is accessible"
echo ""

# Check binlog status
echo "Checking binlog configuration..."
BINLOG_STATUS=$(mysql -u root -e "SHOW VARIABLES LIKE 'log_bin%';" 2>/dev/null)

if echo "$BINLOG_STATUS" | grep -q "log_bin.*ON"; then
    echo "✓ Binary logging is ENABLED"
else
    echo "✗ Binary logging is DISABLED"
    echo ""
    echo "To enable binlog, add these to your MySQL config file:"
    echo "  /etc/mysql/my.cnf (Linux)"
    echo "  /usr/local/etc/my.cnf (macOS Homebrew)"
    echo "  /opt/homebrew/etc/my.cnf (macOS Apple Silicon)"
    echo ""
    echo "[mysqld]"
    echo "server-id=1"
    echo "log-bin=mysql-bin"
    echo "binlog-format=row"
    echo "binlog-row-image=full"
    echo "gtid-mode=ON"
    echo "enforce-gtid-consistency=ON"
    echo ""
    echo "Then restart MySQL:"
    echo "  brew services restart mysql  (macOS)"
    echo "  sudo systemctl restart mysql  (Linux)"
    exit 1
fi

echo ""
echo "Checking binlog format..."
BINLOG_FORMAT=$(mysql -u root -e "SHOW VARIABLES LIKE 'binlog_format';" 2>/dev/null | grep binlog_format | awk '{print $2}')

if [ "$BINLOG_FORMAT" = "ROW" ]; then
    echo "✓ Binlog format is ROW (required)"
else
    echo "✗ Binlog format is $BINLOG_FORMAT (should be ROW)"
    echo "  Update your MySQL config to set: binlog-format=row"
    exit 1
fi

echo ""
echo "=== MySQL is configured correctly for CDC! ==="

