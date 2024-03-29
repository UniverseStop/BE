package be.busstop.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChatMessageEntity is a Querydsl query type for ChatMessageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatMessageEntity extends EntityPathBase<ChatMessageEntity> {

    private static final long serialVersionUID = 581737162L;

    public static final QChatMessageEntity chatMessageEntity = new QChatMessageEntity("chatMessageEntity");

    public final StringPath createdAt = createString("createdAt");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath message = createString("message");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final StringPath roomId = createString("roomId");

    public final StringPath sender = createString("sender");

    public final NumberPath<Long> senderId = createNumber("senderId", Long.class);

    public final EnumPath<be.busstop.domain.chat.dto.ChatMessage.MessageType> type = createEnum("type", be.busstop.domain.chat.dto.ChatMessage.MessageType.class);

    public final NumberPath<Long> userCount = createNumber("userCount", Long.class);

    public QChatMessageEntity(String variable) {
        super(ChatMessageEntity.class, forVariable(variable));
    }

    public QChatMessageEntity(Path<? extends ChatMessageEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatMessageEntity(PathMetadata metadata) {
        super(ChatMessageEntity.class, metadata);
    }

}

