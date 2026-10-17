-- 投资预测表
CREATE TABLE IF NOT EXISTS investment_prediction (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    stock_code VARCHAR(20) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(50) COMMENT '股票名称',
    prediction_type VARCHAR(20) NOT NULL COMMENT '预测类型: short(短期), mid(中期)',
    predicted_price DECIMAL(10, 2) COMMENT '预测价格',
    actual_price DECIMAL(10, 2) COMMENT '实际价格',
    prediction_date DATETIME COMMENT '预测日期',
    result_date DATETIME COMMENT '结果日期',
    is_accurate TINYINT(1) COMMENT '是否准确',
    confidence DECIMAL(5, 2) COMMENT '预测置信度',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_stock_code (stock_code),
    INDEX idx_prediction_date (prediction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资预测记录表';
