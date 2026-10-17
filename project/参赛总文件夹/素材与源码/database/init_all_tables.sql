-- =============================================
-- 模拟投资平台 完整数据库初始化脚本
-- 创建时间：2026-04-30
-- 说明：包含所有业务表结构，按依赖顺序创建
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 第一部分：基础用户表
-- =============================================

-- 删除已存在的表（按依赖关系倒序删除）
DROP TABLE IF EXISTS post_like;
DROP TABLE IF EXISTS post_comment;
DROP TABLE IF EXISTS community_post;
DROP TABLE IF EXISTS user_badge;
DROP TABLE IF EXISTS badge;
DROP TABLE IF EXISTS user_message;
DROP TABLE IF EXISTS study_partner;
DROP TABLE IF EXISTS circle_post;
DROP TABLE IF EXISTS circle_member;
DROP TABLE IF EXISTS study_circle;
DROP TABLE IF EXISTS investment_prediction;
DROP TABLE IF EXISTS user_risk_profile;
DROP TABLE IF EXISTS user_experience;
DROP TABLE IF EXISTS user_honor;
DROP TABLE IF EXISTS user_stock_hold;
DROP TABLE IF EXISTS user_asset;
DROP TABLE IF EXISTS user;

-- ---------------------------------------------
-- 表1：user 用户表（登录信息）
-- ---------------------------------------------
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    nickname VARCHAR(50) COMMENT '用户昵称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------
-- 表2：user_asset 用户资产配置表
-- ---------------------------------------------
CREATE TABLE user_asset (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    stock_ratio INT DEFAULT 0 COMMENT '股票占比%',
    fund_ratio INT DEFAULT 0 COMMENT '基金占比%',
    bond_ratio INT DEFAULT 0 COMMENT '债券占比%',
    etf_ratio INT DEFAULT 0 COMMENT 'ETF占比%',
    derivatives_ratio INT DEFAULT 0 COMMENT '衍生品占比%',
    cash_ratio INT DEFAULT 0 COMMENT '现金占比%',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产配置表';

-- ---------------------------------------------
-- 表3：user_stock_hold 用户持仓股票明细表
-- ---------------------------------------------
CREATE TABLE user_stock_hold (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    stock_code VARCHAR(10) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(50) NOT NULL COMMENT '股票名称',
    hold_num INT NOT NULL COMMENT '持有数量',
    buy_price DECIMAL(10,2) DEFAULT 0 COMMENT '持仓成本价',
    hold_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '持仓时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户持仓股票明细表';

-- =============================================
-- 第二部分：功勋值与勋章系统
-- =============================================

-- ---------------------------------------------
-- 表4：user_honor 用户功勋值表
-- ---------------------------------------------
CREATE TABLE user_honor (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    honor_value INT DEFAULT 0 COMMENT '功勋值',
    honor_level VARCHAR(20) DEFAULT '新手' COMMENT '功勋等级',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户功勋值表';

-- ---------------------------------------------
-- 表5：badge 勋章表
-- ---------------------------------------------
CREATE TABLE badge (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '勋章名称',
    description VARCHAR(200) COMMENT '勋章描述',
    icon_url VARCHAR(255) COMMENT '勋章图标URL',
    condition_type VARCHAR(50) COMMENT '获取条件类型',
    condition_value INT COMMENT '获取条件值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勋章表';

-- ---------------------------------------------
-- 表6：user_badge 用户勋章关联表
-- ---------------------------------------------
CREATE TABLE user_badge (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    badge_id INT NOT NULL COMMENT '勋章ID',
    earned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    UNIQUE KEY uk_user_badge (user_id, badge_id),
    KEY idx_user_id (user_id),
    KEY idx_badge_id (badge_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户勋章关联表';

-- =============================================
-- 第三部分：经验值系统
-- =============================================

-- ---------------------------------------------
-- 表7：user_experience 用户经验值表
-- ---------------------------------------------
CREATE TABLE user_experience (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    exp_value INT DEFAULT 0 COMMENT '经验值',
    level INT DEFAULT 1 COMMENT '等级',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户经验值表';

-- =============================================
-- 第四部分：学习圈子系统
-- =============================================

-- ---------------------------------------------
-- 表8：study_circle 学习圈子表
-- ---------------------------------------------
CREATE TABLE study_circle (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '圈子名称',
    description VARCHAR(500) COMMENT '圈子描述',
    creator_id INT NOT NULL COMMENT '创建者ID',
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    member_count INT DEFAULT 1 COMMENT '成员数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_creator_id (creator_id),
    FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习圈子表';

-- ---------------------------------------------
-- 表9：circle_member 圈子成员表
-- ---------------------------------------------
CREATE TABLE circle_member (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    circle_id INT NOT NULL COMMENT '圈子ID',
    user_id INT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) DEFAULT 'member' COMMENT '角色：admin/member',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_circle_user (circle_id, user_id),
    KEY idx_circle_id (circle_id),
    KEY idx_user_id (user_id),
    FOREIGN KEY (circle_id) REFERENCES study_circle(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员表';

-- ---------------------------------------------
-- 表10：circle_post 圈子帖子表
-- ---------------------------------------------
CREATE TABLE circle_post (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    circle_id INT NOT NULL COMMENT '圈子ID',
    user_id INT NOT NULL COMMENT '用户ID',
    content TEXT COMMENT '帖子内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_circle_id (circle_id),
    KEY idx_user_id (user_id),
    FOREIGN KEY (circle_id) REFERENCES study_circle(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子帖子表';

-- =============================================
-- 第五部分：学习伙伴系统
-- =============================================

-- ---------------------------------------------
-- 表11：study_partner 学习伙伴表
-- ---------------------------------------------
CREATE TABLE study_partner (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    partner_id INT NOT NULL COMMENT '伙伴ID',
    match_score INT COMMENT '匹配分数',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/accepted/rejected',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_partner (user_id, partner_id),
    KEY idx_user_id (user_id),
    KEY idx_partner_id (partner_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (partner_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习伙伴表';

-- ---------------------------------------------
-- 表12：user_message 用户私信表
-- ---------------------------------------------
CREATE TABLE user_message (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    receiver_id INT NOT NULL COMMENT '接收者ID',
    content TEXT COMMENT '消息内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_sender_id (sender_id),
    KEY idx_receiver_id (receiver_id),
    KEY idx_created_at (created_at),
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户私信表';

-- =============================================
-- 第六部分：投资预测系统
-- =============================================

-- ---------------------------------------------
-- 表13：investment_prediction 投资预测记录表
-- ---------------------------------------------
CREATE TABLE investment_prediction (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    stock_code VARCHAR(20) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(50) COMMENT '股票名称',
    prediction_type VARCHAR(20) NOT NULL COMMENT '预测类型：rise/fall/short/mid',
    predicted_price DECIMAL(10,2) COMMENT '预测价格',
    actual_price DECIMAL(10,2) COMMENT '实际价格',
    prediction_date DATETIME COMMENT '预测日期',
    result_date DATETIME COMMENT '结果日期',
    is_accurate BOOLEAN COMMENT '是否准确',
    confidence DECIMAL(5,2) COMMENT '预测置信度',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_stock_code (stock_code),
    KEY idx_prediction_date (prediction_date),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资预测记录表';

-- =============================================
-- 第七部分：风险测评系统
-- =============================================

-- ---------------------------------------------
-- 表14：user_risk_profile 用户风险偏好表
-- ---------------------------------------------
CREATE TABLE user_risk_profile (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    risk_type VARCHAR(20) NOT NULL COMMENT '风险类型：保守型/稳健型/平衡型/成长型/进取型',
    risk_score INT COMMENT '风险分数(1-5)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户风险偏好表';

-- =============================================
-- 第八部分：社区帖子系统
-- =============================================

-- ---------------------------------------------
-- 表15：community_post 社区帖子表
-- ---------------------------------------------
CREATE TABLE community_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    title VARCHAR(200) COMMENT '帖子标题',
    content TEXT COMMENT '帖子内容',
    tag VARCHAR(50) COMMENT '标签',
    tag_color VARCHAR(20) COMMENT '标签颜色',
    avatar_color VARCHAR(20) COMMENT '头像颜色',
    views INT DEFAULT 0 COMMENT '浏览数',
    comments INT DEFAULT 0 COMMENT '评论数',
    likes INT DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_created_at (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- ---------------------------------------------
-- 表16：post_comment 帖子评论表
-- ---------------------------------------------
CREATE TABLE post_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    content TEXT COMMENT '评论内容',
    avatar_color VARCHAR(20) COMMENT '头像颜色',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id),
    FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

-- ---------------------------------------------
-- 表17：post_like 帖子点赞表
-- ---------------------------------------------
CREATE TABLE post_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY unique_like (post_id, user_id),
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id),
    FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- =============================================
-- 第九部分：初始数据
-- =============================================

-- 插入测试用户 (密码123456是明文，实际使用请加密)
INSERT INTO user (username, password, nickname) VALUES
('user1', '123456', '理财新手'),
('user2', '123456', '基金达人'),
('user3', '123456', '债券玩家'),
('user4', '123456', 'ETF投资者'),
('user5', '123456', '价值投资派'),
('user6', '123456', '分散配置者'),
('user7', '123456', '激进交易者'),
('user8', '123456', '稳健理财师'),
('user9', '123456', '指数定投者'),
('user10', '123456', '金融学习者');

-- 插入用户资产配置
INSERT INTO user_asset (user_id, stock_ratio, fund_ratio, bond_ratio, etf_ratio, derivatives_ratio, cash_ratio) VALUES
(1, 20, 30, 20, 15, 0, 15),
(2, 10, 60, 10, 15, 0, 5),
(3, 5, 10, 70, 5, 0, 10),
(4, 0, 20, 10, 60, 0, 10),
(5, 40, 20, 20, 10, 5, 5),
(6, 25, 25, 25, 20, 3, 2),
(7, 50, 10, 5, 20, 10, 5),
(8, 15, 25, 35, 15, 0, 10),
(9, 0, 30, 10, 50, 0, 10),
(10, 30, 20, 20, 20, 5, 5);

-- 插入用户持仓股票明细
INSERT INTO user_stock_hold (user_id, stock_code, stock_name, hold_num, buy_price) VALUES
(1, '600519', '贵州茅台', 100, 1800.00),
(1, '000858', '五粮液', 200, 160.00),
(2, '601318', '中国平安', 500, 45.00),
(2, '600036', '招商银行', 300, 35.00),
(3, '600519', '贵州茅台', 50, 1750.00),
(4, '510300', '沪深300ETF', 1000, 3.80),
(4, '159915', '创业板ETF', 500, 1.90),
(5, '600519', '贵州茅台', 200, 1700.00),
(5, '601888', '中国中免', 150, 70.00),
(6, '600519', '贵州茅台', 80, 1850.00),
(6, '000858', '五粮液', 300, 165.00),
(6, '601318', '中国平安', 400, 46.00),
(7, '600519', '贵州茅台', 300, 1600.00),
(7, '000858', '五粮液', 800, 150.00),
(7, '601318', '中国平安', 1000, 42.00),
(8, '600036', '招商银行', 600, 34.00),
(8, '601318', '中国平安', 200, 47.00),
(9, '510300', '沪深300ETF', 2000, 3.50),
(9, '159919', '沪深300ETF易方达', 1500, 4.20),
(10, '600519', '贵州茅台', 150, 1750.00),
(10, '601318', '中国平安', 350, 44.00),
(10, '600036', '招商银行', 250, 36.00);

-- 插入用户功勋值初始数据
INSERT INTO user_honor (user_id, honor_value, honor_level) VALUES
(1, 50, '新手'),
(2, 120, '达人'),
(3, 80, '新手'),
(4, 200, '达人'),
(5, 350, '达人'),
(6, 150, '达人'),
(7, 520, '专家'),
(8, 180, '达人'),
(9, 90, '新手'),
(10, 280, '达人');

-- 插入用户经验值初始数据
INSERT INTO user_experience (user_id, exp_value, level) VALUES
(1, 50, 1),
(2, 150, 2),
(3, 80, 1),
(4, 220, 3),
(5, 380, 4),
(6, 180, 2),
(7, 550, 6),
(8, 120, 2),
(9, 90, 1),
(10, 300, 4);

-- 插入勋章数据
INSERT INTO badge (name, description, icon_url, condition_type, condition_value) VALUES
('学习先锋', '连续学习7天', '/icons/badges/study_pioneer.png', 'continuous_study_days', 7),
('投资达人', '完成10次模拟交易', '/icons/badges/investment_master.png', 'mock_trade_count', 10),
('社区之星', '发布10篇帖子', '/icons/badges/community_star.png', 'post_count', 10),
('连续登录', '连续登录30天', '/icons/badges/consecutive_login.png', 'consecutive_login_days', 30),
('预测专家', '预测准确率达到70%', '/icons/badges/prediction_expert.png', 'prediction_accuracy', 70),
('圈子达人', '创建或加入3个圈子', '/icons/badges/circle_master.png', 'circle_count', 3),
('人气之星', '获得100个点赞', '/icons/badges/popularity_star.png', 'total_likes', 100);

-- 插入学习圈子数据
INSERT INTO study_circle (name, description, creator_id, is_public, member_count) VALUES
('价值投资交流圈', '分享价值投资理念和方法', 1, TRUE, 5),
('技术分析学习群', '学习股票技术分析方法', 2, TRUE, 3),
('基金定投俱乐部', '基金定投经验分享', 3, TRUE, 4),
('ETF投资交流', 'ETF投资策略讨论', 4, TRUE, 2),
('新手入门指导', '帮助新手快速入门', 5, TRUE, 6);

-- 插入圈子成员数据
INSERT INTO circle_member (circle_id, user_id, role) VALUES
(1, 1, 'admin'),
(1, 2, 'member'),
(1, 3, 'member'),
(1, 5, 'member'),
(1, 7, 'member'),
(2, 2, 'admin'),
(2, 4, 'member'),
(2, 6, 'member'),
(3, 3, 'admin'),
(3, 1, 'member'),
(3, 8, 'member'),
(3, 9, 'member'),
(4, 4, 'admin'),
(4, 9, 'member'),
(5, 5, 'admin'),
(5, 1, 'member'),
(5, 6, 'member'),
(5, 7, 'member'),
(5, 8, 'member'),
(5, 10, 'member');

-- 插入用户风险偏好数据
INSERT INTO user_risk_profile (user_id, risk_type, risk_score) VALUES
(1, '稳健型', 2),
(2, '平衡型', 3),
(3, '保守型', 1),
(4, '平衡型', 3),
(5, '成长型', 4),
(6, '平衡型', 3),
(7, '进取型', 5),
(8, '稳健型', 2),
(9, '平衡型', 3),
(10, '成长型', 4);

-- 插入社区帖子数据
INSERT INTO community_post (user_id, username, title, content, tag, tag_color, avatar_color, views, comments, likes) VALUES
(1, '理财新手', '新手入门：如何开始理财', '大家好，我是新手，想学习理财知识，请问有什么好的建议吗？', '问题求助', 'blue', '#ed8936', 156, 5, 12),
(2, '基金达人', '基金定投经验分享', '坚持定投三年了，分享一下我的心得体会...', '投资心得', 'orange', '#52c41a', 289, 8, 45),
(5, '价值投资派', '价值投资的核心逻辑', '价值投资不是简单的长期持有，而是...', '投资心得', 'orange', '#722ed1', 432, 15, 89),
(7, '激进交易者', '技术分析入门指南', '分享一些技术分析的基础知识...', '市场分析', 'purple', '#1890ff', 567, 22, 134),
(8, '稳健理财师', '资产配置的重要性', '不要把鸡蛋放在一个篮子里...', '投资心得', 'orange', '#faad14', 234, 6, 67);

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 验证查询
-- =============================================
SELECT '=== 数据库初始化完成 ===' AS info;
SELECT '用户表' AS table_name, COUNT(*) AS count FROM user
UNION ALL SELECT '用户资产表', COUNT(*) FROM user_asset
UNION ALL SELECT '用户持仓表', COUNT(*) FROM user_stock_hold
UNION ALL SELECT '功勋值表', COUNT(*) FROM user_honor
UNION ALL SELECT '勋章表', COUNT(*) FROM badge
UNION ALL SELECT '经验值表', COUNT(*) FROM user_experience
UNION ALL SELECT '学习圈子表', COUNT(*) FROM study_circle
UNION ALL SELECT '圈子成员表', COUNT(*) FROM circle_member
UNION ALL SELECT '风险偏好表', COUNT(*) FROM user_risk_profile
UNION ALL SELECT '社区帖子表', COUNT(*) FROM community_post;
