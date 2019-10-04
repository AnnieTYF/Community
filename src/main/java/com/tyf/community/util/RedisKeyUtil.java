package com.tyf.community.util;

/**
 * 统一处理redis的key
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    //前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    //某个实体的赞
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
