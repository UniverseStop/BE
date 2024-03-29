package be.busstop.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoomEntity chatRoom;
    @Column
    private Long userId;
    @Column
    private String nickname;
    @Column
    private String age;
    @Column
    private String gender;
    @Column
    private String profileImageUrl;
    @Column(name = "entry_time")
    private LocalDateTime entryTime;
}