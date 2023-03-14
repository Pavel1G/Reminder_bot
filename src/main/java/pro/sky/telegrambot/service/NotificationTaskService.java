package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Transactional
    public void addNotificationTask(LocalDateTime dateTime, String message, Long userId) {
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setNotificationDateTime(dateTime);
        notificationTask.setMessage(message);
        notificationTask.setId(userId);
        notificationTaskRepository.save(notificationTask);
    }

    public List<NotificationTask> findNotificationTaskForSend() {
        return notificationTaskRepository.findNotificationTaskByNotificationDateTime(LocalDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES));
    }

    public void deleteTask(NotificationTask notificationTask) {
        notificationTaskRepository.delete(notificationTask);
    }
}
