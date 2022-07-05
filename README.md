
# Pokemonish Game   [![Badge License]][License]   ![Badge Status]

*実験ソフトウェア制作用のコード*

<br>

## 説明

中間報告のアドバイスを受けて、最大128人がサーバーに登録できるようにしました。  
対戦して勝ったらモンスターが強くなります(ステータスが強くなる)。 (育っていく?)   
そのため、何回でも接続できるように、ログイン機能を実装しました。(ハッシュ化済み)。  
上の実装のために(対戦が終わって一旦サーバーから接続が切れてもサーバー上に情報が  
残るように)Channelとは別のClientInfoクラスという構造を持つクラスを作ってその  
構造でサーバーにデータを保存するようにしました。Client側のコードも見直しました。  
今まで入力から送信の一連の流れ(例えば対戦中)の塊をClientクラスの関数として実装  
していましたが(main関数が簡潔になるため)、データの流れが分かりにくくなる(サーバ  
ー側とどのように連携を取っているのか分かりにくくなる)ので、入力と送信、受信を一つ  
の関数にまとめず、main関数内で直接whileループなどを使って表現するようにしました。

<br>
<br>

## 問題点

<kbd>  Ctrl + C  </kbd> でプレイヤーが抜けた時の処理を追加して、<br>
<kbd>  Ctrl + C  </kbd> したらそれを検出してサーバーに"SIGINT" <br>
を送信するスレッドを追加して、強制的にログアウトさせ、<br>
waitingPlayerのリストから削除するようにしました。

GUI上でもウィンドウを閉じて終了したら`SIGINT`を送信するようする?

<br>
<br>

## やるべきこと

- 細かい調整 

- ダメージの調整

<br>
<br>

## 実行方法

### Windows

1. を起動

    ```shell
    server.bat
    ```
    
2. を起動

    ```shell
    client.bat
    ```

<br>

### Others

*コンパイルして実行*

- `server/Server.java`
- `client/ConsoleClient.java`

*または*

- `server/Server.java`
- `client/GraphicalClient.java`

<br>


<!----------------------------------------------------------------------------->

[Badge License]: https://img.shields.io/badge/License-Unknown-808080.svg?style=for-the-badge
[Badge Status]: https://img.shields.io/badge/Status-制作途中-EE672F.svg?style=for-the-badge

[License]: #
