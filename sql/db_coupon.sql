/*
 Navicat Premium Data Transfer

 Source Server         : dev
 Source Server Type    : MySQL
 Source Server Version : 50727
 Source Host           : 10.101.40.21:3306
 Source Schema         : db_coupon

 Target Server Type    : MySQL
 Target Server Version : 50727
 File Encoding         : 65001

 Date: 06/09/2021 11:52:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for async_task
-- ----------------------------
DROP TABLE IF EXISTS `async_task`;
CREATE TABLE `async_task`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '异步导出业务类型',
  `down_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下载路径',
  `async_status` int(2) NULL DEFAULT NULL COMMENT '异步状态 0：异步任务待执行 1：异步执行成功 2： 异步执行失败',
  `records` int(11) NULL DEFAULT NULL COMMENT '生成文件的记录数',
  `create_file_time` timestamp(0) NULL DEFAULT NULL COMMENT '异步任务文件的生成时间',
  `success_record` int(11) NULL DEFAULT 0 COMMENT '成功数',
  `fail_record` int(11) NULL DEFAULT 0 COMMENT '失败数',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '异步任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for captchas
-- ----------------------------
DROP TABLE IF EXISTS `captchas`;
CREATE TABLE `captchas`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `mobile` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机',
  `captcha` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '验证码',
  `success_is` int(4) NULL DEFAULT 0 COMMENT '是否校验：0：否；1：是；',
  `expire_time` timestamp(0) NULL DEFAULT NULL,
  `business_type` int(11) NULL DEFAULT NULL COMMENT '短信业务类型',
  `is_deleted` bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除,0-未删除，id-已删除',
  `version_no` int(11) NULL DEFAULT NULL COMMENT '版本号',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建日期',
  `update_time` timestamp(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mobile`(`mobile`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '验证码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for clean_user
-- ----------------------------
DROP TABLE IF EXISTS `clean_user`;
CREATE TABLE `clean_user`  (
  `ddate` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dtime` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `couponid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `unionid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `utype` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coupon001
-- ----------------------------
DROP TABLE IF EXISTS `coupon001`;
CREATE TABLE `coupon001`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon002
-- ----------------------------
DROP TABLE IF EXISTS `coupon002`;
CREATE TABLE `coupon002`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon003
-- ----------------------------
DROP TABLE IF EXISTS `coupon003`;
CREATE TABLE `coupon003`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon004
-- ----------------------------
DROP TABLE IF EXISTS `coupon004`;
CREATE TABLE `coupon004`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon005
-- ----------------------------
DROP TABLE IF EXISTS `coupon005`;
CREATE TABLE `coupon005`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon006
-- ----------------------------
DROP TABLE IF EXISTS `coupon006`;
CREATE TABLE `coupon006`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon007
-- ----------------------------
DROP TABLE IF EXISTS `coupon007`;
CREATE TABLE `coupon007`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon008
-- ----------------------------
DROP TABLE IF EXISTS `coupon008`;
CREATE TABLE `coupon008`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon009
-- ----------------------------
DROP TABLE IF EXISTS `coupon009`;
CREATE TABLE `coupon009`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon010
-- ----------------------------
DROP TABLE IF EXISTS `coupon010`;
CREATE TABLE `coupon010`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon011
-- ----------------------------
DROP TABLE IF EXISTS `coupon011`;
CREATE TABLE `coupon011`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon012
-- ----------------------------
DROP TABLE IF EXISTS `coupon012`;
CREATE TABLE `coupon012`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon013
-- ----------------------------
DROP TABLE IF EXISTS `coupon013`;
CREATE TABLE `coupon013`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon014
-- ----------------------------
DROP TABLE IF EXISTS `coupon014`;
CREATE TABLE `coupon014`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon015
-- ----------------------------
DROP TABLE IF EXISTS `coupon015`;
CREATE TABLE `coupon015`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon016
-- ----------------------------
DROP TABLE IF EXISTS `coupon016`;
CREATE TABLE `coupon016`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon017
-- ----------------------------
DROP TABLE IF EXISTS `coupon017`;
CREATE TABLE `coupon017`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon018
-- ----------------------------
DROP TABLE IF EXISTS `coupon018`;
CREATE TABLE `coupon018`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon019
-- ----------------------------
DROP TABLE IF EXISTS `coupon019`;
CREATE TABLE `coupon019`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon020
-- ----------------------------
DROP TABLE IF EXISTS `coupon020`;
CREATE TABLE `coupon020`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon021
-- ----------------------------
DROP TABLE IF EXISTS `coupon021`;
CREATE TABLE `coupon021`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon022
-- ----------------------------
DROP TABLE IF EXISTS `coupon022`;
CREATE TABLE `coupon022`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon023
-- ----------------------------
DROP TABLE IF EXISTS `coupon023`;
CREATE TABLE `coupon023`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon024
-- ----------------------------
DROP TABLE IF EXISTS `coupon024`;
CREATE TABLE `coupon024`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon025
-- ----------------------------
DROP TABLE IF EXISTS `coupon025`;
CREATE TABLE `coupon025`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon026
-- ----------------------------
DROP TABLE IF EXISTS `coupon026`;
CREATE TABLE `coupon026`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon027
-- ----------------------------
DROP TABLE IF EXISTS `coupon027`;
CREATE TABLE `coupon027`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon028
-- ----------------------------
DROP TABLE IF EXISTS `coupon028`;
CREATE TABLE `coupon028`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon029
-- ----------------------------
DROP TABLE IF EXISTS `coupon029`;
CREATE TABLE `coupon029`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon030
-- ----------------------------
DROP TABLE IF EXISTS `coupon030`;
CREATE TABLE `coupon030`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon031
-- ----------------------------
DROP TABLE IF EXISTS `coupon031`;
CREATE TABLE `coupon031`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon032
-- ----------------------------
DROP TABLE IF EXISTS `coupon032`;
CREATE TABLE `coupon032`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NOT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券活动名称',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券（自动生成） 1实体券（预制券） 2红包券 3第三方券码',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `source` tinyint(4) NOT NULL COMMENT '创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让',
  `source_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id',
  `user_type` int(11) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `device_mac` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_before_give
-- ----------------------------
DROP TABLE IF EXISTS `coupon_before_give`;
CREATE TABLE `coupon_before_give`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_id` bigint(20) NOT NULL COMMENT '券ID',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `give_type` tinyint(4) NULL DEFAULT NULL COMMENT '转赠类型 1短信赠送 2面对面赠送 3微信好友分享',
  `give_userid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '赠送者userid',
  `give_user_type` tinyint(4) NULL DEFAULT NULL COMMENT '0 B端、1 saas端、2 C端、3旧机构',
  `give_avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '赠送者头像地址',
  `give_nickname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '赠送者呢称',
  `give_user_mobile` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '赠送者手机号',
  `terminal_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '取值为android/ios/web/miniapp  app就是传的android、ios',
  `receive_user_mobile` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '接受者手机号',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_userid` bigint(20) NULL DEFAULT NULL COMMENT '更新用户ID',
  `update_username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新用户名',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponid_giveuserid`(`coupon_id`, `give_userid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '劵赠送前记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_generate_batch
-- ----------------------------
DROP TABLE IF EXISTS `coupon_generate_batch`;
CREATE TABLE `coupon_generate_batch`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '券生成批次',
  `type` int(11) NOT NULL COMMENT '券生成类型 0: 批次生券， 为线下 1：批次发券 - 批量导入, 2：批次发券 - 优惠券手动批量导入，3：导入券码',
  `theme_id` bigint(20) NOT NULL COMMENT '券活动ID',
  `generate_nums` int(11) NULL DEFAULT NULL COMMENT '生券张数',
  `upload_file` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '批量导入附件',
  `total_record` int(11) NULL DEFAULT NULL COMMENT '总记录数',
  `send_coupon_status` tinyint(4) NULL DEFAULT NULL COMMENT '发券状态（0：发送中，1：发送完成）',
  `success_record` int(11) NULL DEFAULT NULL COMMENT '成功发送记录数',
  `fail_record` int(11) NULL DEFAULT NULL COMMENT '失败发送记录数',
  `finish_time` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '发送完成时间',
  `download_file` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '批量下载附件',
  `fail_reason` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_createtime`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2106160000016874 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵批次表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_give
-- ----------------------------
DROP TABLE IF EXISTS `coupon_give`;
CREATE TABLE `coupon_give`  (
  `coupon_id` bigint(20) UNSIGNED NOT NULL COMMENT '优惠券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `give_type` tinyint(4) NULL DEFAULT NULL COMMENT '转赠类型 1赠送 2转让',
  `give_user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '转赠人id',
  `give_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '转赠人名称',
  `give_user_mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '转赠人手机号',
  `give_time` timestamp(0) NULL DEFAULT NULL COMMENT '转赠时间',
  `receive_user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接受人id',
  `receive_user_mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接受手机号',
  `receive_coupon_id` bigint(20) NULL DEFAULT NULL COMMENT '接收人获得的新的券id',
  `receive_user_type` tinyint(4) NULL DEFAULT NULL COMMENT '接收人用户类型,0是会员,1是机构经纪人,2是C端用户',
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`coupon_id`) USING BTREE,
  INDEX `idx_receiveCouponId`(`receive_coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵转赠表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_opr_log
-- ----------------------------
DROP TABLE IF EXISTS `coupon_opr_log`;
CREATE TABLE `coupon_opr_log`  (
  `id` bigint(20) NOT NULL,
  `opr_theme_type` int(11) NULL DEFAULT 1 COMMENT '操作类型 1 券 2 券活动',
  `opr_type` int(11) NULL DEFAULT NULL COMMENT '操作类型 1.新建 2.提交审核 3.审核 4.驳回 5.生券 6.编辑 7.复制 8.关闭 9.发券 10.导入券码 11.删除 12.查看 101.作废 102.冻结 103.解冻 104.延期',
  `opr_summary` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作概述：比如：修改模板',
  `opr_ref_id` bigint(20) NOT NULL COMMENT '日志关联主体id',
  `ext_data` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其它扩展数据',
  `oper_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oprRefId`(`opr_ref_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '券相关操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_send_log
-- ----------------------------
DROP TABLE IF EXISTS `coupon_send_log`;
CREATE TABLE `coupon_send_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动ID',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发券事务id',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_themeid_transid`(`coupon_theme_id`, `transaction_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '发券日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coupon_theme
-- ----------------------------
DROP TABLE IF EXISTS `coupon_theme`;
CREATE TABLE `coupon_theme`  (
  `id` bigint(20) NOT NULL,
  `activity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '活动名称',
  `theme_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优惠券名称',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '活动结束时间',
  `theme_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '活动描述',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '活动状态 0 未审核 1 待审核 2 未开始 3 审核不通过 4 进行中 5 已过期 6 已关闭',
  `theme_type` tinyint(4) NULL DEFAULT NULL COMMENT '活动类型 0：平台券  11：商家券 5：集团券 21：店铺券',
  `coupon_type` tinyint(4) NOT NULL COMMENT '券类型 0电子券 1实体券/预制券 2红包券 3：第三方券码',
  `coupon_give_rule` tinyint(4) NULL DEFAULT NULL COMMENT '发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)',
  `use_limit` decimal(14, 4) UNSIGNED ZEROFILL NULL DEFAULT 0000000000.0000 COMMENT '使用限制  0：无限制， 其他：最小金额限制',
  `applicable_user_types` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发布人群,0是会员,1是机构经纪人,2是C端用户',
  `order_use_limit` int(11) NULL DEFAULT 1 COMMENT '单个订单该类型券使用张数限制',
  `coupon_discount_type` tinyint(4) NULL DEFAULT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `discount_amount` decimal(14, 4) NULL DEFAULT NULL COMMENT '券优惠类型 金额时?元 折扣时折扣上限价格?元',
  `discount_value` int(11) NULL DEFAULT NULL COMMENT '券优惠类型 折扣时 ?折 乘于100后的值',
  `coupon_pic_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '券图片地址',
  `eff_date_calc_method` tinyint(4) NULL DEFAULT NULL COMMENT '有效期计算方式  1：固定有效期，2：从领用开始计算',
  `eff_date_start_time` datetime(0) NULL DEFAULT NULL COMMENT '固定有效期开始时间',
  `eff_date_end_time` datetime(0) NULL DEFAULT NULL COMMENT '固定有效期结束时间',
  `eff_date_days` int(11) NULL DEFAULT NULL COMMENT '自用户领取几天后失效',
  `individual_limit` int(11) NULL DEFAULT 0 COMMENT '当前券活动个人可领取券数',
  `every_day_limit` int(11) NULL DEFAULT NULL COMMENT '个人每日限领张数',
  `every_month_limit` int(11) NULL DEFAULT NULL COMMENT '个人每月限领张数',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `belonging_org_id` bigint(20) NULL DEFAULT NULL COMMENT '费用归属组织id',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  `can_donation` tinyint(1) NULL DEFAULT NULL,
  `can_transfer` tinyint(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_theme_title`(`theme_title`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵活动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_theme_org
-- ----------------------------
DROP TABLE IF EXISTS `coupon_theme_org`;
CREATE TABLE `coupon_theme_org`  (
  `id` bigint(20) NOT NULL,
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '关联的优惠券ID',
  `org_id` bigint(20) NOT NULL COMMENT '关联商家ID',
  `org_level_code` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_couponThemeId`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '优惠券所属组织商家关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coupon_theme_statistic
-- ----------------------------
DROP TABLE IF EXISTS `coupon_theme_statistic`;
CREATE TABLE `coupon_theme_statistic`  (
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `total_count` int(11) NOT NULL DEFAULT 0 COMMENT '当前券活动总可领取券数',
  `created_count` int(11) NOT NULL DEFAULT 0 COMMENT '当前券活动已生成券数',
  `sended_count` int(11) NOT NULL DEFAULT 0 COMMENT '已发数量',
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵活动统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_third
-- ----------------------------
DROP TABLE IF EXISTS `coupon_third`;
CREATE TABLE `coupon_third`  (
  `coupon_id` bigint(20) UNSIGNED NOT NULL COMMENT '优惠券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `third_coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '第三方优惠券码',
  `third_coupon_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '第三方优惠券密码(保存密文)',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`coupon_id`) USING BTREE,
  INDEX `idx_couponThemeId`(`coupon_theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '第三方劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_user
-- ----------------------------
DROP TABLE IF EXISTS `coupon_user`;
CREATE TABLE `coupon_user`  (
  `coupon_id` bigint(20) UNSIGNED NOT NULL COMMENT '优惠券ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `bind_tel` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '绑定人手机号',
  `user_type` tinyint(4) NULL DEFAULT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `status` tinyint(4) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`coupon_id`) USING BTREE,
  INDEX `idx_userid_usertype`(`user_id`, `user_type`) USING BTREE,
  INDEX `idx_themeid`(`coupon_theme_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_endtime`(`end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_user_statistic
-- ----------------------------
DROP TABLE IF EXISTS `coupon_user_statistic`;
CREATE TABLE `coupon_user_statistic`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '绑定用户id',
  `user_type` tinyint(4) NOT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `total_count` int(11) NOT NULL DEFAULT 0 COMMENT '总领券数量',
  `month_count` int(11) NOT NULL DEFAULT 0 COMMENT '当月领券数量',
  `today_count` int(11) NOT NULL DEFAULT 0 COMMENT '当天领券数量',
  `last_receive_date` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '最后领券日期',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  `version_no` int(11) NOT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_themeid_userid_usertype`(`coupon_theme_id`, `user_id`, `user_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2108260000000039 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户领券统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coupon_verification
-- ----------------------------
DROP TABLE IF EXISTS `coupon_verification`;
CREATE TABLE `coupon_verification`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint(20) NOT NULL COMMENT '券ID',
  `theme_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优惠券名称',
  `order_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用券的订单号',
  `coupon_theme_id` bigint(20) NOT NULL COMMENT '券活动id',
  `coupon_discount_type` tinyint(4) NOT NULL COMMENT '券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券',
  `coupon_value` decimal(14, 2) NULL DEFAULT NULL COMMENT '折扣时?折 乘于100后的值 金额时?元',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '券码',
  `subscribe_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用券的明源认购书编号',
  `bind_user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定用户id',
  `bind_tel` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定手机号',
  `user_type` tinyint(4) NOT NULL COMMENT '用户类型,0是会员,1是机构经纪人,2是C端用户',
  `used_time` timestamp(0) NULL DEFAULT NULL COMMENT '券核销时间',
  `used_channel` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '核销渠道 0后台手动核销 1明源核销',
  `used_store_id` bigint(20) NULL DEFAULT NULL COMMENT '核销店铺id --迁移数据，后续程序也可使用',
  `used_store_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销店铺编码 --迁移数据，后续程序也可使用',
  `used_store_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销店铺名称 --迁移数据，后续程序也可使用',
  `used_room_guid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销房源guid --迁移数据，后续程序也可使用',
  `product_name` varchar(257) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销商品名称',
  `product_code` varchar(257) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销商品编码',
  `product_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '核销商品金额',
  `status` int(11) NOT NULL COMMENT '券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结',
  `coupon_create_time` datetime(0) NOT NULL COMMENT '优惠券生券时间',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `verify_userid` bigint(20) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '核销人id',
  `verify_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销人名称',
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `u_couponid`(`coupon_id`) USING BTREE,
  INDEX `idx_couponthemeid`(`coupon_theme_id`) USING BTREE,
  INDEX `idx_couponcode`(`coupon_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '劵使用表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for coupon_video
-- ----------------------------
DROP TABLE IF EXISTS `coupon_video`;
CREATE TABLE `coupon_video`  (
  `coupon_id` bigint(20) NOT NULL COMMENT '券Id',
  `coupon_theme_id` bigint(20) NULL DEFAULT NULL COMMENT '券活动ID',
  `open_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'openid',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '城市',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `video_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '视频号',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频直播领券信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mkt_use_rule
-- ----------------------------
DROP TABLE IF EXISTS `mkt_use_rule`;
CREATE TABLE `mkt_use_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rule_type` int(11) NOT NULL COMMENT '6店铺 1商家 11集团',
  `limit_ref` bigint(20) NOT NULL COMMENT '组织id,即orgId',
  `ref_description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '组织名称,即orgName',
  `extend_ref` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '组织代码,即orgCode',
  `theme_ref` bigint(20) NOT NULL COMMENT '券活动主键id',
  `create_userid` bigint(20) NULL DEFAULT NULL,
  `create_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  `update_userid` bigint(20) NULL DEFAULT NULL,
  `update_username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` timestamp(0) NULL DEFAULT NULL,
  `version_no` int(11) NULL DEFAULT NULL,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除字段 0 正常 1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_themeref`(`theme_ref`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2108110000000012 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '规则设置表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
