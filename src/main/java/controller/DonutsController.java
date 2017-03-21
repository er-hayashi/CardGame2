package controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import dao.CardsDAO;
import dao.GameSettingsDAO;
import dao.UserDAO;
import dao.access.GameLogAccessDAO;
import model.CardsModel;
import model.GameLogsModel;
import model.GameSettingsModel;
import model.UserModel;

@Controller
public class DonutsController {
	@Autowired
	private GameSettingsDAO gameSettingsDAO;

	@Autowired
	private CardsDAO cardsDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private GameLogAccessDAO gameLogAccessDAO;

	@ModelAttribute("Game")
	private GameSettingsModel init() {
		return new GameSettingsModel();
	}

	@RequestMapping(value = "/game/donuts/main", method = { GET, POST })
	public String login(@ModelAttribute("Game") GameSettingsModel gameSettingsModel, Model model, HttpSession session) {
		Object sessionCheck = session.getAttribute("session");
		if (sessionCheck == null) {
			return "redirect:/";
		}
		// ユーザー情報の取得
		UserModel userModel = (UserModel) session.getAttribute("session");

		// ゲーム設定の読み込み
		List<GameSettingsModel> gameSettingsItem = gameSettingsDAO.gameSettingsLoad(gameSettingsModel);
		gameSettingsModel.setGameSettingsModel(gameSettingsItem);

		// カードリストの生成
		List<CardsModel> cardList = cardsDAO.getGameCardList(gameSettingsModel.getGameType());

		// ゲーム初アクセス時の処理
		if (cardList.isEmpty()) {
			// 初期データの生成
			cardsDAO.createInitialCardData(gameSettingsModel.getGameType());
			// 生成したカードを取得
			cardList = cardsDAO.getGameCardList(gameSettingsModel.getGameType());
		}
		// 現在のカード取得
		CardsModel currentCard = cardList.get(cardList.size() - 1);

		model.addAttribute("CardList", cardList);
		model.addAttribute("CurrentCard", currentCard);

		// ゲームログの取得
		List<GameLogsModel> logList = gameLogAccessDAO.getGameLogs(gameSettingsModel.getLogCounts());
		model.addAttribute("GameLogsList", logList);

		// ユーザーがカードを引けるか判定する
		int drawState = 0;

		if (!currentCard.getUserId().equals(userModel.getUserId())){
			drawState = 1;
		}

		// ユーザーの待機時間演算

		model.addAttribute("DrawState", drawState);
		return "game/donuts";
	}

	@RequestMapping(value = "/game/donuts/draw", method = { POST })
	public String drawCard(@ModelAttribute("Game") GameSettingsModel gameSettingsModel, Model model,
			HttpSession session) {
		Object sessionCheck = session.getAttribute("session");
		if (sessionCheck == null) {
			return "redirect:/";
		}

		// ユーザー情報の取得
		UserModel userModel = (UserModel) session.getAttribute("session");

		// カード値の生成
		Random rand = new Random(System.currentTimeMillis());
		int value = rand.nextInt(5) + 1; // 1～5の数値を算出

		// 現在のカード取得
		List<CardsModel> cardList = cardsDAO.getGameCardList(gameSettingsModel.getGameType());
		CardsModel currentCard = cardList.get(cardList.size() - 1);

		model.addAttribute("CardList", cardList);
		model.addAttribute("CurrentCard", currentCard);

		// 前のカードと同じ値か判定
		boolean result = (value != currentCard.getValue());

		// 掛け金
		long betType = 10000; // 画面から選択させる

		// 倍率取得
		int magnification = cardList.size();

		// 金額演算
		long prize = betType * magnification;

		// メッセージ作成
		String logMessage;

		if (result) {
			// 成功
			logMessage = userModel.getNickName() + "さんが" + prize + "円獲得しました";
			model.addAttribute("resultMessage", "セーフ！ " + prize + "円獲得しました");

		} else {
			// 失敗
			logMessage = userModel.getNickName() + "さんが" + prize + "円失いました";
			model.addAttribute("resultMessage", "アウトー！ " + prize + "円失いました");
		}

		// 金額の符号判定
		if (!result) {
			prize = prize * (-1);
		}

		// カードデータの生成
		cardsDAO.createCardData(gameSettingsModel.getGameType(), userModel.getUserId(), value, result);

		// UserLastDraws更新or新規作成

		// 資産の増減
		userDAO.userAssetUpdate(userModel.getUserId(), prize);

		// 最新のカードデータを取得
		cardList = cardsDAO.getGameCardList(gameSettingsModel.getGameType());
		currentCard = cardList.get(cardList.size() - 1);
		model.addAttribute("NewCard", currentCard);

		// ログ作成
		gameLogAccessDAO.createGameLog(currentCard.getCardId(), userModel.getUserId(), magnification, betType,
				logMessage);

		// 最新のログ情報を取得
		List<GameLogsModel> logList = gameLogAccessDAO.getGameLogs(gameSettingsModel.getLogCounts());
		model.addAttribute("GameLogsList", logList);

		// ユーザーセッションの更新
		userDAO.updateUserSessionInformation(session);
		return "/game/donuts";
	}

}