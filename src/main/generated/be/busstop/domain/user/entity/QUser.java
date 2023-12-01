package be.busstop.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1448252346L;

    public static final QUser user = new QUser("user");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<UserRoleEnum> role = createEnum("role", UserRoleEnum.class);

    public final ListPath<be.busstop.domain.post.entity.PostSearchHistory, be.busstop.domain.post.entity.QPostSearchHistory> searchHistory = this.<be.busstop.domain.post.entity.PostSearchHistory, be.busstop.domain.post.entity.QPostSearchHistory>createList("searchHistory", be.busstop.domain.post.entity.PostSearchHistory.class, be.busstop.domain.post.entity.QPostSearchHistory.class, PathInits.DIRECT2);

    public final BooleanPath social = createBoolean("social");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

