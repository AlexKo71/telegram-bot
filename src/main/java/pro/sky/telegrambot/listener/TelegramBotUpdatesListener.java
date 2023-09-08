package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationRepository repository;


    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            Message msg = update.message();
            if (msg != null && msg.text() != null) {
                if ("/start".equals(msg.text())) {
                    telegramBot.execute(new SendMessage(msg.chat().id(), "Welcome to the Robby Bot"));
                } else {
                    Matcher matcher = PATTERN.matcher(msg.text());
                    if (matcher.matches()) {
                        var dateTime = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
                        var taskText = matcher.group(3);
                        repository.save(new NotificationTask(taskText, msg.chat().id(), dateTime));
                    }
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}

