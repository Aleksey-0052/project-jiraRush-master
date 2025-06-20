package com.javarush.jira.bugtracking.task;

import com.javarush.jira.bugtracking.Handlers;
import com.javarush.jira.bugtracking.task.to.ActivityTo;
import com.javarush.jira.common.error.DataConflictException;
import com.javarush.jira.common.error.NotFoundException;
import com.javarush.jira.login.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.javarush.jira.bugtracking.task.TaskUtil.getLatestValue;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final TaskRepository taskRepository;

    private final Handlers.ActivityHandler handler;

    private static void checkBelong(HasAuthorId activity) {
        if (activity.getAuthorId() != AuthUser.authId()) {
            throw new DataConflictException("Activity " + activity.getId() + " doesn't belong to " + AuthUser.get());
        }
    }

    @Transactional
    public Activity create(ActivityTo activityTo) {
        checkBelong(activityTo);
        Task task = taskRepository.getExisted(activityTo.getTaskId());
        if (activityTo.getStatusCode() != null) {
            task.checkAndSetStatusCode(activityTo.getStatusCode());
        }
        if (activityTo.getTypeCode() != null) {
            task.setTypeCode(activityTo.getTypeCode());
        }
        return handler.createFromTo(activityTo);
    }

    @Transactional
    public void update(ActivityTo activityTo, long id) {
        checkBelong(handler.getRepository().getExisted(activityTo.getId()));
        handler.updateFromTo(activityTo, id);
        updateTaskIfRequired(activityTo.getTaskId(), activityTo.getStatusCode(), activityTo.getTypeCode());
    }

    @Transactional
    public void delete(long id) {
        Activity activity = handler.getRepository().getExisted(id);
        checkBelong(activity);
        handler.delete(activity.id());
        updateTaskIfRequired(activity.getTaskId(), activity.getStatusCode(), activity.getTypeCode());
    }

    private void updateTaskIfRequired(long taskId, String activityStatus, String activityType) {
        if (activityStatus != null || activityType != null) {
            Task task = taskRepository.getExisted(taskId);
            List<Activity> activities = handler.getRepository().findAllByTaskIdOrderByUpdatedDesc(task.id());
            if (activityStatus != null) {
                String latestStatus = getLatestValue(activities, Activity::getStatusCode);
                if (latestStatus == null) {
                    throw new DataConflictException("Primary activity cannot be delete or update with null values");
                }
                task.setStatusCode(latestStatus);
            }
            if (activityType != null) {
                String latestType = getLatestValue(activities, Activity::getTypeCode);
                if (latestType == null) {
                    throw new DataConflictException("Primary activity cannot be delete or update with null values");
                }
                task.setTypeCode(latestType);
            }
        }
    }

    /**
     * Метод по подсчету времени, сколько задача находилась в работе
     */
    public String calculateTimeTaskInProgress(long taskId) {
        Duration timeBetweenStatusCodes = calculateTimeBetweenStatusCodes(taskId,
                "in_progress", "ready_for_review");
        long numberOfDays = timeBetweenStatusCodes.toDays();
        long numberOfPartHours = timeBetweenStatusCodes.toHoursPart();
        long numberOfPartMinutes = timeBetweenStatusCodes.toMinutesPart();
        long numberOfPartSeconds = timeBetweenStatusCodes.toSecondsPart();
        return String.format("The total time the task in progress is %s days, %s hours, %s minutes, %s seconds",
                numberOfDays, numberOfPartHours, numberOfPartMinutes, numberOfPartSeconds);
    }

    /**
     * Метод по подсчету времени, сколько задача находилась на тестировании
     */
    public String calculateTimeTaskInTesting(long taskId) {
        Duration timeBetweenStatusCodes = calculateTimeBetweenStatusCodes(taskId,
                "ready_for_review", "done");
        long numberOfDays = timeBetweenStatusCodes.toDays();
        long numberOfPartHours = timeBetweenStatusCodes.toHours() % 24;
        long numberOfPartMinutes = timeBetweenStatusCodes.toMinutes() % 60;
        long numberOfPartSeconds = timeBetweenStatusCodes.toSeconds() % 60;
        return String.format("The total time the task in testing is %d days, %d hours, %d minutes, %d seconds",
                numberOfDays, numberOfPartHours, numberOfPartMinutes, numberOfPartSeconds);
    }

    private Duration calculateTimeBetweenStatusCodes(long taskId, String startStatusCode, String endStatusCode) {
        List<Activity> activities = handler.getRepository().findAllByTaskId(taskId);
        if (activities.isEmpty()) {
            throw new NotFoundException("The task with the id - " + taskId + " was not found, or the activity for the " +
                    "task with the id - " + taskId + " was not found");
        }

        LocalDateTime startUpdated = getUpdatedTimeFromActivity(activities, startStatusCode);
        LocalDateTime endUpdated = getUpdatedTimeFromActivity(activities, endStatusCode);

        if (startUpdated.isAfter(endUpdated)) {
            throw new DataConflictException("The time of the status сode: " + startStatusCode + " must be earlier " +
                    "than the time of the status code: " + endStatusCode);
        }
        return Duration.between(startUpdated, endUpdated);
    }

    private LocalDateTime getUpdatedTimeFromActivity(List<Activity> activities, String statusCode) {

        return activities.stream()
                .filter(activity -> activity.getStatusCode() != null)
                .filter(activity -> activity.getStatusCode().equals(statusCode))
                .map(Activity::getUpdated)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No activity with the status code " + statusCode + " was found"));
    }

}
