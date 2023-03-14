package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final Pattern NOTIFICATION_TASK_PATTERN = Pattern.compile(
            "([\\d\\\\.:\\s]{16})(\\s)([А-яA-z\\s\\d,.!?]+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                if (update.callbackQuery() != null) {

                } else if (update.message().text() != null) {
                    String text = update.message().text();
                    Long chatId = update.message().chat().id();
                    if ("/start".equals(text)) {
                        SendMessage sendMessage = new SendMessage(chatId,
                                "Для планирования задачи отправьте ее в формате *01.01.2001 12:00 Сделать работу*");
                        sendMessage.parseMode(ParseMode.Markdown);
                        telegramBot.execute(sendMessage);
                    } else if (text != null) {
                        // Проверяем на соответствие шаблону NOTIFICATION_TASK_PATTERN
                        Matcher matcher = NOTIFICATION_TASK_PATTERN.matcher(text);
                        if (matcher.find()) {
                            LocalDateTime dateTime = parse(matcher.group(1));

                            // Если дата/время не null
                            if (!Objects.isNull(dateTime)) {
                                String message = matcher.group(3);
                                notificationTaskService.addNotificationTask(dateTime, message, chatId);
                                telegramBot.execute(new SendMessage(chatId, "Ваша задача запланирована!"));
                            } else {
                                telegramBot.execute(new SendMessage(chatId, "Некорректный формат даты и/или времени"));
                            }
                        } else {
                            telegramBot.execute(new SendMessage(chatId,
                                    "Некорректный формат задачи для планирования! Формат: \n" +
                                            "*01.01.2001 12:00 Сделать работу*\""));
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }
}
