package com.jccv.tuprivadaapp.service.pollingNotification.implementation;

import com.jccv.tuprivadaapp.dto.pollingNotification.PollingNotificationDto;
import com.jccv.tuprivadaapp.dto.pollingNotification.mapper.PollingNotificationMapper;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.pollingNotification.PollingNotification;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.repository.pollingNotification.PollingNotificationRepository;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.service.pollingNotification.PollingNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PollingNotificationServiceImp implements PollingNotificationService {

    private final PollingNotificationMapper pollingNotificationMapper;

    private final UserFacade userFacade;
    private final CondominiumService condominiumService;
    private final PollingNotificationRepository notificationRepository;

    @Autowired
    public PollingNotificationServiceImp(PollingNotificationMapper pollingNotificationMapper, UserFacade userFacade, CondominiumService condominiumService, PollingNotificationRepository notificationRepository) {
        this.pollingNotificationMapper = pollingNotificationMapper;
        this.userFacade = userFacade;
        this.condominiumService = condominiumService;
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

        Condominium condominium = condominiumService.findById(condominiumId);

        List<User> residents = condominium.getUsers().stream()
                .filter(user -> user.getRole().equals(Role.RESIDENT))
                .toList();

        for (User resident : residents) {
            createNotification(pollingNotificationDto.getTitle(), pollingNotificationDto.getMessage(), resident);
        }
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
