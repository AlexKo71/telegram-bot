package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.jvnet.hk2.annotations.Service;
import org.springframework.scheduling.annotation.Scheduled;
import pro.sky.telegrambot.controller.TelegramBotConfiguration;
import pro.sky.telegrambot.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ScheduledCheck {

    private final TelegramBot bot;
    private final NotificationRepository repository;

    public ScheduledCheck(TelegramBot bot, NotificationRepository repository) {
        this.bot = bot;
        this.repository = repository;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        repository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(task ->  {
                    bot.execute(new SendMessage(task.getChatId(),task.getText()));
                    repository.delete(task);
                });
    }
}
