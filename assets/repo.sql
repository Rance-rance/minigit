/*
 Navicat Premium Data Transfer

 Source Server         : tencent-BEMysql
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : 124.222.41.123:3306
 Source Schema         : repo

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 10/05/2023 17:51:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for branch
-- ----------------------------
DROP TABLE IF EXISTS `branch`;
CREATE TABLE `branch`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `repo_id` bigint(0) NULL DEFAULT NULL,
  `author_id` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `commit_hash` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `branch_repo_id_fk`(`repo_id`) USING BTREE,
  CONSTRAINT `branch_repo_id_fk` FOREIGN KEY (`repo_id`) REFERENCES `repo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1654045712963366914 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of branch
-- ----------------------------
INSERT INTO `branch` VALUES (1654045712963366913, 'main', 1654045712883675138, 1650840922839076866, '2023-05-04 16:50:03', NULL);

-- ----------------------------
-- Table structure for commit
-- ----------------------------
DROP TABLE IF EXISTS `commit`;
CREATE TABLE `commit`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `committer` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `message` varchar(510) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `hash` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `parent_hash` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `commit_branch_id_fk`(`branch_id`) USING BTREE,
  CONSTRAINT `commit_branch_id_fk` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1654057067703492611 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of commit
-- ----------------------------
INSERT INTO `commit` VALUES (1654057067703492610, 1654045712963366913, '2023-05-04 17:35:10', 'jizelong', 'First Commit!', 'e59f47a961dc5d635cf5e0362a0b4eeba33ecbcf', NULL);

-- ----------------------------
-- Table structure for repo
-- ----------------------------
DROP TABLE IF EXISTS `repo`;
CREATE TABLE `repo`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `author_id` bigint(0) NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `is_public` tinyint(1) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1654045712883675139 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repo
-- ----------------------------
INSERT INTO `repo` VALUES (1654045712883675138, 1650840922839076866, 'minigit-test', 1, '2023-05-04 16:50:03', 'D:\\Learning Data\\minigit-test');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `ID` bigint(0) NOT NULL AUTO_INCREMENT,
  `account_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `pwd` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1650840922839076867 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1650840922839076866, 'jizelong', '2023-04-25 20:35:22', '123456', 'jizelong@shu.edu.cn');

SET FOREIGN_KEY_CHECKS = 1;
