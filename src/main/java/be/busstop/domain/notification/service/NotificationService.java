package be.busstop.domain.notification.service;

import be.busstop.domain.notification.dto.NotificationResponseDto;
import be.busstop.domain.notification.entity.Notification;
import be.busstop.domain.notification.repository.EmitterRepository;
import be.busstop.domain.notification.repository.NotificationRepository;
import be.busstop.domain.notification.util.AlarmType;
import be.busstop.domain.user.entity.User;
import be.busstop.global.security.UserDetailsImpl;
import com.amazonaws.services.kms.model.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterRepository emitterRepository ;
    private final NotificationRepository notificationRepository;

    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 1000L;


    public SseEmitter subscribe(UserDetailsImpl userDetails, String lastEventId) {
        Long userId = userDetails.getUser().getId();
        String emitterId = makeTimeIncludeId(userId);
        // lastEventId가 있을 경우, userId와 비교해서 유실된 데이터일 경우 재전송할 수 있다.

        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userId));

        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> {
            log.info("SSE 연결 Complete");
            emitterRepository.deleteById(emitterId);
//            onClientDisconnect(emitter, "Compeletion");
        });
        //시간이 만료된 경우 자동으로 레포지토리에서 삭제하고 클라이언트에서 재요청을 보낸다.
        emitter.onTimeout(() -> {
            log.info("SSE 연결 Timeout");
            emitterRepository.deleteById(emitterId);
//            onClientDisconnect(emitter, "Timeout");
        });
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));
        //Dummy 데이터를 보내 503에러 방지. (SseEmitter 유효시간 동안 어느 데이터도 전송되지 않으면 503에러 발생)
        String eventId = makeTimeIncludeId(userId);
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userId + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방한다.
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, userId, emitterId, emitter);
        }
        return emitter;
    }

    private String makeTimeIncludeId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    public void send(User receiver, AlarmType alarmType, String message, String senderNickname, String senderProfileImageUrl, String url) {
        //send() 메서드는 Member 객체와 AlarmType 열거형, 알림 메시지(String)와 알림 상태(Boolean) 값을 인자로 받아 기능을 구현한다.
        Notification notification = notificationRepository.save(createNotification(receiver, alarmType, message, senderNickname, senderProfileImageUrl, url));

        // Notification 객체의 수신자 ID를 추출하고,
        String receiverId = String.valueOf(receiver.getId());
        // 현재 시간을 포함한 고유한 eventId를 생성한다.
        String eventId = receiverId + "_" + System.currentTimeMillis();

        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(receiverId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotificationResponseDto.create(notification));
                }
        );
    }

    public void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            log.info("eventId : " + eventId);
            log.info("data" + data);
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("sse")
                    .data(data));

        } catch (IOException exception) {
            log.info("예외 발생해서 emitter 삭제됨");
            emitterRepository.deleteById(emitterId);
        }
    }

    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, Long memberId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId(String.valueOf(memberId));

        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    private Notification createNotification(User receiver, AlarmType alarmType, String message, String senderNickname, String senderProfileImageUrl, String url) {
        return Notification.builder()
                .receiver(receiver)
                .alarmType(alarmType)
                .message(message)
                .senderNickname(senderNickname)
                .senderProfileImageUrl(senderProfileImageUrl)
                .url(url)
                .build();
    }

    //받은 알림 전체 조회
    public List<NotificationResponseDto> getAllNotifications(Long userId) {

        List<Notification> notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId);
        log.info(userId + " 님이 알림 전체 조회를 했습니다.");
        return notifications.stream()
                .map(NotificationResponseDto::create )
                .collect(Collectors.toList());
    }

    // 선택된 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotFoundException("Notification not found"));

        // 확인한 유저가 알림을 받은 대상자가 아니라면 예외 발생
        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근권한이 없습니다.");
        }
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void allDeleteNotification(User user) {
        List<Notification> notificationList = notificationRepository.findAllByUser(user);
        notificationRepository.deleteAll(notificationList);
    }

    @Transactional
    public void readNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(()
                -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        notification.read();
        notificationRepository.save(notification);
    }

    public List<NotificationResponseDto> getUnreadNotification(User user) {
        List<Notification> notificationList = notificationRepository.findAllByUnread(false, user);
        return notificationList.stream().map(NotificationResponseDto::new).toList();
    }
}