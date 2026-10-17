-- =============================================
-- 增强社区功能数据库表结构
-- 创建时间：2026-04-30
-- 说明：包含用户功勋、勋章、经验值、学习圈子、学习伙伴、投资预测等表
-- =============================================

-- ---------------------------------------------
-- 用户功勋值表
-- ---------------------------------------------
CREATE TABLE user_honor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  honor_value INT DEFAULT 0 COMMENT '功勋值',
  honor_level VARCHAR(20) COMMENT '功勋等级',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户功勋值表';

-- ---------------------------------------------
-- 勋章表
-- ---------------------------------------------
CREATE TABLE badge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(50) NOT NULL COMMENT '勋章名称',
  description VARCHAR(200) COMMENT '勋章描述',
  icon_url VARCHAR(255) COMMENT '勋章图标URL',
  condition_type VARCHAR(50) COMMENT '获取条件类型',
  condition_value INT COMMENT '获取条件值',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勋章表';

-- ---------------------------------------------
-- 用户勋章关联表
-- ---------------------------------------------
CREATE TABLE user_badge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  badge_id BIGINT NOT NULL COMMENT '勋章ID',
  earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  UNIQUE KEY uk_user_badge (user_id, badge_id),
  KEY idx_user_id (user_id),
  KEY idx_badge_id (badge_id),
  CONSTRAINT fk_user_badge_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_badge_badge FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户勋章关联表';

-- ---------------------------------------------
-- 用户经验值表
-- ---------------------------------------------
CREATE TABLE user_experience (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  exp_value INT DEFAULT 0 COMMENT '经验值',
  level INT DEFAULT 1 COMMENT '等级',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户经验值表';

-- ---------------------------------------------
-- 学习圈子表
-- ---------------------------------------------
CREATE TABLE study_circle (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(100) NOT NULL COMMENT '圈子名称',
  description VARCHAR(500) COMMENT '圈子描述',
  creator_id BIGINT NOT NULL COMMENT '创建者ID',
  is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
  member_count INT DEFAULT 1 COMMENT '成员数量',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_creator_id (creator_id),
  CONSTRAINT fk_study_circle_creator FOREIGN KEY (creator_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习圈子表';

-- ---------------------------------------------
-- 圈子成员表
-- ---------------------------------------------
CREATE TABLE circle_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  circle_id BIGINT NOT NULL COMMENT '圈子ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role VARCHAR(20) DEFAULT 'member' COMMENT '角色：creator/admin/member',
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  UNIQUE KEY uk_circle_user (circle_id, user_id),
  KEY idx_circle_id (circle_id),
  KEY idx_user_id (user_id),
  CONSTRAINT fk_circle_member_circle FOREIGN KEY (circle_id) REFERENCES study_circle(id) ON DELETE CASCADE,
  CONSTRAINT fk_circle_member_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员表';

-- ---------------------------------------------
-- 学习伙伴表
-- ---------------------------------------------
CREATE TABLE study_partner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  partner_id BIGINT NOT NULL COMMENT '伙伴ID',
  match_score INT COMMENT '匹配分数',
  status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/accepted/rejected',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_user_partner (user_id, partner_id),
  KEY idx_user_id (user_id),
  KEY idx_partner_id (partner_id),
  CONSTRAINT fk_study_partner_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
  CONSTRAINT fk_study_partner_partner FOREIGN KEY (partner_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习伙伴表';

-- ---------------------------------------------
-- 投资预测记录表
-- ---------------------------------------------
CREATE TABLE investment_prediction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  stock_code VARCHAR(20) NOT NULL COMMENT '股票代码',
  prediction_type VARCHAR(20) COMMENT '预测类型：rise/fall',
  predicted_price DECIMAL(10,2) COMMENT '预测价格',
  actual_price DECIMAL(10,2) COMMENT '实际价格',
  prediction_date TIMESTAMP COMMENT '预测日期',
  result_date TIMESTAMP COMMENT '结果日期',
  is_accurate BOOLEAN COMMENT '是否准确',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_user_id (user_id),
  KEY idx_stock_code (stock_code),
  KEY idx_prediction_date (prediction_date),
  CONSTRAINT fk_investment_prediction_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资预测记录表';

-- ---------------------------------------------
-- 社区帖子表（如果不存在）
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS community_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  title VARCHAR(200) COMMENT '帖子标题',
  content TEXT COMMENT '帖子内容',
  likes INT DEFAULT 0 COMMENT '点赞数',
  comments INT DEFAULT 0 COMMENT '评论数',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_user_id (user_id),
  KEY idx_created_at (created_at),
  CONSTRAINT fk_community_post_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- ---------------------------------------------
-- 帖子评论表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS post_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  content TEXT COMMENT '评论内容',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_post_id (post_id),
  KEY idx_user_id (user_id),
  CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

-- ---------------------------------------------
-- 帖子点赞表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY unique_like (post_id, user_id),
  KEY idx_post_id (post_id),
  KEY idx_user_id (user_id),
  CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- =============================================
-- 插入初始勋章数据
-- =============================================
INSERT INTO badge (name, description, icon_url, condition_type, condition_value) VALUES
('学习先锋', '连续学习7天', '/icons/badges/study_pioneer.png', 'continuous_study_days', 7),
('投资达人', '完成10次模拟交易', '/icons/badges/investment_master.png', 'mock_trade_count', 10),
('社区之星', '发布10篇帖子', '/icons/badges/community_star.png', 'post_count', 10),
('连续登录', '连续登录30天', '/icons/badges/consecutive_login.png', 'consecutive_login_days', 30);

-- ---------------------------------------------
-- 用户私信表
-- ---------------------------------------------
CREATE TABLE user_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  sender_id BIGINT NOT NULL COMMENT '发送者ID',
  receiver_id BIGINT NOT NULL COMMENT '接收者ID',
  content TEXT COMMENT '消息内容',
  is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_sender_id (sender_id),
  KEY idx_receiver_id (receiver_id),
  KEY idx_created_at (created_at),
  CONSTRAINT fk_user_message_sender FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_message_receiver FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户私信表';
