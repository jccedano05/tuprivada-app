package com.jccv.tuprivadaapp.service.pollingNotification.implementation;

import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.dto.pollingNotification.mapper.PollingNotificationMapper;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.pollingNotification.PollingNotification;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.repository.pollingNotification.PollingNotificationRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PollingNotificationServiceImp implements PollingNotificationService {

    private final PollingNotificationMapper pollingNotificationMapper;

    private final UserFacade userFacade;
    private final PollingNotificationRepository notificationRepository;

    @Autowired
    public PollingNotificationServiceImp(PollingNotificationMapper pollingNotificationMapper, UserFacade userFacade, CondominiumService condominiumService, PollingNotificationRepository notificationRepository) {
        this.pollingNotificationMapper = pollingNotificationMapper;
        this.userFacade = userFacade;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void createNotification(PollingNotificationDto pollingNotificationDto) {

        User recipient = userFacade.findById(pollingNotificationDto.getUserId());

        PollingNotification notification = PollingNotification.builder()
                .title(pollingNotificationDto.getTitle())
                .message(pollingNotificationDto.getMessage())
                .read(false)
                .createdAt(LocalDateTime.now())
                .user(recipient).build();
        notificationRepository.save(notification);
    }

    public void createNotification(String title, String message, User user) {


        PollingNotification notification = PollingNotification.builder()
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .user(user).build();
        notificationRepository.save(notification);
    }

    @Override
    public void createNotificationForCondominium(Long condominiumId, PollingNotificationDto pollingNotificationDto) {

        List<User> users = userFacade.findUsersResidentByCondominiumId(condominiumId);

        savePollingNotificationsByUserList(pollingNotificationDto, users);


//        for (User resident : residents) {
//            createNotification(pollingNotificationDto.getTitle(), pollingNotificationDto.getMessage(), resident);
//        }
    }

    @Override
    public void createNotificationForByUserIds(List<Long> userIds, PollingNotificationDto pollingNotificationDto) {

        List<User> users = userFacade.findUsersByUserIds(userIds);

        savePollingNotificationsByUserList(pollingNotificationDto, users);
    }

    @Override
    public void createNotificationForUserList(List<User> users, PollingNotificationDto pollingNotificationDto) {


        savePollingNotificationsByUserList(pollingNotificationDto, users);
    }

    private void savePollingNotificationsByUserList(PollingNotificationDto pollingNotificationDto, List<User> users){

        // Crear una lista de notificaciones para guardar en lote
        List<PollingNotification> notifications = new ArrayList<>();

        for (User user : users) {
            PollingNotification notification =  PollingNotification.builder()
                    .title(pollingNotificationDto.getTitle())
                    .message(pollingNotificationDto.getMessage())
                    .user(user)
                    .build();
            notifications.add(notification);
        }

        // Guardar todas las notificaciones en una sola operación (en lote)
        notificationRepository.saveAll(notifications);
    }

    @Override
    public List<PollingNotificationDto> getUnreadNotificationsForUser(Long userId) {
        User user = userFacade.findById(userId);
        return notificationRepository.findByUserAndReadFalse(user).stream().map(pollingNotificationMapper::convertToDto).toList();
    }


    @Override
    public void markNotificationsAsRead(List<Long> notificationIds) {
        List<PollingNotification> notifications = notificationRepository.findAllById(notificationIds);
        for (PollingNotification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

}
