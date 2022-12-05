package com.example.chat.utils;

public class RedisKeyUtil {

    //个人验证
    public static final String PREFIX_USER_TOKEN = "user:token:";

    //激活验证码
    public static final String PREFIX_USER_CODE = "user:code:";

    //修改密码
    public static final String PREFIX_USER_EMAIL = "user:email:";

    //获得总赞
    public static final String PREFIX_USER_LIKE_TOTAL = "user:like:total";

    //收藏
    public static final String PREFIX_USER_COLLECT = "user:collect:";

    //关注
    public static final String PREFIX_USER_FOLLOW = "user:follow:";

    //粉丝列表
    public static final String PREFIX_USER_FANS = "user:fans:";

    public static final String PREFIX_POST_LIKE = "post:like:";

    public static final String PREFIX_POST_COLLECT = "post:collect:";

    public static final String PREDIX_POST_SCORE = "post:score";

    public static final String PREFIX_COMMENT_LIKE = "comment:like:";


    public static final String getUserTokenKey(String uuid) {
        return PREFIX_USER_TOKEN + uuid;
    }

    public static final String getUserCodeKey(String count) {
        return PREFIX_USER_CODE + count;
    }

    public static final String getUserEmailKey(String count) {
        return PREFIX_USER_EMAIL + count;
    }

    public static final String getUserLikeTotal(int userId) {
        return PREFIX_USER_LIKE_TOTAL + userId;
    }

    public static final String getUserCollect(int userId) {
        return PREFIX_USER_COLLECT + userId;
    }

    public static final String getUserFollow(int userId) {
        return PREFIX_USER_FOLLOW + userId;
    }

    public static final String getUserFans(int userId) {
        return PREFIX_USER_FANS + userId;
    }

    public static final String getPostLike(int postId) {
        return PREFIX_POST_LIKE + postId;
    }

    public static final String getPostCollect(int postId) {
        return PREFIX_POST_COLLECT + postId;
    }

    public static final String getCommentLike(int postId) {
        return PREFIX_COMMENT_LIKE + postId;
    }

}
