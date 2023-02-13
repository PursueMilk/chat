package com.example.chat.utils;

public class ConstantUtil {


    /**
     * 主题：注册
     */
    public static final String TOPIC_REGISTER = "inform.email.register";


    /**
     * 主题：忘记密码
     */
    public static final String TOPIC_FORGET = "inform.email.forget";


    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_PATTERN="^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$";

    /**
     * 实体类型: 帖子
     */
    public static final int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论
     */
    public static final int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型: 用户
     */
    public static final int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论
     */
    public static final String TOPIC_COMMENT = "inform.message.comment";

    /**
     * 主题: 点赞
     */
    public static final String TOPIC_LIKE = "inform.message.like";

    /**
     * 主题: 关注
     */
    public static final String TOPIC_FOLLOW = "inform.message.follow";


    /**
     * 主题: 收藏
     */
    public static final String TOPIC_COLLECT = "inform.message.collect";

    /**
     * 系统通知
     */
    public static final String TOPIC_SYSTEM = "inform.message.system";

    /**
     * 主题: 发帖
     */
    public static final String TOPIC_PUBLISH = "inform.es.publish";

    /**
     * 主题: 删帖
     */
    public static final String TOPIC_DELETE = "inform.es.delete";


    /**
     * 主题: 删帖
     */
    public static final String TOPIC_RESET_ES = "inform.es.reset";


    /**
     * 系统用户ID
     */
    public static final int SYSTEM_USER_ID = -1;

}
