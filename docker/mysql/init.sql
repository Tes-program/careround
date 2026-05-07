CREATE DATABASE IF NOT EXISTS careround_core    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS careround_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS careround_audit   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON careround_core.*         TO 'careround'@'%';
GRANT ALL PRIVILEGES ON careround_notification.* TO 'careround'@'%';
GRANT ALL PRIVILEGES ON careround_audit.*        TO 'careround'@'%';

FLUSH PRIVILEGES;
