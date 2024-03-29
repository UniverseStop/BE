package be.busstop.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoomEntity is a Querydsl query type for ChatRoomEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoomEntity extends EntityPathBase<ChatRoomEntity> {

    private static final long serialVersionUID = -1140239202L;

    public static final QChatRoomEntity chatRoomEntity = new QChatRoomEntity("chatRoomEntity");

    public final be.busstop.global.utils.QTimestamped _super = new be.busstop.global.utils.QTimestamped(this);

    public final ListPath<ChatRoomParticipant, QChatRoomParticipant> chatRoomParticipants = this.<ChatRoomParticipant, QChatRoomParticipant>createList("chatRoomParticipants", ChatRoomParticipant.class, QChatRoomParticipant.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath lastMessage = createString("lastMessage");

    public final StringPath lastMessageSender = createString("lastMessageSender");

    public final StringPath lastMessageSenderProfileImageUrl = createString("lastMessageSenderProfileImageUrl");

    public final DateTimePath<java.time.LocalDateTime> lastMessageTime = createDateTime("lastMessageTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> MasterId = createNumber("MasterId", Long.class);

    public final StringPath MasterNickname = createString("MasterNickname");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath roomId = createString("roomId");

    public final NumberPath<Long> subLimit = createNumber("subLimit", Long.class);

    public final StringPath title = createString("title");

    public final StringPath titleImageUrl = createString("titleImageUrl");

    public QChatRoomEntity(String variable) {
        super(ChatRoomEntity.class, forVariable(variable));
    }

    public QChatRoomEntity(Path<? extends ChatRoomEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatRoomEntity(PathMetadata metadata) {
        super(ChatRoomEntity.class, metadata);
    }

}

