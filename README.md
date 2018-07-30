gitbucket-backup-plugin
===
Provides all in one backup features for GitBucket

## Features
This plugin provides backup feature for below data.

- Database contents
- User repositories
- Wiki repositories
- Attachment file of issue and release
- User avatar data 

And email notification what backup success or failure.

## Configuration
Configuration `GITBUCKET_HOME/backup.conf` as below.

```
# Backup timing (Required)
# For details, see http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
# This example, backup 12am every day
akka {
  quartz {
    schedules {
      Backup {
        expression = "0 0 0 * * ?"
      }
    }
  }
}

backup {
  # Backup archive destination directory (Optional)
  # If not specified, archive is saved into GITBUCKET_HOME
  archive-destination = """/path/to/archive-dest-dir"""

  # Maximum number of backup archives to keep (if 0 or negative value, keep unlimited) (Optional)
  # If not specified, keep unlimited
  archive-limit = 10

  # Send notify email when backup is success (Optional, default:false)
  notify-on-success = true

  # Send notify email when backup is failure (Optional, default:false)
  notify-on-failure = true

  # Notify email destination (Optional)
  notify-dest = ["jyuch@localhost"]

  # S3 compatible object storage for backup upload (Optional)
  s3 {
    # Endpoing URL
    endpoint = "http://localhost:9000"

    # Region
    region = "US_EAST_1"

    # Access key
    access-key = "access-key"

    # Secret key
    secret-key = "secret-key"

    # Bucket of backup destination
    bucket = "gitbucket"
  }
}
```

And you need to setup gitbucket SMTP configuration when use email notification.

## Send test mail

Send HTTP Post to `http://localhost:8080/api/v3/backup/mail-test` with administrator authority when you want to send test mail.

## On demand backup

Send HTTP Post to `http://localhost:8080/api/v3/backup/execute` with administrator authority when you want to execute backup immediately.

## Restoring backup

1. Execute GitBucket once.
1. Copy `data` and `repositories` from backup archive file to `GITBUCKET_HOME`.
1. Import `gitbucket.sql` from `System administration` -> `Data export/import`.
1. Re-configure other plugins.

If you are using PostgreSQL, you have to run following to the setup sequence values:

``` sql
SELECT setval('label_label_id_seq', (select max(label_id) + 1 from label));
SELECT setval('activity_activity_id_seq', (select max(activity_id) + 1 from activity));
SELECT setval('access_token_access_token_id_seq', (select max(access_token_id) + 1 from access_token));
SELECT setval('commit_comment_comment_id_seq', (select max(comment_id) + 1 from commit_comment));
SELECT setval('commit_status_commit_status_id_seq', (select max(commit_status_id) + 1 from commit_status));
SELECT setval('milestone_milestone_id_seq', (select max(milestone_id) + 1 from milestone));
SELECT setval('issue_comment_comment_id_seq', (select max(comment_id) + 1 from issue_comment));
SELECT setval('ssh_key_ssh_key_id_seq', (select max(ssh_key_id) + 1 from ssh_key));
SELECT setval('priority_priority_id_seq', (select max(priority_id) + 1 from priority));
```

For more details, see [External database configuration](https://github.com/gitbucket/gitbucket/wiki/External-database-configuration#postgresql).

## Compatibility with GitBucket

|Plugin version|GitBucket version|
|:-:|:-|
|1.0.0-RC1|4.27|