package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import server.bin.Monster;

public class Channel extends Thread {
	Server server;
	Socket socket;
	BufferedReader in;
	PrintWriter out;
	public int id;

	// クライアントの状態。ターンの判定などに使う
	private final String MY_TURN = "3";
	private final String OPPONENT_TURN = "4";

	Channel(Socket socket, Server server) {
		this.server = server;
		this.socket = socket;
		try {
			in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(
					socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("入出力接続エラー");
		}
		this.start();
	}

	// クライアントの1回の対戦に対応
	public void run() {
		try {
			send("サーバーに接続しました");
			boolean isNewAccount = receive().equals("new");
			if (!isNewAccount) {// ログイン
				while (true) {
					String name = receive();
					String password = receive();
					if (server.isValidAccount(name, password)) {
						send("correct");
						id = server.getIdByName(name);
						server.clientsInfo[id].login(out);
						break;
					} else {
						send("incorrect");
						if (receive().equals("onemore")) {
							continue;
						} else {
							isNewAccount = true;
							break;
						}
					}
				}
			}
			if (isNewAccount) { // 新規登録
				String name, password, monsterType;
				while (true) {
					name = receive();
					if (server.isDuplicateName(name)) {
						send("duplicate");
					} else {
						send("correct");
						if (!server.registeList.contains(name)) { // 登録中の人のリストに名前を追加
						server.registeList.add(name);
						}
						break;
					}
				}
				password = receive();
				monsterType = receive();
				if (server.registeList.contains(name)) {
					server.registeList.remove(server.registeList.indexOf(name));
				}
				id = server.addAccount(name, password, monsterType, this.out);
			}

			server.showStats(id);
			int opponentId = server.findOpponent(id);
			String opponentName = server.getNameById(opponentId);
			Thread.sleep(500);
			server.removeWaitingPlayer(id);
			send("対戦相手が見つかりました\n" +
					"対戦相手は" + opponentName + "です");
			send(server.isFirstTurn(id, opponentId) ? "first" : "second");

			// 対戦開始
			while (true) {
				String clientState = receive();
				if (clientState.equals(MY_TURN)) {
					server.showMoveLineup(id);
					int moveIndex = Integer.parseInt(receive());
					server.useMove(id, opponentId, moveIndex);
					server.showCurrentHp(id, opponentId);
					if (server.isGameover(id, opponentId)) {
						send("gameisover");
						break;
					} else {
						send(OPPONENT_TURN);
					}
				} else if (clientState.equals(OPPONENT_TURN)) {
					// 攻撃側の呼び出しで技の結果, HPを表示
					receive(); // 技の結果の受け取り確認
					if (server.isGameover(id, opponentId)) {
						send("gameisover");
						break;
					} else {
						send(MY_TURN);
					}
				}
			}
			Thread.sleep(500);
			if (receive().equals("WIN")) {
				server.showStats(id);
				int status = Integer.parseInt(receive());
				server.levelUp(id, status);
				send(Monster.val2stats(status) + "が3点上がりました");
				server.showStats(id);
			}
		} catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
			System.out.println("クライアント切断");
		} finally {
			server.clientsInfo[id].logout();
		}
	}

	public void send(String s) {
		out.println(s);
	}

	public String receive() throws IOException {
		return in.readLine();
	}

	public void logging(Object s) {
		System.out.println(s);
	}
}