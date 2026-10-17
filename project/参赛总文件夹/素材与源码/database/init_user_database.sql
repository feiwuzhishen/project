-- ============================================
-- 模拟投资平台 数据库初始化脚本
-- ============================================

-- -------------------------------------------
-- 表1：user 用户表（登录信息）
-- -------------------------------------------
DROP TABLE IF EXISTS user_notification;
DROP TABLE IF EXISTS user_stock_hold;
DROP TABLE IF EXISTS user_asset;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    nickname VARCHAR(50) COMMENT '用户昵称',
    avatar VARCHAR(255) COMMENT '用户头像URL',
    gender VARCHAR(10) DEFAULT 'male' COMMENT '性别(male/female)',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------
-- 表2：user_asset 用户资产配置表
-- -------------------------------------------
CREATE TABLE user_asset (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    stock_ratio INT DEFAULT 0 COMMENT '股票占比%',
    fund_ratio INT DEFAULT 0 COMMENT '基金占比%',
    bond_ratio INT DEFAULT 0 COMMENT '债券占比%',
    etf_ratio INT DEFAULT 0 COMMENT 'ETF占比%',
    derivatives_ratio INT DEFAULT 0 COMMENT '衍生品占比%',
    cash_ratio INT DEFAULT 0 COMMENT '现金占比%',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产配置表';

-- -------------------------------------------
-- 表3：user_stock_hold 用户持仓股票明细表
-- -------------------------------------------
CREATE TABLE user_stock_hold (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    stock_code VARCHAR(10) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(50) NOT NULL COMMENT '股票名称',
    hold_num INT NOT NULL COMMENT '持有数量',
    buy_price DECIMAL(10,2) DEFAULT 0 COMMENT '持仓成本价',
    hold_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '持仓时间',
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户持仓股票明细表';

-- -------------------------------------------
-- 表4：user_notification 用户通知表
-- -------------------------------------------
CREATE TABLE user_notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '通知类型(system/message/warning/notice)',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读(0未读/1已读)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';

-- ============================================
-- 测试数据
-- ============================================

-- 插入用户 (密码123456是明文，实际使用请加密)
INSERT INTO user (username, password, nickname, gender, email, phone) VALUES
('user1', '123456', '理财新手', 'male', 'user1@example.com', '13800000001'),
('user2', '123456', '基金达人', 'female', 'user2@example.com', '13800000002'),
('user3', '123456', '债券玩家', 'male', 'user3@example.com', '13800000003'),
('user4', '123456', 'ETF投资者', 'female', 'user4@example.com', '13800000004'),
('user5', '123456', '价值投资派', 'male', 'user5@example.com', '13800000005'),
('user6', '123456', '分散配置者', 'female', 'user6@example.com', '13800000006'),
('user7', '123456', '激进交易者', 'male', 'user7@example.com', '13800000007'),
('user8', '123456', '稳健理财师', 'female', 'user8@example.com', '13800000008'),
('user9', '123456', '指数定投者', 'male', 'user9@example.com', '13800000009'),
('user10', '123456', '金融学习者', 'female', 'user10@example.com', '13800000010');

-- 插入资产配置
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
-- user1 的持仓（低资产人群，总资产约3500元）
(1, '600036', '招商银行', 10, 35.00),
(1, '601398', '工商银行', 50, 5.20),
(1, '601288', '农业银行', 100, 3.50),
-- user2 的持仓（基金达人，总资产约4200元）
(2, '601318', '中国平安', 20, 45.00),
(2, '600036', '招商银行', 30, 35.00),
-- user3 的持仓（债券玩家，总资产约2800元）
(3, '601288', '农业银行', 200, 3.50),
(3, '601398', '工商银行', 100, 5.20),
-- user4 的持仓（ETF投资者，总资产约3800元）
(4, '510300', '沪深300ETF', 200, 3.80),
(4, '159915', '创业板ETF', 300, 1.90),
-- user5 的持仓（价值投资派，总资产约4500元）
(5, '600036', '招商银行', 30, 35.00),
(5, '601398', '工商银行', 100, 5.20),
(5, '601288', '农业银行', 200, 3.50),
-- user6 的持仓（分散配置者，总资产约3200元）
(6, '600036', '招商银行', 20, 35.00),
(6, '601398', '工商银行', 80, 5.20),
(6, '601288', '农业银行', 100, 3.50),
-- user7 的持仓（激进交易者，总资产约4800元）
(7, '600036', '招商银行', 40, 35.00),
(7, '601398', '工商银行', 150, 5.20),
(7, '601288', '农业银行', 200, 3.50),
-- user8 的持仓（稳健理财师，总资产约2600元）
(8, '600036', '招商银行', 15, 34.00),
(8, '601398', '工商银行', 60, 5.20),
-- user9 的持仓（指数定投者，总资产约4000元）
(9, '510300', '沪深300ETF', 300, 3.50),
(9, '159919', '沪深300ETF易方达', 200, 4.20),
-- user10 的持仓（金融学习者，总资产约3500元）
(10, '600036', '招商银行', 25, 35.00),
(10, '601398', '工商银行', 80, 5.20),
(10, '601288', '农业银行', 100, 3.50);

-- 插入用户通知示例数据
INSERT INTO user_notification (user_id, type, title, content, is_read) VALUES
(1, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(1, 'notice', 'A股三大指数集体收涨', '今日A股三大指数集体收涨，沪指涨1.2%，深成指涨1.5%，创业板指涨2.1%。', 0),
(1, 'warning', '持仓股票波动提醒', '您持仓的招商银行今日上涨2.5%，请注意关注市场动态。', 1),
(2, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(2, 'message', '新的学习伙伴请求', '用户"理财新手"向您发送了学习伙伴请求，快去看看吧！', 0),
(3, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(4, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(5, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(6, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(7, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(8, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(9, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(10, 'system', '欢迎使用金融学习平台', '感谢您注册金融学习平台，开始您的投资学习之旅吧！', 0),
(10, 'notice', '央行宣布定向降准', '中国人民银行宣布定向降准0.5个百分点，释放长期资金约8000亿元。', 0);

-- ============================================
-- 查询验证
-- ============================================
SELECT '=== 用户列表 ===' AS info;
SELECT * FROM user;

SELECT '=== 用户资产配置 ===' AS info;
SELECT u.username, u.nickname, a.* FROM user_asset a
JOIN user u ON a.user_id = u.id;

SELECT '=== 用户持仓明细 ===' AS info;
SELECT u.username, u.nickname, s.* FROM user_stock_hold s
JOIN user u ON s.user_id = u.id
ORDER BY u.id, s.stock_code;
