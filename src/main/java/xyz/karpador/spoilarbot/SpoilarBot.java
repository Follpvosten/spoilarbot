/*
 * Copyright (C) 2017 Follpvosten
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xyz.karpador.spoilarbot;

import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class SpoilarBot extends TelegramLongPollingBot {

	@Override
	public void onUpdateReceived(Update update) {
		try {
			if(update.hasMessage()) {
				if (update.getMessage().isUserMessage()) {
					Message message = update.getMessage();
					InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
					List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
					List<InlineKeyboardButton> row = new ArrayList<>();
					row.add(new InlineKeyboardButton()
						.setSwitchInlineQuery(message.getChatId() + ":" + message.getMessageId())
						.setText("Share message as spoiler")
					);
					buttons.add(row);
					markup.setKeyboard(buttons);
					SendMessage sendMessage = new SendMessage()
						.setChatId(message.getChatId())
						.setText("Placeholder (press my button)")
						.setReplyMarkup(markup);
					sendMessage(sendMessage);
				}
			} else if(update.hasInlineQuery()) {
				handleInlineQuery(update.getInlineQuery());
			} else if(update.hasCallbackQuery()) {
				CallbackQuery cq = update.getCallbackQuery();
				answerCallbackQuery(
					new AnswerCallbackQuery()
						.setCallbackQueryId(cq.getId())
						.setText("You may need to message me first")
				);
				String[] data = cq.getData().split(":");
				forwardMessage(
					new ForwardMessage()
						.setChatId(cq.getFrom().getId().longValue())
						.setFromChatId(Long.parseLong(data[0]))
						.setMessageId(Integer.parseInt(data[1]))
				);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	// Trololo
	private static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch(NumberFormatException e) {
			return false;
		} catch(NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	private void handleInlineQuery(InlineQuery inlineQuery) throws TelegramApiException {
		String query = inlineQuery.getQuery();
		if(!query.isEmpty()) {
			// Check if the query is actually valid
			String[] queryNumbers = query.split(":");
			if(queryNumbers.length != 2) return;
			// Throw and exception if we don't have numbers, yay!
			if(!isInteger(queryNumbers[0]) || !isInteger(queryNumbers[1]))
				return;

			InlineQueryResultArticle result = new InlineQueryResultArticle()
				.setHideUrl(true)
				.setTitle("Hidden Message")
				.setDescription("Try sending message as a spoiler")
				.setId("0");
			InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
			List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
			List<InlineKeyboardButton> row = new ArrayList<>();
			row.add(new InlineKeyboardButton().setText("Tell me!").setCallbackData(query));
			buttons.add(row);
			markup.setKeyboard(buttons);
			result.setReplyMarkup(markup);
			InputTextMessageContent content = new InputTextMessageContent()
				.disableWebPagePreview()
				.setMessageText("<Spoiler>");
			result.setInputMessageContent(content);
			AnswerInlineQuery answer = new AnswerInlineQuery()
				.setInlineQueryId(inlineQuery.getId())
				.setResults(result)
				.setPersonal(true);
			answerInlineQuery(answer);
		}
	}

	@Override
	public String getBotUsername() {
		return "spoilarbot";
	}

	@Override
	public String getBotToken() {
		return BotConfig.getInstance().getTelegramBotToken();
	}
}
