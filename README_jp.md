gitbucket-backup-plugin
===
GitBucketのオールインワンのバックアップ機能を提供します。

## 機能
この以下のデータの定期的なバックアップを実行します。

- データベース
- ユーザリポジトリ
- Wikiリポジトリ
- issue及びリリースページの添付ファイル
- アバターイメージ

また、バックアップの成功もしくは失敗時にメール通知を行います。

## 設定
本プラグインを使用するには`GITBUCKET_HOME/backup.conf`を以下のように設定します。

```
# バックアップタイミング（必須）
# 詳細は http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html を参照してください。
# この例では毎日午前0時にバックアップを実行します
# また、タイムゾーンは明示しないとUTCとなる為、Asia/Tokyoを明示的に指定します 
akka {
  quartz {
    schedules {
      Backup {
        expression = "0 0 0 * * ?"
        timezone = "Asia/Tokyo"
      }
    }
  }
}

backup {
  # バックアップアーカイブの保存先（オプション）
  # 指定されなかった場合、 GITBUCKET_HOME に保存されます
  archive-destination = """/path/to/archive-dest-dir"""

  # バックアップアーカイブを保持する最大数（0もしくは負数の場合は無限に保持しようとします）（オプション）
  # 指定されなかった場合、無限に保持します
  archive-limit = 10

  # バックアップに成功した場合、通知メールを送信します（オプション、デフォルトでfalse）
  notify-on-success = true

  # バックアップに失敗した場合、通知メールを送信します（オプション、デフォルトでfalse）
  notify-on-failure = true

  # 通知メールの送信先（オプション）
  notify-dest = ["jyuch@localhost"]

  # バックアップをアップロードするS3互換のオブジェクトストレージ（オプション）
  s3 {
    # エンドポイントURL
    endpoint = "http://localhost:9000"

    # リージョン
    region = "US_EAST_1"

    # アクセスキー
    access-key = "access-key"

    # セキュリティキー
    secret-key = "secret-key"

    # バックアップを格納するバケット名
    bucket = "gitbucket"
  }
}
```

また、メール通知を使用する場合はGitBucketのSMTP設定を設定しておく必要があります。

## テストメールの送信

通知メールのテストメールを送信するには管理者権限で`http://localhost:8080/api/v3/backup/mail-test`にHTTP POSTを送信します。

## バックアップの即時実行

バックアップを即時実行するには管理者権限で`http://localhost:8080/api/v3/backup/execute`にHTTP POSTを送信します。

## バックアップのリストア方法

1. 一度GitBucketを実行します。
1. `data`及び`repositories`をバックアップアーカイブから`GITBUCKET_HOME`にコピーします。
1. `System administration` -> `Data export/import`より`gitbucket.sql`をインポートします。
1. 他のプラグインを再設定します。

また、PostgreSQLを使用している場合、以下のSQLを実行してシーケンスの値を設定する必要があります。

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

詳細については、 [External database configuration](https://github.com/gitbucket/gitbucket/wiki/External-database-configuration#postgresql)をご覧ください。

## 各バージョンのGitBucketとの互換性

|プラグインバージョン|GitBucketバージョン|
|:-:|:-:|
|1.2.1|4.29|
|1.2.0|4.29|
|1.1.0|4.28|
|1.0.0|4.27|
|1.0.0-RC1|4.27|
